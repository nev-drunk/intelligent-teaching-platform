package com.huadi.intelligentteachingplatform.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 相似度候选条目（请求用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarCandidate {
    private Object id;
    private String text;
}
