package com.wisewind.zhiyou.controller;

import com.wisewind.zhiyou.common.BaseResponse;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.common.ResultUtils;
import com.wisewind.zhiyou.exception.BusinessException;
import com.wisewind.zhiyou.model.vo.MessageVO;
import com.wisewind.zhiyou.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/team")
    public BaseResponse<List<MessageVO>> teamChat(Long teamId){
        if(teamId == null || teamId < 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        List<MessageVO> messageVOList = chatService.listTeamChat(teamId);
        return ResultUtils.success(messageVOList);
    }
}
