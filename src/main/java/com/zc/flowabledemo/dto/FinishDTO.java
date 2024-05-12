package com.zc.flowabledemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zengc
 * @date 2024/05/08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinishDTO {
    private String id;
    private String name;
    private String approved;
}
