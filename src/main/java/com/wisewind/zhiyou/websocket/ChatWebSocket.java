package com.wisewind.zhiyou.websocket;

import com.google.gson.Gson;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.constant.ChatConstant;
import com.wisewind.zhiyou.exception.BusinessException;


import com.wisewind.zhiyou.model.domain.Chat;
import com.wisewind.zhiyou.model.domain.Team;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.model.request.MessageRequest;
import com.wisewind.zhiyou.model.vo.MessageVO;
import com.wisewind.zhiyou.model.vo.UserVO;
import com.wisewind.zhiyou.model.vo.MessageVO;
import com.wisewind.zhiyou.service.ChatService;
import com.wisewind.zhiyou.service.TeamService;
import com.wisewind.zhiyou.service.UserService;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/{userId}/{teamId}")
public class ChatWebSocket {
    //存放会话对象
    private static final ConcurrentHashMap<Long, ConcurrentHashMap<Long, ChatWebSocket>> ROOMS = new ConcurrentHashMap<>();

    private Session session;
    private Long userId;
    private Long teamId;

    private static UserService userService;
    private static TeamService teamService;
    private static ChatService chatService;

    @Autowired
    private void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private void setTeamService(TeamService teamService) {
        this.teamService = teamService;
    }

    @Autowired
    private void setChatService(ChatService chatService){
        this.chatService = chatService;
    }
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId, @PathParam("teamId") Long teamId) {
        if (userId == null || teamId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "创建聊天室失败！");
        }
        this.session = session;
        this.userId = userId;
        this.teamId = teamId;
        if (!ROOMS.containsKey(teamId)) {
            ConcurrentHashMap<Long, ChatWebSocket> room = new ConcurrentHashMap<>();
            room.put(userId, this);
            ROOMS.put(teamId, room);
        } else {
            ROOMS.get(teamId).put(userId, this);
        }
        log.info("websocket 连接建立,uid:{}, tid:{}, session:{}", userId, teamId, session.getId());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message) {
        if (message.equals("PING")) {
            log.info("收到心跳");
            sendMessage("PONG");
            return;
        }
        if (teamId == null || userId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 收到聊天消息
        log.info("服务端收到用户username={}的消息:{}", userId, message);
        MessageRequest messageRequest = new Gson().fromJson(message, MessageRequest.class);
        Long teamId = messageRequest.getTeamId();
        String text = messageRequest.getText();
        User fromUser = userService.getById(userId);
        Team team = teamService.getById(teamId);

        // 分发聊天信息
        MessageVO messageVO = new MessageVO();
        UserVO fromUserVO = new UserVO();
        BeanUtils.copyProperties(fromUser, fromUserVO);
        messageVO.setFromUser(fromUserVO);
        messageVO.setText(text);
        messageVO.setTeamId(team.getId());
        Date createTime = new Date();
        messageVO.setCreateTime(createTime);

        String toJson = new Gson().toJson(messageVO);
        try {
            sendToAllClient(toJson);
            savaChat(userId, text, team.getId(), ChatConstant.TEAM_CHAT, createTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (teamId == null || userId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        ROOMS.get(teamId).remove(userId);
        log.info("websocket 连接关闭,uid:{}, tid:{}", userId, teamId);
    }

    /**
     * 队内群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        if (teamId == null || userId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Collection<ChatWebSocket> room = ROOMS.get(teamId).values();
        for (ChatWebSocket chat : room) {
            try {
                //服务器向客户端发送消息
                chat.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        if (teamId == null || userId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        try {
            //服务器向客户端发送消息
            this.session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存聊天信息
     * @param userId
     * @param text
     * @param teamId
     * @param chatType
     */
    private void savaChat(Long userId,  String text, Long teamId, Integer chatType, Date createTime) {
        Chat chat = new Chat();
        chat.setFromId(userId);
        chat.setChatText(String.valueOf(text));
        chat.setChatType(chatType);
        chat.setCreateTime(createTime);
        if (teamId != null && teamId > 0) {
            chat.setTeamId(teamId);
        }
        chatService.save(chat);
    }
}
