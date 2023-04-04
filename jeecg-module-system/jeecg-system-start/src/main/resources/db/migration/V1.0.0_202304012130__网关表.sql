
/* --------------- 创建表 --------------- */
DROP TABLE IF EXISTS tb_gateway;
CREATE TABLE tb_gateway(
    `id` VARCHAR(64) NOT NULL   COMMENT 'ID' ,
    `tenant_id` VARCHAR(64)    COMMENT '租户号' ,
    `version` VARCHAR(64)    COMMENT '乐观锁' ,
    `create_by` VARCHAR(64)    COMMENT '创建人' ,
    `create_dept` VARCHAR(64)    COMMENT '创建部门' ,
    `create_time` DATETIME    COMMENT '创建时间' ,
    `update_by` VARCHAR(64)    COMMENT '更新人' ,
    `update_time` DATETIME    COMMENT '更新时间' ,
    `update_ip` VARCHAR(64)    COMMENT '更新IP' ,
    `del_flag` INT(1)    COMMENT '逻辑删除标记;0-未删除 1-已删除' ,
    `sort` INT    COMMENT '排序' ,
    `remark` VARCHAR(255)    COMMENT '备注' ,
    `code` VARCHAR(255)    COMMENT '编码' ,
    `name` VARCHAR(255)    COMMENT '网关名称' ,
    `type` INT    COMMENT '网关类型;0-虚拟网关 1-硬件网关' ,
    `token` VARCHAR(255)    COMMENT '令牌;用于连接mqtt' ,
    `protocol` JSON    COMMENT '协议' ,
    PRIMARY KEY (id)
)  COMMENT = '设备接入网关表';
