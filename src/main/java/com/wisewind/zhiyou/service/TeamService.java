package com.wisewind.zhiyou.service;

import com.wisewind.zhiyou.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author ffz
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-07-07 22:51:49
*/
public interface TeamService extends IService<Team> {

     long addTeam(Team team, HttpServletRequest httpServletRequest);
}
