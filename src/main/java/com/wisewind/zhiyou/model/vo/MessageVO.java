package com.wisewind.zhiyou.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class MessageVO {
    /**
     * 聊天室所属的队伍id
     */
    private Long teamId;
    /**
     * 聊天内容
     */
    private String text;
    /**
     * 聊天消息发送的时间
     */
    private Date createTime;
    /**
     * 发送消息的用户
     */
    private UserVO fromUser;

}
