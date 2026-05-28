package com.example.back.controller;

import com.example.back.dto.MaterialRequest;
import com.example.back.dto.Result;
import com.example.back.entity.Material;
import com.example.back.entity.Tag;
import com.example.back.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping("/tags")
    public Result<List<Tag>> getAllTags(@RequestParam(required = false) String category) {
        return Result.success(materialService.getAllTags(category));
    }

    @GetMapping("/approved")
    public Result<List<Material>> getApprovedMaterials() {
        try {
            List<Material> materials = materialService.getApprovedMaterials();
            return Result.success(materials);
        } catch (Exception e) {
            return Result.error("获取已审核素材失败: " + e.getMessage());
        }
    }

    @PostMapping("/tag")
    public Result<Tag> createTag(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String category = body.get("category");
        if (name == null || name.trim().isEmpty()) {
            return Result.error("标签名称不能为空");
        }
        if (category == null || category.trim().isEmpty()) {
            return Result.error("请选择标签大类");
        }
        return Result.success(materialService.createTag(name.trim(), category.trim()));
    }

    @PostMapping("/tag/category")
    public Result<String> updateTagCategory(@RequestBody Map<String, Object> body) {
        try {
            Object idObj = body.get("id");
            String category = body.get("category") != null ? String.valueOf(body.get("category")) : null;
            Long id = idObj instanceof Number ? ((Number) idObj).longValue() : (idObj != null ? Long.valueOf(String.valueOf(idObj)) : null);
            materialService.updateTagCategory(id, category);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/{teacherId}")
    public Result<List<Material>> getByTeacher(@PathVariable Long teacherId) {
        try {
            System.out.println("获取教师素材, teacherId: " + teacherId);
            List<Material> materials = materialService.getMaterialsByTeacher(teacherId);
            System.out.println("返回素材数量: " + (materials != null ? materials.size() : 0));
            return Result.success(materials);
        } catch (Exception e) {
            System.err.println("获取教师素材失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取素材失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/all")
    public Result<List<Material>> getAllMaterials() {
        try {
            System.out.println("获取所有素材");
            List<Material> materials = materialService.getAllMaterials();
            System.out.println("返回所有素材数量: " + (materials != null ? materials.size() : 0));
            return Result.success(materials);
        } catch (Exception e) {
            System.err.println("获取所有素材失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取素材失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public Result<Material> getById(@PathVariable Long id) {
        Material m = materialService.getMaterialById(id);
        return m != null ? Result.success(m) : Result.error("素材不存在");
    }
    
    // 单独获取文件URL（用于下载/查看大文件）
    @GetMapping("/file/{id}")
    public Result<String> getFileUrl(@PathVariable Long id) {
        try {
            String fileUrl = materialService.getFileUrlById(id);
            return fileUrl != null ? Result.success(fileUrl) : Result.error("文件不存在");
        } catch (Exception e) {
            return Result.error("获取文件失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public Result<Material> create(@RequestBody MaterialRequest request) {
        try {
            Material m = new Material();
            m.setTitle(request.getTitle());
            m.setType(request.getType());
            m.setContent(request.getContent());
            m.setFileUrl(request.getFileUrl());
            m.setDescription(request.getDescription());
            m.setTeacherId(request.getTeacherId());
            Material created = materialService.createMaterial(m, request.getTagIds());
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Result<Material> update(@RequestBody MaterialRequest request) {
        try {
            Material m = new Material();
            m.setId(request.getId());
            m.setTitle(request.getTitle());
            m.setContent(request.getContent());
            m.setFileUrl(request.getFileUrl());
            m.setDescription(request.getDescription());
            Material updated = materialService.updateMaterial(m, request.getTagIds());
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            materialService.deleteMaterial(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
