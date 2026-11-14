package com.zhmindnotes.service;

import com.zhmindnotes.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 本地文件存储：图片 / 音频片段保存到 uploads，返回前端可访问的相对 URL。
 */
@Service
public class FileStorageService {

    private static final List<String> IMAGE_EXT = Arrays.asList("png", "jpg", "jpeg", "gif", "webp");
    private static final List<String> AUDIO_EXT = Arrays.asList("mp3", "wav", "m4a", "aac", "ogg");

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.access-prefix:/uploads/}")
    private String accessPrefix;

    /**
     * @param kind image / audio
     * @return 前端可直接使用的相对 URL，如 /uploads/xxx.png
     */
    public String store(MultipartFile file, String kind) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件为空");
        }
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new BusinessException("上传目录初始化失败：" + e.getMessage());
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = getExtension(original);
        if ("image".equals(kind) && !IMAGE_EXT.contains(ext)) {
            throw new BusinessException(400, "仅支持 png/jpg/jpeg/gif/webp 图片");
        }
        if ("audio".equals(kind) && !AUDIO_EXT.contains(ext)) {
            throw new BusinessException(400, "仅支持 mp3/wav/m4a/aac/ogg 音频");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = Paths.get(uploadDir, filename);
        try {
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }
        return accessPrefix + filename;
    }

    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0) return "";
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
