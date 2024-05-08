package com.zc.flowabledemo.task;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @author zengc
 * @date 2024/05/06
 */
@Slf4j
public class FailTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("请假失败...");
    }
}
