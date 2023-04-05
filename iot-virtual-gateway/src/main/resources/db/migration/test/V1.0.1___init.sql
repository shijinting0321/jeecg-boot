CREATE TABLE `cec_soft_device_data`
(
    `id`          varchar(64) NOT NULL COMMENT '主键',
    `device_id`   varchar(64) NOT NULL COMMENT '设备id',
    `code`        varchar(50) NOT NULL COMMENT '属性',
    `value`       text        NOT NULL COMMENT '数据',
    `timestamp`   varchar(20) NOT NULL COMMENT '采集时间戳',
    `create_by`   varchar(64)          DEFAULT NULL COMMENT '添加者id',
    `update_by`   varchar(64)          DEFAULT NULL COMMENT '修改者id',
    `create_time` datetime             DEFAULT NULL COMMENT '添加时间',
    `update_time` datetime             DEFAULT NULL COMMENT '修改时间',
    `sort`        int(11)     NOT NULL DEFAULT '0' COMMENT '排序字段',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT '设备数据';