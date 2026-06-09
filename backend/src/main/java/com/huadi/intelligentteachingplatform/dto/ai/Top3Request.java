package com.huadi.intelligentteachingplatform.dto.ai;

import lombok.Data;
import java.util.List;

/**
 * Top3 相似问题请求 DTO
 */
@Data
public class Top3Request {
    private String query;
    private List<String> candidates;
}
