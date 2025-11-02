create table conversation
(
    id          varchar(40)                        not null comment '注解Id'
        primary key,
    user_id     varchar(30)                        not null comment '用户ID',
    title       varchar(50)                        not null comment '会话标题',
    model_id    varchar(30)                        not null comment '模型Id',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '会话';

create table message
(
    id              varchar(40)                                              not null comment 'Id'
        primary key,
    conversation_id varchar(40)                                              not null comment '会话Id',
    sender          enum ('user', 'ai')                                      not null comment 'user / ai',
    content         text                                                     not null comment '文字',
    type            enum ('text', 'image', 'file') default 'text'            not null comment '类型',
    create_time     datetime                       default CURRENT_TIMESTAMP not null comment '创建时间'
);

create table model
(
    id          varchar(40)                        not null comment '主键'
        primary key,
    name        varchar(50)                        not null comment '模型名称',
    provider    varchar(50)                        not null comment '厂家：DashScope、openAi',
    model_key   varchar(40)                        null comment 'apikey',
    max_tokens  int                                not null comment '最大token数',
    description varchar(100)                       not null comment '描述',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间'
);

create table role
(
    id          varchar(40)                        not null comment '角色'
        primary key,
    name        varchar(50)                        not null comment '名字',
    description varchar(100)                       not null comment '描述',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '角色';

create table role_model
(
    id       varchar(30) not null comment 'id'
        primary key,
    role_id  varchar(30) not null comment '角色id',
    model_id varchar(30) not null comment '模型id'
)
    comment '角色与模型关联表';

create table role_tool
(
    id      varchar(30) not null comment 'Id'
        primary key,
    role_id varchar(30) not null comment '角色id',
    tool_id varchar(30) not null comment '工具id'
);

create table tool
(
    id          varchar(50)                           not null comment 'Id'
        primary key,
    name        varchar(50) default ''                not null comment '工具名字',
    type        varchar(50)                           not null comment '类型',
    config      json                                  not null comment '格式为json',
    description varchar(100)                          not null comment '描述',
    create_time datetime    default CURRENT_TIMESTAMP not null
)
    comment '工具';

create table user
(
    id          varchar(40)                           not null comment '主键'
        primary key,
    username    varchar(50)                           not null comment '用户名',
    password    varchar(60)                           not null comment '密码',
    email       varchar(20)                           not null comment '邮箱',
    model_id    varchar(30) default ''                not null comment '模型id',
    create_time datetime    default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '用户表';

create table user_role
(
    id      varchar(40) not null comment 'Id'
        primary key,
    user_id varchar(30) not null comment '用户Id',
    role_id varchar(30) null comment '角色Id'
);

create table user_tool
(
    id      varchar(30) not null comment 'Id'
        primary key,
    user_id varchar(30) not null comment '用户Id',
    tool_id varchar(30) null comment '工具Id'
);

