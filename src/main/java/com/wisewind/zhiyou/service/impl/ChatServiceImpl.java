package com.wisewind.zhiyou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wisewind.zhiyou.model.domain.Chat;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.model.vo.MessageVO;
import com.wisewind.zhiyou.model.vo.UserVO;
import com.wisewind.zhiyou.service.ChatService;
import com.wisewind.zhiyou.mapper.ChatMapper;
import com.wisewind.zhiyou.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author ffz
* @description 针对表【chat(聊天消息表)】的数据库操作Service实现
* @createDate 2024-08-04 13:44:06
*/
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
    implements ChatService{
    @Autowired
    private UserService userService;
    @Override
    public List<MessageVO> listTeamChat(Long teamId) {
        LambdaQueryWrapper<Chat> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Chat::getTeamId, teamId);
        List<Chat> chatList = this.list(lambdaQueryWrapper);
        List<MessageVO> messageVOList = new ArrayList<>();
        for (Chat chat : chatList) {
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(chat, messageVO);
            messageVO.setText(chat.getChatText());
            User user = userService.getById(chat.getFromId());
            UserVO fromUser = new UserVO();
            BeanUtils.copyProperties(user, fromUser);
            messageVO.setFromUser(fromUser);
            messageVOList.add(messageVO);
        }
        return messageVOList;
    }
}




