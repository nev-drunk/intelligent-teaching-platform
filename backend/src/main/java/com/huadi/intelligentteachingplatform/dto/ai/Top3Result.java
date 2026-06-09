package com.huadi.intelligentteachingplatform.dto.ai;

import lombok.Data;
import java.util.List;

/**
 * Top3 相似问题结果 DTO
 */
@Data
public class Top3Result {
    private List<SimilarItem> results;
}
