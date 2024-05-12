package com.zc.flowabledemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zengc
 * @date 2024/05/08
 * @description 代办数据列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {
    private String id;
    private String name;
    private String userId;
    private Integer money;
    private String description;

}
