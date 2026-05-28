package com.example.back.service;

import com.example.back.entity.Material;
import com.example.back.entity.Tag;
import com.example.back.mapper.MaterialMapper;
import com.example.back.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;

@Service
public class MaterialService {
    
    @Autowired
    private MaterialMapper materialMapper;
    
    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private FileStorageService fileStorageService;
    
    public List<Tag> getAllTags(String category) {
        if (category == null || category.trim().isEmpty() || "全部".equals(category)) {
            return tagMapper.findAll();
        }
        return tagMapper.findByCategory(category.trim());
    }

    public List<Tag> getAllTags() {
        return getAllTags(null);
    }

    public List<Material> getApprovedMaterials() {
        try {
            List<Material> materials = materialMapper.findApprovedOnShelf();
            if (materials != null) {
                for (Material m : materials) {
                    try {
                        m.setTags(tagMapper.findByMaterialId(m.getId()));
                    } catch (Exception e) {
                        m.setTags(new java.util.ArrayList<>());
                    }
                }
            }
            return materials != null ? materials : new java.util.ArrayList<>();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    public Tag createTag(String name, String category) {
        Tag existing = tagMapper.findByName(name);
        if (existing != null) return existing;
        Tag tag = new Tag();
        tag.setName(name);
        tag.setCategory((category == null || category.trim().isEmpty()) ? "其他" : category.trim());
        tagMapper.insert(tag);
        return tag;
    }

    public Tag createTag(String name) {
        return createTag(name, "其他");
    }

    @Transactional
    public void updateTagCategory(Long id, String category) {
        if (id == null) throw new RuntimeException("标签ID不能为空");
        String c = (category == null || category.trim().isEmpty()) ? "其他" : category.trim();
        tagMapper.updateCategory(id, c);
    }
    
    public List<Material> getMaterialsByTeacher(Long teacherId) {
        try {
            List<Material> materials = materialMapper.findByTeacherId(teacherId);
            if (materials != null) {
                for (Material m : materials) {
                    try {
                        m.setTags(tagMapper.findByMaterialId(m.getId()));
                    } catch (Exception e) {
                        System.err.println("加载素材标签失败: " + e.getMessage());
                        m.setTags(new java.util.ArrayList<>());
                    }
                }
            }
            return materials != null ? materials : new java.util.ArrayList<>();
        } catch (Exception e) {
            System.err.println("getMaterialsByTeacher 错误: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Material> getAllMaterials() {
        try {
            List<Material> materials = materialMapper.findAll();
            if (materials != null) {
                for (Material m : materials) {
                    try {
                        m.setTags(tagMapper.findByMaterialId(m.getId()));
                    } catch (Exception e) {
                        m.setTags(new java.util.ArrayList<>());
                    }
                    if (m.getFileUrl() != null && fileStorageService != null && fileStorageService.isDataUrl(m.getFileUrl())) {
                        m.setFileUrl(normalizeAndMaybeMigrateFileUrl(m.getId(), m.getFileUrl()));
                    }
                    m.setContent(null); // 列表不需要庞大的富文本内容
                }
            }
            return materials != null ? materials : new java.util.ArrayList<>();
        } catch (Exception e) {
            System.err.println("getAllMaterials 错误: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    public Material getMaterialById(Long id) {
        try {
            materialMapper.incrementViewCount(id);
        } catch (Exception ignored) {
            // ignore
        }
        Material m = materialMapper.findById(id);
        if (m != null) {
            m.setTags(tagMapper.findByMaterialId(id));
            if (m.getContent() != null && fileStorageService != null) {
                String updated = fileStorageService.replaceMediaDataUrls(m.getContent(), "material-content");
                if (!updated.equals(m.getContent())) {
                    m.setContent(updated);
                    try { materialMapper.update(m); } catch (Exception ignored) {}
                }
            }
        }
        return m;
    }

    public List<Material> getLeaderArticles(String tagName) {
        List<Material> materials;
        if (tagName != null && !tagName.trim().isEmpty()) {
            materials = materialMapper.findApprovedArticlesForLeaderByTagName(tagName.trim());
        } else {
            materials = materialMapper.findApprovedArticlesForLeader();
        }
        if (materials == null) return new ArrayList<>();
        for (Material m : materials) {
            try {
                m.setTags(tagMapper.findByMaterialId(m.getId()));
            } catch (Exception e) {
                m.setTags(new ArrayList<>());
            }
            if (m.getFileUrl() != null && fileStorageService != null && fileStorageService.isDataUrl(m.getFileUrl())) {
                m.setFileUrl(normalizeAndMaybeMigrateFileUrl(m.getId(), m.getFileUrl()));
            }
            m.setContent(null); // 列表不需要庞大的富文本内容
        }
        return materials;
    }

    public Material getLeaderArticleDetail(Long id) {
        try {
            materialMapper.incrementViewCount(id);
        } catch (Exception ignored) {
            // ignore
        }
        Material m = materialMapper.findApprovedArticleDetailById(id);
        if (m != null) {
            m.setTags(tagMapper.findByMaterialId(id));
            if (m.getContent() != null && fileStorageService != null) {
                String updated = fileStorageService.replaceMediaDataUrls(m.getContent(), "leader-material");
                if (!updated.equals(m.getContent())) {
                    m.setContent(updated);
                    try { materialMapper.update(m); } catch (Exception ignored) {}
                }
            }
        }
        return m;
    }
    
    public String getFileUrlById(Long id) {
        if (id == null) return null;
        int attempts = 0;
        while (attempts < 2) {
            attempts++;
            try {
                Long len = materialMapper.findFileUrlLength(id);
                if (len == null || len <= 0) return null;

                // 防止极端情况下占用过多内存
                if (len > 100L * 1024 * 1024) {
                    throw new RuntimeException("文件过大，无法在线加载，请联系管理员处理");
                }

                // file_url 很小（通常是 /uploads/... 或短 dataURL）时，直接读取
                final long directReadMax = 1024L * 1024; // 1MB
                if (len <= directReadMax) {
                    String fileUrl = materialMapper.findFileUrlStringById(id);
                    if (fileUrl == null) return null;
                    return normalizeAndMaybeMigrateFileUrl(id, fileUrl);
                }

                // file_url 过大（历史 base64 视频/图片），用分片读取避免超过 max_allowed_packet
                final int chunkSize = 512 * 1024; // 512KB
                StringBuilder sb = new StringBuilder(len.intValue());
                long offset = 1; // MySQL SUBSTRING 从 1 开始
                while (offset <= len) {
                    int readLen = (int) Math.min(chunkSize, len - offset + 1);
                    String part = materialMapper.findFileUrlChunk(id, offset, readLen);
                    if (part != null) sb.append(part);
                    offset += readLen;
                }
                String fileUrl = sb.toString();
                return normalizeAndMaybeMigrateFileUrl(id, fileUrl);
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "" : e.getMessage();
                // 连接被服务端断开时尝试重试一次（拿新连接）
                if (attempts < 2 && (msg.contains("Communications link failure") || msg.contains("Socket is closed"))) {
                    continue;
                }
                throw e;
            }
        }
        return null;
    }

    private String normalizeAndMaybeMigrateFileUrl(Long materialId, String fileUrl) {
        if (fileUrl == null) return null;
        if (fileStorageService == null) return fileUrl;
        // 若仍为 dataURL，则落盘并回写数据库，后续通过 /uploads 访问
        if (fileStorageService.isDataUrl(fileUrl)) {
            String url = fileStorageService.saveDataUrl(fileUrl, "material");
            try {
                if (materialId != null) materialMapper.updateFileUrlById(materialId, url);
            } catch (Exception ignored) {
                // ignore
            }
            return url;
        }
        return fileUrl;
    }

    @Transactional
    public Material createMaterial(Material material, List<Long> tagIds) {
        if (material != null && material.getFileUrl() != null && fileStorageService != null) {
            // 对图片/视频类素材：若前端传的是 dataURL(base64)，则先落盘为 /uploads/...，避免数据库存超大字段导致视频无法播放
            if (fileStorageService.isDataUrl(material.getFileUrl())) {
                material.setFileUrl(fileStorageService.saveDataUrl(material.getFileUrl(), "material"));
            }
        }
        if (material != null && material.getContent() != null && fileStorageService != null) {
            material.setContent(fileStorageService.replaceMediaDataUrls(material.getContent(), "material-content"));
        }
        material.setStatus("PENDING");
        materialMapper.insert(material);
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                materialMapper.insertMaterialTag(material.getId(), tagId);
            }
        }
        material.setTags(tagMapper.findByMaterialId(material.getId()));
        return material;
    }
    
    @Transactional
    public Material updateMaterial(Material material, List<Long> tagIds) {
        if (material != null && material.getId() != null && material.getFileUrl() != null && fileStorageService != null) {
            if (fileStorageService.isDataUrl(material.getFileUrl())) {
                material.setFileUrl(fileStorageService.saveDataUrl(material.getFileUrl(), "material"));
            }
        }
        if (material != null && material.getContent() != null && fileStorageService != null) {
            material.setContent(fileStorageService.replaceMediaDataUrls(material.getContent(), "material-content"));
        }
        materialMapper.update(material);
        materialMapper.deleteMaterialTags(material.getId());
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                materialMapper.insertMaterialTag(material.getId(), tagId);
            }
        }
        return getMaterialById(material.getId());
    }
    
    @Transactional
    public void deleteMaterial(Long id) {
        materialMapper.deleteMaterialTags(id);
        materialMapper.delete(id);
    }
}
