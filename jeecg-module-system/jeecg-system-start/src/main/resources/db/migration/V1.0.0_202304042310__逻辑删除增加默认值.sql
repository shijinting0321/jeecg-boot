ALTER TABLE `iot_gateway`
    MODIFY COLUMN `del_flag` int NULL DEFAULT 0 COMMENT '逻辑删除标记;0-未删除 1-已删除' AFTER `update_ip`;
ALTER TABLE `iot_device`
    MODIFY COLUMN `del_flag` int NULL DEFAULT 0 COMMENT '逻辑删除标记;0-未删除 1-已删除' AFTER `update_ip`;
ALTER TABLE `iot_device_param`
    MODIFY COLUMN `del_flag` int NULL DEFAULT 0 COMMENT '逻辑删除标记;0-未删除 1-已删除' AFTER `update_ip`;
ALTER TABLE `iot_protocol_plugins`
    MODIFY COLUMN `del_flag` int NULL DEFAULT 0 COMMENT '逻辑删除标记;0-未删除 1-已删除' AFTER `update_ip`;