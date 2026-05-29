package xyz.haimianxiaozi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分片上传相关 DTO
 */
public class MultipartUploadDTO {

    /**
     * 初始化分片上传请求
     */
    @Data
    public static class InitRequest {

        @NotBlank(message = "文件名不能为空")
        private String filename;

        @NotNull(message = "文件大小不能为空")
        private Long fileSize;
    }

    /**
     * 上传分片请求
     */
    @Data
    public static class UploadPartRequest {

        @NotBlank(message = "uploadId 不能为空")
        private String uploadId;

        @NotBlank(message = "objectKey 不能为空")
        private String objectKey;

        @NotNull(message = "分片编号不能为空")
        private Integer partNumber;
    }

    /**
     * 完成分片上传请求
     */
    @Data
    public static class CompleteRequest {

        @NotBlank(message = "uploadId 不能为空")
        private String uploadId;

        @NotBlank(message = "objectKey 不能为空")
        private String objectKey;

        @NotNull(message = "分片信息不能为空")
        private List<PartInfo> parts;
    }

    /**
     * 取消分片上传请求
     */
    @Data
    public static class AbortRequest {

        @NotBlank(message = "uploadId 不能为空")
        private String uploadId;

        @NotBlank(message = "objectKey 不能为空")
        private String objectKey;
    }

    /**
     * 初始化分片上传响应
     */
    @Data
    public static class InitResponse {

        private String uploadId;
        private String objectKey;
        private Integer chunkSize;

        public InitResponse(String uploadId, String objectKey, Integer chunkSize) {
            this.uploadId = uploadId;
            this.objectKey = objectKey;
            this.chunkSize = chunkSize;
        }
    }
}