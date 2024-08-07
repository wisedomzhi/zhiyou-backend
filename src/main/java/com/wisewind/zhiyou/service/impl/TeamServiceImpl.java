package com.wisewind.zhiyou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.wisewind.zhiyou.model.request.TeamQuitRequest;
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
import java.util.stream.Collectors;

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

    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, HttpServletRequest httpServletRequest) {
        if(team == null || httpServletRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        User currentUser = userService.getCurrentUser(httpServletRequest);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long id = currentUser.getId();
        team.setUserId(id);
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
        if(expireTime != null && expireTime.before(now)){
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
    public List<TeamUserVO> listTeams(TeamQueryDTO teamQueryDTO, boolean isAdmin, User currentUser) {
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        if(teamQueryDTO != null){
            Long id = teamQueryDTO.getId();
            if(id != null && id > 0){
                queryWrapper.eq(Team::getId, id);
            }
            Long userId = teamQueryDTO.getUserId();
            if(userId != null && userId > 0){
                queryWrapper.eq(Team::getUserId, userId);
            }
            List<Long> teamIds = teamQueryDTO.getTeamIds();
            if(!CollectionUtils.isEmpty(teamIds)){
                queryWrapper.in(Team::getId, teamIds);
            }
            String teamName = teamQueryDTO.getTeamName();
            if(StringUtils.isNotBlank(teamName)){
                queryWrapper.like(Team::getTeamName, teamName);
            }
            Integer teamStatus = teamQueryDTO.getTeamStatus();
            TeamStatus status = TeamStatus.getByValue(teamStatus);
            if(status == null && !isAdmin){
                status = TeamStatus.PUBLIC;
            }
            if(!isAdmin && status.equals(TeamStatus.PRIVATE) && currentUser == null){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            if(status != null){
                queryWrapper.eq(Team::getTeamStatus, status.getValue());
                if(status.equals(TeamStatus.PRIVATE)){
                    queryWrapper.eq(Team::getUserId, currentUser.getId());
                }
            }

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
        Page<Team> page = new Page<>(teamQueryDTO.getPage(), teamQueryDTO.getPageSize());
        List<Team> list = this.list(page, queryWrapper);
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<>();
        }

        // 关联查询当前用户加入了哪些队伍
        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getUserId, currentUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(lambdaQueryWrapper);
        List<Long> teamIdList = userTeamList.stream().map(UserTeam::getTeamId).toList();

        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : list) {
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            // 关联查询每个队伍有哪些用户
            lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserTeam::getTeamId, team.getId());
            List<Long> teamMemberIds = userTeamService.list(lambdaQueryWrapper).stream().map(UserTeam::getUserId).toList();
            LambdaQueryWrapper<User> userQueryMapper = new LambdaQueryWrapper<>();
            if(!CollectionUtils.isEmpty(teamMemberIds)){
                userQueryMapper.in(User::getId, teamMemberIds);
            }
            List<UserVO> teamMembers = userService.list(userQueryMapper).stream().map(user -> {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                return userVO;
            }).toList();
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            teamUserVO.setJoinedUser(teamMembers);
            BeanUtils.copyProperties(team, teamUserVO);
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVO.setHasJoin(teamIdList.contains(team.getId()));
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
        if(status != null && status.equals(TeamStatus.SECRET)){
            if (!TeamStatus.getByValue(oldTeam.getTeamStatus()).equals(TeamStatus.SECRET) && StringUtils.isBlank(teamUpdateRequest.getTeamPassword())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR,"加密房间必须设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Transactional(rollbackFor = Exception.class)
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
        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
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

    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User currentUser) {
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);

        //判断是否已经加入队伍

        Long userId = currentUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);

        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);

        long count = userTeamService.count(queryWrapper);
        if(count == 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "未加入队伍");
        }

        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        long teamJoinedNum = userTeamService.count(lambdaQueryWrapper);

        // 如果队伍只剩一人，解散队伍
        if(teamJoinedNum == 1){
            this.removeById(teamId);
        }else {
            // 队伍至少还剩两人
            // 是否为队长
            if(team.getUserId().equals(userId)){
                // user_team表中的id根据用户加入的时间（插入的顺序）递增
                lambdaQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(lambdaQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() < 2){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }

                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                team.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(team);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队长失败");
                }

            }
        }

        return userTeamService.remove(queryWrapper);
    }

    private Team getTeamById(Long teamId) {
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return team;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Long id, User currentUser) {
        Team team = getTeamById(id);
        Long teamId = team.getId();

        if(!team.getUserId().equals(currentUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH, "只有队长可以解散队伍");
        }

        // 移除用户与队伍的关联关系
        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        userTeamService.remove(lambdaQueryWrapper);

        // 移除队伍
        return this.removeById(teamId);
    }
}




