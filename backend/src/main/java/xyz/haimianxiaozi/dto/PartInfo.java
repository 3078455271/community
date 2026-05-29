package xyz.haimianxiaozi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分片信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartInfo {

    /** 分片编号 */
    private Integer partNumber;

    /** 分片 ETag */
    private String etag;
}