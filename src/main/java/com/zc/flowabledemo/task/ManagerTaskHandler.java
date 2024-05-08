package com.zc.flowabledemo.task;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * @author zengc
 * @date 2024/05/08
 * @description 经理审批
 */
public class ManagerTaskHandler implements TaskListener {
    /**
     * @param delegateTask 任务分配人
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setAssignee("经理");
    }
}
