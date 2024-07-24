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


# 创建队伍表
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
    is_delete        tinyint   default 0                 not null comment '是否删除'
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
