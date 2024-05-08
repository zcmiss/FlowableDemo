package com.zc.flowabledemo.controller;

import com.zc.flowabledemo.vo.OAStartVo;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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

    /**
     * 启动流程实例
     */
    @PostMapping("/start")
    public String start(@RequestBody OAStartVo oaStartVo) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("taskUser", oaStartVo.getUserId());
        map.put("money", oaStartVo.getMoney());
        map.put("reason", oaStartVo.getDescription());

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ExpenseProcess", map);
        log.info("开启请假流程 processId:{}", processInstance.toString());
        return "提交成功.流程Id为：" + processInstance.getId();
    }

    /**
     * 获取审批管理列表
     */
    @GetMapping(value = "/list")
    public String list(@RequestParam("userId") String userId) {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
        for (Task task : tasks) {
            log.info("任务ID:{}", task.getId());
        }
        return Arrays.toString(tasks.toArray());
    }

    /**
     * 批准
     *
     * @param taskId 任务ID
     */
    @GetMapping("/apply")
    public String apply(@RequestParam("taskId") String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (Objects.isNull(task)) {
            throw new RuntimeException("流程不存在");
        }
        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "通过");
        taskService.complete(taskId, map);
        return "processed ok!";
    }

    /**
     * 拒绝
     */
    @GetMapping("/reject")
    public String reject(String taskId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "驳回");
        taskService.complete(taskId, map);
        return "reject";
    }

    /**
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @GetMapping("/processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

        //流程走完的不显示图
        if (Objects.isNull(pi)) {
            return;
        }
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String instanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(instanceId)
                .list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(
                bpmnModel,
                "png",
                activityIds,
                flows,
                engconf.getActivityFontName(),
                engconf.getLabelFontName(),
                engconf.getAnnotationFontName(),
                engconf.getClassLoader(),
                1.0,
                true
        );
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (Objects.nonNull(in)) {
                in.close();
            }
            if (Objects.nonNull(out)) {
                out.close();
            }
        }
    }

    /**
     * 查询审批结束后的列表
     *
     * @param userId 用户ID
     * @return {@link String }  审批结束后的列表
     */
    @GetMapping("/listFinished")
    public String listFinished(@RequestParam("userId") String userId) {
        // 查询已经完成的任务
        List<HistoricTaskInstance> list = processEngine.getHistoryService()
                // 创建历史任务实例查询
                .createHistoricTaskInstanceQuery()
                // 指定历史任务的办理人
                .taskAssignee(userId)
                // 查询已经完成的任务
                .finished()
                // 根据任务的结束时间降序排序
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                // 返回结果
                .list();
        return Arrays.toString(list.toArray());
    }
}
