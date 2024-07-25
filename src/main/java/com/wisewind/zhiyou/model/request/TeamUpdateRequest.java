package com.wisewind.zhiyou.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class TeamUpdateRequest {
    /**
     * id

     */
    private Long id;

    /**
     * 队伍名
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String teamDescription;

    /**
     * 队伍密码
     */
    private String teamPassword;


    /**
     * 队伍过期时间
     */
    private Date expireTime;


    /**
     * 队伍状态 0-公开， 1-私有， 2-加密
     */
    private Integer teamStatus;


}
