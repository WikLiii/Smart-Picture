-- 创建数据库
create database if not exists smart_picture;
-- 切换库
use smart_picture;


-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

# 几个注意事项：
#
# 1）editTime 和 updateTime 的区别：editTime 表示用户编辑个人信息的时间（需要业务代码来更新），而 updateTime 表示这条用户记录任何字段发生修改的时间（由数据库自动更新）。
#
# 2）给唯一值添加唯一键（唯一索引），比如账号 userAccount，利用数据库天然防重复，同时可以增加查询效率。
#
# 3）给经常用于查询的字段添加索引，比如用户昵称 userName，可以增加查询效率。

-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                      null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                          -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction),          -- 用于模糊搜索图片简介
    INDEX idx_category (category),                  -- 提升基于图片分类的查询性能
    INDEX idx_tags (tags),                          -- 提升基于图片标签的查询性能
    INDEX idx_userId (userId)                       -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;
# 几个注意事项：
#
# 1）字段设计：
#
# 基础信息：包括图片的 URL、名称、简介、分类、标签等，满足图片管理和分类筛选的基本需求。
#
# 图片属性：记录图片大小、分辨率（宽度、高度）、宽高比和格式，方便后续按照多种维度筛选图片。
#
# 用户关联：通过 userId 字段关联用户表，表示由哪个用户创建了该图片。
#
# 多个标签：由于标签支持多个值，使用 JSON 数组字符串来维护，而不是单独新建一个表，可以提高开发效率。示例格式：["标签1", "标签2"]
#
# 2）时间字段区分：
#
# updateTime：任何字段的修改都会触发数据库自动更新，便于记录最新变动。该字段可以不让用户看到
#
# editTime：专用于记录图片信息被编辑的时间，需要通过业务逻辑主动更新。该字段可以对用户公开
#
# 3）索引设计：为高频查询的字段（如图片名称、简介、分类、标签、用户 id）添加索引，提高查询效率。
#
# 4）逻辑删除：也就是 “软删除”，使用 isDelete 字段标记是否删除，避免直接删除数据导致数据不可恢复问题