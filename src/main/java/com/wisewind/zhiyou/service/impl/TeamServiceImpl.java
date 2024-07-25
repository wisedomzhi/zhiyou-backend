package com.wisewind.zhiyou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.constant.UserConstant;
import com.wisewind.zhiyou.enumeration.TeamStatus;
import com.wisewind.zhiyou.exception.BusinessException;
import com.wisewind.zhiyou.model.domain.Team;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.model.domain.UserTeam;
import com.wisewind.zhiyou.model.dto.TeamQueryDTO;
import com.wisewind.zhiyou.model.request.TeamJoinRequest;
import com.wisewind.zhiyou.model.request.TeamUpdateRequest;
import com.wisewind.zhiyou.model.vo.TeamUserVO;
import com.wisewind.zhiyou.model.vo.UserVO;
import com.wisewind.zhiyou.service.TeamService;
import com.wisewind.zhiyou.mapper.TeamMapper;
import com.wisewind.zhiyou.service.UserService;
import com.wisewind.zhiyou.service.UserTeamService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        if(currentUser == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "未登录");
        }
        Long id = currentUser.getId();
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
        if(status.equals(TeamStatus.SECRET)){
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

    @Override
    public List<TeamUserVO> listTeams(TeamQueryDTO teamQueryDTO, boolean isAdmin) {
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        if(teamQueryDTO != null){
            Long id = teamQueryDTO.getId();
            if(id != null && id > 0){
                queryWrapper.eq(Team::getId, id);
            }
            String teamName = teamQueryDTO.getTeamName();
            if(StringUtils.isNotBlank(teamName)){
                queryWrapper.like(Team::getTeamName, teamName);
            }
            Integer teamStatus = teamQueryDTO.getTeamStatus();
            TeamStatus status = TeamStatus.getByValue(teamStatus);
            if(status == null){
                status = TeamStatus.PUBLIC;
            }
            if(!isAdmin && !status.equals(TeamStatus.PUBLIC)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq(Team::getTeamStatus, status.getValue());

            String teamDescription = teamQueryDTO.getTeamDescription();
            if(StringUtils.isNotBlank(teamDescription)){
                queryWrapper.like(Team::getTeamDescription, teamDescription);
            }
            Integer maxNum = teamQueryDTO.getMaxNum();
            if(maxNum != null && maxNum > 0){
                queryWrapper.eq(Team::getMaxNum, maxNum);
            }
            String searchText = teamQueryDTO.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw -> qw.like(Team::getTeamName, searchText).or().like(Team::getTeamDescription, searchText));
            }
        }
        //过滤已过期队伍
        queryWrapper.and(qw->qw.gt(Team::getExpireTime, new Date()).or().isNull(Team::getExpireTime));
        List<Team> list = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<>();
        }

        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : list) {
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreatUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User currentUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (!oldTeam.getUserId().equals(currentUser.getId()) && !currentUser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatus status = TeamStatus.getByValue(teamUpdateRequest.getTeamStatus());
        if(status.equals(TeamStatus.SECRET)){
            if (!TeamStatus.getByValue(oldTeam.getTeamStatus()).equals(TeamStatus.SECRET) && StringUtils.isBlank(teamUpdateRequest.getTeamPassword())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR,"加密房间必须设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User currentUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 加入的队伍必须存在
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍不存在");
        }
        // 最多只能加入5个队伍
        Long userId = currentUser.getId();
        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(UserTeam::getUserId, userId);
        long count = userTeamService.count(lambdaQueryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "最多只能加入5个队伍！");
        }
        // 不能加入已经过期的队伍
        Date expireTime = team.getExpireTime();
        Date now = new Date();
        if (expireTime != null && now.after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前队伍已过期");
        }
        // 不能加入已满队伍
        lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        long hasJoinCount = userTeamService.count(lambdaQueryWrapper);
        if (hasJoinCount >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍已满");
        }
        // 不能加入私密队伍
        TeamStatus status = TeamStatus.getByValue(team.getTeamStatus());
        if (TeamStatus.PRIVATE.equals(status)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能加入私密队伍");
        }
        if (TeamStatus.SECRET.equals(status)) {
            if (StringUtils.isBlank(teamJoinRequest.getTeamPassword())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "密码不能为空");
            } else if (!team.getTeamPassword().equals(teamJoinRequest.getTeamPassword())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "密码错误");
            }
        }
        // 不能重复加入队伍
        lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        lambdaQueryWrapper.eq(UserTeam::getUserId, userId);
        long repeatJoinCount = userTeamService.count(lambdaQueryWrapper);
        if (repeatJoinCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能重复加入队伍");
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        return userTeamService.save(userTeam);
    }
}




