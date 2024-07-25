package com.wisewind.zhiyou.model.vo;


import lombok.Data;

import java.util.Date;

@Data
public class UserVO {

    /**
     * id

     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态 0-正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户权限 0-默认用户	1-管理员
     */
    private Integer userRole;

    /**
     * 用户描述
     */
    private String userProfile;

    /**
     * 用户标签
     */
    private String tags;
}
