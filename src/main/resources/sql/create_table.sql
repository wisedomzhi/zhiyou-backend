# 创建用户表
create table user
(
    id            bigint auto_increment comment 'id
'
        primary key,
    username      varchar(256)                        null comment '用户名',
    user_account  varchar(256)                        null comment '账号',
    user_password varchar(256)                        not null comment '密码',
    avatar_url    varchar(1024)                       null comment '头像',
    gender        tinyint                             null comment '性别',
    phone         varchar(128)                        null comment '电话',
    email         varchar(256)                        null comment '邮箱',
    user_status   int       default 0                 null comment '用户状态 0-正常',
    create_time   timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   timestamp default CURRENT_TIMESTAMP null comment '更新时间',
    is_delete     tinyint   default 0                 not null comment '是否删除',
    user_role     int       default 0                 not null comment '用户权限 0-默认用户	1-管理员',
    tags          varchar(1024)                       null comment '用户标签',
    user_profile  varchar(512)                        null comment '用户描述'
)
    comment '用户';


create table team
(
    id               bigint auto_increment comment 'id
'
        primary key,
    team_name        varchar(256)                        null comment '队伍名',
    team_description varchar(256)                        null comment '队伍描述',
    team_password    varchar(256)                        null comment '队伍密码',
    max_num          int                                 null comment '队伍最大人数',
    expire_time      timestamp                           null comment '队伍过期时间',
    user_id          bigint                              null comment '队伍创建人',
    team_status      int       default 0                 null comment '队伍状态 0-公开， 1-私有， 2-加密',
    create_time      timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    update_time      timestamp default CURRENT_TIMESTAMP null comment '更新时间',
    is_delete        tinyint   default 0                 not null comment '是否删除',
    team_avatar_url  varchar(512)                        null comment '队伍头像'
)
    comment '队伍';
# 创建用户队伍关系表
create table user_team
(
    id          bigint auto_increment comment 'id
'
        primary key,
    user_id     bigint                              null comment '用户id',
    team_id     bigint                              null comment '队伍id',
    join_time   timestamp                           null comment '加入队伍时间',
    create_time timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP null comment '更新时间',
    is_delete   tinyint   default 0                 not null comment '是否删除'
)
    comment '用户队伍关系表';

# 创建标签表
create table tag
(
    id          bigint                             not null
        primary key,
    tag_name    varchar(256)                       not null comment '标签名',
    parent_id   bigint                             null comment '父标签id',
    is_parent   tinyint                            not null comment '是否为父标签',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    is_delete   tinyint  default 0                 null comment '是否删除
0-未删除 1-删除',
    constraint idx_tag_name
        unique (tag_name),
    constraint tag_name
        unique (tag_name)
)
    comment '标签表';


create table chat
(
    id          bigint auto_increment comment '聊天记录id'
        primary key,
    from_id     bigint                                  not null comment '发送消息id',
    to_id       bigint                                  null comment '接收消息id',
    chat_text   varchar(512) collate utf8mb4_unicode_ci null,
    chat_type   tinyint                                 not null comment '聊天类型 1-私聊 2-群聊',
    create_time datetime default CURRENT_TIMESTAMP      null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP      null,
    team_id     bigint                                  null,
    is_delete   tinyint  default 0                      null comment '逻辑删除 1-删除'
)
    comment '聊天消息表';
