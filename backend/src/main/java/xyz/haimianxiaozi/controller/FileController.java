package xyz.haimianxiaozi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.haimianxiaozi.common.R;
import xyz.haimianxiaozi.dto.MultipartUploadDTO;
import xyz.haimianxiaozi.dto.PartInfo;
import xyz.haimianxiaozi.service.OssService;
import xyz.haimianxiaozi.util.UserContext;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final UserContext userContext;
    private final OssService ossService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    /**
     * 单文件上传（保留原有接口，兼容小文件）
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }

        // 检查文件大小（限制 5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return R.fail("文件大小不能超过 5MB");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return R.fail("文件名不能为空");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!isAllowedExtension(extension)) {
            return R.fail("不支持的文件类型");
        }

        // 生成唯一文件名
        String filename = UUID.randomUUID().toString() + extension;

        // 创建上传目录
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }

        // 保存文件
        try {
            File destFile = new File(uploadPath, filename);
            file.transferTo(destFile);
            return R.ok(baseUrl + "/" + filename);
        } catch (IOException e) {
            return R.fail("文件上传失败");
        }
    }

    /**
     * 初始化分片上传
     */
    @PostMapping("/multipart/init")
    public R<MultipartUploadDTO.InitResponse> initMultipartUpload(
            @Valid @RequestBody MultipartUploadDTO.InitRequest request) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        // 生成对象键
        String objectKey = ossService.generateObjectKey(request.getFilename());

        // 初始化分片上传
        String uploadId = ossService.initMultipartUpload(objectKey);

        // 分片大小 5MB
        int chunkSize = 5 * 1024 * 1024;

        return R.ok(new MultipartUploadDTO.InitResponse(uploadId, objectKey, chunkSize));
    }

    /**
     * 上传分片
     */
    @PostMapping("/multipart/upload")
    public R<PartInfo> uploadPart(
            @Valid @RequestBody MultipartUploadDTO.UploadPartRequest request,
            @RequestParam("file") MultipartFile file) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        try {
            String etag = ossService.uploadPart(
                    request.getObjectKey(),
                    request.getUploadId(),
                    request.getPartNumber(),
                    file.getBytes());

            return R.ok(new PartInfo(request.getPartNumber(), etag));
        } catch (IOException e) {
            return R.fail("分片上传失败");
        }
    }

    /**
     * 完成分片上传
     */
    @PostMapping("/multipart/complete")
    public R<String> completeMultipartUpload(
            @Valid @RequestBody MultipartUploadDTO.CompleteRequest request) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        String fileUrl = ossService.completeMultipartUpload(
                request.getObjectKey(),
                request.getUploadId(),
                request.getParts());

        return R.ok(fileUrl);
    }

    /**
     * 取消分片上传
     */
    @PostMapping("/multipart/abort")
    public R<Void> abortMultipartUpload(
            @Valid @RequestBody MultipartUploadDTO.AbortRequest request) {
        Long userId = userContext.getCurrentUserId();
        if (userId == null) {
            return R.fail(401, "请先登录");
        }

        ossService.abortMultipartUpload(request.getObjectKey(), request.getUploadId());
        return R.ok();
    }

    private boolean isAllowedExtension(String extension) {
        return extension.equals(".jpg") ||
                extension.equals(".jpeg") ||
                extension.equals(".png") ||
                extension.equals(".gif") ||
                extension.equals(".webp");
    }
}