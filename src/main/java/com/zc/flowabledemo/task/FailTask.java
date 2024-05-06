package com.zc.flowabledemo.task;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zengc
 * @date 2024/05/06
 */
public class FailTask implements JavaDelegate {
    private static final Logger log = LoggerFactory.getLogger(FailTask.class);

    @Override
    public void execute(DelegateExecution execution) {
        log.info("请假失败...");
    }
}
