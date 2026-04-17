-- 清空表数据
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sys_dict_item;
TRUNCATE TABLE sys_dict;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入测试字典数据
INSERT INTO sys_dict (dict_name, dict_code, description, status) VALUES 
('性别', 'gender', '用户性别', 1),
('状态', 'status', '通用状态', 1); 

-- 用户数据初始化已清理 - 请使用实际的用户表结构