package com.wisewind.zhiyou.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wisewind.zhiyou.common.BaseResponse;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.common.ResultUtils;
import com.wisewind.zhiyou.exception.BusinessException;
import com.wisewind.zhiyou.model.domain.Team;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.model.dto.TeamQueryDTO;
import com.wisewind.zhiyou.model.request.TeamJoinRequest;
import com.wisewind.zhiyou.model.request.TeamUpdateRequest;
import com.wisewind.zhiyou.model.vo.TeamUserVO;
import com.wisewind.zhiyou.service.TeamService;
import com.wisewind.zhiyou.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/team")
public class TeamController {
    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> add(@RequestBody Team team, HttpServletRequest httpServletRequest){
        if(team == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        long id = teamService.addTeam(team, httpServletRequest);
        if(id < 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(id);
    }


    @DeleteMapping("/delete")
    public BaseResponse<Boolean> delete(Long id){
        if(id == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        boolean result = teamService.removeById(id);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    @PutMapping("/update")
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
        boolean isAdmin = userService.isAdmin(httpServletRequest);
        List<TeamUserVO> list = teamService.listTeams(teamQueryDTO, isAdmin);
        return ResultUtils.success(list);
    }

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
}
