package com.wisewind.zhiyou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.constant.UserConstant;
import com.wisewind.zhiyou.exception.BusinessException;
import com.wisewind.zhiyou.model.domain.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static com.wisewind.zhiyou.constant.UserConstant.ADMIN_ROLE;
import static com.wisewind.zhiyou.constant.UserConstant.userLoginStatus;

/**
* @author ffz
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-05-29 21:37:56
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkPassword 校验密码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户注册
     *
     * @param userPassword 账号
     *                     * @param httpServletRequest
     * @return 用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest);

    User getSafetyUser(User user);

    int userLogout(HttpServletRequest httpServletRequest);

    List<User> getByTags(List<String> tags);

    List<User> getByTagsMemo(List<String> tags);

    int updateUser(User user, HttpServletRequest httpServletRequest);

    List<User> getRecommendUsers(int page, int pageSize, HttpServletRequest httpServletRequest);

    boolean isCurrentUser(HttpServletRequest httpServletRequest, User user);

    boolean isAdmin(HttpServletRequest httpServletRequest);

    User getCurrentUser(HttpServletRequest httpServletRequest);

    List<User> matchUsers(int num, HttpServletRequest httpServletRequest);
}
