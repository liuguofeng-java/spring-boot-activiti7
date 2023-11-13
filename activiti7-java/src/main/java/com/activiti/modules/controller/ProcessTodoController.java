package com.activiti.modules.controller;

import com.activiti.modules.entity.SysUserEntity;
import com.activiti.modules.entity.dto.workflow.TodoApprovalDto;
import com.activiti.modules.entity.dto.workflow.TodoListDto;
import com.activiti.modules.service.ProcessTodoService;
import com.activiti.utils.R;
import com.activiti.utils.TokenUtils;
import com.activiti.utils.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 代办流程
 *
 * @author liuguofeng
 * @date 2023/11/04 19:12
 **/
@RequestMapping("processTodo")
@RestController
public class ProcessTodoController {

    @Autowired
    private ProcessTodoService processTodoService;

    /**
     * 查看我代办的流程
     *
     * @param dto 参数
     */
    @GetMapping("list")
    public TableDataInfo list(TodoListDto dto) {
        SysUserEntity user = TokenUtils.getUser();
        dto.setUserId(user.getUserId());
        return processTodoService.queryPage(dto);
    }

    /**
     * 审批节点
     *
     * @param dto 参数
     */
    @PostMapping("approval")
    public R<String> approval(@RequestBody TodoApprovalDto dto) {
        SysUserEntity user = TokenUtils.getUser();
        dto.setUserId(user.getUserId());
        processTodoService.approval(dto);
        return R.ok();
    }


}