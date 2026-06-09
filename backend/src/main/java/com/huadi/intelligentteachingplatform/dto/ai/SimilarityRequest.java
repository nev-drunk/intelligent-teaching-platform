package com.huadi.intelligentteachingplatform.dto.ai;

import lombok.Data;
import java.util.List;

/**
 * 相似度对比请求 DTO
 */
@Data
public class SimilarityRequest {
    private String text1;
    private String text2;
}
