package com.wisewind.zhiyou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.constant.UserConstant;
import com.wisewind.zhiyou.exception.BusinessException;
import com.wisewind.zhiyou.model.domain.User;
import com.wisewind.zhiyou.service.UserService;
import com.wisewind.zhiyou.mapper.UserMapper;
import com.wisewind.zhiyou.utils.AlgorithmUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SALT = "wisewind";

    private static String redisUserKey = "zhiyou:user:recommend:%s";


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
        //TODO 添加对要修改用户的校验，如果传入的修改用户属性都为空值，直接返回
        User oldUser = userMapper.selectById(user.getId());
        if(oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "不存在该用户！");
        }
        String key = String.format(redisUserKey, oldUser.getId());
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userMapper.updateById(user);
    }

    @Override
    public List<User> getRecommendUsers(int page, int pageSize, HttpServletRequest httpServletRequest){
        User currentUser = getCurrentUser(httpServletRequest);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long id = currentUser.getId();
        String key = String.format(redisUserKey, id);
        List<User> userList = (List<User>)redisTemplate.opsForValue().get(key);
        if(userList != null){
            return userList;
        }
        Page<User> pageResult = this.page(new Page<>(page, pageSize));
        userList = pageResult.getRecords();
        List<User> list = userList.stream().map(user -> this.getSafetyUser(user)).collect(Collectors.toList());
        redisTemplate.opsForValue().set(key, list, 2, TimeUnit.HOURS);
        return list;
    }

    public boolean isCurrentUser(HttpServletRequest httpServletRequest, User user){
        if(httpServletRequest == null || user == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = getCurrentUser(httpServletRequest);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return currentUser.getId().equals(user.getId());
    }

    public boolean isAdmin(HttpServletRequest httpServletRequest){
        User currentUser = getCurrentUser(httpServletRequest);
        return currentUser != null && currentUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public User getCurrentUser(HttpServletRequest httpServletRequest){
        if(httpServletRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User user = (User)httpServletRequest.getSession().getAttribute(UserConstant.userLoginStatus);
        return user;
    }

    @Override
    public List<User> matchUsers(int num, HttpServletRequest httpServletRequest) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(User::getId, User::getTags);
        lambdaQueryWrapper.isNotNull(User::getTags);
        List<User> userList = this.list(lambdaQueryWrapper);
        User currentUser = getCurrentUser(httpServletRequest);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        String tags = currentUser.getTags();
        Gson gson = new Gson();
        List<String> currentTagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());

        PriorityQueue<Pair<User, Integer>> priorityQueue = new PriorityQueue<>(new Comparator<Pair<User, Integer>>() {
            @Override
            public int compare(Pair<User, Integer> o1, Pair<User, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            if(StringUtils.isBlank(userTags) || user.getId().equals(currentUser.getId())){
                continue;
            }
            List<String> targetTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());

            int score = AlgorithmUtil.minDistance4Tags(currentTagList, targetTagList);
            if(priorityQueue.size() < num){
                priorityQueue.offer(new ImmutablePair<>(user, score));
            }else{
                Pair<User, Integer> pair = priorityQueue.peek();
                if(pair != null && score < pair.getValue()){
                    priorityQueue.poll();
                    priorityQueue.offer(new ImmutablePair<>(user, score));
                }
            }
        }
        List<Long> matchedUserIds = new ArrayList<>();
        while (!priorityQueue.isEmpty()){
            Pair<User, Integer> pair = priorityQueue.poll();
            matchedUserIds.add(pair.getKey().getId());
        }
        Collections.reverse(matchedUserIds);
        StringBuilder sb = new StringBuilder();
        sb.append("order by field(id,");
        for (int i = 0; i < matchedUserIds.size(); i++) {
            if(i == 0){
                sb.append(matchedUserIds.get(i));
            }else {
                sb.append(",").append(matchedUserIds.get(i));
            }
        }
        sb.append(")");
        lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(User::getId, matchedUserIds).last(sb.toString());
        List<User> matchedUsers = this.list(lambdaQueryWrapper);
        return matchedUsers;
    }
}




