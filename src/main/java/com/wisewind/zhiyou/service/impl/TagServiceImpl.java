package com.wisewind.zhiyou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wisewind.zhiyou.model.domain.Tag;
import com.wisewind.zhiyou.service.TagService;
import com.wisewind.zhiyou.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author ffz
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2024-06-22 14:54:34
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




