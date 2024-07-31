package com.wisewind.zhiyou.model.vo;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TeamUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3109186619587015120L;
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
     * 队伍最大人数
     */
    private Integer maxNum;

    /**
     * 队伍过期时间
     */
    private Date expireTime;

    /**
     * 队伍创建人
     */
    private Long userId;

    /**
     * 队伍状态 0-公开， 1-私有， 2-加密
     */
    private Integer teamStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 队伍创建者
     */
    private UserVO creatUser;

    /**
     *当前用户是否已经加入该队伍
     */
    private Boolean hasJoin;

    /**
     * 已经加入队伍的用户
     */
    private List<UserVO> joinedUser;
}
