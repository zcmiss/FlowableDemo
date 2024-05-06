package com.zc.flowabledemo.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * 控制器类，用于处理流程相关的请求
 *
 * @author zengc
 */
@RestController
@Slf4j
public class FlowableController {

    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private ProcessEngine processEngine;

    // 定义角色常量
    private static final String EMPLOYEE = "1001";
    private static final String MENTOR = "101";
    private static final String MANAGER = "10";


    /**
     * 根据给定的流程ID显示流程图。
     *
     * @param response  用于处理HTTP响应的HttpServletResponse对象
     * @param processId 表示流程实例的唯一标识符的字符串
     * @throws IOException 如果在读取或写入时发生I/O错误
     */
    @GetMapping("/pic")
    public void showProcessDiagram(HttpServletResponse response, @RequestParam("processId") String processId) throws IOException {
        // 检索流程实例信息
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

        // 如果未找到流程实例，则返回
        if (processInstance == null) {
            return;
        }

        // 检索流程实例的执行列表
        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceId(processId)
                .list();

        // 从执行中提取活动的活动ID
        List<String> activityIds = new ArrayList<>();
        executions.forEach(execution -> {
            List<String> activeActivityIds = runtimeService.getActiveActivityIds(execution.getId());
            activityIds.addAll(activeActivityIds);
        });

        // 生成流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        InputStream inputStream = processDiagramGenerator.generateDiagram(
                bpmnModel,
                "png",
                activityIds,
                // 不需要流程图的流程路径
                Collections.emptyList(),
                processEngineConfiguration.getActivityFontName(),
                processEngineConfiguration.getLabelFontName(),
                processEngineConfiguration.getAnnotationFontName(),
                processEngineConfiguration.getClassLoader(),
                1.0,
                false
        );

        OutputStream out = null;
        try {
            out = response.getOutputStream();
            byte[] bytes = new byte[2048];
            int length;
            while ((length = inputStream.read(bytes)) != -1) {
                out.write(bytes, 0, length);
            }
        } finally {
            // 关闭输入流和输出流
            if (Objects.nonNull(inputStream)) {
                inputStream.close();
            }
            if (Objects.nonNull(out)) {
                out.close();
            }
        }
    }

    /**
     * 启动流程实例
     */
    @PostMapping("/start")
    public void start() {
        Map<String, Object> map = Map.of(
                "employee", EMPLOYEE,
                "name", "ego",
                "reason", "出去玩",
                "days", 10
        );
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("askForLeave", map);
        log.info("开启请假流程 processId:{}", instance.getId());
    }

    /**
     * 员工提交给组长
     */
    @PostMapping("/submitToMentor")
    public void submitToMentor() {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(EMPLOYEE)
                .orderByTaskId()
                .desc()
                .list();

        tasks.forEach(task -> {
            Map<String, Object> variables = taskService.getVariables(task.getId());
            log.info("员工任务：任务id:{}, 请假人：{}, 请假原因：{}, 请假天数：{}",
                    task.getId(), variables.get("name"), variables.get("reason"), variables.get("days"));

            Map<String, Object> map = new HashMap<>(1);
            map.put("mentor", MENTOR);
            taskService.complete(task.getId(), map);
        });
    }

    /**
     * 组长提交给经理
     *
     * @param approved 是否通过
     */
    @PostMapping("/submitToManager")
    public void submitToManager(Boolean approved) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(MENTOR)
                .orderByTaskId()
                .desc()
                .list();

        tasks.forEach(task -> {
            Map<String, Object> variables = taskService.getVariables(task.getId());
            log.info("组长任务：任务id:{}, 请假人：{}, 请假原因：{}, 请假天数：{}",
                    task.getId(), variables.get("name"), variables.get("reason"), variables.get("days"));
            Map<String, Object> map = new HashMap<>(1);
            map.put("approved", approved);
            if (approved) {
                map.put("manager", MANAGER);
            }
            taskService.complete(task.getId(), map);
        });
    }

    /**
     * 经理审核
     *
     * @param approved 是否通过
     */
    @PostMapping("/managerApprove")
    public void managerApprove(Boolean approved) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(MANAGER)
                .orderByTaskId()
                .desc()
                .list();

        tasks.forEach(task -> {
            Map<String, Object> variables = taskService.getVariables(task.getId());
            log.info("经理任务：任务id:{}, 请假人：{}, 请假原因：{}, 请假天数：{}",
                    task.getId(), variables.get("name"), variables.get("reason"), variables.get("days"));
            Map<String, Object> map = new HashMap<>(1);
            map.put("approved", approved);
            taskService.complete(task.getId(), map);
        });
    }
}
