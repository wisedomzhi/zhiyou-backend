package com.wisewind.zhiyou.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wisewind.zhiyou.common.BaseResponse;
import com.wisewind.zhiyou.common.DeleteRequest;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.common.ResultUtils;
import com.wisewind.zhiyou.exception.BusinessException;
import com.wisewind.zhiyou.model.domain.Team;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.model.domain.UserTeam;
import com.wisewind.zhiyou.model.dto.TeamQueryDTO;
import com.wisewind.zhiyou.model.request.TeamAddRequest;
import com.wisewind.zhiyou.model.request.TeamJoinRequest;
import com.wisewind.zhiyou.model.request.TeamQuitRequest;
import com.wisewind.zhiyou.model.request.TeamUpdateRequest;
import com.wisewind.zhiyou.model.vo.TeamUserVO;
import com.wisewind.zhiyou.service.TeamService;
import com.wisewind.zhiyou.service.UserService;
import com.wisewind.zhiyou.service.UserTeamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/team")
public class TeamController {
    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserTeamService userTeamService;

    @GetMapping("/get")
    public BaseResponse<Team> get(Long id){
        if(id == null || id < 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = teamService.getById(id);
        return ResultUtils.success(team);
    }
    @PostMapping("/add")
    public BaseResponse<Long> add(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest httpServletRequest){
        if(teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long id = teamService.addTeam(team, httpServletRequest);
        if(id < 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(id);
    }


    /**
     * 删除队伍
     * @param deleteRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpServletRequest){
        if(deleteRequest == null || deleteRequest.getId() < 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long id = deleteRequest.getId();
        User currentUser = userService.getCurrentUser(httpServletRequest);
        boolean result = teamService.deleteTeam(id, currentUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> update(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest httpServletRequest){
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(httpServletRequest);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = teamService.updateTeam(teamUpdateRequest, currentUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }



    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> list(@ParameterObject TeamQueryDTO teamQueryDTO, HttpServletRequest httpServletRequest){
        if(teamQueryDTO == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(httpServletRequest);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean isAdmin = userService.isAdmin(httpServletRequest);
        List<TeamUserVO> list = teamService.listTeams(teamQueryDTO, isAdmin, currentUser);
        return ResultUtils.success(list);
    }

    @GetMapping("/list/create")
    public BaseResponse<List<TeamUserVO>> listMyCreate(@ParameterObject TeamQueryDTO teamQueryDTO, HttpServletRequest httpServletRequest){
        if(teamQueryDTO == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(httpServletRequest);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        teamQueryDTO.setUserId(userId);
        List<TeamUserVO> list = teamService.listTeams(teamQueryDTO, true, currentUser);
        return ResultUtils.success(list);
    }

    @GetMapping("/list/join")
    public BaseResponse<List<TeamUserVO>> listMyJoin(@ParameterObject TeamQueryDTO teamQueryDTO, HttpServletRequest httpServletRequest){
        if(teamQueryDTO == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(httpServletRequest);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getUserId, userId);
        List<UserTeam> userTeamList = userTeamService.list(lambdaQueryWrapper);
        List<Long> teamIds = new ArrayList<>();
        for (UserTeam userTeam : userTeamList) {
            teamIds.add(userTeam.getTeamId());
        }
        teamQueryDTO.setTeamIds(teamIds);
        List<TeamUserVO> list = teamService.listTeams(teamQueryDTO, true, currentUser);
        return ResultUtils.success(list);
    }

    @Deprecated
    @GetMapping("/list/page")
    public BaseResponse<List<Team>> listPage(@ParameterObject TeamQueryDTO teamQueryDTO){
        if(teamQueryDTO == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQueryDTO, team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamQueryDTO.getPage(), teamQueryDTO.getPageSize());
        List<Team> list = teamService.list(page, queryWrapper);
        return ResultUtils.success(list);
    }

    @PostMapping("/join")
    public  BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest httpServletRequest){
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(httpServletRequest);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = teamService.joinTeam(teamJoinRequest, currentUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public  BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest httpServletRequest){
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(httpServletRequest);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = teamService.quitTeam(teamQuitRequest, currentUser);
        return ResultUtils.success(result);
    }
}
