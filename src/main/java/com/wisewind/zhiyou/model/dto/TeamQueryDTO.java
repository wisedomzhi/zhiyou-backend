package com.wisewind.zhiyou.model.dto;

import com.wisewind.zhiyou.common.PageRequest;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeamQueryDTO extends PageRequest {

    private Long id;

    /**
     * 队伍创建人
     */
    private Long userId;

    /**
     * 已经加入的队伍id
     */
    private List<Long> teamIds;

    /**
     * 队伍名
     */
    private String teamName;


    /**
     * 队伍状态 0-公开， 1-私有， 2-加密
     */
    private Integer teamStatus;

    /**
     * 队伍描述
     */
    private String teamDescription;

    /**
     * 队伍最大人数
     */
    private Integer maxNum;

    /**
     * 根据名称或描述进行查询
     */
    private String searchText;


}
