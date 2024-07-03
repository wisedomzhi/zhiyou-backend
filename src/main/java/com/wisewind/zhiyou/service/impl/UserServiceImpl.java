package com.wisewind.zhiyou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wisewind.zhiyou.common.BaseResponse;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.constant.UserConstant;
import com.wisewind.zhiyou.exception.BusinessException;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.service.UserService;
import com.wisewind.zhiyou.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.wisewind.zhiyou.constant.UserConstant.ADMIN_ROLE;
import static com.wisewind.zhiyou.constant.UserConstant.userLoginStatus;

/**
 * @author ffz
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-05-29 21:37:56
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    public static final String SALT = "wisewind";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if (StringUtils.isAnyEmpty(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "注册信息存在空值！");
        }

        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号或密码不合法！");
        }

        String regEx = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号或密码不合法！");
        }

        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "校验密码不正确！");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "注册用户已存在！");
        }


        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "注册失败！");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest) {
        //1. 账号密码校验
        if (StringUtils.isAnyEmpty(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "登录信息存在空值！");
        }

        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号或密码不合法！");
        }

        String regEx = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号或密码不合法！");
        }
        //2.密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        queryWrapper.eq(User::getUserPassword, encryptPassword);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, account cannot match password!");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在！");
        }

        //3. 脱敏
        User handledUser = getSafetyUser(user);
        //4.保存登录态
        httpServletRequest.getSession().setAttribute(userLoginStatus, handledUser);


        return handledUser;
    }

    @Override
    public User getSafetyUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "脱敏用户为空！");
        }
        User handledUser = new User();
        handledUser.setId(user.getId());
        handledUser.setUsername(user.getUsername());
        handledUser.setUserAccount(user.getUserAccount());
        handledUser.setAvatarUrl(user.getAvatarUrl());
        handledUser.setGender(user.getGender());
        handledUser.setPhone(user.getPhone());
        handledUser.setEmail(user.getEmail());
        handledUser.setUserRole(user.getUserRole());
        handledUser.setUserStatus(user.getUserStatus());
        handledUser.setCreateTime(user.getCreateTime());
        handledUser.setUpdateTime(user.getUpdateTime());
        handledUser.setTags(user.getTags());
        handledUser.setUserProfile(user.getUserProfile());
        return handledUser;
    }

    @Override
    public int userLogout(HttpServletRequest httpServletRequest) {
        httpServletRequest.getSession().removeAttribute(userLoginStatus);
        return 1;
    }

    @Override
    public List<User> getByTags(List<String> tags) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tag : tags) {
            queryWrapper = queryWrapper.like("tags", tag);
        }
        List<User> safetyUsers = this.list(queryWrapper).stream().map(this::getSafetyUser).collect(Collectors.toList());
        return safetyUsers;
    }

    // 内存处理标签
    @Deprecated
    public List<User> getByTagsMemo(List<String> tags) {
        if(CollectionUtils.isEmpty(tags))
            throw new BusinessException(ErrorCode.PARAM_ERROR, "标签列表为空");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> users = this.list(queryWrapper);
        Gson gson = new Gson();
        users = users.stream().filter((user) -> {
            String tagsStr = user.getTags();
            Set<String> tagSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());
            for (String tag : tags) {
                if (!tagSet.contains(tag))
                    return false;
            }
            return true;
        }).collect(Collectors.toList());

        List<User> safetyUsers = users.stream().map(this::getSafetyUser).collect(Collectors.toList());
        return safetyUsers;
    }

    @Override
    public int updateUser(User user, HttpServletRequest httpServletRequest) {
        if(!(isAdmin(httpServletRequest) || isCurrentUser(httpServletRequest, user))){
            throw new BusinessException(ErrorCode.NO_AUTH, "没有修改权限");
        }
        User oldUser = userMapper.selectById(user.getId());
        if(oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "不存在该用户！");
        }
        return userMapper.updateById(user);
    }

    public boolean isCurrentUser(HttpServletRequest httpServletRequest, User user){
        if(httpServletRequest == null || user == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = (User)httpServletRequest.getSession().getAttribute(userLoginStatus);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前无登录用户");
        }
        return currentUser.getId().equals(user.getId());
    }

    public boolean isAdmin(HttpServletRequest httpServletRequest){
        if(httpServletRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User user = (User)httpServletRequest.getSession().getAttribute(UserConstant.userLoginStatus);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}




