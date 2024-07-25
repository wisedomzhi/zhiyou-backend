package com.wisewind.zhiyou.model.request;

import lombok.Data;

@Data
public class TeamJoinRequest {

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 队伍密码
     */
    private String teamPassword;
}
