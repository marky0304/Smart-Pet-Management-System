package com.pet.controller;

import com.pet.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    @Value("${file.upload.url:http://localhost:8080/uploads/}")
    private String uploadUrl;

    /**
     * 上传单个图片
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }

        // 校验文件类型（Content-Type + 扩展名双重校验）
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase() : "";
        boolean validType = contentType != null && contentType.startsWith("image/")
                && (originalFilename.endsWith(".jpg") || originalFilename.endsWith(".jpeg")
                || originalFilename.endsWith(".png") || originalFilename.endsWith(".gif")
                || originalFilename.endsWith(".webp"));
        if (!validType) {
            return Result.error(400, "只支持 JPG/PNG/GIF/WEBP 格式的图片");
        }

        // 校验文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error(400, "文件大小不能超过5MB");
        }

        try {
            String fileName = saveFile(file, "images");
            String fileUrl = uploadUrl + "images/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            return Result.error(500, "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传多个图片
     */
    @PostMapping("/images")
    public Result<List<String>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files.length == 0) {
            return Result.error(400, "请选择要上传的文件");
        }

        if (files.length > 9) {
            return Result.error(400, "最多只能上传9张图片");
        }

        List<String> fileUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error(400, "只支持图片文件");
            }

            // 检查文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error(400, "单个文件大小不能超过5MB");
            }

            try {
                String fileName = saveFile(file, "images");
                String fileUrl = uploadUrl + "images/" + fileName;
                fileUrls.add(fileUrl);
            } catch (IOException e) {
                return Result.error(500, "文件上传失败：" + e.getMessage());
            }
        }

        return Result.success(fileUrls);
    }

    /**
     * 上传视频
     */
    @PostMapping("/video")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            return Result.error(400, "只支持视频文件");
        }

        // 检查文件大小（50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            return Result.error(400, "视频文件大小不能超过50MB");
        }

        try {
            String fileName = saveFile(file, "videos");
            String fileUrl = uploadUrl + "videos/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            return Result.error(500, "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只支持图片文件");
        }

        // 检查文件大小（2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error(400, "头像文件大小不能超过2MB");
        }

        try {
            String fileName = saveFile(file, "avatars");
            String fileUrl = uploadUrl + "avatars/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            return Result.error(500, "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 保存文件
     */
    private String saveFile(MultipartFile file, String subDir) throws IOException {
        // 转为绝对路径
        String basePath = new File(uploadPath).getAbsolutePath();
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fullPath = basePath + File.separator + subDir + File.separator + datePath;
        File uploadDir = new File(fullPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成文件名（UUID，防止目录穿越和文件名注入）
        String origName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String extension = "";
        if (origName.contains(".")) {
            String ext = origName.substring(origName.lastIndexOf(".")).toLowerCase();
            // 白名单扩展名
            if (ext.matches("\\.(jpg|jpeg|png|gif|webp|mp4|mov|avi)")) {
                extension = ext;
            }
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // 保存文件
        File targetFile = new File(uploadDir, fileName);
        file.transferTo(targetFile);

        return datePath + "/" + fileName;
    }
}