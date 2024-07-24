package com.wisewind.zhiyou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.enumeration.TeamStatus;
import com.wisewind.zhiyou.exception.BusinessException;
import com.wisewind.zhiyou.model.domain.Team;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.model.domain.UserTeam;
import com.wisewind.zhiyou.service.TeamService;
import com.wisewind.zhiyou.mapper.TeamMapper;
import com.wisewind.zhiyou.service.UserService;
import com.wisewind.zhiyou.service.UserTeamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
* @author ffz
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-07-07 22:51:49
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Autowired
    private UserService userService;
    @Autowired
    private UserTeamService userTeamService;

    @Transactional
    public long addTeam(Team team, HttpServletRequest httpServletRequest) {
        if(team == null || httpServletRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        User currentUser = userService.getCurrentUser(httpServletRequest);
        Long id = currentUser.getId();
        if(currentUser == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "未登录");
        }
        int maxNum = team.getMaxNum();
        if(maxNum <= 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍人数必须大于1小于等于20");
        }
        String teamName = team.getTeamName();
        if(teamName.length() > 20){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍名称过长");
        }
        String teamDescription = team.getTeamDescription();
        if(teamDescription.length() > 512){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍描述过长");
        }
        Integer teamStatus = team.getTeamStatus();
        TeamStatus status = TeamStatus.getByValue(teamStatus);
        if(status == null){
            status = TeamStatus.PUBLIC;
            team.setTeamStatus(0);
        }
        if(teamStatus.equals(TeamStatus.SECRET)){
            if(team.getTeamPassword() == null){
                throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍密码为空");
            }
        }
        Date expireTime = team.getExpireTime();
        Date now = new Date();
        if(expireTime.before(now)){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "过期时间早于当前时间");
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id);
        long count = this.count(queryWrapper);
        if(count >= 5){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍数量达到上限");
        }
        boolean res = this.save(team);
        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(id);
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        res = userTeamService.save(userTeam);
        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return team.getId();
    }
}




