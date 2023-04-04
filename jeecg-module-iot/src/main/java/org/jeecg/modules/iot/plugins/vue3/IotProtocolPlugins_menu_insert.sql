-- 注意：该页面对应的前台目录为views/plugins文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external) 
VALUES ('2023040404541690480', NULL, '协议插件管理', '/plugins/iotProtocolPluginsList', 'plugins/IotProtocolPluginsList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2023-04-04 16:54:48', NULL, NULL, 0);

-- 权限控制sql
-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('2023040404541690481', '2023040404541690480', '添加协议插件管理', NULL, NULL, 0, NULL, NULL, 2, 'plugins:iot_protocol_plugins:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2023-04-04 16:54:48', NULL, NULL, 0, 0, '1', 0);
-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('2023040404541690482', '2023040404541690480', '编辑协议插件管理', NULL, NULL, 0, NULL, NULL, 2, 'plugins:iot_protocol_plugins:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2023-04-04 16:54:48', NULL, NULL, 0, 0, '1', 0);
-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('2023040404541690483', '2023040404541690480', '删除协议插件管理', NULL, NULL, 0, NULL, NULL, 2, 'plugins:iot_protocol_plugins:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2023-04-04 16:54:48', NULL, NULL, 0, 0, '1', 0);
-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('2023040404541690484', '2023040404541690480', '批量删除协议插件管理', NULL, NULL, 0, NULL, NULL, 2, 'plugins:iot_protocol_plugins:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2023-04-04 16:54:48', NULL, NULL, 0, 0, '1', 0);
-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('2023040404541690485', '2023040404541690480', '导出excel_协议插件管理', NULL, NULL, 0, NULL, NULL, 2, 'plugins:iot_protocol_plugins:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2023-04-04 16:54:48', NULL, NULL, 0, 0, '1', 0);
-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('2023040404541690486', '2023040404541690480', '导入excel_协议插件管理', NULL, NULL, 0, NULL, NULL, 2, 'plugins:iot_protocol_plugins:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2023-04-04 16:54:48', NULL, NULL, 0, 0, '1', 0);