package com.example.back.service;

import com.example.back.entity.*;
import com.example.back.dto.HotTopic;
import com.example.back.dto.LeaderMaterialRequest;
import com.example.back.mapper.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdminService {
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private CarouselMapper carouselMapper;
    
    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private NewsMaterialMapper newsMaterialMapper;
    
    @Autowired
    private MaterialMapper materialMapper;
    
    @Autowired
    private ForumTopicMapper forumTopicMapper;
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private DiscussionMapper discussionMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ScienceItemMapper scienceItemMapper;

    @Autowired
    private FileStorageService fileStorageService;

    private static final long MAX_TOPIC_COMMENT_IMAGE_BYTES = 2L * 1024 * 1024;

    @Autowired
    private CommentModerationService commentModerationService;

    private static final int FILE_URL_CHUNK_SIZE = 1024 * 1024;

    private static final String LEADER_SYSTEM_TEACHER_USERNAME = "leader_admin";

    private static final ObjectMapper JSON = new ObjectMapper();

    private String toJsonIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return null;
        try {
            return JSON.writeValueAsString(ids);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Long> parseJsonIds(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        try {
            List<Long> ids = JSON.readValue(json, new TypeReference<List<Long>>() {});
            return ids == null ? new ArrayList<>() : ids;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    //  管理员登录
    public Admin login(String username, String password) {
        Admin admin = adminMapper.findByUsernameAndPassword(username, password);
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        return admin;
    }

    public Map<String, Object> getPlatformStats() {
        int studentCount = 0;
        int teacherCount = 0;
        int materialCount = 0;
        int taskCount = 0;
        int discussionCount = 0;
        int newsCount = 0;
        int forumTopicCount = 0;

        long newsViewCount = 0L;
        long discussionViewCount = 0L;
        long forumTopicViewCount = 0L;

        try {
            studentCount = adminMapper.countStudents();
        } catch (Exception ignored) {
        }
        try {
            teacherCount = adminMapper.countTeachers();
        } catch (Exception ignored) {
        }
        try {
            materialCount = adminMapper.countMaterials();
        } catch (Exception ignored) {
        }
        try {
            taskCount = adminMapper.countLearningTasks();
        } catch (Exception ignored) {
        }
        try {
            discussionCount = adminMapper.countDiscussions();
        } catch (Exception ignored) {
        }
        try {
            newsCount = adminMapper.countNews();
        } catch (Exception ignored) {
        }
        try {
            forumTopicCount = adminMapper.countForumTopics();
        } catch (Exception ignored) {
        }

        try {
            Long v = adminMapper.sumNewsViewCount();
            newsViewCount = v == null ? 0L : v;
        } catch (Exception ignored) {
        }
        try {
            Long v = adminMapper.sumDiscussionViewCount();
            discussionViewCount = v == null ? 0L : v;
        } catch (Exception e) {
            System.err.println("sumDiscussionViewCount 统计失败: " + e.getMessage());
        }
        try {
            Long v = adminMapper.sumForumTopicViewCount();
            forumTopicViewCount = v == null ? 0L : v;
        } catch (Exception ignored) {
        }

        Long materialViewCount = null;
        try {
            materialViewCount = materialMapper.sumViewCount();
        } catch (Exception e) {
            System.err.println("sumMaterialViewCount 统计失败: " + e.getMessage());
            materialViewCount = 0L;
        }

        Map<String, Object> r = new HashMap<>();
        r.put("studentCount", studentCount);
        r.put("teacherCount", teacherCount);
        r.put("userCount", studentCount + teacherCount);
        r.put("materialCount", materialCount);
        r.put("taskCount", taskCount);
        r.put("discussionCount", discussionCount);
        r.put("newsCount", newsCount);
        r.put("forumTopicCount", forumTopicCount);

        r.put("newsViewCount", newsViewCount);
        r.put("discussionViewCount", discussionViewCount);
        r.put("forumTopicViewCount", forumTopicViewCount);

        r.put("materialViewCount", materialViewCount);

        try {
            r.put("dbName", adminMapper.currentDatabase());
            r.put("dbHost", adminMapper.currentHostname());
            r.put("dbPort", adminMapper.currentPort());
            r.put("hasDiscussionViewCount", adminMapper.hasDiscussionViewCountColumn());
            r.put("hasMaterialViewCount", adminMapper.hasMaterialViewCountColumn());
        } catch (Exception ignored) {
        }
        return r;
    }

    private Long getOrCreateLeaderSystemTeacherId() {
        User existing = userMapper.findTeacherByUsername(LEADER_SYSTEM_TEACHER_USERNAME);
        if (existing != null && existing.getId() != null) {
            return existing.getId();
        }

        User u = new User();
        u.setUsername(LEADER_SYSTEM_TEACHER_USERNAME);
        u.setPassword(UUID.randomUUID().toString());
        u.setRealName("系统人物素材");
        u.setMajor("SYSTEM");
        u.setAvatar(null);
        userMapper.insertTeacher(u);
        return u.getId();
    }

    @Transactional
    public Material createLeaderMaterial(LeaderMaterialRequest request) {
        if (request == null) throw new RuntimeException("参数错误");
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("标题不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("内容不能为空");
        }

        Material m = new Material();
        m.setTitle(request.getTitle().trim());
        m.setType("ARTICLE");
        m.setDescription(request.getDescription());
        
        String content = request.getContent();
        if (fileStorageService != null) {
            content = fileStorageService.replaceMediaDataUrls(content, "leader-material");
        }
        m.setContent(content);
        m.setFileUrl(request.getFileUrl());
        m.setTeacherId(getOrCreateLeaderSystemTeacherId());
        m.setStatus("APPROVED");
        materialMapper.insert(m);

        try {
            materialMapper.updateShelfStatus(m.getId(), "ON");
        } catch (Exception ignored) {
            // ignore
        }

        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                if (tagId == null) continue;
                try {
                    materialMapper.insertMaterialTag(m.getId(), tagId);
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }

        try {
            m.setTags(tagMapper.findByMaterialId(m.getId()));
        } catch (Exception ignored) {
            // ignore
        }

        return m;
    }
    
    // 轮播图管理
    public List<Carousel> getAllCarousels() {
        return carouselMapper.findAll();
    }

    public List<Carousel> getAllCarousels(String page) {
        if (page == null || page.trim().isEmpty()) {
            return carouselMapper.findAll();
        }
        return carouselMapper.findAllByPage(page.trim());
    }
    
    public List<Carousel> getActiveCarousels() {
        return carouselMapper.findActive();
    }

    public List<Carousel> getActiveCarousels(String page) {
        if (page == null || page.trim().isEmpty()) {
            return carouselMapper.findActive();
        }
        return carouselMapper.findActiveByPage(page.trim());
    }
    
    public Carousel createCarousel(Carousel carousel) {
        if (carousel.getSortOrder() == null) carousel.setSortOrder(0);
        if (carousel.getStatus() == null) carousel.setStatus(true);
        if (carousel.getPage() != null) carousel.setPage(carousel.getPage().trim());
        carouselMapper.insert(carousel);
        return carousel;
    }
    
    public Carousel updateCarousel(Carousel carousel) {
        if (carousel.getPage() != null) carousel.setPage(carousel.getPage().trim());
        carouselMapper.update(carousel);
        return carouselMapper.findById(carousel.getId());
    }
    
    public void deleteCarousel(Long id) {
        carouselMapper.delete(id);
    }

    // 科学栏目管理
    public List<ScienceItem> getAllScienceItems(String module) {
        String m = (module == null) ? null : module.trim();
        if (m != null && m.isEmpty()) m = null;
        List<ScienceItem> list = scienceItemMapper.findAllLite(m);
        normalizeScienceCover(list);
        return list;
    }

    public List<ScienceItem> getActiveScienceItems(String module) {
        String m = (module == null) ? null : module.trim();
        if (m != null && m.isEmpty()) m = null;
        List<ScienceItem> list = scienceItemMapper.findActiveLite(m);
        normalizeScienceCover(list);
        return list;
    }

    private void normalizeScienceCover(List<ScienceItem> list) {
        if (list == null || list.isEmpty()) return;

        for (ScienceItem item : list) {
            if (item == null || item.getId() == null) continue;
            if (fileStorageService == null) continue;

            // 迁移 cover_image（封面图 base64 → 文件路径）
            String c = item.getCoverImage();
            if (c != null && !c.trim().isEmpty() && fileStorageService.isDataUrl(c)) {
                try {
                    String url = fileStorageService.saveDataUrl(c.trim(), "science-cover");
                    if (url != null && !url.trim().isEmpty()) {
                        item.setCoverImage(url);
                        try {
                            scienceItemMapper.updateCoverImageById(item.getId(), url);
                        } catch (Exception ignored) { }
                    }
                } catch (Exception e) {
                    // dataUrl 被截断或过大，置空避免超大字段响应到前端
                    item.setCoverImage(null);
                }
            }

            // 列表接口不响应 link_url（可能是视频 base64，由前端按需加载）
            // link_url 从 DB 读取时已被 ScienceItemMapper.findActiveLite 排除
            // 此处额外确保 item 上不携带该字段
            item.setLinkUrl(null);
        }
    }

    public ScienceItem getActiveScienceItemById(Long id) {
        if (id == null) return null;
        ScienceItem item = scienceItemMapper.findActiveById(id);
        if (item != null && fileStorageService != null) {
            // 迁移封面图 base64 → 文件路径
            String cover = item.getCoverImage();
            if (cover != null && !cover.trim().isEmpty() && fileStorageService.isDataUrl(cover)) {
                try {
                    String url = fileStorageService.saveDataUrl(cover.trim(), "science-cover");
                    item.setCoverImage(url);
                    try { scienceItemMapper.updateCoverImageById(id, url); } catch (Exception ignored) { }
                } catch (Exception e) {
                    item.setCoverImage(null);
                }
            }
            // 迁移 link_url（视频 base64 → 文件路径）
            String linkUrl = item.getLinkUrl();
            if (linkUrl != null && !linkUrl.trim().isEmpty() && fileStorageService.isDataUrl(linkUrl)) {
                try {
                    String url = fileStorageService.saveDataUrl(linkUrl.trim(), "science-video");
                    item.setLinkUrl(url);
                    try { scienceItemMapper.updateLinkUrlById(id, url); } catch (Exception ignored) { }
                } catch (Exception e) {
                    // 无法迁移时保持原值让前端兜底处理
                }
            }
        }
        return item;
    }

    public ScienceItem createScienceItem(ScienceItem item) {
        if (item == null) throw new RuntimeException("参数错误");
        if (item.getSortOrder() == null) item.setSortOrder(0);
        if (item.getStatus() == null) item.setStatus(true);
        if (item.getModule() != null) item.setModule(item.getModule().trim());
        if (item.getTitle() != null) item.setTitle(item.getTitle().trim());

        normalizeScienceItemContent(item);

        scienceItemMapper.insert(item);
        return item;
    }

    public ScienceItem updateScienceItem(ScienceItem item) {
        if (item == null || item.getId() == null) throw new RuntimeException("参数错误");
        if (item.getModule() != null) item.setModule(item.getModule().trim());
        if (item.getTitle() != null) item.setTitle(item.getTitle().trim());

        normalizeScienceItemContent(item);

        scienceItemMapper.update(item);
        return scienceItemMapper.findById(item.getId());
    }

    private void normalizeScienceItemContent(ScienceItem item) {
        if (item == null) return;

        String module = item.getModule();
        if (module != null) module = module.trim();

        String type = item.getContentType();
        if (type != null) type = type.trim().toUpperCase();

        if ("empower".equalsIgnoreCase(module)) {
            item.setContentType("VIDEO");
            item.setArticleContent(null);
            // 科学赋能（empower）视频存入 link_url，需落盘迁移
            String linkUrl = item.getLinkUrl();
            if (linkUrl != null && !linkUrl.trim().isEmpty()
                    && fileStorageService != null && fileStorageService.isDataUrl(linkUrl)) {
                item.setLinkUrl(fileStorageService.saveDataUrl(linkUrl.trim(), "science-video"));
            }
            return;
        }

        if (type == null || type.isEmpty()) {
            type = "VIDEO";
        }
        if (!"VIDEO".equals(type) && !"ARTICLE".equals(type)) {
            type = "VIDEO";
        }
        item.setContentType(type);

        if ("ARTICLE".equals(type)) {
            String content = item.getArticleContent();
            if (content != null) content = content.trim();
            if (content == null || content.isEmpty()) {
                throw new RuntimeException("文章正文不能为空");
            }
            item.setArticleContent(content);
            item.setCoverImage(null);
        } else {
            // VIDEO 类型：视频可能存在 cover_image（旧逻辑）或 link_url，均需落盘迁移
            String video = item.getCoverImage();
            if (video != null && !video.trim().isEmpty()
                    && fileStorageService != null && fileStorageService.isDataUrl(video)) {
                item.setCoverImage(fileStorageService.saveDataUrl(video.trim(), "science-video"));
            } else if ("news".equalsIgnoreCase(module) || "knowledge".equalsIgnoreCase(module)) {
                if (video == null || video.trim().isEmpty()) {
                    throw new RuntimeException("请上传视频");
                }
            }
            // 同时处理 link_url 的视频 base64
            String linkUrl = item.getLinkUrl();
            if (linkUrl != null && !linkUrl.trim().isEmpty()
                    && fileStorageService != null && fileStorageService.isDataUrl(linkUrl)) {
                item.setLinkUrl(fileStorageService.saveDataUrl(linkUrl.trim(), "science-video"));
            }
            item.setArticleContent(null);
        }
    }

    public void deleteScienceItem(Long id) {
        if (id == null) throw new RuntimeException("ID不能为空");
        scienceItemMapper.delete(id);
    }

    // ========== 新闻管理 ==========
    public List<News> getAllNews() {
        return newsMapper.findAll();
    }

    public List<News> getAllNews(String category) {
        if (category == null || category.trim().isEmpty() || "全部".equals(category)) {
            List<News> list = newsMapper.findAll();
            normalizeNewsCover(list);
            return list;
        }
        List<News> list = newsMapper.findAllByCategory(category.trim());
        normalizeNewsCover(list);
        return list;
    }
    
    public List<News> getPublishedNews() {
        List<News> list = newsMapper.findPublished();
        normalizeNewsCover(list);
        return list;
    }

    public List<News> getPublishedNews(String category) {
        if (category == null || category.trim().isEmpty() || "全部".equals(category)) {
            List<News> list = newsMapper.findPublished();
            normalizeNewsCover(list);
            return list;
        }
        List<News> list = newsMapper.findPublishedByCategory(category.trim());
        normalizeNewsCover(list);
        return list;
    }
    
    public List<News> getLatestNews(int limit) {
        return newsMapper.findPublishedLimit(limit);
    }

    public List<News> getLatestNews(int limit, String category) {
        if (category == null || category.trim().isEmpty() || "全部".equals(category)) {
            List<News> list = newsMapper.findPublishedLimit(limit);
            normalizeNewsCover(list);
            return list;
        }
        List<News> list = newsMapper.findPublishedLimitByCategory(limit, category.trim());
        normalizeNewsCover(list);
        return list;
    }

    private void normalizeNewsCover(List<News> list) {
        if (list == null || list.isEmpty()) return;
        for (News n : list) {
            if (n == null || n.getId() == null) continue;
            String c = n.getCoverImage();
            if (c == null || c.trim().isEmpty()) continue;
            if (fileStorageService == null) continue;
            if (!fileStorageService.isDataUrl(c)) continue;
            try {
                String url = fileStorageService.saveDataUrl(c.trim(), "news-cover");
                if (url != null && !url.trim().isEmpty()) {
                    n.setCoverImage(url);
                    try {
                        newsMapper.updateCoverImageById(n.getId(), url);
                    } catch (Exception ignored) {
                        // ignore
                    }
                }
            } catch (Exception e) {
                // dataUrl可能被截断，无法解码；尝试从关联素材恢复封面
                String recovered = recoverNewsCoverFromMaterials(n.getId());
                if (recovered != null && !recovered.trim().isEmpty()) {
                    n.setCoverImage(recovered);
                    try {
                        newsMapper.updateCoverImageById(n.getId(), recovered);
                    } catch (Exception ignored) {
                        // ignore
                    }
                } else {
                    // 无法恢复时保留原值，让前端 onerror 兜底显示默认封面
                    n.setCoverImage(c);
                }
            }
        }
    }

    private String recoverNewsCoverFromMaterials(Long newsId) {
        if (newsId == null) return null;
        try {
            List<Long> materialIds = newsMaterialMapper.findMaterialIdsByNewsId(newsId);
            if (materialIds == null || materialIds.isEmpty()) return null;

            for (Long mid : materialIds) {
                if (mid == null) continue;
                Material m = materialMapper.findByIdWithoutFile(mid);
                if (m == null || !"IMAGE".equals(m.getType())) continue;

                String fileUrl = materialMapper.findFileUrlStringById(mid);
                if (fileUrl == null || fileUrl.trim().isEmpty()) continue;

                if (fileStorageService != null) {
                    try {
                        return fileStorageService.saveDataUrl(fileUrl.trim(), "news-cover");
                    } catch (Exception ignored) {
                        // ignore
                    }
                }
                return fileUrl;
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }
    
    public News getNewsById(Long id) {
        News news = newsMapper.findById(id);
        if (news != null) {
            newsMapper.incrementViewCount(id);
            try {
                news.setMaterialIds(newsMaterialMapper.findMaterialIdsByNewsId(id));
            } catch (Exception ignored) { }
            // 对 cover_image 做迁移（列表接口已处理，但历史单条详情未处理）
            String cover = news.getCoverImage();
            if (cover != null && !cover.trim().isEmpty()
                    && fileStorageService != null && fileStorageService.isDataUrl(cover)) {
                try {
                    String url = fileStorageService.saveDataUrl(cover.trim(), "news-cover");
                    news.setCoverImage(url);
                    try { newsMapper.updateCoverImageById(id, url); } catch (Exception ignored) { }
                } catch (Exception e) {
                    news.setCoverImage(null);
                }
            }
            // 对 content 里内嵌的视频/图片 base64 做落盘迁移，避免响应内容高达百万字符
            normalizeNewsContentMedia(news);
            // 如果 content 已迁移，异步回写数据库，下次无需重复迁移
            try { newsMapper.update(news); } catch (Exception ignored) { }
        }
        return news;
    }

    public News getNewsByIdForAdmin(Long id) {
        News news = newsMapper.findById(id);
        if (news != null) {
            try {
                news.setMaterialIds(newsMaterialMapper.findMaterialIdsByNewsId(id));
            } catch (Exception ignored) {
                // ignore
            }
        }
        return news;
    }
    
    private void updateNewsMaterialsAndCover(News news) {
        normalizeNewsContentMedia(news);
        List<Long> materialIds = news.getMaterialIds();
        try {
            newsMaterialMapper.deleteByNewsId(news.getId());
        } catch (Exception ignored) {
            // ignore
        }

        String providedCover = news.getCoverImage();
        boolean hasProvidedCover = providedCover != null && !providedCover.trim().isEmpty();

        String cover = null;

        if (materialIds != null && !materialIds.isEmpty()) {
            try {
                for (Long mid : materialIds) {
                    if (mid != null) {
                        newsMaterialMapper.insert(news.getId(), mid);
                    }
                }
            } catch (Exception ignored) {
                // ignore
            }

            // 取选中素材里的第一张图片作为新闻封面
            for (Long mid : materialIds) {
                if (mid == null) continue;
                Material m = materialMapper.findByIdWithoutFile(mid);
                if (m != null && "IMAGE".equals(m.getType())) {
                    cover = materialMapper.findFileUrlStringById(mid);
                    break;
                }
            }
        }

        // 优先使用管理员上传/填写的封面；否则从关联素材里自动取第一张图片
        if (!hasProvidedCover) {
            if (cover != null && !cover.isEmpty() && fileStorageService != null) {
                try {
                    cover = fileStorageService.saveDataUrl(cover, "news-cover");
                } catch (Exception e) {
                    cover = null;
                }
            }
            news.setCoverImage((cover != null && !cover.isEmpty()) ? cover : null);
        } else {
            String c = providedCover.trim();
            if (fileStorageService != null) {
                try {
                    c = fileStorageService.saveDataUrl(c, "news-cover");
                } catch (Exception e) {
                    // ignore
                }
            }
            news.setCoverImage(c);
        }
        newsMapper.update(news);
    }

    @Transactional
    public News createNews(News news) {
        normalizeNewsContentMedia(news);
        if (news.getStatus() == null) news.setStatus(true);
        newsMapper.insert(news);
        updateNewsMaterialsAndCover(news);
        return news;
    }
    
    @Transactional
    public News updateNews(News news) {
        normalizeNewsContentMedia(news);
        updateNewsMaterialsAndCover(news);
        return newsMapper.findById(news.getId());
    }

    private void normalizeNewsContentMedia(News news) {
        if (news == null) return;
        if (fileStorageService == null) return;
        String content = news.getContent();
        if (content == null || content.trim().isEmpty()) return;

        String updated = fileStorageService.replaceMediaDataUrls(content, "news");
        if (updated != null && !updated.equals(content)) {
            news.setContent(updated);
        }
    }

    
    public void deleteNews(Long id) {
        newsMapper.delete(id);
    }
    
    // 素材审核
    public List<Material> getPendingMaterials() {
        List<Material> list = materialMapper.findByStatus("PENDING");
        if (list != null) {
            for (Material m : list) m.setContent(null); // 列表不需要庞大的富文本内容
        }
        return list;
    }
    
    public List<Material> getAllMaterialsForAdmin() {
        List<Material> list = materialMapper.findAllForAdmin();
        if (list != null) {
            for (Material m : list) m.setContent(null); // 列表不需要庞大的富文本内容
        }
        return list;
    }
    
    public Material getMaterialDetail(Long id) {
        Material material = materialMapper.findByIdWithFile(id);
        if (material == null) {
            throw new RuntimeException("素材不存在");
        }
        return material;
    }
    
    @Transactional
    public void approveMaterial(Long id) {
        materialMapper.updateStatus(id, "APPROVED");
        try {
            materialMapper.updateShelfStatus(id, "ON");
        } catch (Exception ignored) {
            // ignore
        }
        // 通知教师
        Material m = materialMapper.findByIdWithoutFile(id);
        if (m != null) {
            sendNotification(m.getTeacherId(), "TEACHER", "素材审核通过", 
                "您上传的素材《" + m.getTitle() + "》已通过审核，可以正常使用了。", "INFO");
        }
    }
    
    @Transactional
    public void rejectMaterial(Long id, String reason) {
        materialMapper.updateStatus(id, "REJECTED");
        try {
            materialMapper.updateShelfStatus(id, "OFF");
        } catch (Exception ignored) {
            // ignore
        }
        // 通知教师
        Material m = materialMapper.findByIdWithoutFile(id);
        if (m != null) {
            sendNotification(m.getTeacherId(), "TEACHER", "素材审核未通过", 
                "您上传的素材《" + m.getTitle() + "》未通过审核。原因：" + reason, "WARNING");
        }
    }
    
    @Transactional
    public void revokeMaterial(Long id, String reason) {
        // 获取素材信息
        Material m = materialMapper.findByIdWithoutFile(id);
        if (m == null) {
            throw new RuntimeException("素材不存在");
        }
        
        // 验证素材状态必须为 APPROVED
        if (!"APPROVED".equals(m.getStatus())) {
            throw new RuntimeException("只能撤回已通过的素材");
        }
        
        // 更新状态为 PENDING
        materialMapper.updateStatus(id, "PENDING");
        try {
            materialMapper.updateShelfStatus(id, "OFF");
        } catch (Exception ignored) {
            // ignore
        }
        
        // 通知教师
        sendNotification(m.getTeacherId(), "TEACHER", "素材审核已撤回", 
            "您的素材《" + m.getTitle() + "》的审核已被撤回，状态重新变为待审核。原因：" + reason, "WARNING");
    }

    @Transactional
    public void setMaterialShelfStatus(Long id, String shelfStatus) {
        if (id == null) throw new RuntimeException("ID不能为空");
        String s = (shelfStatus == null) ? "" : shelfStatus.trim().toUpperCase();
        if (!"ON".equals(s) && !"OFF".equals(s)) {
            throw new RuntimeException("上架状态错误");
        }

        Material m = materialMapper.findByIdWithoutFile(id);
        if (m == null) throw new RuntimeException("素材不存在");
        if (!"APPROVED".equals(m.getStatus())) {
            throw new RuntimeException("仅已通过审核的素材可上架/下架");
        }

        materialMapper.updateShelfStatus(id, s);
    }

    @Transactional
    public void updateMaterialCategory(Long id, String category) {
        if (id == null) throw new RuntimeException("ID不能为空");
        Material m = materialMapper.findByIdWithoutFile(id);
        if (m == null) throw new RuntimeException("素材不存在");

        String c = (category == null) ? null : category.trim();
        if (c != null && c.isEmpty()) c = null;
        if (c != null && c.length() > 50) {
            throw new RuntimeException("分类过长");
        }
        if (c != null) {
            boolean ok = "专业思政案例".equals(c)
                    || "政策解读".equals(c)
                    || "安全类思政".equals(c)
                    || "科技伦理".equals(c)
                    || "课程关联思政".equals(c);
            if (!ok) {
                throw new RuntimeException("分类不合法");
            }
        }
        materialMapper.updateCategory(id, c);
    }
    
    // 讨论管理
    @Transactional
    public void deleteDiscussionComment(Long commentId, String reason) {
        // 获取评论信息
        DiscussionComment comment = discussionMapper.findCommentById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        discussionMapper.deleteComment(commentId);

        sendNotification(comment.getUserId(), comment.getUserType(), "发言被删除", 
            "您的发言内容存在问题，已被管理员删除。原因：" + reason + "\n原内容：" + 
            (comment.getContent().length() > 50 ? comment.getContent().substring(0, 50) + "..." : comment.getContent()), 
            "WARNING");
    }
    
    //  时事论坛话题管理
    public List<ForumTopic> getAllTopics() {
        List<ForumTopic> topics = forumTopicMapper.findAll();
        // 论坛列表不加载素材，避免数据包过大
        // 素材信息只在详情页加载
        return topics;
    }
    
    public List<ForumTopic> getActiveTopics() {
        List<ForumTopic> topics = forumTopicMapper.findActive();
        // 论坛列表不加载素材，避免数据包过大
        // 素材信息只在详情页加载
        return topics;
    }

    public List<HotTopic> getHotTopics(int limit) {
        if (limit <= 0) limit = 5;
        if (limit > 20) limit = 20;

        List<HotTopic> result = new ArrayList<>();

        try {
            List<ForumTopic> topics = forumTopicMapper.findHotTopics(limit);
            for (ForumTopic t : topics) {
                HotTopic ht = new HotTopic();
                ht.setType("TOPIC");
                ht.setId(null);
                ht.setCode(t.getCode());
                ht.setTitle(t.getTitle());
                ht.setViewCount(t.getViewCount() == null ? 0 : t.getViewCount());
                result.add(ht);
            }
        } catch (Exception ignored) {
            // ignore
        }

        try {
            List<Discussion> discussions = discussionMapper.findHotDiscussions(limit);
            for (Discussion d : discussions) {
                HotTopic ht = new HotTopic();
                ht.setType("DISCUSSION");
                ht.setId(d.getId());
                ht.setCode(d.getCode());
                ht.setTitle(d.getTitle());
                ht.setViewCount(d.getViewCount() == null ? 0 : d.getViewCount());
                result.add(ht);
            }
        } catch (Exception ignored) {
            // ignore
        }

        result.sort(Comparator.comparing((HotTopic h) -> h.getViewCount() == null ? 0 : h.getViewCount()).reversed());
        if (result.size() > limit) {
            return new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }
    
    public ForumTopic getTopicById(Long id) {
        ForumTopic topic = forumTopicMapper.findById(id);
        if (topic != null) {
            // 获取关联的素材列表（不含file_url）
            List<Material> materials = forumTopicMapper.findMaterialsByTopicId(id);
            
            // 只为 ARTICLE 类型加载 content(文本内容不会太大)
            // VIDEO 和 IMAGE 类型不加载 file_url,改为前端按需加载
            if (materials != null && !materials.isEmpty()) {
                for (Material m : materials) {
                    // ARTICLE类型需要content
                    if ("ARTICLE".equals(m.getType())) {
                        Material fullMaterial = materialMapper.findByIdWithFile(m.getId());
                        if (fullMaterial != null) {
                            m.setContent(fullMaterial.getContent());
                        }
                    }
                    // VIDEO 和 IMAGE 类型不加载 file_url,避免数据包过大
                }
            }
            
            topic.setMaterials(materials);

            List<Long> newsIds = parseJsonIds(topic.getNewsIdsJson());
            topic.setNewsIds(newsIds);
            if (newsIds != null && !newsIds.isEmpty()) {
                try {
                    topic.setRelatedNews(newsMapper.findPublishedByIds(newsIds));
                } catch (Exception ignored) {
                    topic.setRelatedNews(Collections.emptyList());
                }
            } else {
                topic.setRelatedNews(Collections.emptyList());
            }
            forumTopicMapper.incrementViewCount(id);
        }
        return topic;
    }
    
    public ForumTopic getTopicByCode(String code) {
        ForumTopic topic = forumTopicMapper.findByCode(code);
        if (topic != null) {
            // 获取关联的素材列表（不含file_url）
            List<Material> materials = forumTopicMapper.findMaterialsByTopicId(topic.getId());
            
            // 只为 ARTICLE 类型加载 content(文本内容不会太大)
            // VIDEO 和 IMAGE 类型不加载 file_url,改为前端按需加载
            if (materials != null && !materials.isEmpty()) {
                for (Material m : materials) {
                    // ARTICLE类型需要content
                    if ("ARTICLE".equals(m.getType())) {
                        Material fullMaterial = materialMapper.findByIdWithFile(m.getId());
                        if (fullMaterial != null) {
                            m.setContent(fullMaterial.getContent());
                        }
                    }
                    // VIDEO 和 IMAGE 类型不加载 file_url,避免数据包过大
                }
            }
            
            topic.setMaterials(materials);

            List<Long> newsIds = parseJsonIds(topic.getNewsIdsJson());
            topic.setNewsIds(newsIds);
            if (newsIds != null && !newsIds.isEmpty()) {
                try {
                    topic.setRelatedNews(newsMapper.findPublishedByIds(newsIds));
                } catch (Exception ignored) {
                    topic.setRelatedNews(Collections.emptyList());
                }
            } else {
                topic.setRelatedNews(Collections.emptyList());
            }
            forumTopicMapper.incrementViewCount(topic.getId());
        }
        return topic;
    }
    
    @Transactional
    public ForumTopic createTopic(ForumTopic topic, List<Long> materialIds) {
        return createTopic(topic, materialIds, null);
    }

    @Transactional
    public ForumTopic createTopic(ForumTopic topic, List<Long> materialIds, List<Long> newsIds) {
        // 生成话题码
        String code = "TOPIC" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        topic.setCode(code);
        topic.setStatus("ACTIVE");
        topic.setNewsIdsJson(toJsonIds(newsIds));
        forumTopicMapper.insert(topic);
        
        // 保存素材关联
        if (materialIds != null && !materialIds.isEmpty()) {
            for (Long materialId : materialIds) {
                forumTopicMapper.insertTopicMaterial(topic.getId(), materialId);
            }
        }
        
        return topic;
    }
    
    @Transactional
    public ForumTopic updateTopic(ForumTopic topic, List<Long> materialIds) {
        return updateTopic(topic, materialIds, null);
    }

    @Transactional
    public ForumTopic updateTopic(ForumTopic topic, List<Long> materialIds, List<Long> newsIds) {
        topic.setNewsIdsJson(toJsonIds(newsIds));
        forumTopicMapper.update(topic);
        
        // 更新素材关联
        forumTopicMapper.deleteTopicMaterials(topic.getId());
        if (materialIds != null && !materialIds.isEmpty()) {
            for (Long materialId : materialIds) {
                forumTopicMapper.insertTopicMaterial(topic.getId(), materialId);
            }
        }
        
        return getTopicById(topic.getId());
    }
    
    @Transactional
    public void deleteTopic(Long id) {
        forumTopicMapper.deleteTopicMaterials(id);
        forumTopicMapper.delete(id);
    }
    
    // 话题评论
    public TopicComment addTopicComment(TopicComment comment) {
        if (comment == null) {
            throw new RuntimeException("参数错误");
        }

        String content = comment.getContent() == null ? "" : comment.getContent().trim();
        String imageUrl = comment.getImageUrl() == null ? null : comment.getImageUrl().trim();
        if ((content == null || content.isEmpty()) && (imageUrl == null || imageUrl.isEmpty())) {
            throw new RuntimeException("评论内容不能为空");
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            imageUrl = normalizeAndSaveTopicCommentImage(imageUrl);
            comment.setImageUrl(imageUrl);
        }

        if (content != null && !content.isEmpty()) {
            CommentModerationService.ModerationResult r = commentModerationService.moderateTopicComment(content);
            if (r == null || !r.isPass()) {
                String reason = (r == null) ? "内容可能包含敏感信息" : r.getReason();
                throw new RuntimeException(reason);
            }
        }
        comment.setIsPinned(false);
        comment.setStatus("NORMAL");
        forumTopicMapper.insertComment(comment);
        return comment;
    }

    public List<TopicComment> getTopicComments(Long topicId) {
        return getTopicComments(topicId, null, null);
    }

    public List<TopicComment> getTopicComments(Long topicId, Long currentUserId, String currentUserType) {
        List<TopicComment> comments = forumTopicMapper.findCommentsByTopicId(topicId);
        for (TopicComment c : comments) {
            loadCommentUserInfo(c);
            fillTopicCommentLiked(c, currentUserId, currentUserType);
            List<TopicComment> replies = forumTopicMapper.findRepliesByParentId(c.getId());
            for (TopicComment r : replies) {
                loadCommentUserInfo(r);
                loadReplyToUserInfo(r);
                fillTopicCommentLiked(r, currentUserId, currentUserType);
            }
            c.setReplies(replies);
        }
        return comments;
    }

    @Transactional
    public boolean toggleTopicCommentLike(Long commentId, Long userId, String userType) {
        if (commentId == null) throw new IllegalArgumentException("commentId不能为空");
        if (userId == null) throw new IllegalArgumentException("userId不能为空");
        if (userType == null || userType.trim().isEmpty()) throw new IllegalArgumentException("userType不能为空");

        String t = userType.trim().toUpperCase();
        if (!"STUDENT".equals(t) && !"TEACHER".equals(t) && !"ADMIN".equals(t)) {
            throw new IllegalArgumentException("userType仅支持STUDENT/TEACHER/ADMIN");
        }

        TopicComment c = forumTopicMapper.findCommentByIdForUpdate(commentId);
        if (c == null) throw new RuntimeException("评论不存在");

        String key = t + ":" + userId;
        Set<String> set = parseLikeUsers(c.getLikeUsersJson());
        boolean liked;
        if (set.contains(key)) {
            set.remove(key);
            liked = false;
        } else {
            set.add(key);
            liked = true;
        }
        String json = toLikeUsersJson(set);
        forumTopicMapper.updateLikeInfo(commentId, set.size(), json);
        return liked;
    }
    
    @Transactional
    public void deleteTopicComment(Long commentId, String reason) {
        TopicComment comment = forumTopicMapper.findCommentById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        forumTopicMapper.deleteComment(commentId, reason);
        
        // 通知发言人
        if (!"ADMIN".equals(comment.getUserType())) {
            sendNotification(comment.getUserId(), comment.getUserType(), "发言被删除", 
                "您在时事论坛的发言存在问题，已被管理员删除。原因：" + reason, "WARNING");
        }
    }
    
    public String toggleTopicCommentPin(Long commentId, Long topicId) {
        TopicComment comment = forumTopicMapper.findCommentById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        if (comment.getIsPinned() != null && comment.getIsPinned()) {
            forumTopicMapper.updateCommentPinned(commentId, false);
            return "已取消置顶";
        } else {
            int pinnedCount = forumTopicMapper.countPinnedComments(topicId);
            if (pinnedCount >= 3) {
                throw new RuntimeException("最多只能置顶3条评论");
            }
            forumTopicMapper.updateCommentPinned(commentId, true);
            return "已置顶";
        }
    }

    private String normalizeAndSaveTopicCommentImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) return imageUrl;
        String v = imageUrl.trim();
        if (fileStorageService == null) return v;
        if (!fileStorageService.isDataUrl(v)) return v;

        int headerEnd = v.indexOf(";base64,");
        if (headerEnd < 0) throw new RuntimeException("图片格式不正确");
        String mime = v.substring(5, headerEnd).toLowerCase();
        if (!(mime.startsWith("image/"))) {
            throw new RuntimeException("仅支持图片类型上传");
        }
        if (!(mime.equals("image/jpeg") || mime.equals("image/jpg") || mime.equals("image/png") || mime.equals("image/gif") || mime.equals("image/webp"))) {
            throw new RuntimeException("仅支持jpg/png/gif/webp图片");
        }

        String base64 = v.substring(v.indexOf(',') + 1);
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            if (bytes.length > MAX_TOPIC_COMMENT_IMAGE_BYTES) {
                throw new RuntimeException("图片过大，最大支持2MB");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("图片base64不合法");
        }

        return fileStorageService.saveDataUrl(v, "topic-comment");
    }

    private void fillTopicCommentLiked(TopicComment c, Long currentUserId, String currentUserType) {
        if (c == null) return;
        if (currentUserId == null || currentUserType == null || currentUserType.trim().isEmpty()) {
            c.setLiked(false);
            return;
        }
        String t = currentUserType.trim().toUpperCase();
        if (!"STUDENT".equals(t) && !"TEACHER".equals(t) && !"ADMIN".equals(t)) {
            c.setLiked(false);
            return;
        }
        String key = t + ":" + currentUserId;
        String json = c.getLikeUsersJson();
        if (json == null || json.trim().isEmpty()) {
            c.setLiked(false);
            return;
        }
        String token = "\"" + key + "\"";
        c.setLiked(json.contains(token));
    }

    private Set<String> parseLikeUsers(String json) {
        Set<String> set = new LinkedHashSet<>();
        if (json == null) return set;
        String t = json.trim();
        if (t.isEmpty()) return set;
        if (t.startsWith("[") && t.endsWith("]")) {
            String inner = t.substring(1, t.length() - 1).trim();
            if (inner.isEmpty()) return set;
            String[] parts = inner.split("\\s*,\\s*");
            for (String p : parts) {
                if (p == null) continue;
                String s = p.trim();
                if (s.startsWith("\"") && s.endsWith("\"")) {
                    s = s.substring(1, s.length() - 1);
                }
                if (!s.isEmpty()) set.add(s);
            }
            return set;
        }
        set.add(t);
        return set;
    }

    private String toLikeUsersJson(Set<String> set) {
        if (set == null || set.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (String s : set) {
            if (s == null) continue;
            String v = s.replace("\\", "\\\\").replace("\"", "\\\"");
            if (!first) sb.append(',');
            sb.append('"').append(v).append('"');
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }
    
    // 通知相关
    public void sendNotification(Long userId, String userType, String title, String content, String type) {
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setUserType(userType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notificationMapper.insert(notification);
    }
    
    public List<UserNotification> getUserNotifications(Long userId, String userType) {
        return notificationMapper.findByUser(userId, userType);
    }
    
    public int getUnreadCount(Long userId, String userType) {
        return notificationMapper.countUnread(userId, userType);
    }
    
    public void markNotificationAsRead(Long id) {
        notificationMapper.markAsRead(id);
    }
    
    public void markAllNotificationsAsRead(Long userId, String userType) {
        notificationMapper.markAllAsRead(userId, userType);
    }
    
    // 辅助方法
    private void loadCommentUserInfo(TopicComment comment) {
        User user = null;
        if ("STUDENT".equals(comment.getUserType())) {
            user = userMapper.findStudentById(comment.getUserId());
        } else if ("TEACHER".equals(comment.getUserType())) {
            user = userMapper.findTeacherById(comment.getUserId());
        } else if ("ADMIN".equals(comment.getUserType())) {
            Admin admin = adminMapper.findById(comment.getUserId());
            if (admin != null) {
                comment.setUserName(admin.getUsername());
                comment.setUserRealName(admin.getRealName());
                comment.setUserAvatar(admin.getAvatar());
            }
            return;
        }
        if (user != null) {
            comment.setUserName(user.getUsername());
            comment.setUserRealName(user.getRealName());
            comment.setUserAvatar(user.getAvatar());
        }
    }
    
    private void loadReplyToUserInfo(TopicComment comment) {
        if (comment.getReplyToUserId() == null) return;
        
        User user = null;
        if ("STUDENT".equals(comment.getReplyToUserType())) {
            user = userMapper.findStudentById(comment.getReplyToUserId());
        } else if ("TEACHER".equals(comment.getReplyToUserType())) {
            user = userMapper.findTeacherById(comment.getReplyToUserId());
        } else if ("ADMIN".equals(comment.getReplyToUserType())) {
            Admin admin = adminMapper.findById(comment.getReplyToUserId());
            if (admin != null) {
                comment.setReplyToUserName(admin.getUsername());
                comment.setReplyToUserRealName(admin.getRealName());
            }
            return;
        }
        if (user != null) {
            comment.setReplyToUserName(user.getUsername());
            comment.setReplyToUserRealName(user.getRealName());
        }
    }
    
    // 素材文件按需加载
    public Material getMaterialFile(Long id) {
        // 只返回 id 和 file_url
        Material material = materialMapper.findFileUrlById(id);
        if (material == null) {
            throw new RuntimeException("素材不存在");
        }
        return material;
    }

    public Long getMaterialFileUrlLength(Long id) {
        return materialMapper.findFileUrlLength(id);
    }

    public String getMaterialFileUrlChunk(Long id, long offset) {
        return materialMapper.findFileUrlChunk(id, offset, FILE_URL_CHUNK_SIZE);
    }
    
    public List<Material> getMaterialFiles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        // 限制最多10个
        if (ids.size() > 10) {
            throw new RuntimeException("单次最多获取10个素材文件");
        }
        return materialMapper.findFileUrlsByIdsLimited(ids);
    }
}
