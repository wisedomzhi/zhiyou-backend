package com.wisewind.zhiyou.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wisewind.zhiyou.common.BaseResponse;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.common.ResultUtils;
import com.wisewind.zhiyou.constant.UserConstant;
import com.wisewind.zhiyou.exception.BusinessException;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.model.request.UserLoginRequest;
import com.wisewind.zhiyou.model.request.UserRegisterRequest;
import com.wisewind.zhiyou.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.wisewind.zhiyou.constant.UserConstant.ADMIN_ROLE;

@Slf4j
@RequestMapping("/user")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        String username = userRegisterRequest.getUsername();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if(StringUtils.isAnyEmpty(username, userAccount, userPassword, checkPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户注册信息存在空值！");
        }
        long id = userService.userRegister(username, userAccount, userPassword, checkPassword);
        return ResultUtils.success(id);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest){
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if(StringUtils.isAnyEmpty(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户登录信息存在空值！");
        }
        User user = userService.userLogin(userAccount, userPassword, httpServletRequest);
        return ResultUtils.success(user);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(String username, HttpServletRequest httpServletRequest){
        if(!userService.isAdmin(httpServletRequest)){
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无管理员权限！");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username))
            queryWrapper.like("username", username);
        List<User> users = userService.list(queryWrapper);
        List<User> list = users.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam List<String> tags){
        if(CollectionUtils.isEmpty(tags)){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        List<User> users = userService.getByTags(tags);
        return ResultUtils.success(users);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(long id, HttpServletRequest httpServletRequest){
        if(!userService.isAdmin(httpServletRequest))
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无管理员权限！");
        if(id < 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "需要删除的用户不存在！");
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest httpServletRequest){
        if(user == null || httpServletRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        int ret = userService.updateUser(user, httpServletRequest);
        if(ret <= 0){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(ret);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest httpServletRequest){
        User user = (User)httpServletRequest.getSession().getAttribute(UserConstant.userLoginStatus);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long id = user.getId();
        user = userService.getById(id);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest httpServletRequest){
        if (httpServletRequest == null){
           throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        int result = userService.userLogout(httpServletRequest);
        return ResultUtils.success(result);
    }


    @GetMapping("/recommend")
    public BaseResponse<List<User>> recommendUsers(int page, int pageSize, HttpServletRequest httpServletRequest){
        List<User> userList = userService.getRecommendUsers(page, pageSize, httpServletRequest);
        return ResultUtils.success(userList);
    }

    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(int num, HttpServletRequest httpServletRequest){
        if(num < 0 || num > 20){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        List<User> userList = userService.matchUsers(num, httpServletRequest);
        return ResultUtils.success(userList);
    }

}
