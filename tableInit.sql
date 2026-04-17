-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: scm
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_general_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',NULL,'root','2025-11-16 07:02:32',0,1),(2,'1.0.2','Create Product Table','SQL','V1.0.2__Create_Product_Table.sql',-560797984,'root','2025-11-16 07:02:32',40,0);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict`
--

DROP TABLE IF EXISTS `sys_dict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典名称',
  `dict_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典编码',
  `description` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dict_code` (`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict`
--

LOCK TABLES `sys_dict` WRITE;
/*!40000 ALTER TABLE `sys_dict` DISABLE KEYS */;
INSERT INTO `sys_dict` VALUES (1,'Product Category','product_category','物品类别',0,'2025-10-20 11:13:39','2025-10-21 21:13:54'),(2,'Order Status','order_status','订单状态',1,'2025-10-20 11:13:39','2025-10-21 21:13:54'),(8,'Product Status','product_status','商品销售状态',1,'2025-12-08 16:20:29','2025-12-08 16:20:29'),(9,'Production Status','production_status','生产状态',1,'2025-12-08 16:33:53','2025-12-08 16:33:53');
/*!40000 ALTER TABLE `sys_dict` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_item`
--

DROP TABLE IF EXISTS `sys_dict_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_id` bigint NOT NULL COMMENT '字典ID',
  `label` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典项名称',
  `value` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典项值',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dict_id` (`dict_id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_item`
--

