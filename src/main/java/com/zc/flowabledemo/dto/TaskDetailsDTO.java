package com.zc.flowabledemo.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author zengc
 * @date 2024/05/08
 */
@Data
public class TaskDetailsDTO {
    private String taskId;
    private String taskName;
    private String assignee;
    private Date startTime;
    private Date endTime;
    private Map<String, Object> variables;
    private String processInstanceId;
    private String processDefinitionId;
    private Date processStartTime;
    private Date processEndTime;
}
