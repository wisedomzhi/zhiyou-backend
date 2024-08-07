package com.wisewind.zhiyou.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 队伍
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team implements Serializable {
    /**
     * id

     */
    @TableId(type = IdType.AUTO)
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
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 队伍头像
     */
    private String teamAvatarUrl;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
