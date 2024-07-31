package com.wisewind.zhiyou.service;

import com.wisewind.zhiyou.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.model.dto.TeamQueryDTO;
import com.wisewind.zhiyou.model.request.TeamJoinRequest;
import com.wisewind.zhiyou.model.request.TeamQuitRequest;
import com.wisewind.zhiyou.model.request.TeamUpdateRequest;
import com.wisewind.zhiyou.model.vo.TeamUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author ffz
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-07-07 22:51:49
*/
public interface TeamService extends IService<Team> {

     long addTeam(Team team, HttpServletRequest httpServletRequest);

     /**
      * 搜索队伍
      * @param teamQueryDTO
      * @return
      */
     List<TeamUserVO> listTeams(TeamQueryDTO teamQueryDTO, boolean isAdmin, User currentUser);

     boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User currentUser);

     boolean joinTeam(TeamJoinRequest teamJoinRequest, User currentUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User currentUser);

     boolean deleteTeam(Long id, User currentUser);
}
