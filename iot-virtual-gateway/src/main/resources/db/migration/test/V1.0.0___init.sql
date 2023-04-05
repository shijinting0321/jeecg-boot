CREATE TABLE `cec_soft_device`
(
    `id`             varchar(64)  NOT NULL COMMENT '主键',
    `parent_id`      varchar(64)           DEFAULT NULL COMMENT '上级id',
    `device_id`      varchar(64)           DEFAULT NULL COMMENT '上位机设备id',
    `serial`         varchar(100) NOT NULL COMMENT '序列号',
    `brand_model_id` varchar(64)  NOT NULL COMMENT '品牌型号id',
    `name`           varchar(100) NOT NULL COMMENT '名称',
    `link_attr`      text                  DEFAULT NULL COMMENT '连接属性',
    `create_by`      varchar(64)           DEFAULT NULL COMMENT '添加者id',
    `update_by`      varchar(64)           DEFAULT NULL COMMENT '修改者id',
    `create_time`    datetime              DEFAULT NULL COMMENT '添加时间',
    `update_time`    datetime              DEFAULT NULL COMMENT '修改时间',
    `sort`           int(11)      NOT NULL DEFAULT '0' COMMENT '排序字段',
    `deleted`        tinyint(1)   NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
    `sync_result`    varchar(30)           DEFAULT NULL COMMENT '同步结果',
    `source`         varchar(30)           DEFAULT NULL COMMENT '来源',
    `plugin_id`      varchar(100)          DEFAULT NULL COMMENT '驱动唯一标识',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT '设备';