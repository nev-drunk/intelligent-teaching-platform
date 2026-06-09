package com.huadi.intelligentteachingplatform.dto.ai;

import lombok.Data;
import java.util.List;

/**
 * OCR 识别结果 DTO
 */
@Data
public class OcrResult {
    private String text;
    private List<String> chars;
}
