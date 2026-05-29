package xyz.haimianxiaozi.service;

import xyz.haimianxiaozi.dto.PartInfo;

import java.util.List;

/**
 * OSS 对象存储服务接口
 */
public interface OssService {

    /**
     * 初始化分片上传
     *
     * @param objectKey 对象键
     * @return uploadId
     */
    String initMultipartUpload(String objectKey);

    /**
     * 上传分片
     *
     * @param objectKey 对象键
     * @param uploadId  上传 ID
     * @param partNumber 分片编号
     * @param data      分片数据
     * @return ETag
     */
    String uploadPart(String objectKey, String uploadId, int partNumber, byte[] data);

    /**
     * 完成分片上传
     *
     * @param objectKey 对象键
     * @param uploadId  上传 ID
     * @param parts     分片信息列表
     * @return 文件访问 URL
     */
    String completeMultipartUpload(String objectKey, String uploadId, List<PartInfo> parts);

    /**
     * 取消分片上传
     *
     * @param objectKey 对象键
     * @param uploadId  上传 ID
     */
    void abortMultipartUpload(String objectKey, String uploadId);

    /**
     * 生成对象键
     *
     * @param originalFilename 原始文件名
     * @return 对象键
     */
    String generateObjectKey(String originalFilename);

    /**
     * 获取文件访问 URL
     *
     * @param objectKey 对象键
     * @return 文件访问 URL
     */
    String getFileUrl(String objectKey);
}