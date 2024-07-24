package com.wisewind.zhiyou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wisewind.zhiyou.model.domain.UserTeam;
import com.wisewind.zhiyou.service.UserTeamService;
import com.wisewind.zhiyou.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author ffz
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2024-07-07 22:53:36
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




