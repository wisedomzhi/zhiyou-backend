package com.wisewind.zhiyou.common;

import lombok.Data;

@Data
public class PageRequest {
    /**
     * 当前页数
     */
    protected int page = 1;
    /**
     * 页面大小
     */
    protected int pageSize = 10;
}
