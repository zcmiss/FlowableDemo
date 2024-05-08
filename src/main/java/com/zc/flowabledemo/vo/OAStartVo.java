package com.zc.flowabledemo.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * @author zengc
 * @date 2024/05/08
 */
@Data
public class OAStartVo {
    @NotBlank(message = "userId不能为空")
    private String userId;
    @Positive(message = "money必须为正数")
    @NotBlank(message = "money不能为空")
    private Integer money;
    private String description;
}
