package com.wisewind.zhiyou.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.wisewind.zhiyou.common.PageRequest;
import lombok.Data;

import java.util.Date;

@Data
public class TeamQueryDTO extends PageRequest {

    private Long id;

    /**
     * 队伍名
     */
    private String teamName;


    /**
     * 队伍状态 0-公开， 1-私有， 2-加密
     */
    private Integer teamStatus;



}
