package com.huadi.intelligentteachingplatform.dto.ai;

import lombok.Data;

/**
 * 相似问题条目
 */
@Data
public class SimilarItem {
    private Object id;
    private String text;
    private Double score;
    private Integer index;
}
