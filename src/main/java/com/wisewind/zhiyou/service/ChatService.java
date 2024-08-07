package com.wisewind.zhiyou.service;

import com.wisewind.zhiyou.model.domain.Chat;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wisewind.zhiyou.model.vo.MessageVO;

import java.util.List;

/**
* @author ffz
* @description 针对表【chat(聊天消息表)】的数据库操作Service
* @createDate 2024-08-04 13:44:06
*/
public interface ChatService extends IService<Chat> {

    /**
     * 获取队伍历史聊天信息
     * @param teamId
     * @return
     */
    List<MessageVO> listTeamChat(Long teamId);
}
