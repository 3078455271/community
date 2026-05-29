package xyz.haimianxiaozi.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.haimianxiaozi.config.OssConfig;
import xyz.haimianxiaozi.dto.PartInfo;
import xyz.haimianxiaozi.service.OssService;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

/**
 * OSS 对象存储服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    /**
     * 分片大小：5MB
     */
    private static final int CHUNK_SIZE = 5 * 1024 * 1024;

    @Override
    public String initMultipartUpload(String objectKey) {
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(ossConfig.getBucketName(), objectKey);
        InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
        String uploadId = result.getUploadId();
        log.info("初始化分片上传成功，uploadId: {}, objectKey: {}", uploadId, objectKey);
        return uploadId;
    }

    @Override
    public String uploadPart(String objectKey, String uploadId, int partNumber, byte[] data) {
        UploadPartRequest request = new UploadPartRequest();
        request.setBucketName(ossConfig.getBucketName());
        request.setKey(objectKey);
        request.setUploadId(uploadId);
        request.setPartNumber(partNumber);
        request.setInputStream(new ByteArrayInputStream(data));
        request.setPartSize(data.length);

        UploadPartResult result = ossClient.uploadPart(request);
        String etag = result.getPartETag().getETag();
        log.info("分片上传成功，partNumber: {}, etag: {}", partNumber, etag);
        return etag;
    }

    @Override
    public String completeMultipartUpload(String objectKey, String uploadId, List<PartInfo> parts) {
        // 将 PartInfo 转换为 PartETag
        List<PartETag> partETags = parts.stream()
                .map(part -> new PartETag(part.getPartNumber(), part.getEtag()))
                .toList();

        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(
                ossConfig.getBucketName(), objectKey, uploadId, partETags);
        ossClient.completeMultipartUpload(request);

        String fileUrl = getFileUrl(objectKey);
        log.info("分片上传完成，objectKey: {}, fileUrl: {}", objectKey, fileUrl);
        return fileUrl;
    }

    @Override
    public void abortMultipartUpload(String objectKey, String uploadId) {
        AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(
                ossConfig.getBucketName(), objectKey, uploadId);
        ossClient.abortMultipartUpload(request);
        log.info("取消分片上传成功，uploadId: {}, objectKey: {}", uploadId, objectKey);
    }

    @Override
    public String generateObjectKey(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "uploads/" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    @Override
    public String getFileUrl(String objectKey) {
        return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectKey;
    }
}