LOCK TABLES `sys_dict_item` WRITE;
/*!40000 ALTER TABLE `sys_dict_item` DISABLE KEYS */;
INSERT INTO `sys_dict_item` VALUES (1,1,'Computer','1',1,1,'2025-10-21 18:18:16','2025-10-21 21:14:21'),(2,1,'Phone','2',2,1,'2025-10-21 18:18:16','2025-10-21 21:14:21'),(3,1,'Tablet','3',3,1,'2025-10-21 18:18:16','2025-10-21 21:14:21'),(4,1,'Audio','4',4,1,'2025-10-21 18:18:16','2025-10-21 21:14:21'),(5,1,'Wearable','5',5,1,'2025-10-21 18:18:16','2025-10-21 21:14:21'),(6,2,'待支付','0',1,1,'2025-10-21 18:18:16','2025-10-21 21:14:21'),(7,2,'已支付','1',2,1,'2025-10-21 18:18:16','2025-10-21 21:14:21'),(8,2,'已发货','2',3,1,'2025-10-21 18:18:16','2025-10-21 21:14:21'),(9,2,'完成','3',4,1,'2025-10-21 18:18:16','2025-10-21 21:14:21'),(10,2,'已取消','-1',5,1,'2025-10-21 18:18:16','2025-10-21 21:14:21'),(31,8,'上架','1',1,1,'2025-12-08 16:20:57','2025-12-08 16:20:57'),(33,8,'下架','0',2,1,'2025-12-08 16:23:24','2025-12-08 16:23:24'),(34,9,'计划中','1',1,1,'2025-12-08 16:34:28','2025-12-08 16:34:28'),(35,9,'生产中','2',2,1,'2025-12-08 16:34:44','2025-12-08 16:34:44'),(36,9,'完成','3',3,1,'2025-12-08 16:34:53','2025-12-08 16:34:53'),(37,9,'取消','4',4,1,'2025-12-08 16:35:03','2025-12-08 16:35:03');
/*!40000 ALTER TABLE `sys_dict_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_material`
--

DROP TABLE IF EXISTS `sys_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_material` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `material_code` varchar(50) NOT NULL,
  `material_name` varchar(100) NOT NULL,
  `material_type` tinyint NOT NULL,
  `required_materials` text,
  `required_time` decimal(10,2) DEFAULT NULL,
  `description` text,
  `status` tinyint DEFAULT '1',
  `unit` varchar(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `material_code` (`material_code`),
  UNIQUE KEY `uk_material_code` (`material_code`),
  KEY `idx_material_type` (`material_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_material`
--

LOCK TABLES `sys_material` WRITE;
/*!40000 ALTER TABLE `sys_material` DISABLE KEYS */;
INSERT INTO `sys_material` VALUES (10,'MAT-RAW-003','塑料颗粒',1,NULL,0.00,NULL,1,'吨','2025-12-06 18:02:16','2025-12-06 18:02:30'),(11,'MAT-RAW-004','沙料',1,NULL,0.00,NULL,1,'吨','2025-12-06 18:02:16','2025-12-06 18:02:30'),(12,'MAT-RAW-005','石料',1,NULL,0.00,NULL,1,'吨','2025-12-06 18:02:16','2025-12-06 18:02:30'),(13,'MAT-RAW-006','铜材',1,NULL,0.00,NULL,1,'吨','2025-12-06 18:02:16','2025-12-06 18:02:30'),(14,'MAT-SEMI-004','金属元器件',2,'10*1/17*1',1.00,NULL,1,'箱','2025-12-06 18:02:16','2025-12-06 18:02:30'),(15,'MAT-SEMI-005','PCB电路板',2,'18*1/17*2/16*1/14*2',2.10,NULL,1,'套','2025-12-06 18:02:16','2025-12-06 18:02:30'),(16,'MAT-SEMI-006','铜箔',2,'13*1',0.40,NULL,1,'张','2025-12-06 18:02:16','2025-12-06 18:02:30'),(17,'MAT-SEMI-007','铜丝',2,'13*2',1.20,NULL,1,'卷','2025-12-06 18:02:16','2025-12-06 18:02:30'),(18,'MAT-SEMI-008','电路塑料板',2,'10*2',1.10,NULL,1,'张','2025-12-06 18:02:16','2025-12-06 18:02:30'),(19,'MAT-FIN-005','耳机',3,'10*1/17*1/15*1/14*1',1.30,NULL,1,'个','2025-12-06 18:02:16','2025-12-06 18:02:30'),(21,'MAT-FIN-007','水泥',3,'11*3/12*1',2.20,NULL,1,'吨','2025-12-06 18:02:16','2025-12-06 18:02:30');
/*!40000 ALTER TABLE `sys_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图标',
  `parent_id` bigint DEFAULT NULL COMMENT '父级ID',
  `sort` int DEFAULT '0' COMMENT '排序',
  `hidden` tinyint(1) DEFAULT '0' COMMENT '是否隐藏',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,'首页','/dashboard','dashboard/index.vue','House',0,1,0,'2025-10-25 18:17:45','2025-10-25 18:21:36'),(2,'系统管理','/system',NULL,'Setting',0,2,0,'2025-10-25 18:17:45','2025-10-25 18:23:27'),(3,'用户管理','/system/user','system/user/index.vue',NULL,2,1,0,'2025-10-25 18:17:45','2025-10-25 18:23:27'),(4,'菜单管理','/system/menu','system/menu/index.vue',NULL,2,2,0,'2025-10-25 18:17:45','2025-10-25 18:23:27'),(5,'角色管理','/system/role','system/role/index.vue',NULL,2,3,0,'2025-10-25 18:17:45','2025-10-25 18:23:27'),(6,'商品管理','/product/index','product/index.vue','Goods',0,4,0,'2025-10-25 18:17:45','2025-12-04 12:20:26'),(7,'订单管理','/order/index','order/index.vue','List',0,5,0,'2025-10-25 18:17:45','2025-12-04 12:20:26'),(18,'字典管理','/system/dict','system/dict/index.vue',NULL,2,4,0,'2025-10-25 18:35:51','2025-10-25 18:35:51'),(19,'供应商管理','/supplier/index','supplier/index.vue','Shop',0,6,0,'2025-10-25 18:35:51','2025-12-04 12:20:26'),(20,'销售额管理','/sales/index','sales/index.vue','Money',0,7,0,'2025-10-25 18:35:51','2025-12-04 12:20:26'),(24,'库存管理','/inventory/index','inventory/index.vue','Box',0,8,0,'2025-11-11 20:59:36','2025-12-04 12:20:26'),(27,'生产计划管理','/production/index','production/index.vue','DataAnalysis',0,10,0,'2025-11-22 15:28:06','2025-12-04 12:20:26'),(28,'物料管理','/material/index','material/index.vue','Management',0,9,0,'2025-11-22 16:53:06','2025-12-04 12:20:26'),(29,'商品选购','/product/selection','product/selection.vue','ShoppingCart',0,3,0,'2025-12-04 12:20:26','2025-12-04 12:23:05');
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_order`
--

DROP TABLE IF EXISTS `sys_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态：-1已取消，0待支付，1已支付，2已发货，3已完成',
  `address` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货地址',
  `consignee` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货人',
  `phone` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系电话',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单备注',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_no_deleted` (`order_no`,`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_order`
--

LOCK TABLES `sys_order` WRITE;
/*!40000 ALTER TABLE `sys_order` DISABLE KEYS */;
INSERT INTO `sys_order` VALUES (1,'SYS202510210001',2,17997.00,2,'Address 100 Tech Rd','Zhang San','13900139001','Urgent order',NULL,NULL,NULL,'2025-09-19 21:02:33','2025-12-14 16:10:49',0),(2,'SYS202510210002',2,19995.00,2,'Address 1 Century Ave','Li Si','13900139002','Please ship soon',NULL,NULL,NULL,'2025-10-20 21:02:33','2025-12-14 16:11:08',0),(3,'SYS202510210003',35,3597.00,2,'Address 10 Pearl River','Wang Wu','13900139003',NULL,NULL,NULL,NULL,'2025-10-27 21:02:33','2025-12-14 16:17:27',0),(49,'SYS202510210007',1,120.00,0,'ihcihiujhoasidjpahsd','aaa','18918918989',NULL,NULL,NULL,NULL,'2025-11-08 18:02:56','2025-11-08 18:02:56',0),(55,'ORD-1764867195501-6599',2,469.00,1,'qwryduuygi','qqqq','12312312334',NULL,NULL,NULL,NULL,'2025-12-05 00:53:30','2025-12-05 00:54:03',0);
/*!40000 ALTER TABLE `sys_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_order_item`
--

DROP TABLE IF EXISTS `sys_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `price` decimal(10,2) NOT NULL COMMENT '商品单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_amount` decimal(10,2) NOT NULL COMMENT '小计金额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `product_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单商品图片',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_order_item`
--

LOCK TABLES `sys_order_item` WRITE;
/*!40000 ALTER TABLE `sys_order_item` DISABLE KEYS */;
INSERT INTO `sys_order_item` VALUES (1,1,1,'笔记本电脑',5999.00,3,17997.00,'2025-10-21 14:16:58','2025-12-14 16:07:06','/upload/product/2025/11/10/950608ae-b606-4d89-b1b7-7b41064fb543.jpg'),(2,2,2,'手机',3999.00,5,19995.00,'2025-10-21 18:16:59','2025-12-14 16:07:06','/upload/product/2025/11/10/26a9ea3b-2071-41b1-aa43-0e6db2264852.jpg'),(3,3,3,'平板电脑',2999.00,1,2999.00,'2025-10-21 18:16:59','2025-11-16 15:18:33','/upload/product/2025/11/10/36bb92d0-43cd-4017-b5ad-306eb0d7fb63.jpg'),(4,3,4,'耳机',299.00,2,598.00,'2025-10-21 18:16:59','2025-12-14 16:07:26','/upload/product/2025/11/10/d1841f64-50cc-4800-a0b3-4e4c9310047c.jpg'),(23,49,6,'纸巾',20.00,6,120.00,'2025-11-08 18:02:56','2025-11-16 15:14:14','/upload/product/2025/11/10/2acd1c28-2598-472a-a8cb-f9408d3874ef.jpg'),(30,55,28,'游戏键盘',399.00,1,399.00,'2025-12-05 00:53:30','2025-12-05 00:53:30','/upload/product/2025/11/09/f972151e-9fe2-470a-8bd7-40b82e20ff7b.jpg'),(31,55,6,'纸巾',20.00,1,20.00,'2025-12-05 00:53:30','2025-12-05 00:53:30','/upload/product/2025/11/10/2acd1c28-2598-472a-a8cb-f9408d3874ef.jpg'),(32,55,7,'台灯',50.00,1,50.00,'2025-12-05 00:53:30','2025-12-05 00:53:30','/upload/product/2025/11/10/90931dd1-a972-49b7-bf59-7f816a311042.jpg');
/*!40000 ALTER TABLE `sys_order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_product`
--

DROP TABLE IF EXISTS `sys_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '物品名称',
  `code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物品编码',
  `category` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品分类',
  `price` decimal(10,2) DEFAULT NULL COMMENT '价格',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `status` tinyint DEFAULT '0' COMMENT '销售状态：0-下架，1-上架',
  `image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物品图片',
  `description` text COLLATE utf8mb4_general_ci COMMENT '商品描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `library_coding` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '库位编码',
  `forbidden` int NOT NULL DEFAULT '1' COMMENT '禁用状态：0-禁用，1-启用',
  `property` int NOT NULL COMMENT '物品属性：1-原料，2-半成品，3-成品',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_product`
--

LOCK TABLES `sys_product` WRITE;
/*!40000 ALTER TABLE `sys_product` DISABLE KEYS */;
INSERT INTO `sys_product` VALUES (1,'笔记本电脑','NB001','电子产品',5999.00,80,1,'/upload/product/2025/11/10/950608ae-b606-4d89-b1b7-7b41064fb543.jpg','高性能笔记本电脑','2025-10-25 17:36:18','2025-12-04 00:13:04','GD001-002-003',1,3),(2,'手机','PH001','电子产品',3999.00,200,1,'/upload/product/2025/11/10/26a9ea3b-2071-41b1-aa43-0e6db2264852.jpg','5G 智能手机','2025-10-21 21:02:33','2025-12-03 23:35:21','JS001-002-003',1,3),(3,'平板电脑','TB001','电子产品',2999.00,300,1,'/upload/product/2025/11/10/36bb92d0-43cd-4017-b5ad-306eb0d7fb63.jpg','轻巧平板电脑','2025-10-21 21:02:33','2025-12-03 23:35:22','GX001-002-003',1,3),(4,'耳机','HD001','电子产品',299.00,400,1,'/upload/product/2025/11/10/d1841f64-50cc-4800-a0b3-4e4c9310047c.jpg','高清耳机','2025-10-21 21:02:33','2025-12-03 23:35:22','XZ001-002-003',1,3),(5,'游戏鼠标','P006','配件',299.00,500,1,'/upload/product/2025/11/10/a952af7d-56c6-42d5-8c80-6caeb4ab3060.jpg','高精度游戏鼠标','2025-10-21 18:15:55','2025-12-03 23:35:24','FJ001-002-003',1,3),(6,'纸巾','ZJ001','家用',20.00,600,1,'/upload/product/2025/11/10/2acd1c28-2598-472a-a8cb-f9408d3874ef.jpg','强吸水纸巾','2025-10-25 18:15:55','2025-12-05 00:54:26','HN001-002-003',1,3),(7,'台灯','TD001','家居',50.00,700,1,'/upload/product/2025/11/10/90931dd1-a972-49b7-bf59-7f816a311042.jpg','护眼台灯','2025-10-25 18:15:55','2025-12-05 00:54:26','HB001-002-003',1,3),(28,'游戏键盘','JP001','有线键盘',399.00,800,1,'/upload/product/2025/11/09/f972151e-9fe2-470a-8bd7-40b82e20ff7b.jpg','机械键盘','2025-10-26 22:12:47','2025-12-05 00:54:26','GS001-002-003',1,3),(29,'木材','MC001','',0.00,40,0,'','木制品原料','2025-10-26 22:12:47','2025-12-10 12:35:06','HLJ001-002-003',1,1),(30,'木板','MB001','',NULL,20,0,'','木制品中间产物','2025-11-12 18:37:24','2025-11-29 17:43:09','SH001-002-003',1,2),(35,'木浆','MJ001','',0.00,20,0,'','','2025-11-25 23:13:47','2025-12-10 15:35:10','HN001-002-003',1,2),(36,'钢材','GC001',NULL,NULL,234,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','HB001-002-003',1,1),(37,'钢板','GB001',NULL,NULL,234,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','GS001-002-003',1,2),(38,'木桌','MZ001','家居',66.00,546,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-10 19:48:02','HN001-002-003',1,3),(39,'铁钉','TD002','建筑材料',2.00,14,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','HB001-002-003',1,3),(40,'木楔子','MXZ001','建筑材料',5.00,36,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','FJ001-002-003',1,3),(41,'定制模具','DZMJ001','工具',1000.00,135,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','HN001-002-003',1,3),(42,'塑料颗粒','SLKL001','',0.00,100,0,'','','2025-12-03 23:36:25','2025-12-10 11:53:31','HN001-002-003',1,1),(43,'沙料','SL001',NULL,NULL,245,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','HB001-002-003',1,1),(44,'石料','SL002',NULL,NULL,564,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','GS001-002-003',1,1),(45,'铜材','TC001',NULL,NULL,67,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','FJ001-002-003',1,1),(46,'金属元器件','JSSQJ001',NULL,NULL,110,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-09 23:55:48','HN001-002-003',1,2),(47,'PCB电路板','PCBDLB001',NULL,NULL,14,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','HB001-002-003',1,2),(48,'铜箔','TB002',NULL,NULL,124,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','FJ001-002-003',1,2),(49,'铜丝','TS001','',0.00,100,0,'','','2025-12-03 23:36:25','2025-12-10 12:16:27','HN001-002-003',1,2),(50,'电路塑料板','DLSLB001',NULL,NULL,120,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','HB001-002-003',1,2),(52,'木桶','MT001','家居',20.00,25,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','GS001-002-003',1,3),(53,'水泥','SN001','建筑材料',100.00,356,0,NULL,NULL,'2025-12-03 23:36:25','2025-12-03 23:53:51','HLJ001-002-003',1,3);
/*!40000 ALTER TABLE `sys_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_production_plan`
--

DROP TABLE IF EXISTS `sys_production_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_production_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_name` varchar(100) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `required_materials` text NOT NULL,
  `production_time` decimal(10,2) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` tinyint DEFAULT '1' COMMENT '1:计划中, 2:生产中, 3:完成, 4:取消',
  `start_time` datetime DEFAULT NULL,
  `expected_end_time` datetime DEFAULT NULL,
  `product_quantity` int NOT NULL DEFAULT '1' COMMENT '生产的产物个数',
  `product_property` int DEFAULT '3' COMMENT 'Product property: 2=Semi-finished, 3=Finished product',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_expected_end_time` (`expected_end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='生产计划表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_production_plan`
--

LOCK TABLES `sys_production_plan` WRITE;
/*!40000 ALTER TABLE `sys_production_plan` DISABLE KEYS */;
INSERT INTO `sys_production_plan` VALUES (8,'金属元器件-5-20251206181721','金属元器件','10*5/17*5',5.00,'2025-12-06 18:17:30','2025-12-09 23:55:48',3,'2025-12-09 23:19:59','2025-12-10 04:19:59',5,2);
/*!40000 ALTER TABLE `sys_production_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '角色描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'admin','ADMIN','企业管理员',1,'2025-10-21 21:02:33','2025-12-08 21:43:46'),(2,'seller','SELLER','销售员',1,'2025-10-21 21:02:33','2025-12-02 18:42:10'),(3,'inventoryManager','InventoryManager','库存管理员',1,'2025-10-21 21:02:33','2025-12-02 18:56:10'),(23,'user','USER','客户',1,'2025-10-25 14:57:49','2025-12-05 00:59:37'),(24,'financialStaff','FinancialStaff','财务人员',1,'2025-12-07 16:51:45','2025-12-07 16:51:45'),(25,'productionManager','ProductionManager','生产管理员',1,'2025-12-07 16:53:18','2025-12-07 16:53:18'),(26,'purchaser','PURCHASER','采购人员',1,'2025-12-07 17:09:39','2025-12-07 17:09:39');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_menu`
--

DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=176 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu`
--

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (143,1,1,'2025-12-04 12:20:42'),(144,1,2,'2025-12-04 12:20:42'),(145,1,3,'2025-12-04 12:20:42'),(146,1,4,'2025-12-04 12:20:42'),(147,1,5,'2025-12-04 12:20:42'),(148,1,18,'2025-12-04 12:20:42'),(149,1,29,'2025-12-04 12:20:42'),(150,1,6,'2025-12-04 12:20:42'),(151,1,7,'2025-12-04 12:20:42'),(152,1,19,'2025-12-04 12:20:42'),(153,1,20,'2025-12-04 12:20:42'),(154,1,24,'2025-12-04 12:20:42'),(155,1,28,'2025-12-04 12:20:42'),(156,1,27,'2025-12-04 12:20:42'),(157,23,29,'2025-12-05 00:59:49'),(158,2,1,'2025-12-07 16:55:13'),(159,2,6,'2025-12-07 16:55:13'),(160,2,7,'2025-12-07 16:55:13'),(161,2,20,'2025-12-07 16:55:13'),(166,25,28,'2025-12-07 17:02:12'),(167,25,27,'2025-12-07 17:02:12'),(168,24,1,'2025-12-07 17:03:20'),(169,24,7,'2025-12-07 17:03:20'),(170,24,20,'2025-12-07 17:03:20'),(173,3,24,'2025-12-07 17:10:27'),(174,26,19,'2025-12-07 17:10:31'),(175,26,24,'2025-12-07 17:10:31');
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_supplier`
--

DROP TABLE IF EXISTS `sys_supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称',
  `code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商编码',
  `contact` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人',
  `phone` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系电话',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地址',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=151 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='供应商表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_supplier`
--

LOCK TABLES `sys_supplier` WRITE;
/*!40000 ALTER TABLE `sys_supplier` DISABLE KEYS */;
INSERT INTO `sys_supplier` VALUES (1,'1有限公司','gys001','Sir1','12312312312',NULL,1,'2025-11-25 21:30:31','2025-12-08 16:45:07','email001@gmail.com','a1'),(5,'2有限公司','gys002','Sir2','19019019019',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email002@gmail.com','a2'),(6,'3有限公司','gys003','Sir3','19019019020',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email003@gmail.com','a3'),(7,'4有限公司','gys004','Sir4','19019019021',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email004@gmail.com','a4'),(8,'5有限公司','gys005','Sir5','19019019022',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email005@gmail.com','a5'),(9,'6有限公司','gys006','Sir6','19019019023',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email006@gmail.com','a6'),(10,'7有限公司','gys007','Sir7','19019019024',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email007@gmail.com','a7'),(11,'8有限公司','gys008','Sir8','19019019025',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email008@gmail.com','a8'),(12,'9有限公司','gys009','Sir9','19019019026',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email009@gmail.com','a9'),(13,'10有限公司','gys010','Sir10','19019019027',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email010@gmail.com','a10'),(14,'11有限公司','gys011','Sir11','19019019028',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:05','email011@gmail.com','a11'),(15,'12有限公司','gys012','Sir12','19019019029',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email012@gmail.com','a12'),(16,'13有限公司','gys013','Sir13','19019019030',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email013@gmail.com','a13'),(17,'14有限公司','gys014','Sir14','19019019031',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email014@gmail.com','a14'),(18,'15有限公司','gys015','Sir15','19019019032',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email015@gmail.com','a15'),(19,'16有限公司','gys016','Sir16','19019019033',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email016@gmail.com','a16'),(20,'17有限公司','gys017','Sir17','19019019034',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email017@gmail.com','a17'),(21,'18有限公司','gys018','Sir18','19019019035',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email018@gmail.com','a18'),(22,'19有限公司','gys019','Sir19','19019019036',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:04','email019@gmail.com','a19'),(23,'20有限公司','gys020','Sir20','19019019037',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email020@gmail.com','a20'),(24,'21有限公司','gys021','Sir21','19019019038',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email021@gmail.com','a21'),(25,'22有限公司','gys022','Sir22','19019019039',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email022@gmail.com','a22'),(26,'23有限公司','gys023','Sir23','19019019040',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email023@gmail.com','a23'),(27,'24有限公司','gys024','Sir24','19019019041',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email024@gmail.com','a24'),(28,'25有限公司','gys025','Sir25','19019019042',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email025@gmail.com','a25'),(29,'26有限公司','gys026','Sir26','19019019043',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email026@gmail.com','a26'),(30,'27有限公司','gys027','Sir27','19019019044',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email027@gmail.com','a27'),(31,'28有限公司','gys028','Sir28','19019019045',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email028@gmail.com','a28'),(32,'29有限公司','gys029','Sir29','19019019046',NULL,0,'2024-06-05 21:30:31','2025-12-03 22:23:05','email029@gmail.com','a29'),(33,'30有限公司','gys030','Sir30','19019019047',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email030@gmail.com','a30'),(34,'31有限公司','gys031','Sir31','19019019048',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email031@gmail.com','a31'),(35,'32有限公司','gys032','Sir32','19019019049',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email032@gmail.com','a32'),(36,'33有限公司','gys033','Sir33','19019019050',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email033@gmail.com','a33'),(37,'34有限公司','gys034','Sir34','19019019051',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email034@gmail.com','a34'),(38,'35有限公司','gys035','Sir35','19019019052',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email035@gmail.com','a35'),(39,'36有限公司','gys036','Sir36','19019019053',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email036@gmail.com','a36'),(40,'37有限公司','gys037','Sir37','19019019054',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email037@gmail.com','a37'),(41,'38有限公司','gys038','Sir38','19019019055',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email038@gmail.com','a38'),(42,'39有限公司','gys039','Sir39','19019019056',NULL,0,'2025-09-25 21:30:31','2025-12-03 22:23:05','email039@gmail.com','a39'),(43,'40有限公司','gys040','Sir40','19019019057',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email040@gmail.com','a40'),(44,'41有限公司','gys041','Sir41','19019019058',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email041@gmail.com','a41'),(45,'42有限公司','gys042','Sir42','19019019059',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email042@gmail.com','a42'),(46,'43有限公司','gys043','Sir43','19019019060',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email043@gmail.com','a43'),(47,'44有限公司','gys044','Sir44','19019019061',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email044@gmail.com','a44'),(48,'45有限公司','gys045','Sir45','19019019062',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email045@gmail.com','a45'),(49,'46有限公司','gys046','Sir46','19019019063',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email046@gmail.com','a46'),(50,'47有限公司','gys047','Sir47','19019019064',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:05','email047@gmail.com','a47'),(51,'48有限公司','gys048','Sir48','19019019065',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email048@gmail.com','a48'),(52,'49有限公司','gys049','Sir49','19019019066',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email049@gmail.com','a49'),(53,'50有限公司','gys050','Sir50','19019019067',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email050@gmail.com','a50'),(54,'51有限公司','gys051','Sir51','19019019068',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email051@gmail.com','a51'),(55,'52有限公司','gys052','Sir52','19019019069',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email052@gmail.com','a52'),(56,'53有限公司','gys053','Sir53','19019019070',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email053@gmail.com','a53'),(57,'54有限公司','gys054','Sir54','19019019071',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email054@gmail.com','a54'),(58,'55有限公司','gys055','Sir55','19019019072',NULL,0,'2024-06-05 21:30:31','2025-12-03 22:23:04','email055@gmail.com','a55'),(59,'56有限公司','gys056','Sir56','19019019073',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email056@gmail.com','a56'),(60,'57有限公司','gys057','Sir57','19019019074',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email057@gmail.com','a57'),(61,'58有限公司','gys058','Sir58','19019019075',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email058@gmail.com','a58'),(62,'59有限公司','gys059','Sir59','19019019076',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email059@gmail.com','a59'),(63,'60有限公司','gys060','Sir60','19019019077',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email060@gmail.com','a60'),(64,'61有限公司','gys061','Sir61','19019019078',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email061@gmail.com','a61'),(65,'62有限公司','gys062','Sir62','19019019079',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email062@gmail.com','a62'),(66,'63有限公司','gys063','Sir63','19019019080',NULL,0,'2025-09-25 21:30:31','2025-12-03 22:23:05','email063@gmail.com','a63'),(67,'64有限公司','gys064','Sir64','19019019081',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email064@gmail.com','a64'),(68,'65有限公司','gys065','Sir65','19019019082',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email065@gmail.com','a65'),(69,'66有限公司','gys066','Sir66','19019019083',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email066@gmail.com','a66'),(70,'67有限公司','gys067','Sir67','19019019084',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email067@gmail.com','a67'),(71,'68有限公司','gys068','Sir68','19019019085',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email068@gmail.com','a68'),(72,'69有限公司','gys069','Sir69','19019019086',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email069@gmail.com','a69'),(73,'70有限公司','gys070','Sir70','19019019087',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email070@gmail.com','a70'),(74,'71有限公司','gys071','Sir71','19019019088',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email071@gmail.com','a71'),(75,'72有限公司','gys072','Sir72','19019019089',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:04','email072@gmail.com','a72'),(76,'73有限公司','gys073','Sir73','19019019090',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email073@gmail.com','a73'),(77,'74有限公司','gys074','Sir74','19019019091',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email074@gmail.com','a74'),(78,'75有限公司','gys075','Sir75','19019019092',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email075@gmail.com','a75'),(79,'76有限公司','gys076','Sir76','19019019093',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email076@gmail.com','a76'),(80,'77有限公司','gys077','Sir77','19019019094',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email077@gmail.com','a77'),(81,'78有限公司','gys078','Sir78','19019019095',NULL,0,'2024-06-05 21:30:31','2025-12-03 22:23:04','email078@gmail.com','a78'),(82,'79有限公司','gys079','Sir79','19019019096',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email079@gmail.com','a79'),(83,'80有限公司','gys080','Sir80','19019019097',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email080@gmail.com','a80'),(84,'81有限公司','gys081','Sir81','19019019098',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email081@gmail.com','a81'),(85,'82有限公司','gys082','Sir82','19019019099',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email082@gmail.com','a82'),(86,'83有限公司','gys083','Sir83','19019019100',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email083@gmail.com','a83'),(87,'84有限公司','gys084','Sir84','19019019101',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email084@gmail.com','a84'),(88,'85有限公司','gys085','Sir85','19019019102',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email085@gmail.com','a85'),(89,'86有限公司','gys086','Sir86','19019019103',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email086@gmail.com','a86'),(90,'87有限公司','gys087','Sir87','19019019104',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:04','email087@gmail.com','a87'),(91,'88有限公司','gys088','Sir88','19019019105',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:04','email088@gmail.com','a88'),(92,'89有限公司','gys089','Sir89','19019019106',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:05','email089@gmail.com','a89'),(93,'90有限公司','gys090','Sir90','19019019107',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email090@gmail.com','a90'),(94,'91有限公司','gys091','Sir91','19019019108',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email091@gmail.com','a91'),(95,'92有限公司','gys092','Sir92','19019019109',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email092@gmail.com','a92'),(96,'93有限公司','gys093','Sir93','19019019110',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email093@gmail.com','a93'),(97,'94有限公司','gys094','Sir94','19019019111',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email094@gmail.com','a94'),(98,'95有限公司','gys095','Sir95','19019019112',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email095@gmail.com','a95'),(99,'96有限公司','gys096','Sir96','19019019113',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email096@gmail.com','a96'),(100,'97有限公司','gys097','Sir97','19019019114',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email097@gmail.com','a97'),(101,'98有限公司','gys098','Sir98','19019019115',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email098@gmail.com','a98'),(102,'99有限公司','gys099','Sir99','19019019116',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:04','email099@gmail.com','a99'),(103,'100有限公司','gys100','Sir100','19019019117',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email100@gmail.com','a100'),(104,'101有限公司','gys101','Sir101','19019019118',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email101@gmail.com','a101'),(105,'102有限公司','gys102','Sir102','19019019119',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email102@gmail.com','a102'),(106,'103有限公司','gys103','Sir103','19019019120',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email103@gmail.com','a103'),(107,'104有限公司','gys104','Sir104','19019019121',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email104@gmail.com','a104'),(108,'105有限公司','gys105','Sir105','19019019122',NULL,0,'2024-06-05 21:30:31','2025-12-03 22:23:05','email105@gmail.com','a105'),(109,'106有限公司','gys106','Sir106','19019019123',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email106@gmail.com','a106'),(110,'107有限公司','gys107','Sir107','19019019124',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email107@gmail.com','a107'),(111,'108有限公司','gys108','Sir108','19019019125',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email108@gmail.com','a108'),(112,'109有限公司','gys109','Sir109','19019019126',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email109@gmail.com','a109'),(113,'110有限公司','gys110','Sir110','19019019127',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email110@gmail.com','a110'),(114,'111有限公司','gys111','Sir111','19019019128',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email111@gmail.com','a111'),(115,'112有限公司','gys112','Sir112','19019019129',NULL,0,'2025-09-25 21:30:31','2025-12-03 22:23:04','email112@gmail.com','a112'),(116,'113有限公司','gys113','Sir113','19019019130',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email113@gmail.com','a113'),(117,'114有限公司','gys114','Sir114','19019019131',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email114@gmail.com','a114'),(118,'115有限公司','gys115','Sir115','19019019132',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email115@gmail.com','a115'),(119,'116有限公司','gys116','Sir116','19019019133',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email116@gmail.com','a116'),(120,'117有限公司','gys117','Sir117','19019019134',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email117@gmail.com','a117'),(121,'118有限公司','gys118','Sir118','19019019135',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:04','email118@gmail.com','a118'),(122,'119有限公司','gys119','Sir119','19019019136',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email119@gmail.com','a119'),(123,'120有限公司','gys120','Sir120','19019019137',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email120@gmail.com','a120'),(124,'121有限公司','gys121','Sir121','19019019138',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email121@gmail.com','a121'),(125,'122有限公司','gys122','Sir122','19019019139',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email122@gmail.com','a122'),(126,'123有限公司','gys123','Sir123','19019019140',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:05','email123@gmail.com','a123'),(127,'124有限公司','gys124','Sir124','19019019141',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email124@gmail.com','a124'),(128,'125有限公司','gys125','Sir125','19019019142',NULL,1,'2024-06-05 21:30:31','2025-12-03 22:23:04','email125@gmail.com','a125'),(129,'126有限公司','gys126','Sir126','19019019143',NULL,0,'2024-06-05 21:30:31','2025-12-03 22:23:04','email126@gmail.com','a126'),(130,'127有限公司','gys127','Sir127','19019019144',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email127@gmail.com','a127'),(131,'128有限公司','gys128','Sir128','19019019145',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email128@gmail.com','a128'),(132,'129有限公司','gys129','Sir129','19019019146',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email129@gmail.com','a129'),(133,'130有限公司','gys130','Sir130','19019019147',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email130@gmail.com','a130'),(134,'131有限公司','gys131','Sir131','19019019148',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email131@gmail.com','a131'),(135,'132有限公司','gys132','Sir132','19019019149',NULL,0,'2025-09-25 21:30:31','2025-12-03 22:23:04','email132@gmail.com','a132'),(136,'133有限公司','gys133','Sir133','19019019150',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email133@gmail.com','a133'),(137,'134有限公司','gys134','Sir134','19019019151',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email134@gmail.com','a134'),(138,'135有限公司','gys135','Sir135','19019019152',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email135@gmail.com','a135'),(139,'136有限公司','gys136','Sir136','19019019153',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email136@gmail.com','a136'),(140,'137有限公司','gys137','Sir137','19019019154',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email137@gmail.com','a137'),(141,'138有限公司','gys138','Sir138','19019019155',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email138@gmail.com','a138'),(142,'139有限公司','gys139','Sir139','19019019156',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:04','email139@gmail.com','a139'),(143,'140有限公司','gys140','Sir140','19019019157',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email140@gmail.com','a140'),(144,'141有限公司','gys141','Sir141','19019019158',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:05','email141@gmail.com','a141'),(145,'142有限公司','gys142','Sir142','19019019159',NULL,1,'2025-11-25 21:30:31','2025-12-03 22:23:04','email142@gmail.com','a142'),(146,'143有限公司','gys143','Sir143','19019019160',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email143@gmail.com','a143'),(147,'144有限公司','gys144','Sir144','19019019161',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:04','email144@gmail.com','a144'),(148,'145有限公司','gys145','Sir145','19019019162',NULL,1,'2025-09-25 21:30:31','2025-12-03 22:23:05','email145@gmail.com','a145'),(149,'146有限公司','gys146','Sir146','19019019163',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:04','email146@gmail.com','a146'),(150,'147有限公司','gys147','Sir147','19019019164',NULL,0,'2025-11-25 21:30:31','2025-12-03 22:23:05','email147@gmail.com','a147');
/*!40000 ALTER TABLE `sys_supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_supplier_item`
--

DROP TABLE IF EXISTS `sys_supplier_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_supplier_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品唯一ID',
  `supplier_id` bigint NOT NULL COMMENT '?????D',
  `product_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '??????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '??????',
  `property` int NOT NULL COMMENT '产品属性：1-原料，2-半成品',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_product` (`supplier_id`,`product_name`),
  CONSTRAINT `fk_supplier_detail_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `sys_supplier` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='供应商明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_supplier_item`
--

LOCK TABLES `sys_supplier_item` WRITE;
/*!40000 ALTER TABLE `sys_supplier_item` DISABLE KEYS */;
INSERT INTO `sys_supplier_item` VALUES (10,1,'木材','2025-12-03 22:48:35','2025-12-03 22:48:35',1),(11,1,'钢材','2025-12-03 22:48:59','2025-12-03 22:48:59',1),(12,7,'木板','2025-12-03 22:49:16','2025-12-03 22:49:16',2),(13,7,'钢板','2025-12-03 22:49:23','2025-12-03 22:49:23',2),(14,47,'木浆','2025-12-03 22:49:57','2025-12-03 22:49:57',2),(15,48,'塑料颗粒','2025-12-03 22:50:19','2025-12-03 22:50:19',1),(16,48,'木材','2025-12-03 22:50:34','2025-12-03 22:50:34',1),(17,5,'钢材','2025-12-03 22:50:45','2025-12-03 22:50:45',1),(18,49,'沙料','2025-12-03 22:51:04','2025-12-03 22:51:04',1),(19,49,'石料','2025-12-03 22:51:12','2025-12-03 22:51:12',1),(20,50,'沙料','2025-12-03 22:51:27','2025-12-03 22:51:27',1),(21,144,'塑料颗粒','2025-12-03 22:51:48','2025-12-03 22:51:48',1),(22,144,'铜材','2025-12-03 22:52:03','2025-12-03 22:52:03',1),(23,127,'金属元器件','2025-12-03 22:52:23','2025-12-03 22:52:23',2),(24,127,'PCB电路板','2025-12-03 22:52:36','2025-12-03 22:52:36',2),(25,127,'铜箔','2025-12-03 22:52:47','2025-12-03 22:52:47',2),(26,148,'PCB电路板','2025-12-03 22:53:22','2025-12-03 22:53:22',2),(27,148,'铜丝','2025-12-03 22:53:29','2025-12-03 22:53:29',2),(28,83,'电路塑料板','2025-12-03 22:53:53','2025-12-03 22:53:53',2),(29,83,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 22:54:04',1),(31,118,'木材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(32,135,'木板','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(33,129,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(35,134,'钢板','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(36,118,'钢板','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(37,120,'钢材','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(38,118,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(39,117,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(40,105,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(41,111,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(42,130,'钢板','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(43,109,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(44,137,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(45,139,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(46,132,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(47,123,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(48,106,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(50,111,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(51,110,'钢材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(52,102,'木板','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(53,123,'木板','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(55,109,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(56,108,'钢材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(57,111,'木材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(58,108,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(59,130,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(60,122,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(61,123,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(62,104,'木材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(63,128,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(64,120,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(65,103,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(66,112,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(67,102,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(68,116,'木板','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(69,124,'钢板','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(70,134,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(71,114,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(72,135,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(73,107,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(74,112,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(75,110,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(76,124,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(77,127,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(78,126,'木材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(79,139,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(81,135,'木材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(82,135,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(83,102,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(85,104,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(86,113,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(87,112,'木材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(88,136,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(89,131,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(90,101,'PCB电路板','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(91,99,'石料','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(92,91,'PCB电路板','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(93,88,'钢材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(94,81,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(95,77,'木板','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(96,71,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(97,66,'木材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(98,61,'PCB电路板','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(99,55,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(100,51,'铜材','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(101,44,'沙料','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(102,41,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(103,33,'木材','2025-12-03 22:54:04','2025-12-03 23:30:16',2),(104,31,'PCB电路板','2025-12-03 22:54:04','2025-12-03 23:30:16',1),(105,21,'塑料颗粒','2025-12-03 22:54:04','2025-12-03 23:30:16',1);
/*!40000 ALTER TABLE `sys_supplier_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像',
  `role` varchar(50) COLLATE utf8mb4_general_ci DEFAULT 'user' COMMENT '角色',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin001','123456','彭于晏','13800138000','admin1@example.com','/api/upload/2025/11/11/90d2cd68-a398-4b5a-9dfb-07f5b13b291f.jpg?1762792308868?1765367529833?1766244132238','ADMIN',1,'2025-11-11 00:09:12','2025-12-20 23:22:20',0),(2,'seller001','123456','Ning ning','13800138001','seller1@example.com','/api/upload/2025/11/11/ba6b19e7-6a94-4fb8-b1fd-4b9cb5376f67.jpg?1764053275420?1764672404099?1765032548990','SELLER',1,'2025-11-11 00:09:12','2025-12-06 22:49:13',0),(3,'inventoryManager001','123456','李丽','13800138002','inventoryManager1@example.com','/api/upload/2025/11/11/cf5c8f6e-9058-4168-893f-45f5b1f523e6.jpg','InventoryManager',1,'2025-11-11 00:25:41','2025-11-11 00:25:41',0),(4,'user001','123456','张伟','13800138003','user1@example.com','/api/upload/2025/11/11/5c3fd148-9565-4eb6-8cb8-2b4802651af2.png','USER',1,'2025-11-11 00:26:46','2025-11-11 00:26:46',0),(5,'financialStaff001','123456','Giselle','13800138003','financialStaff1@example.com','/api/upload/2025/11/11/1502b335-4f71-4bd4-a27c-04034b7916d4.jpg','FinancialStaff',1,'2025-11-11 00:28:04','2025-11-11 00:28:04',0),(6,'user002','123456','牢吴','13800138004','user2@example.com','/api/upload/2025/11/11/2055801f-8a10-424e-849a-1190f0482d32.png','USER',0,'2025-11-11 00:28:53','2025-11-11 00:28:53',0),(11,'productionManager001','123456','吴彦祖','13800138005','productionManager1@example.com','/api/upload/2025/12/07/91478cdf-6304-4144-8606-66b8b333bc57.png','ProductionManager',1,'2025-11-11 00:28:53','2025-11-11 00:28:53',0),(12,'purchaser001','123456','小黑','13800138006','purchaser1@example.com','/api/upload/2025/12/12/4930dd6d-997b-4cb7-a2d0-c31a743fe044.jpg?1765512125990','PURCHASER',1,'2025-12-12 10:37:02','2025-12-12 12:02:11',0),(13,'test001','123456','test','19009109109','test@qq.com','/api/upload/2025/12/20/2d0d98ce-ab65-4b1b-8e01-2c2fb6857a3e.png','SELLER',1,'2025-12-20 23:14:54','2025-12-20 23:14:54',0);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'scm'
--

--
-- Dumping routines for database 'scm'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-17 20:07:23
