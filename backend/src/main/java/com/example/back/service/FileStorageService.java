package com.example.back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String DEFAULT_CATEGORY = "common";

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public boolean isDataUrl(String value) {
        return value != null && value.startsWith("data:") && (value.contains(";base64,") || value.contains(":base64,"));
    }

    public String saveFile(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String normalizedCategory = normalizeCategory(category);
            byte[] bytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            } else {
                ext = extensionFromMime(file.getContentType());
            }

            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String hash = computeMd5Prefix(bytes);
            String ts = String.valueOf(LocalDateTime.now().getNano() % 100000);
            String fileName = hash + ts + (ext.isEmpty() ? "" : ("." + ext));

            Path dir = Paths.get(uploadDir, normalizedCategory, date);
            Path filePath = dir.resolve(fileName);

            if (Files.exists(filePath)) {
                return "/uploads/" + normalizedCategory + "/" + date + "/" + fileName;
            }

            Files.createDirectories(dir);
            Files.write(filePath, bytes);

            return "/uploads/" + normalizedCategory + "/" + date + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    public String saveDataUrl(String dataUrl, String category) {
        if (!isDataUrl(dataUrl)) {
            return dataUrl;
        }

        String normalizedCategory = normalizeCategory(category);

        int base64Idx = dataUrl.indexOf(";base64,");
        if (base64Idx == -1) {
            base64Idx = dataUrl.indexOf(":base64,");
        }
        if (base64Idx == -1) {
            return dataUrl;
        }

        String header = dataUrl.substring(5, base64Idx);
        String base64 = dataUrl.substring(base64Idx + 8);

        String ext = extensionFromMime(header);
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        // 使用 MD5哈希前16位 + 时间戳短码命名文件，兼具唯一性与可读性
        // 相同内容 MD5 相同，可实现幂等去重
        String hash = computeMd5Prefix(base64);
        String ts = String.valueOf(LocalDateTime.now().getNano() % 100000); // 5位微秒尾号
        String fileName = hash + ts + (ext.isEmpty() ? "" : ("." + ext));

        Path dir = Paths.get(uploadDir, normalizedCategory, date);
        Path filePath = dir.resolve(fileName);

        // 幂等：若同名文件已存在（同内容），直接返回现有路径，避免重复落盘
        if (Files.exists(filePath)) {
            return "/uploads/" + normalizedCategory + "/" + date + "/" + fileName;
        }

        try {
            Files.createDirectories(dir);
            byte[] bytes = Base64.getDecoder().decode(base64);
            Files.write(filePath, bytes);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("base64内容不合法", e);
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败", e);
        }

        return "/uploads/" + normalizedCategory + "/" + date + "/" + fileName;
    }

    /**
     * 替换 HTML 文本中所有内嵌的 base64 媒体数据为文件路径，避免超大字段。
     */
    public String replaceMediaDataUrls(String content, String categoryPrefix) {
        if (content == null || content.isEmpty()) return content;

        java.util.regex.Pattern p = java.util.regex.Pattern.compile("src\\s*=\\s*([\"'])(data:(image|video)/.*?[;:]base64,.*?)\\1", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher m = p.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean changed = false;

        while (m.find()) {
            String quote = m.group(1);
            String dataUrl = m.group(2);
            String type = m.group(3).toLowerCase(); // image or video
            String url = dataUrl;
            try {
                url = saveDataUrl(dataUrl, categoryPrefix + "-" + type);
            } catch (Exception ignored) { }

            String replacement = "src=" + quote + url + quote;
            m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
            changed = true;
        }
        m.appendTail(sb);
        return changed ? sb.toString() : content;
    }

    /**
     * 计算 base64 字符串的 MD5 哈希前16位。
     */
    private String computeMd5Prefix(String base64) {
        try {
            byte[] sample = base64.length() > 4096
                    ? base64.substring(0, 4096).getBytes()
                    : base64.getBytes();
            return computeMd5Prefix(sample);
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }

    /**
     * 计算字节数组的 MD5 哈希前16位。
     */
    private String computeMd5Prefix(byte[] bytes) {
        try {
            byte[] sample = bytes.length > 4096 ? java.util.Arrays.copyOf(bytes, 4096) : bytes;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sample);
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) { // 取前8字节 = 16个16进制字符
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }

    private String extensionFromMime(String mime) {
        if (mime == null) return "";
        mime = mime.toLowerCase();
        return switch (mime) {
            case "image/jpeg" -> "jpg";
            case "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "video/mp4" -> "mp4";
            case "video/webm" -> "webm";
            case "video/ogg" -> "ogg";
            default -> "bin";
        };
    }

    public String getUploadDirAbsolutePath() {
        Path p = Paths.get(uploadDir).toAbsolutePath().normalize();
        return p.toString();
    }

    private String normalizeCategory(String category) {
        String value = category == null ? "" : category.trim().toLowerCase();
        if (value.isEmpty()) {
            return DEFAULT_CATEGORY;
        }

        value = value.replace('\\', '/');
        value = value.replaceAll("/+", "-");
        value = value.replaceAll("[^a-z0-9._-]", "-");
        value = value.replaceAll("-+", "-");
        value = value.replaceAll("^[._-]+|[._-]+$", "");

        if (value.isEmpty()) {
            return DEFAULT_CATEGORY;
        }

        return value;
    }
}
