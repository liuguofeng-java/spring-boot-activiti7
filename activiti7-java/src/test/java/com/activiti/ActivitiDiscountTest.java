package com.activiti;

import com.github.pagehelper.Page;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 测试折扣Activiti
 *
 * @author liuguofeng
 * @date 2023/10/16 16:03
 **/
@SpringBootTest
public class ActivitiDiscountTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    /**
     * 部署流程测试
     */
    @Test
    public void testDeploy() {
        //进行部署
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/leave_demo.bpmn")
                .addClasspathResource("bpmn/leave_demo.png")
                .name("折扣流程申请")
                .deploy();
        //输出部署的一些信息
        System.out.println("流程部署ID:" + deployment.getId());
        System.out.println("流程部署名称:" + deployment.getName());
    }

    /**
     * 查询部署流程
     */
    @Test
    public void testProcessDefinition() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionId()
                .orderByProcessDefinitionVersion()
                .desc();
//            query.processDefinitionNameLike("%折扣流程申请%");
//            query.processDefinitionKeyLike("%leave_demo%");
        List<ProcessDefinition> list = query.listPage(0, 10);
        System.out.println(list);
    }

    /**
     * 启动流程
     */
    @Test
    public void testStartProcess() {
        //根据流程定义的key启动流程实例,这个key是在定义bpmn的时候设置的
        ProcessInstance instance = runtimeService.
                startProcessInstanceByKey("leave_demo");
        //获取流程实例的相关信息
        System.out.println("流程定义的id = " + instance.getProcessDefinitionId());
        System.out.println("流程实例的id = " + instance.getId());
    }

    /**
     * 任务查询
     * 流程启动后，各个任务的负责人就可以查询自己当前需要处理的任务，查询出来的任务都是该用户的待办任务。
     */
    @Test
    public void testSelectTodoTaskList() {
        //任务负责人
        String assignee = "lisi";
        //获取任务集合
        List<Task> taskList = taskService.createTaskQuery()
                .processDefinitionKey("leave_demo")

//                .taskAssignee(assignee)
                .list();
        //遍历任务列表
        for (Task task : taskList) {
            System.out.println("流程定义id = " + task.getProcessDefinitionId());
            System.out.println("流程实例id = " + task.getProcessInstanceId());
            System.out.println("任务id = " + task.getId());
            System.out.println("任务名称 = " + task.getName());
            System.out.println("执行人 = " + task.getAssignee());
        }
    }

    /**
     * 审批任务节点 并 添加审批意见
     */
    @Test
    public void testCompleteTask() {
        //任务负责人
        String assignee = "wangwu";
        //获取任务集合
        List<Task> taskList = taskService.createTaskQuery()
                .processDefinitionKey("leave_demo")
//                .processInstanceId()
                .taskAssignee(assignee)
                .list();
        //遍历任务列表
        for (Task task : taskList) {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), "审批通过--测试xx");
            taskService.complete(task.getId());
        }
    }

    /**
     * 审批节点，驳回
     */
    @Test
    public void doCheckRejectTask() {
        //任务负责人
        String assignee = "wangwu";
        //获取任务集合
        List<Task> taskList = taskService.createTaskQuery()
                .processDefinitionKey("leave_demo")
//                .processInstanceId()
                .taskAssignee(assignee)
                .list();
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("checkReject", true);
        varMap.put("checkPass", false);
        for (Task task : taskList) {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), "审批通过--测试xx");
            taskService.complete("7509", varMap);
        }

    }

    /**
     * 查询审批记录
     */
    @Test
    public void testSelectHistoryTask() {
        //流程实例ID
        String processInstanceId = "04a48245-7b0f-11ee-afbf-30c9aba6c580";
        //获取历史审核信息
        List<HistoricActivityInstance> list = historyService
                .createHistoricActivityInstanceQuery()
                .activityType("userTask")//只获取用户任务
                .processInstanceId(processInstanceId)
//                .taskAssignee(taskAssignee)
                .finished()
                .list();
        for (HistoricActivityInstance instance : list) {
            System.out.println("任务名称:" + instance.getActivityName());
            System.out.println("任务开始时间:" + instance.getStartTime());
            System.out.println("任务结束时间:" + instance.getEndTime());
            System.out.println("审批人:" + instance.getAssignee());
            System.out.println("processInstanceId:" + instance.getProcessInstanceId());
            //获取审核批注信息
            List<Comment> taskComments = taskService.getTaskComments(instance.getTaskId());
            if (taskComments.size() > 0) {
                System.out.println("审批批注:" + taskComments.get(0).getFullMessage());
            }
        }
    }

    @Test
    public void testHistoric() {
        String processInstanceId = "1f4fdde6-7bbc-11ee-917f-30c9aba6c580";


        // 已审批审批节点
        List<HistoricActivityInstance> historicList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .list();

        // 获取未审批节点
        List<HistoricTaskInstance> unfinishedList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .unfinished()
                .list();

    }


}
