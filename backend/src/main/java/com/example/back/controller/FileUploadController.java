package com.example.back.controller;

import com.example.back.dto.Result;
import com.example.back.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "common") String category) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error(400, "文件不能为空");
            }

            String url = fileStorageService.saveFile(file, category);
            return Result.success(url);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
