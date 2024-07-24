package com.wisewind.zhiyou.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wisewind.zhiyou.common.BaseResponse;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.common.ResultUtils;
import com.wisewind.zhiyou.exception.BusinessException;
import com.wisewind.zhiyou.model.domain.Team;
import com.wisewind.zhiyou.model.dto.TeamQueryDTO;
import com.wisewind.zhiyou.service.TeamService;
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

    @PostMapping("/add")
    public BaseResponse<Long> add(@RequestBody Team team){
        if(team == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        boolean result = teamService.save(team);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Long id = team.getId();
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
    public BaseResponse<Boolean> update(@RequestBody Team team){
        if(team == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        boolean result = teamService.updateById(team);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/list")
    public BaseResponse<List<Team>> list(@ParameterObject TeamQueryDTO teamQueryDTO){
        if(teamQueryDTO == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQueryDTO, team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        List<Team> list = teamService.list(queryWrapper);
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
}
