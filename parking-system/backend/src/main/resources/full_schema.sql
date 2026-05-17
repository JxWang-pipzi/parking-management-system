-- 停车位信息管理系统交付版数据库初始化脚本
-- 适用目标：Web 后台管理员端 + 微信小程序用户端 + MySQL 数据互通
-- 执行方式：mysql -uroot -p < full_schema.sql

DROP DATABASE IF EXISTS parking_system;
CREATE DATABASE parking_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE parking_system;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 用户表：role 0=普通用户，1=管理员；status 0=禁用，1=启用
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `email` VARCHAR(100) NULL,
  `avatar` VARCHAR(500) NULL,
  `role` INT NOT NULL DEFAULT 0,
  `status` INT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  UNIQUE KEY `uk_user_email` (`email`),
  KEY `idx_user_role_status` (`role`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 停车场表：Web 后台维护，小程序实时读取
CREATE TABLE `parking_lot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `address` VARCHAR(255) NOT NULL,
  `total_spaces` INT NOT NULL DEFAULT 0,
  `available_spaces` INT NOT NULL DEFAULT 0,
  `hourly_rate` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `latitude` DOUBLE NOT NULL DEFAULT 0,
  `longitude` DOUBLE NOT NULL DEFAULT 0,
  `status` INT NOT NULL DEFAULT 1 COMMENT '0=关闭，1=开放',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_lot_status` (`status`),
  KEY `idx_lot_location` (`latitude`, `longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车场表';

-- 车位表：status 0=空闲，1=占用，2=预约
CREATE TABLE `parking_space` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parking_lot_id` BIGINT NOT NULL,
  `space_number` VARCHAR(30) NOT NULL,
  `type` INT NOT NULL DEFAULT 0 COMMENT '0=普通，1=无障碍，2=VIP/充电',
  `status` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_space_lot_number` (`parking_lot_id`, `space_number`),
  KEY `idx_space_lot_status` (`parking_lot_id`, `status`),
  CONSTRAINT `fk_space_lot` FOREIGN KEY (`parking_lot_id`) REFERENCES `parking_lot` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车位表';

-- 订单表：统一状态 0=待支付，1=已完成，2=已取消，3=停车中，5=已退款
CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `parking_lot_id` BIGINT NOT NULL,
  `parking_space_id` BIGINT NOT NULL,
  `plate_number` VARCHAR(20) NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NULL,
  `duration` INT NULL COMMENT '停车时长，单位分钟',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `status` INT NOT NULL DEFAULT 3,
  `order_no` VARCHAR(50) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `payment_time` DATETIME NULL,
  `completion_time` DATETIME NULL,
  `cancellation_time` DATETIME NULL,
  `cancellation_reason` VARCHAR(255) NULL,
  `invoice_requested` TINYINT(1) NOT NULL DEFAULT 0,
  `invoice_type` VARCHAR(20) NULL,
  `invoice_title` VARCHAR(100) NULL,
  `invoice_tax_no` VARCHAR(50) NULL,
  `invoice_email` VARCHAR(100) NULL,
  `invoice_no` VARCHAR(50) NULL,
  `invoice_generated_time` DATETIME NULL,
  `invoice_url` VARCHAR(255) NULL,
  `invoice_status` VARCHAR(20) NULL,
  `remark` VARCHAR(255) NULL,
  `discount_amount` DECIMAL(10,2) NULL DEFAULT 0.00,
  `actual_amount` DECIMAL(10,2) NULL DEFAULT 0.00,
  `coupon_code` VARCHAR(50) NULL,
  `rating` INT NULL,
  `feedback` TEXT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_user_status_time` (`user_id`, `status`, `create_time`),
  KEY `idx_order_lot_time` (`parking_lot_id`, `create_time`),
  KEY `idx_order_space_status` (`parking_space_id`, `status`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_lot` FOREIGN KEY (`parking_lot_id`) REFERENCES `parking_lot` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_space` FOREIGN KEY (`parking_space_id`) REFERENCES `parking_space` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 支付记录表：小程序离场支付后写入，Web 后台支付管理读取
CREATE TABLE `payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `payment_method` INT NOT NULL DEFAULT 1 COMMENT '0=微信，1=支付宝，2=现金',
  `transaction_id` VARCHAR(100) NOT NULL,
  `status` INT NOT NULL DEFAULT 1 COMMENT '0=待支付，1=已支付，2=失败，3=已退款',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_transaction` (`transaction_id`),
  KEY `idx_payment_order` (`order_id`),
  KEY `idx_payment_user` (`user_id`),
  KEY `idx_payment_status_time` (`status`, `create_time`),
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 用户车辆表：小程序“我的车辆”使用
CREATE TABLE `user_vehicle` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `plate_number` VARCHAR(20) NOT NULL,
  `brand` VARCHAR(50) NULL,
  `color` VARCHAR(20) NULL,
  `is_default` TINYINT(1) NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vehicle_user_plate` (`user_id`, `plate_number`),
  KEY `idx_vehicle_user_default` (`user_id`, `is_default`),
  CONSTRAINT `fk_vehicle_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户车辆表';

-- 车辆进出记录表：Web 后台车牌识别/出入场管理使用
CREATE TABLE `vehicle_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parking_lot_id` BIGINT NOT NULL,
  `parking_space_id` BIGINT NULL,
  `plate_number` VARCHAR(20) NOT NULL,
  `entry_time` DATETIME NOT NULL,
  `exit_time` DATETIME NULL,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0=在场，1=已离场',
  `plate_image_url` VARCHAR(500) NULL,
  `recognition_confidence` DECIMAL(5,2) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_vehicle_record_lot_status` (`parking_lot_id`, `status`),
  KEY `idx_vehicle_record_plate` (`plate_number`),
  CONSTRAINT `fk_vehicle_record_lot` FOREIGN KEY (`parking_lot_id`) REFERENCES `parking_lot` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vehicle_record_space` FOREIGN KEY (`parking_space_id`) REFERENCES `parking_space` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆进出记录表';

-- 传感器设备表：多源停车数据融合
CREATE TABLE `parking_sensor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parking_lot_id` BIGINT NOT NULL,
  `space_id` BIGINT NULL,
  `sensor_code` VARCHAR(50) NOT NULL,
  `sensor_type` INT NOT NULL COMMENT '1=地磁，2=摄像头，3=超声波，4=红外',
  `sensor_name` VARCHAR(100) NOT NULL,
  `manufacturer` VARCHAR(100) NULL,
  `model` VARCHAR(50) NULL,
  `status` INT NOT NULL DEFAULT 1 COMMENT '0=离线，1=在线',
  `last_value` DOUBLE NULL,
  `last_update_time` DATETIME NULL,
  `data_quality` INT NOT NULL DEFAULT 100,
  `ip_address` VARCHAR(50) NULL,
  `port` INT NULL,
  `config` TEXT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sensor_code` (`sensor_code`),
  KEY `idx_sensor_lot` (`parking_lot_id`),
  KEY `idx_sensor_space` (`space_id`),
  KEY `idx_sensor_status` (`status`),
  CONSTRAINT `fk_sensor_lot` FOREIGN KEY (`parking_lot_id`) REFERENCES `parking_lot` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sensor_space` FOREIGN KEY (`space_id`) REFERENCES `parking_space` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传感器设备表';

CREATE TABLE `parking_sensor_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `sensor_id` BIGINT NOT NULL,
  `parking_lot_id` BIGINT NOT NULL,
  `space_id` BIGINT NULL,
  `raw_value` DOUBLE NULL,
  `processed_value` DOUBLE NULL,
  `data_type` INT NOT NULL DEFAULT 0 COMMENT '0=占用状态，1=车辆信息，2=环境数据',
  `data_quality` INT NOT NULL DEFAULT 100,
  `quality_metrics` TEXT NULL,
  `is_anomaly` INT NOT NULL DEFAULT 0,
  `anomaly_type` VARCHAR(50) NULL,
  `collect_time` DATETIME NOT NULL,
  `process_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sensor_data_sensor_time` (`sensor_id`, `collect_time`),
  KEY `idx_sensor_data_lot_time` (`parking_lot_id`, `collect_time`),
  KEY `idx_sensor_data_anomaly` (`is_anomaly`),
  CONSTRAINT `fk_sensor_data_sensor` FOREIGN KEY (`sensor_id`) REFERENCES `parking_sensor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传感器数据表';

-- 用户行为与推荐：满足开题报告智能推荐要求
CREATE TABLE `parking_user_behavior` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `parking_lot_id` BIGINT NULL,
  `space_id` BIGINT NULL,
  `behavior_type` INT NOT NULL COMMENT '1=预约，2=停车，3=离开，4=查看',
  `time_slot` INT NOT NULL,
  `weekday` INT NOT NULL,
  `duration` INT NULL,
  `amount` DECIMAL(10,2) NULL,
  `satisfaction_score` DOUBLE NULL,
  `behavior_tags` VARCHAR(255) NULL,
  `behavior_time` DATETIME NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_behavior_user_time` (`user_id`, `behavior_time`),
  KEY `idx_behavior_lot_time` (`parking_lot_id`, `behavior_time`),
  CONSTRAINT `fk_behavior_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_behavior_lot` FOREIGN KEY (`parking_lot_id`) REFERENCES `parking_lot` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';

CREATE TABLE `parking_recommendation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `parking_lot_id` BIGINT NOT NULL,
  `space_id` BIGINT NULL,
  `recommendation_score` DOUBLE NOT NULL,
  `recommendation_reason` VARCHAR(255) NULL,
  `recommendation_type` INT NOT NULL DEFAULT 0,
  `time_slot` INT NULL,
  `weekday` INT NULL,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0=待使用，1=已点击，2=已使用',
  `click_count` INT NOT NULL DEFAULT 0,
  `use_count` INT NOT NULL DEFAULT 0,
  `expire_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_recommend_user_score` (`user_id`, `recommendation_score`),
  KEY `idx_recommend_lot` (`parking_lot_id`),
  KEY `idx_recommend_expire` (`expire_time`),
  CONSTRAINT `fk_recommend_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_recommend_lot` FOREIGN KEY (`parking_lot_id`) REFERENCES `parking_lot` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐结果表';

CREATE TABLE `parking_reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `parking_lot_id` BIGINT NOT NULL,
  `space_id` BIGINT NOT NULL,
  `plate_number` VARCHAR(20) NOT NULL,
  `reservation_time` DATETIME NOT NULL,
  `duration` INT NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0=待确认，1=已确认，2=已取消，3=已完成，4=已过期',
  `cancel_reason` VARCHAR(255) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reservation_user_status` (`user_id`, `status`),
  KEY `idx_reservation_lot_time` (`parking_lot_id`, `reservation_time`),
  CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reservation_lot` FOREIGN KEY (`parking_lot_id`) REFERENCES `parking_lot` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reservation_space` FOREIGN KEY (`space_id`) REFERENCES `parking_space` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

CREATE TABLE `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL,
  `config_value` TEXT NULL,
  `description` VARCHAR(255) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `type` INT NOT NULL DEFAULT 0,
  `is_read` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_notification_user_read` (`user_id`, `is_read`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NULL,
  `username` VARCHAR(50) NULL,
  `operation` VARCHAR(100) NOT NULL,
  `method` VARCHAR(200) NULL,
  `params` TEXT NULL,
  `ip` VARCHAR(50) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operation_user_time` (`user_id`, `create_time`),
  KEY `idx_operation_type` (`operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

SET FOREIGN_KEY_CHECKS = 1;

-- 初始账号：管理员 admin/123456，普通用户 user/123456
INSERT INTO `user` (`id`, `username`, `password`, `name`, `phone`, `email`, `avatar`, `role`, `status`) VALUES
(1, 'admin', '$2a$10$uM02fEkNVHPmpgJ8UbHY/e78J4GfY4QA60BL8bdo6w4HwSMyCLEHa', '系统管理员', '13800138000', 'admin@example.com', NULL, 1, 1),
(2, 'user',  '$2a$10$uM02fEkNVHPmpgJ8UbHY/e78J4GfY4QA60BL8bdo6w4HwSMyCLEHa', '演示用户', '13800000000', 'user@example.com', NULL, 0, 1);

INSERT INTO `parking_lot` (`id`, `name`, `address`, `total_spaces`, `available_spaces`, `hourly_rate`, `latitude`, `longitude`, `status`) VALUES
(1, '锦城学院东门停车场', '成都市高新区西源大道1号东门', 12, 10, 6.00, 30.731201, 103.944602, 1),
(2, '软件园智慧停车场', '成都市高新区天府软件园C区', 10, 9, 8.00, 30.552271, 104.066541, 1),
(3, '环球中心地下停车场', '成都市高新区天府大道北段1700号', 8, 7, 10.00, 30.571230, 104.064850, 1),
(4, '城市广场立体停车楼', '成都市武侯区人民南路四段', 6, 4, 9.00, 30.623501, 104.067902, 1);

INSERT INTO `parking_space` (`id`, `parking_lot_id`, `space_number`, `type`, `status`) VALUES
(1, 1, 'A001', 0, 0), (2, 1, 'A002', 0, 0), (3, 1, 'A003', 1, 0), (4, 1, 'A004', 2, 0),
(5, 1, 'A005', 0, 0), (6, 1, 'A006', 0, 0), (7, 1, 'A007', 0, 0), (8, 1, 'A008', 0, 0),
(9, 1, 'A009', 0, 0), (10, 1, 'A010', 0, 0), (11, 1, 'A011', 0, 1), (12, 1, 'A012', 0, 1),
(13, 2, 'B001', 0, 0), (14, 2, 'B002', 0, 0), (15, 2, 'B003', 1, 0), (16, 2, 'B004', 0, 0),
(17, 2, 'B005', 2, 0), (18, 2, 'B006', 0, 0), (19, 2, 'B007', 0, 0), (20, 2, 'B008', 0, 0),
(21, 2, 'B009', 0, 0), (22, 2, 'B010', 0, 1),
(23, 3, 'C001', 0, 0), (24, 3, 'C002', 0, 0), (25, 3, 'C003', 0, 0), (26, 3, 'C004', 1, 0),
(27, 3, 'C005', 2, 0), (28, 3, 'C006', 0, 0), (29, 3, 'C007', 0, 0), (30, 3, 'C008', 0, 1),
(31, 4, 'D001', 0, 0), (32, 4, 'D002', 0, 0), (33, 4, 'D003', 0, 0), (34, 4, 'D004', 1, 0),
(35, 4, 'D005', 2, 2), (36, 4, 'D006', 0, 1);

INSERT INTO `user_vehicle` (`id`, `user_id`, `plate_number`, `brand`, `color`, `is_default`) VALUES
(1, 2, '川A12345', '比亚迪', '白色', 1),
(2, 2, '川A8P66D', '特斯拉', '黑色', 0);

-- 演示订单：一个停车中订单、一个已完成订单、一个已取消订单
INSERT INTO `order` (`id`, `user_id`, `parking_lot_id`, `parking_space_id`, `plate_number`, `start_time`, `end_time`, `duration`, `amount`, `actual_amount`, `status`, `order_no`, `payment_time`, `completion_time`, `cancellation_time`, `cancellation_reason`) VALUES
(1, 2, 1, 11, '川A12345', DATE_SUB(NOW(), INTERVAL 35 MINUTE), NULL, NULL, 0.00, 0.00, 3, 'ORD-DEMO-ACTIVE', NULL, NULL, NULL, NULL),
(2, 2, 2, 21, '川A8P66D', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 95 MINUTE, 95, 12.67, 12.67, 1, 'ORD-DEMO-PAID', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 95 MINUTE, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 95 MINUTE, NULL, NULL),
(3, 2, 3, 29, '川A12345', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL, 0.00, 0.00, 2, 'ORD-DEMO-CANCEL', NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 8 MINUTE, '用户取消');

INSERT INTO `payment_record` (`id`, `order_id`, `user_id`, `amount`, `payment_method`, `transaction_id`, `status`, `create_time`) VALUES
(1, 2, 2, 12.67, 0, 'PAY-DEMO-0001', 1, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 95 MINUTE);

INSERT INTO `vehicle_record` (`id`, `parking_lot_id`, `parking_space_id`, `plate_number`, `entry_time`, `exit_time`, `status`, `plate_image_url`, `recognition_confidence`) VALUES
(1, 1, 11, '川A12345', DATE_SUB(NOW(), INTERVAL 35 MINUTE), NULL, 0, NULL, 96.50),
(2, 2, 21, '川A8P66D', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 95 MINUTE, 1, NULL, 95.20);

INSERT INTO `parking_sensor` (`id`, `parking_lot_id`, `space_id`, `sensor_code`, `sensor_type`, `sensor_name`, `manufacturer`, `model`, `status`, `last_value`, `last_update_time`, `data_quality`, `ip_address`, `port`) VALUES
(1, 1, 1, 'JC-E-A001-GM', 1, '东门A001地磁传感器', '华为', 'HM-G100', 1, 0, NOW(), 98, '192.168.10.101', 8080),
(2, 1, 11, 'JC-E-A011-GM', 1, '东门A011地磁传感器', '华为', 'HM-G100', 1, 1, NOW(), 96, '192.168.10.111', 8080),
(3, 2, 21, 'RJY-B009-CAM', 2, '软件园B009摄像头', '海康威视', 'HK-C300', 1, 1, NOW(), 93, '192.168.20.109', 8080),
(4, 3, 29, 'HQ-C007-US', 3, '环球中心C007超声波', '大华', 'DH-U200', 0, 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 72, '192.168.30.107', 8080);

INSERT INTO `parking_sensor_data` (`sensor_id`, `parking_lot_id`, `space_id`, `raw_value`, `processed_value`, `data_type`, `data_quality`, `quality_metrics`, `is_anomaly`, `anomaly_type`, `collect_time`, `process_time`) VALUES
(1, 1, 1, 0, 0, 0, 98, '{"source":"geomagnetic","confidence":0.98}', 0, NULL, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW()),
(2, 1, 11, 1, 1, 0, 96, '{"source":"geomagnetic","confidence":0.96}', 0, NULL, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW()),
(3, 2, 21, 1, 1, 0, 93, '{"source":"camera","confidence":0.93}', 0, NULL, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NOW()),
(4, 3, 29, 1, 1, 0, 72, '{"source":"ultrasonic","confidence":0.72}', 1, '传感器离线后数据滞后', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW());

INSERT INTO `parking_user_behavior` (`user_id`, `parking_lot_id`, `space_id`, `behavior_type`, `time_slot`, `weekday`, `duration`, `amount`, `satisfaction_score`, `behavior_tags`, `behavior_time`) VALUES
(2, 1, 11, 2, 4, 1, 35, NULL, NULL, '校园,近距离,停车中', DATE_SUB(NOW(), INTERVAL 35 MINUTE)),
(2, 2, 21, 2, 5, 3, 95, 12.67, 4.8, '工作日,软件园,已完成', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 3, 29, 1, 7, 4, NULL, NULL, NULL, '商业区,取消', DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `parking_recommendation` (`user_id`, `parking_lot_id`, `space_id`, `recommendation_score`, `recommendation_reason`, `recommendation_type`, `time_slot`, `weekday`, `status`, `expire_time`) VALUES
(2, 1, NULL, 96.5, '距离最近且空位充足，适合当前定位', 1, 4, WEEKDAY(NOW()), 0, DATE_ADD(NOW(), INTERVAL 2 HOUR)),
(2, 2, NULL, 88.0, '历史停车频率高，通勤场景匹配', 1, 4, WEEKDAY(NOW()), 0, DATE_ADD(NOW(), INTERVAL 2 HOUR)),
(2, 4, NULL, 80.5, '价格适中，剩余车位稳定', 2, 4, WEEKDAY(NOW()), 0, DATE_ADD(NOW(), INTERVAL 2 HOUR));

INSERT INTO `parking_reservation` (`user_id`, `parking_lot_id`, `space_id`, `plate_number`, `reservation_time`, `duration`, `amount`, `status`) VALUES
(2, 4, 35, '川A8P66D', DATE_ADD(NOW(), INTERVAL 1 HOUR), 60, 9.00, 1);

INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('billing.min_minutes', '1', '最小计费分钟数'),
('billing.default_hourly_rate', '10.00', '停车场未配置费率时的默认小时费率'),
('recommendation.enabled', 'true', '是否开启智能推荐');

INSERT INTO `notification` (`user_id`, `title`, `content`, `type`, `is_read`) VALUES
(2, '欢迎使用智慧停车', '可在首页查看附近停车场并开始停车。', 0, 0),
(2, '停车中提醒', '您的车辆川A12345正在锦城学院东门停车场停车。', 2, 0);

-- 管理端常用视图：停车场实时状态
CREATE OR REPLACE VIEW `v_parking_lot_realtime` AS
SELECT
  pl.id,
  pl.name,
  pl.address,
  pl.total_spaces,
  pl.available_spaces,
  pl.hourly_rate,
  pl.status,
  ROUND(IF(pl.total_spaces = 0, 0, pl.available_spaces / pl.total_spaces * 100), 2) AS availability_rate,
  SUM(CASE WHEN ps.status = 1 THEN 1 ELSE 0 END) AS online_sensors,
  SUM(CASE WHEN ps.status = 0 THEN 1 ELSE 0 END) AS offline_sensors
FROM parking_lot pl
LEFT JOIN parking_sensor ps ON ps.parking_lot_id = pl.id
GROUP BY pl.id, pl.name, pl.address, pl.total_spaces, pl.available_spaces, pl.hourly_rate, pl.status;

CREATE OR REPLACE VIEW `v_order_statistics` AS
SELECT
  DATE(create_time) AS order_date,
  COUNT(*) AS total_orders,
  SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS active_orders,
  SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS completed_orders,
  SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS cancelled_orders,
  SUM(CASE WHEN status = 1 THEN amount ELSE 0 END) AS total_revenue
FROM `order`
GROUP BY DATE(create_time)
ORDER BY order_date DESC;

SELECT 'parking_system schema initialized successfully' AS message;
