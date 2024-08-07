package com.wisewind.zhiyou.model.request;

import lombok.Data;

@Data
public class MessageRequest {
    /**
     * 发送信息的用户id
     */
    private Long fromId;
    /**
     * 聊天信息所在的队伍id
     */
    private Long teamId;
    /**
     * 聊天信息
     */
    private String text;
}
