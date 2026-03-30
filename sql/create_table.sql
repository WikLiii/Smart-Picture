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
ALTER TABLE picture

    ADD COLUMN reviewStatus  INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN reviewMessage VARCHAR(512)  NULL COMMENT '审核信息',
    ADD COLUMN reviewerId    BIGINT        NULL COMMENT '审核人 ID',
    ADD COLUMN reviewTime    DATETIME      NULL COMMENT '审核时间';

#创建基于 reviewStatus 列的索引
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);
# 注意事项：
#
# 1）审核状态：reviewStatus 使用整数（0、1、2）表示不同的审核状态，而不是用字符串，可以节约表的空间、提升查找效率。
#
# 2）索引设计：由于要根据审核状态筛选图片，所以给该字段添加索引，提升查询性能。
ALTER TABLE picture

    ADD COLUMN thumbnailUrl varchar(512) NULL COMMENT '缩略图 url';
#数据表 picture 新增缩略图字段
create table if not exists space
(
    id         bigint auto_increment comment 'id' primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-pro专业版 2-proMax旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间下的图片数量',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',

    index idx_userId (userId),
    index idx_spaceName (spaceName),
    index idx_spaceLevel (spaceLevel)
) comment '空间' collate = utf8mb4_unicode_ci;
#  注意事项：
# 1.空间级别字段：空间级别包括普通版、pro专业版和proMax旗舰版，是可枚举的，因此使用整型来节约空间、提高查询效率。
# 2.空间限额字段：除了级别字段外，增加 maxSize 和 maxCount 字段用于限制空间的图片总大小与数量，而不是在代码中根据级别读取限额。
# 这样管理员可以单独设置限额，不用完全和级别绑定，利于扩展；而且查询限额时也更方便。
# 3.索引设计：为高频查询的字段（如空间名称、空间级别、用户 id）添加索引，提高查询效率。


ALTER TABLE picture
    ADD COLUMN spaceId  bigint  null comment '空间 id（为空表示公共空间）';


CREATE INDEX idx_spaceId ON picture (spaceId);
#给图片表添加新列
# 默认情况下，spaceId 为空，表示图片上传到了公共图库。
ALTER TABLE picture
    ADD COLUMN picColor varchar(16) null comment '图片主色调';
# 补充颜色字段

-- 支持空间类型，添加新列
ALTER TABLE space
    ADD COLUMN spaceType int default 0 not null comment '空间类型：0-私有 1-团队';

CREATE INDEX idx_spaceType ON space (spaceType);

-- 空间成员表
create table if not exists space_user
(
    id         bigint auto_increment comment 'id' primary key,
    spaceId    bigint                                 not null comment '空间 id',
    userId     bigint                                 not null comment '用户 id',
    spaceRole  varchar(128) default 'viewer'          null comment '空间角色：viewer/editor/admin',
    createTime datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    -- 索引设计
    UNIQUE KEY uk_spaceId_userId (spaceId, userId), -- 唯一索引，用户在一个空间中只能有一个角色
    INDEX idx_spaceId (spaceId),                    -- 提升按空间查询的性能
    INDEX idx_userId (userId)                       -- 提升按用户查询的性能
) comment '空间用户关联' collate = utf8mb4_unicode_ci;
# 1. 给 spaceId 和 userId 添加唯一索引，确保同一用户在同一空间中只能有一个角色（不能重复加入）。由于有唯一键，不需要使用逻辑删除字段，否则无法退出后再重新加入。
# 2. 给关联字段添加索引，提高查询效率
# 3. 为了跟用户自身在项目中的角色 userRole 区分开，空间角色的名称使用 spaceRole
