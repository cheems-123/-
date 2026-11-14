package com.zhmindnotes.controller;

import com.zhmindnotes.common.Result;
import com.zhmindnotes.common.SessionUtil;
import com.zhmindnotes.service.FileStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService storageService;

    public FileController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * 上传图片或音频。
     * form-data: file 文件；kind=image|audio
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                               @RequestParam(defaultValue = "image") String kind,
                                               HttpServletRequest request) {
        SessionUtil.requireUid(request);
        String url = storageService.store(file, kind);
        return Result.ok(Map.of("url", url, "kind", kind));
    }
}
