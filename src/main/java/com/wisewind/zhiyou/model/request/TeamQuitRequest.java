package com.wisewind.zhiyou.model.request;

import lombok.Data;

/**
 * 退出队伍请求
 * @author ffz
 */

@Data
public class TeamQuitRequest {

    /**
     * 队伍id
     */
    private Long teamId;

}
