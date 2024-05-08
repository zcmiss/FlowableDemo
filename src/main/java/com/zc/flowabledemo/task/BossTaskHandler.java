package com.zc.flowabledemo.task;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * @author zengc
 * @date 2024/05/08
 * @description 老板审批任务
 */
public class BossTaskHandler implements TaskListener {
    /**
     * @param delegateTask 任务的分配人
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setAssignee("老板");
    }
}
