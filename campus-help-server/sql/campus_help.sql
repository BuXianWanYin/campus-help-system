/*
 Navicat Premium Dump SQL

 Source Server         : mysql8
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : campus_help

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 07/12/2025 00:36:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for address
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区县',
  `detail_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `full_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '完整地址（省+市+区+详细地址，用于订单显示）',
  `postal_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮政编码',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认地址：0-否，1-是',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_default`(`user_id` ASC, `is_default` ASC) USING BTREE,
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收货地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of address
-- ----------------------------
INSERT INTO `address` VALUES (1, 4, '测试', '15166568794', '广西', '南宁市', '系襄阳区', '发觉傻瓜卡刷卡噶时光', '广西南宁市系襄阳区发觉傻瓜卡刷卡噶时光', '', 1, 0, '2025-12-05 20:10:23', '2025-12-05 20:10:23');
INSERT INTO `address` VALUES (2, 2, '安抚', '16652356654', '案发时', '案说法', '案说法', '啊萨法说法是案说法啊', '案发时案说法案说法啊萨法说法是案说法啊', '', 1, 0, '2025-12-06 13:35:08', '2025-12-06 13:35:08');

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型：TEXT-文字，IMAGE-图片',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片URL列表（JSON数组）',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_chat_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_message_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 142 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私信消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (1, 1, 2, 4, 'TEXT', '你好', NULL, 1, 0, '2025-12-04 10:38:29', '2025-12-04 10:38:33');
INSERT INTO `chat_message` VALUES (2, 3, 5, 2, 'TEXT', '你好', NULL, 1, 0, '2025-12-04 11:57:12', '2025-12-04 11:57:17');
INSERT INTO `chat_message` VALUES (3, 3, 2, 5, 'TEXT', '哈喽', NULL, 1, 0, '2025-12-04 11:57:22', '2025-12-04 11:57:22');
INSERT INTO `chat_message` VALUES (4, 1, 4, 2, 'TEXT', '多少钱', NULL, 1, 0, '2025-12-05 10:57:29', '2025-12-05 10:57:45');
INSERT INTO `chat_message` VALUES (5, 1, 4, 2, 'TEXT', '谷歌', NULL, 1, 0, '2025-12-05 11:44:08', '2025-12-05 11:44:35');
INSERT INTO `chat_message` VALUES (6, 1, 4, 2, 'GOODS_CARD', '{\"goodsId\":2}', NULL, 1, 0, '2025-12-05 14:36:13', '2025-12-05 17:47:05');
INSERT INTO `chat_message` VALUES (7, 1, 4, 2, 'GOODS_CARD', '{\"goodsId\":2}', NULL, 1, 0, '2025-12-05 14:36:16', '2025-12-05 17:47:05');
INSERT INTO `chat_message` VALUES (8, 1, 4, 2, 'GOODS_CARD', '{\"goodsId\":2}', NULL, 1, 0, '2025-12-05 14:36:57', '2025-12-05 17:47:05');
INSERT INTO `chat_message` VALUES (9, 1, 4, 2, 'GOODS_CARD', '{\"goodsId\":2}', NULL, 1, 0, '2025-12-05 14:37:04', '2025-12-05 17:47:05');
INSERT INTO `chat_message` VALUES (10, 1, 4, 2, 'GOODS_CARD', '{\"goodsId\":2}', NULL, 1, 0, '2025-12-05 17:46:56', '2025-12-05 17:47:05');
INSERT INTO `chat_message` VALUES (11, 1, 4, 2, 'GOODS_CARD', '{\"goodsId\":2}', NULL, 1, 0, '2025-12-05 17:46:58', '2025-12-05 17:47:05');
INSERT INTO `chat_message` VALUES (12, 1, 4, 2, 'GOODS_CARD', '{\"goodsId\":2}', NULL, 1, 0, '2025-12-05 17:47:00', '2025-12-05 17:47:05');
INSERT INTO `chat_message` VALUES (13, 1, 4, 2, 'TEXT', '会话', NULL, 1, 0, '2025-12-05 17:47:23', '2025-12-05 17:47:23');
INSERT INTO `chat_message` VALUES (14, 1, 4, 2, 'GOODS_CARD', '{\"goodsId\":2}', NULL, 1, 0, '2025-12-05 17:47:25', '2025-12-05 17:47:28');
INSERT INTO `chat_message` VALUES (15, 1, 4, 2, 'IMAGE', '哈哈哈', '[\"/uploads/chat/2025/12/05/d0424630-54ad-4f6a-bd54-bfdc2febd287.png\"]', 1, 0, '2025-12-05 17:57:19', '2025-12-05 18:03:58');
INSERT INTO `chat_message` VALUES (16, 1, 4, 2, 'TEXT', '发', NULL, 1, 0, '2025-12-05 17:57:27', '2025-12-05 18:03:58');
INSERT INTO `chat_message` VALUES (17, 1, 4, 2, 'IMAGE', '惺惺惜惺惺哈哈哈哈哈啊哈哈哈\n萨伏啊师傅哈是否', '[\"/uploads/chat/2025/12/05/5533062c-2287-4959-b39b-ae49b3138473.png\"]', 1, 0, '2025-12-05 18:04:31', '2025-12-05 18:04:31');
INSERT INTO `chat_message` VALUES (18, 1, 4, 2, 'TEXT', '按时发放\n啊沙发沙发\n啊沙发沙发\n案说法啊', NULL, 1, 0, '2025-12-05 18:04:38', '2025-12-05 18:04:41');
INSERT INTO `chat_message` VALUES (19, 1, 2, 4, 'TEXT', '拐拐哥', NULL, 1, 0, '2025-12-05 18:22:31', '2025-12-05 18:22:34');
INSERT INTO `chat_message` VALUES (20, 1, 4, 2, 'ORDER_CARD', '{\"orderId\":1,\"orderNo\":\"GO176493662510604\",\"goodsId\":2,\"goodsTitle\":\"二手iphone15paomax\",\"quantity\":1,\"price\":2500,\"totalAmount\":2500,\"shippingFee\":8,\"status\":\"PENDING_PAYMENT\",\"tradeMethod\":\"MAIL\"}', NULL, 1, 0, '2025-12-05 21:26:06', '2025-12-05 21:26:30');
INSERT INTO `chat_message` VALUES (21, 1, 4, 2, 'ORDER_CARD', '{\"orderId\":2,\"orderNo\":\"GO176494307005804\",\"goodsId\":1,\"goodsTitle\":\"二手iphone15paomax\",\"goodsImages\":\"[\\\"/uploads/goods/2025/12/04/7eaa4d16-72df-4d7d-93e6-2aec6431f8b4.jpg\\\"]\",\"quantity\":1,\"price\":2500,\"totalAmount\":2500,\"shippingFee\":0,\"status\":\"COMPLETED\",\"tradeMethod\":\"FACE_TO_FACE\",\"buyerId\":4,\"sellerId\":2}', NULL, 1, 0, '2025-12-05 22:00:08', '2025-12-06 13:33:46');
INSERT INTO `chat_message` VALUES (22, 1, 4, 2, 'ORDER_CARD', '{\"orderId\":1,\"orderNo\":\"GO176493662510604\",\"goodsId\":2,\"goodsTitle\":\"二手iphone15paomax\",\"goodsImages\":\"[\\\"/uploads/goods/2025/12/04/4d85661b-5f86-4718-970a-23083ae6ca35.jpg\\\"]\",\"quantity\":1,\"price\":2400,\"totalAmount\":2400,\"shippingFee\":8,\"status\":\"COMPLETED\",\"tradeMethod\":\"MAIL\",\"buyerId\":4,\"sellerId\":2}', NULL, 1, 0, '2025-12-05 22:00:11', '2025-12-06 13:33:46');
INSERT INTO `chat_message` VALUES (23, 1, 2, 4, 'GOODS_CARD', '{\"goodsId\":4}', NULL, 1, 0, '2025-12-06 13:33:51', '2025-12-06 13:49:56');
INSERT INTO `chat_message` VALUES (24, 1, 2, 4, 'TEXT', '能少点吗', NULL, 1, 0, '2025-12-06 13:33:56', '2025-12-06 13:49:56');
INSERT INTO `chat_message` VALUES (25, 1, 2, 4, 'ORDER_CARD', '{\"orderId\":3,\"orderNo\":\"GO176499928220102\",\"goodsId\":4,\"goodsTitle\":\"无敌梳子\",\"goodsImages\":\"[\\\"/uploads/goods/2025/12/06/ee84f971-2f1b-4564-81b3-710d6e668cde.jpg\\\"]\",\"quantity\":1,\"price\":15,\"totalAmount\":15,\"shippingFee\":0,\"status\":\"PENDING_PAYMENT\",\"tradeMethod\":\"FACE_TO_FACE\",\"buyerId\":2,\"sellerId\":4}', NULL, 1, 0, '2025-12-06 13:34:45', '2025-12-06 13:49:56');
INSERT INTO `chat_message` VALUES (26, 1, 2, 4, 'ORDER_CARD', '{\"orderId\":3,\"orderNo\":\"GO176499928220102\",\"goodsId\":4,\"goodsTitle\":\"无敌梳子\",\"goodsImages\":\"[\\\"/uploads/goods/2025/12/06/ee84f971-2f1b-4564-81b3-710d6e668cde.jpg\\\"]\",\"quantity\":1,\"price\":15,\"totalAmount\":15,\"shippingFee\":0,\"status\":\"PENDING_PICKUP\",\"tradeMethod\":\"FACE_TO_FACE\",\"buyerId\":2,\"sellerId\":4}', NULL, 1, 0, '2025-12-06 13:36:00', '2025-12-06 13:49:56');
INSERT INTO `chat_message` VALUES (27, 1, 4, 2, 'TEXT', '少不了一点', NULL, 1, 0, '2025-12-06 13:50:02', '2025-12-06 18:15:41');
INSERT INTO `chat_message` VALUES (28, 1, 2, 4, 'TEXT', '是的方法', NULL, 1, 0, '2025-12-06 18:15:44', '2025-12-06 18:18:13');
INSERT INTO `chat_message` VALUES (29, 1, 2, 4, 'TEXT', '双方夫', NULL, 1, 0, '2025-12-06 18:23:00', '2025-12-06 18:23:07');
INSERT INTO `chat_message` VALUES (30, 1, 2, 4, 'TEXT', '萨法说法', NULL, 1, 0, '2025-12-06 18:23:12', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (31, 1, 2, 4, 'TEXT', '双方夫', NULL, 1, 0, '2025-12-06 18:24:40', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (32, 1, 2, 4, 'TEXT', '是否', NULL, 1, 0, '2025-12-06 18:25:01', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (33, 1, 2, 4, 'TEXT', '案说法', NULL, 1, 0, '2025-12-06 18:27:55', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (34, 1, 2, 4, 'TEXT', '撒啊', NULL, 1, 0, '2025-12-06 18:30:24', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (35, 1, 2, 4, 'TEXT', '阿发', NULL, 1, 0, '2025-12-06 18:30:38', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (36, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:49', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (37, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:49', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (38, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:49', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (39, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:49', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (40, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:49', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (41, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:50', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (42, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:50', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (43, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:50', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (44, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:50', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (45, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:50', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (46, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:50', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (47, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:50', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (48, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:51', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (49, 1, 2, 4, 'TEXT', '11', NULL, 1, 0, '2025-12-06 18:31:51', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (50, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:51', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (51, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:51', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (52, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:51', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (53, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:52', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (54, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:52', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (55, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:52', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (56, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:52', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (57, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:52', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (58, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:52', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (59, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:52', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (60, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:53', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (61, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:53', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (62, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:53', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (63, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:53', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (64, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:53', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (65, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:54', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (66, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:54', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (67, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:54', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (68, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:54', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (69, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:31:55', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (70, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:08', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (71, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:09', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (72, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:09', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (73, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:09', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (74, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:09', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (75, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:09', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (76, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:10', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (77, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:10', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (78, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:10', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (79, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:10', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (80, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:10', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (81, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:10', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (82, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:11', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (83, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:11', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (84, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:11', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (85, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:16', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (86, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:17', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (87, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:17', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (88, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:17', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (89, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:17', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (90, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:18', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (91, 1, 2, 4, 'TEXT', '11', NULL, 1, 0, '2025-12-06 18:32:18', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (92, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:18', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (93, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:18', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (94, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:18', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (95, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:18', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (96, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:18', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (97, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:18', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (98, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:19', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (99, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:19', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (100, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:19', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (101, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:19', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (102, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:19', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (103, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:19', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (104, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:25', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (105, 1, 2, 4, 'TEXT', '2', NULL, 1, 0, '2025-12-06 18:32:25', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (106, 1, 2, 4, 'TEXT', '2', NULL, 1, 0, '2025-12-06 18:32:26', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (107, 1, 2, 4, 'TEXT', '2', NULL, 1, 0, '2025-12-06 18:32:26', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (108, 1, 2, 4, 'TEXT', '2', NULL, 1, 0, '2025-12-06 18:32:26', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (109, 1, 2, 4, 'TEXT', '2', NULL, 1, 0, '2025-12-06 18:32:26', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (110, 1, 2, 4, 'TEXT', '2', NULL, 1, 0, '2025-12-06 18:32:26', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (111, 1, 2, 4, 'TEXT', '2', NULL, 1, 0, '2025-12-06 18:32:26', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (112, 1, 2, 4, 'TEXT', '2', NULL, 1, 0, '2025-12-06 18:32:26', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (113, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:27', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (114, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:27', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (115, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:27', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (116, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:27', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (117, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:27', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (118, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:28', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (119, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:31', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (120, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:32', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (121, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:32', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (122, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:32', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (123, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:32', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (124, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:32', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (125, 1, 2, 4, 'TEXT', '11', NULL, 1, 0, '2025-12-06 18:32:34', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (126, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:34', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (127, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:34', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (128, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:34', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (129, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:34', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (130, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:32:35', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (131, 1, 2, 4, 'TEXT', '11', NULL, 1, 0, '2025-12-06 18:33:13', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (132, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 18:33:40', '2025-12-06 22:15:04');
INSERT INTO `chat_message` VALUES (133, 1, 2, 4, 'TEXT', '1111', NULL, 1, 0, '2025-12-06 22:15:19', '2025-12-06 22:15:19');
INSERT INTO `chat_message` VALUES (134, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 22:15:19', '2025-12-06 22:15:24');
INSERT INTO `chat_message` VALUES (135, 1, 2, 4, 'TEXT', '1', NULL, 1, 0, '2025-12-06 22:15:20', '2025-12-06 22:15:24');
INSERT INTO `chat_message` VALUES (136, 1, 4, 2, 'TEXT', '111', NULL, 1, 0, '2025-12-06 22:15:44', '2025-12-06 22:15:44');
INSERT INTO `chat_message` VALUES (137, 1, 4, 2, 'TEXT', '1', NULL, 1, 0, '2025-12-06 22:15:45', '2025-12-06 22:15:49');
INSERT INTO `chat_message` VALUES (138, 1, 4, 2, 'TEXT', '1', NULL, 1, 0, '2025-12-06 22:15:45', '2025-12-06 22:15:49');
INSERT INTO `chat_message` VALUES (139, 1, 4, 2, 'TEXT', '11', NULL, 1, 0, '2025-12-06 22:15:45', '2025-12-06 22:15:49');
INSERT INTO `chat_message` VALUES (140, 1, 4, 2, 'TEXT', '你好', NULL, 1, 0, '2025-12-06 22:15:47', '2025-12-06 22:15:49');
INSERT INTO `chat_message` VALUES (141, 1, 4, 2, 'TEXT', '你操', NULL, 1, 0, '2025-12-06 22:15:51', '2025-12-06 22:15:54');

-- ----------------------------
-- Table structure for chat_session
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user1_id` bigint NOT NULL COMMENT '用户1 ID',
  `user2_id` bigint NOT NULL COMMENT '用户2 ID',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联类型：LOST_FOUND-失物招领，GOODS-商品，TASK-跑腿任务',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID（失物ID、商品ID、任务ID等）',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
  `last_message_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后消息内容',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user1_id`(`user1_id` ASC) USING BTREE,
  INDEX `idx_user2_id`(`user2_id` ASC) USING BTREE,
  INDEX `idx_related_type_id`(`related_type` ASC, `related_id` ASC) USING BTREE,
  INDEX `idx_last_message_time`(`last_message_time` ASC) USING BTREE,
  CONSTRAINT `fk_chat_session_user1` FOREIGN KEY (`user1_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_session_user2` FOREIGN KEY (`user2_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私信会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_session
-- ----------------------------
INSERT INTO `chat_session` VALUES (1, 2, 4, 'GOODS', 3, '2025-12-06 22:15:51', '你操', 0, '2025-12-03 12:52:55', '2025-12-06 22:15:51');
INSERT INTO `chat_session` VALUES (2, 2, 4, 'LOST_FOUND', 2, NULL, NULL, 1, '2025-12-03 13:06:27', '2025-12-03 13:54:00');
INSERT INTO `chat_session` VALUES (3, 2, 5, 'LOST_FOUND', 3, '2025-12-04 11:57:22', '哈喽', 0, '2025-12-04 11:57:07', '2025-12-04 11:57:22');

-- ----------------------------
-- Table structure for claim_record
-- ----------------------------
DROP TABLE IF EXISTS `claim_record`;
CREATE TABLE `claim_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认领记录ID',
  `lost_found_id` bigint NOT NULL COMMENT '失物ID',
  `claimer_id` bigint NOT NULL COMMENT '认领者ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '物品特征描述',
  `lost_time` datetime NULL DEFAULT NULL COMMENT '丢失时间',
  `other_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '其他信息',
  `proof_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '证明文件（JSON数组）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待确认，CONFIRMED-已确认，REJECTED-已拒绝',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '确认时间',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `reject_time` datetime NULL DEFAULT NULL COMMENT '拒绝时间',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lost_found_id`(`lost_found_id` ASC) USING BTREE,
  INDEX `idx_claimer_id`(`claimer_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_claim_record_claimer` FOREIGN KEY (`claimer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_claim_record_lost_found` FOREIGN KEY (`lost_found_id`) REFERENCES `lost_found` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '认领记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of claim_record
-- ----------------------------
INSERT INTO `claim_record` VALUES (3, 2, 4, '吃吃吃', '2025-12-04 02:55:40', '捡到了', '[\"/uploads/lost-found/2025/12/04/4f64f408-5fb9-4d07-a7cb-1064cfec166a.jpg\"]', 'PENDING', NULL, NULL, NULL, 0, '2025-12-04 10:55:47', '2025-12-04 10:55:46');
INSERT INTO `claim_record` VALUES (4, 3, 4, '我的我的', '2025-12-04 02:57:55', '阿斯弗萨芬', '[\"/uploads/lost-found/2025/12/04/8e94131c-5ca9-4e11-8a64-4cfcc4e9f035.jpg\"]', 'PENDING', NULL, NULL, NULL, 0, '2025-12-04 10:57:59', '2025-12-04 10:57:59');
INSERT INTO `claim_record` VALUES (5, 3, 5, '我的吧', '2025-12-04 03:57:46', '案发时', '[\"/uploads/lost-found/2025/12/04/1034303d-497f-439a-8664-9d20372a2ca5.jpg\"]', 'PENDING', NULL, NULL, NULL, 0, '2025-12-04 12:02:30', '2025-12-04 12:02:30');
INSERT INTO `claim_record` VALUES (6, 5, 2, '我见到了', '2025-12-06 05:33:20', '阿飞萨芬', '[\"/uploads/lost-found/2025/12/06/d6ee05c0-d276-423b-8406-df16b151f6df.jpg\"]', 'CONFIRMED', '2025-12-06 18:08:15', NULL, NULL, 0, '2025-12-06 13:33:25', '2025-12-06 13:33:25');
INSERT INTO `claim_record` VALUES (7, 6, 2, '我的', '2025-12-06 05:33:32', '发生发', NULL, 'CONFIRMED', '2025-12-06 18:04:31', NULL, NULL, 0, '2025-12-06 13:33:34', '2025-12-06 13:33:34');

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '卖家ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品标题',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品分类：数码产品、图书教材、服装鞋包、生活用品、运动健身、乐器、文创用品、其他',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品描述',
  `current_price` decimal(10, 2) NOT NULL COMMENT '售价',
  `condition` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品成色：全新、几乎全新、轻微使用痕迹、明显使用痕迹',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品图片（JSON数组，1-9张）',
  `trade_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易方式：FACE_TO_FACE-自提，MAIL-邮寄',
  `trade_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自提地点（自提时必填）',
  `shipping_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '邮寄费用（邮寄时可选）',
  `stock` int NOT NULL DEFAULT 1 COMMENT '库存数量（最少1件）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING_REVIEW' COMMENT '状态：PENDING_REVIEW-待审核，ON_SALE-在售，SOLD_OUT-已售完，CLOSED-已关闭，REJECTED-已拒绝，ADMIN_OFFSHELF-已下架',
  `audit_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
  `audit_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核拒绝原因',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_admin_id` bigint NULL DEFAULT NULL COMMENT '审核管理员ID',
  `audit_trigger_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发人工审核原因',
  `offshelf_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下架类型：USER-用户自行下架，ADMIN-管理员下架',
  `offshelf_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下架原因',
  `offshelf_time` datetime NULL DEFAULT NULL COMMENT '下架时间',
  `offshelf_admin_id` bigint NULL DEFAULT NULL COMMENT '下架管理员ID',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `version` int NULL DEFAULT 0 COMMENT '乐观锁版本号（用于防止超卖）',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  INDEX `idx_current_price`(`current_price` ASC) USING BTREE,
  INDEX `idx_stock`(`stock` ASC) USING BTREE,
  INDEX `idx_view_count`(`view_count` ASC) USING BTREE,
  INDEX `idx_status_create_time`(`status` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_status_view_count`(`status` ASC, `view_count` DESC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_goods_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, 2, '二手iphone15paomax', '数码产品', '二手iphone15paomax二手iphone15paomax', 2500.00, '几乎全新', '[\"/uploads/goods/2025/12/04/7eaa4d16-72df-4d7d-93e6-2aec6431f8b4.jpg\"]', 'FACE_TO_FACE', 'XXX', 0.00, 0, 'SOLD_OUT', 'APPROVED', NULL, '2025-12-05 21:57:18', 1, '新注册用户；', NULL, NULL, NULL, NULL, 6, 1, 0, '2025-12-04 23:36:05', '2025-12-05 21:58:33');
INSERT INTO `goods` VALUES (2, 2, '二手iphone15paomax', '数码产品', '二手iphone15paomax二手iphone15paomax', 2500.00, '几乎全新', '[\"/uploads/goods/2025/12/04/4d85661b-5f86-4718-970a-23083ae6ca35.jpg\"]', 'MAIL', '', 8.00, 0, 'SOLD_OUT', 'APPROVED', NULL, '2025-12-05 10:53:24', 1, '发布频繁；新注册用户；', NULL, NULL, NULL, NULL, 2391, 1, 0, '2025-12-04 23:36:12', '2025-12-06 22:20:02');
INSERT INTO `goods` VALUES (3, 4, '无敌大耳机', '数码产品', '无敌大耳机无敌大耳机无敌大耳机无敌大耳机无敌大耳机', 2000.00, '全新', '[\"/uploads/goods/2025/12/06/65ac8340-0c04-43d2-bf0c-0c390cb04875.jpg\"]', 'MAIL', '', 10.00, 1, 'ADMIN_OFFSHELF', 'APPROVED', NULL, '2025-12-06 13:19:24', 1, '新注册用户；', 'ADMIN', '测试', '2025-12-06 19:36:09', 1, 6, 1, 0, '2025-12-06 13:09:28', '2025-12-06 19:36:09');
INSERT INTO `goods` VALUES (4, 4, '无敌梳子', '服装鞋包', '无敌梳子无敌梳子无敌梳子无敌梳子', 15.00, '全新', '[\"/uploads/goods/2025/12/06/ee84f971-2f1b-4564-81b3-710d6e668cde.jpg\"]', 'FACE_TO_FACE', '无敌梳子无敌梳子无敌梳子', 0.00, 4, 'ON_SALE', 'APPROVED', NULL, '2025-12-06 13:19:22', 1, '新注册用户；', NULL, NULL, NULL, NULL, 42, 1, 0, '2025-12-06 13:10:03', '2025-12-06 22:20:02');
INSERT INTO `goods` VALUES (5, 4, '衣服衣服', '服装鞋包', '衣服衣服衣服衣服衣服衣服衣服衣服衣服衣服衣服衣服', 56.00, '轻微使用痕迹', '[\"/uploads/goods/2025/12/06/3c7b5989-9422-445f-bfce-af816ce19f0a.jpg\"]', 'MAIL', '', 3.00, 1, 'ON_SALE', 'APPROVED', NULL, '2025-12-06 13:40:25', 1, '新注册用户；', NULL, NULL, NULL, NULL, 1, 0, 0, '2025-12-06 13:39:23', '2025-12-06 22:16:08');

-- ----------------------------
-- Table structure for lost_found
-- ----------------------------
DROP TABLE IF EXISTS `lost_found`;
CREATE TABLE `lost_found`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '失物ID',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品名称',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品分类：证件类、电子产品、生活用品、书籍、其他',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品描述',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：LOST-失物，FOUND-招领',
  `lost_time` datetime NOT NULL COMMENT '丢失/拾取时间',
  `lost_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '丢失/拾取地点',
  `contact_info` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '物品图片（JSON数组）',
  `reward` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '悬赏金额',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING_REVIEW' COMMENT '状态：PENDING_REVIEW-待审核，PENDING_CLAIM-待认领，CLAIMING-认领中，CLAIMED-已认领，CLOSED-已关闭，REJECTED-已拒绝，ADMIN_OFFSHELF-已下架',
  `audit_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
  `audit_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核拒绝原因',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_admin_id` bigint NULL DEFAULT NULL COMMENT '审核管理员ID',
  `audit_trigger_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发人工审核原因（敏感词、发布频繁等）',
  `offshelf_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下架类型：USER-用户自行下架，ADMIN-管理员下架',
  `offshelf_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下架原因',
  `offshelf_time` datetime NULL DEFAULT NULL COMMENT '下架时间',
  `offshelf_admin_id` bigint NULL DEFAULT NULL COMMENT '下架管理员ID',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `version` int NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  INDEX `idx_status_type_create_time`(`status` ASC, `type` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_lost_found_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '失物招领表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lost_found
-- ----------------------------
INSERT INTO `lost_found` VALUES (1, 2, '遗失黑色钱包一个', '证件类', '内有身份证、银行卡等物品，钱包是黑色真皮的，比较旧了', 'LOST', '2025-12-02 20:29:58', '图书馆二楼', NULL, '[\"/uploads/lost-found/2025/12/03/064c6b07-2d95-43bd-aa8a-3d4f5028041d.jpg\"]', 50.00, 'PENDING_CLAIM', 'APPROVED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 14, 0, 0, '2025-12-02 20:29:58', '2025-12-04 14:03:48');
INSERT INTO `lost_found` VALUES (2, 2, '遗失小米手机一部', '电子产品', '黑色小米11，有蓝色手机壳，屏幕有轻微划痕', 'LOST', '2025-12-02 20:29:58', '教学楼A302', '15177587564', '[\"/uploads/lost-found/2025/12/03/45869921-fb1b-44d4-9dd0-d2b30ce1a9b6.jpg\",\"/uploads/lost-found/2025/12/03/28a590e0-e6a6-4c7b-887b-9731d4ca41c4.jpg\"]', 100.00, 'CLAIMING', 'APPROVED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 141, 2, 0, '2025-12-02 20:29:58', '2025-12-04 14:05:34');
INSERT INTO `lost_found` VALUES (3, 2, '捡到校园卡一张', '证件类', '姓名：王*明，学号：20220XXX，计算机学院，请失主尽快与我联系。', 'FOUND', '2025-12-02 20:29:58', '食堂一楼', NULL, '[\"/uploads/lost-found/2025/12/03/a35b65e4-34d4-4a18-a0cb-53e9415a30eb.jpg\"]', 0.00, 'CLAIMING', 'APPROVED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 127, 2, 0, '2025-12-02 20:29:58', '2025-12-06 13:49:48');
INSERT INTO `lost_found` VALUES (4, 2, '测试笔记本一台', '电子产品', '黑色联想ThinkPad笔记本，14寸屏幕，键盘有几个键有点磨损，机身有一些使用痕迹，正常运行。于2025年1月2日下午丢失。', 'LOST', '2026-01-02 00:00:00', '测试楼201教室', '13800138000', '[\"/uploads/lost-found/2025/12/04/b412550a-e30c-4fd1-a684-f4965890718e.jpg\"]', 5.00, 'PENDING_CLAIM', 'APPROVED', '踩踩踩', '2025-12-04 14:24:22', 1, '编辑被拒绝的失物，需要重新审核', NULL, NULL, NULL, NULL, 25, 0, 0, '2025-12-03 09:27:17', '2025-12-05 11:31:50');
INSERT INTO `lost_found` VALUES (5, 4, '舒舒服服', '电子产品', '舒舒服服舒舒服服舒舒服服', 'LOST', '2025-12-06 13:08:15', '舒舒服服', '舒舒服服', '[\"/uploads/lost-found/2025/12/06/c3df97c8-f1da-4120-a41e-744cda5e7174.jpg\"]', 10.00, 'CLOSED', 'APPROVED', NULL, '2025-12-06 13:19:28', 1, '新注册用户；', NULL, NULL, NULL, NULL, 15, 0, 0, '2025-12-06 13:08:28', '2025-12-06 18:11:02');
INSERT INTO `lost_found` VALUES (6, 4, '阿飞萨芬萨芬', '电子产品', '阿飞萨芬萨芬阿飞萨芬萨芬阿飞萨芬萨芬', 'FOUND', '2025-12-06 13:08:43', '阿飞萨芬萨芬', '阿飞萨芬萨芬', '[\"/uploads/lost-found/2025/12/06/f81ab4cf-005e-4ade-bf07-0725542b6d57.jpg\"]', 10.00, 'CLOSED', 'APPROVED', NULL, '2025-12-06 13:19:27', 1, '新注册用户；', NULL, NULL, NULL, NULL, 9, 0, 0, '2025-12-06 13:08:58', '2025-12-06 18:09:10');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '唯一订单号（格式：GO+时间戳13位+用户ID后2位，示例：GO170406720000012）',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `session_id` bigint NULL DEFAULT NULL COMMENT '关联聊天会话ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '购买数量',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额（单价×数量）',
  `shipping_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '邮寄费用（邮寄时）',
  `trade_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易方式：FACE_TO_FACE-自提，MAIL-邮寄',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人电话',
  `receiver_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
  `pickup_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自提地点（自提方式时使用）',
  `tracking_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流单号',
  `logistics_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司',
  `ship_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态：PENDING_PAYMENT-待付款，PAID-已付款（仅邮寄方式，等待发货），SHIPPED-已发货（仅邮寄方式），PENDING_PICKUP-待自提（仅自提方式，付款后自动进入，无需发货），COMPLETED-已完成，CANCELLED-已取消，REFUNDED-已退款',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '付款时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `price_update_time` datetime NULL DEFAULT NULL COMMENT '改价时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_buyer_status`(`buyer_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_seller_status`(`seller_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_order_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (1, 'GO176493662510604', 2, 1, 4, 2, 1, 2400.00, 2400.00, 8.00, 'MAIL', '测试', '15166568794', '广西南宁市系襄阳区发觉傻瓜卡刷卡噶时光', NULL, '124124214', 'XXX', '2025-12-05 21:55:45', 'COMPLETED', '2025-12-05 20:10:25', '2025-12-05 21:55:23', '2025-12-05 21:55:58', NULL, NULL, NULL, 0, '2025-12-05 21:55:58');
INSERT INTO `order` VALUES (2, 'GO176494307005804', 1, 1, 4, 2, 1, 2500.00, 2500.00, 0.00, 'FACE_TO_FACE', NULL, NULL, NULL, 'XXX', NULL, NULL, NULL, 'COMPLETED', '2025-12-05 21:57:50', '2025-12-05 21:58:26', '2025-12-05 21:59:31', NULL, NULL, NULL, 0, '2025-12-05 21:59:31');
INSERT INTO `order` VALUES (3, 'GO176499928220102', 4, 1, 2, 4, 1, 15.00, 15.00, 0.00, 'FACE_TO_FACE', NULL, NULL, NULL, '无敌梳子无敌梳子无敌梳子', NULL, NULL, NULL, 'COMPLETED', '2025-12-06 13:34:42', '2025-12-06 13:35:51', '2025-12-06 13:36:07', NULL, NULL, NULL, 0, '2025-12-06 13:36:07');
INSERT INTO `order` VALUES (4, 'GO176499931047302', 3, 1, 2, 4, 1, 2000.00, 2000.00, 10.00, 'MAIL', '安抚', '16652356654', '案发时案说法案说法啊萨法说法是案说法啊', NULL, '4124123123123', '大苏打', '2025-12-06 13:43:25', 'COMPLETED', '2025-12-06 13:35:10', '2025-12-06 13:35:24', '2025-12-06 13:43:34', NULL, NULL, NULL, 0, '2025-12-06 13:43:34');

-- ----------------------------
-- Table structure for search_history
-- ----------------------------
DROP TABLE IF EXISTS `search_history`;
CREATE TABLE `search_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '搜索历史ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '搜索关键词',
  `module_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块类型：LOST_FOUND-失物招领，GOODS-闲置交易，TASK-跑腿服务',
  `search_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_module_type`(`module_type` ASC) USING BTREE,
  INDEX `idx_search_time`(`search_time` ASC) USING BTREE,
  INDEX `idx_user_module_time`(`user_id` ASC, `module_type` ASC, `search_time` DESC) USING BTREE,
  CONSTRAINT `fk_search_history_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '搜索历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of search_history
-- ----------------------------

-- ----------------------------
-- Table structure for sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_word`;
CREATE TABLE `sensitive_word`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
  `word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '敏感词',
  `level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NORMAL' COMMENT '严重程度：NORMAL-普通，SEVERE-严重',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_word`(`word` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '敏感词表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sensitive_word
-- ----------------------------
INSERT INTO `sensitive_word` VALUES (1, '法轮功', 'SEVERE', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (2, '邪教', 'SEVERE', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (3, '毒品', 'SEVERE', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (4, '赌博', 'SEVERE', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (5, '色情', 'SEVERE', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (6, '诈骗', 'NORMAL', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (7, '传销', 'SEVERE', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (8, '暴力', 'NORMAL', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (9, '政治', 'NORMAL', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (10, '广告', 'NORMAL', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (11, '刷单', 'NORMAL', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (12, '代考', 'NORMAL', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (13, '作弊', 'NORMAL', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (14, '枪', 'SEVERE', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);
INSERT INTO `sensitive_word` VALUES (15, '弹', 'SEVERE', '2025-12-02 20:19:47', '2025-12-02 20:19:47', 0);

-- ----------------------------
-- Table structure for study_answer
-- ----------------------------
DROP TABLE IF EXISTS `study_answer`;
CREATE TABLE `study_answer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回答ID',
  `question_id` bigint NOT NULL COMMENT '问题ID',
  `user_id` bigint NOT NULL COMMENT '回答者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回答内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '回答图片（JSON数组，最多3张）',
  `is_accepted` tinyint(1) NULL DEFAULT 0 COMMENT '是否被采纳：0-未采纳，1-已采纳',
  `audit_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
  `audit_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核拒绝原因',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_admin_id` bigint NULL DEFAULT NULL COMMENT '审核管理员ID',
  `audit_trigger_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发人工审核原因',
  `accept_time` datetime NULL DEFAULT NULL COMMENT '采纳时间',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数量（可选功能）',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回答时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_accepted`(`is_accepted` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  CONSTRAINT `fk_study_answer_question` FOREIGN KEY (`question_id`) REFERENCES `study_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_study_answer_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习回答表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of study_answer
-- ----------------------------
INSERT INTO `study_answer` VALUES (1, 1, 4, '这个我真会', '[\"/uploads/study/2025/12/06/2ac8280b-e267-490f-b84a-7d9e3bf87bfa.jpg\"]', 0, 'APPROVED', NULL, '2025-12-06 20:30:38', 1, NULL, NULL, 0, 0, '2025-12-06 13:04:23', '2025-12-06 20:30:38');
INSERT INTO `study_answer` VALUES (2, 1, 5, '这我也会', '[\"/uploads/study/2025/12/06/0dffc19f-42f2-4c19-a504-d013607b6b79.jpg\"]', 1, 'APPROVED', NULL, '2025-12-06 20:30:36', 1, NULL, '2025-12-06 13:05:11', 0, 0, '2025-12-06 13:04:44', '2025-12-06 20:30:36');
INSERT INTO `study_answer` VALUES (3, 3, 5, '踩踩踩', '[\"/uploads/study/2025/12/06/b8b6e647-4ab9-4dd3-9b46-ff089e663e88.jpg\"]', 0, 'APPROVED', NULL, '2025-12-06 20:30:55', NULL, NULL, NULL, 0, 0, '2025-12-06 20:30:55', '2025-12-06 20:30:55');

-- ----------------------------
-- Table structure for study_question
-- ----------------------------
DROP TABLE IF EXISTS `study_question`;
CREATE TABLE `study_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题标题',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题分类：MATH-数学，PHYSICS-物理，CHEMISTRY-化学，BIOLOGY-生物，COMPUTER-计算机，ENGLISH-英语，LITERATURE-文学，HISTORY-历史，PHILOSOPHY-哲学，ECONOMICS-经济，MANAGEMENT-管理，LAW-法律，EDUCATION-教育，ART-艺术，ENGINEERING-工程，MEDICINE-医学，AGRICULTURE-农学，OTHER-其他',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题描述',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '问题图片（JSON数组，最多3张）',
  `reward` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '悬赏金额（可选，0表示无酬劳）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING_REVIEW' COMMENT '状态：PENDING_REVIEW-待审核，PENDING_ANSWER-待解答，ANSWERED-已回答，SOLVED-已解决，CANCELLED-已取消，REJECTED-已拒绝，ADMIN_OFFSHELF-已下架',
  `audit_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
  `audit_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核拒绝原因',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_admin_id` bigint NULL DEFAULT NULL COMMENT '审核管理员ID',
  `audit_trigger_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发人工审核原因',
  `offshelf_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下架类型',
  `offshelf_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下架原因',
  `offshelf_time` datetime NULL DEFAULT NULL COMMENT '下架时间',
  `offshelf_admin_id` bigint NULL DEFAULT NULL COMMENT '下架管理员ID',
  `accepted_answer_id` bigint NULL DEFAULT NULL COMMENT '已采纳的回答ID',
  `accept_time` datetime NULL DEFAULT NULL COMMENT '采纳时间',
  `solve_time` datetime NULL DEFAULT NULL COMMENT '解决时间',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `answer_count` int NULL DEFAULT 0 COMMENT '回答数量',
  `last_answer_time` datetime NULL DEFAULT NULL COMMENT '最后回答时间',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  INDEX `idx_reward`(`reward` ASC) USING BTREE,
  INDEX `idx_view_count`(`view_count` ASC) USING BTREE,
  INDEX `idx_answer_count`(`answer_count` ASC) USING BTREE,
  INDEX `idx_status_reward`(`status` ASC, `reward` DESC) USING BTREE,
  INDEX `idx_status_create_time`(`status` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `fk_study_question_answer`(`accepted_answer_id` ASC) USING BTREE,
  CONSTRAINT `fk_study_question_answer` FOREIGN KEY (`accepted_answer_id`) REFERENCES `study_answer` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_study_question_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习问题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of study_question
-- ----------------------------
INSERT INTO `study_question` VALUES (1, 2, '计算机问题', 'COMPUTER', '计算机问题计算机问题', '[\"/uploads/study/2025/12/06/c895c8de-dc45-47d5-b7a2-9582b115229d.jpg\"]', 1.00, 'SOLVED', 'APPROVED', NULL, '2025-12-06 13:02:33', 1, '新注册用户；', NULL, NULL, NULL, NULL, 2, NULL, '2025-12-06 13:05:11', 33, 4, '2025-12-06 20:30:38', 0, '2025-12-06 12:21:47', '2025-12-06 20:30:38');
INSERT INTO `study_question` VALUES (2, 2, '我不会java', 'COMPUTER', '我不会java我不会java我不会java', '[\"/uploads/study/2025/12/06/c2518e37-f9c4-4d05-a832-54abf0e9b26d.jpg\"]', 0.00, 'PENDING_REVIEW', 'PENDING', '发发发', '2025-12-06 13:05:44', 1, '新注册用户；', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, 0, '2025-12-06 12:56:08', '2025-12-06 13:18:14');
INSERT INTO `study_question` VALUES (3, 4, 'MysqlXXX', 'COMPUTER', '发生过撒公司东莞市东莞市东莞市', '[\"/uploads/study/2025/12/06/8feca2be-8164-42cc-9df4-5e806f5176e0.jpg\"]', 10.00, 'ANSWERED', 'APPROVED', NULL, '2025-12-06 13:19:16', 1, '新注册用户；', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 45, 1, '2025-12-06 20:30:55', 0, '2025-12-06 13:10:36', '2025-12-06 22:13:37');

-- ----------------------------
-- Table structure for system_message
-- ----------------------------
DROP TABLE IF EXISTS `system_message`;
CREATE TABLE `system_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型：VERIFICATION_APPROVED-认证通过，VERIFICATION_REJECTED-认证拒绝，ORDER_STATUS-订单状态，TASK_STATUS-任务状态，ANNOUNCEMENT-系统公告',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联类型：VERIFICATION-实名认证，ORDER-订单，TASK-任务，ANNOUNCEMENT-公告',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 67 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_message
-- ----------------------------
INSERT INTO `system_message` VALUES (1, 2, 'CLAIM_APPLY', '收到认领申请', '您发布的失物《捡到校园卡一张》收到了认领申请，请及时查看', 1, 'LOST_FOUND', 3, 0, '2025-12-03 12:52:55', '2025-12-03 13:55:51');
INSERT INTO `system_message` VALUES (2, 4, 'CLAIM_CONFIRMED', '认领成功', '您认领的失物《捡到校园卡一张》已被确认，请联系发布者取回物品', 1, 'LOST_FOUND', 3, 0, '2025-12-03 12:54:34', '2025-12-03 13:05:59');
INSERT INTO `system_message` VALUES (3, 4, 'CLAIM_CONFIRMED', '认领成功', '您认领的失物《捡到校园卡一张》已被确认，请联系发布者取回物品', 1, 'LOST_FOUND', 3, 0, '2025-12-03 12:54:44', '2025-12-03 13:05:59');
INSERT INTO `system_message` VALUES (4, 2, 'CLAIM_APPLY', '收到认领申请', '您发布的失物《遗失小米手机一部》收到了认领申请，请及时查看', 1, 'LOST_FOUND', 2, 0, '2025-12-03 13:06:27', '2025-12-03 13:55:47');
INSERT INTO `system_message` VALUES (7, 4, 'CLAIM_REJECTED', '认领被拒绝', '很抱歉，您认领的失物《遗失小米手机一部》被拒绝。原因：不是你的', 1, 'LOST_FOUND', 2, 0, '2025-12-04 10:49:39', '2025-12-04 12:04:34');
INSERT INTO `system_message` VALUES (8, 2, 'CLAIM_APPLY', '收到认领申请', '您发布的失物《遗失小米手机一部》收到了认领申请，请及时查看', 1, 'LOST_FOUND', 2, 0, '2025-12-04 10:55:47', '2025-12-04 10:58:10');
INSERT INTO `system_message` VALUES (11, 2, 'LOST_FOUND_REJECTED', '失物审核被拒绝', '很抱歉，您发布的失物《测试笔记本一台》未通过审核。原因：测试', 1, 'LOST_FOUND', 4, 0, '2025-12-04 14:04:42', '2025-12-04 14:04:45');
INSERT INTO `system_message` VALUES (12, 2, 'LOST_FOUND_REJECTED', '失物审核被拒绝', '很抱歉，您发布的失物《测试笔记本一台》未通过审核。原因：踩踩踩。您可以修改后重新提交审核。', 1, 'LOST_FOUND', 4, 0, '2025-12-04 14:22:25', '2025-12-04 18:47:42');
INSERT INTO `system_message` VALUES (14, 2, 'LOST_FOUND_APPROVED', '失物审核通过', '您发布的失物《测试笔记本一台》已通过审核，现已上线。', 1, 'LOST_FOUND', 4, 0, '2025-12-04 14:24:22', '2025-12-04 18:47:44');
INSERT INTO `system_message` VALUES (15, 2, 'VERIFICATION_APPROVED', '实名认证审核通过', '恭喜您！您的实名认证已通过审核。您现在可以发布闲置商品和跑腿任务，也可以参与商品交易和接单跑腿了。', 1, 'VERIFICATION', 2, 0, '2025-12-04 23:27:08', '2025-12-05 10:57:42');
INSERT INTO `system_message` VALUES (18, 2, 'GOODS_APPROVED', '商品审核通过', '您的商品《二手iphone15paomax》审核通过，已上架', 1, 'GOODS', 2, 0, '2025-12-05 10:53:24', '2025-12-05 10:57:42');
INSERT INTO `system_message` VALUES (20, 4, 'VERIFICATION_APPROVED', '实名认证审核通过', '恭喜您！您的实名认证已通过审核。您现在可以发布闲置商品和跑腿任务，也可以参与商品交易和接单跑腿了。', 1, 'VERIFICATION', 4, 0, '2025-12-05 11:07:52', '2025-12-05 20:53:05');
INSERT INTO `system_message` VALUES (21, 2, 'ORDER_CREATED', '收到新订单', '您的商品《二手iphone15paomax》被购买了，订单号：GO176493662510604', 1, 'ORDER', 1, 0, '2025-12-05 20:10:25', '2025-12-05 21:55:36');
INSERT INTO `system_message` VALUES (22, 4, 'ORDER_PRICE_UPDATED', '订单价格已修改', '订单号：GO176493662510604 的价格已修改为：2400元', 1, 'ORDER', 1, 0, '2025-12-05 21:46:10', '2025-12-05 21:55:32');
INSERT INTO `system_message` VALUES (23, 2, 'ORDER_PAID', '订单已付款', '订单号：GO176493662510604 已付款，请及时发货', 1, 'ORDER', 1, 0, '2025-12-05 21:55:23', '2025-12-05 21:55:36');
INSERT INTO `system_message` VALUES (24, 4, 'ORDER_SHIPPED', '订单已发货', '订单号：GO176493662510604 已发货，快递单号：124124214', 1, 'ORDER', 1, 0, '2025-12-05 21:55:45', '2025-12-05 22:01:25');
INSERT INTO `system_message` VALUES (25, 2, 'ORDER_COMPLETED', '订单已完成', '订单号：GO176493662510604 买家已确认收货，订单完成', 1, 'ORDER', 1, 0, '2025-12-05 21:55:58', '2025-12-06 13:05:49');
INSERT INTO `system_message` VALUES (26, 2, 'GOODS_APPROVED', '商品审核通过', '您的商品《二手iphone15paomax》审核通过，已上架', 1, 'GOODS', 1, 0, '2025-12-05 21:57:18', '2025-12-06 13:05:49');
INSERT INTO `system_message` VALUES (27, 2, 'ORDER_CREATED', '收到新订单', '您的商品《二手iphone15paomax》被购买了，订单号：GO176494307005804', 1, 'ORDER', 2, 0, '2025-12-05 21:57:50', '2025-12-06 13:05:49');
INSERT INTO `system_message` VALUES (28, 2, 'ORDER_PAID', '订单已付款', '订单号：GO176494307005804 已付款（自提方式，无需发货），请等待买家自提', 1, 'ORDER', 2, 0, '2025-12-05 21:58:26', '2025-12-06 13:05:49');
INSERT INTO `system_message` VALUES (29, 2, 'ORDER_COMPLETED', '订单已完成', '订单号：GO176494307005804 买家已确认收货，订单完成', 1, 'ORDER', 2, 0, '2025-12-05 21:59:31', '2025-12-06 13:05:49');
INSERT INTO `system_message` VALUES (31, 1, 'ADMIN_AUDIT_REQUIRED', '新的学习问题待审核', '有一条新的学习问题《我不会java》需要审核，触发原因：新注册用户；', 1, 'STUDY_QUESTION', 2, 0, '2025-12-06 12:56:08', '2025-12-06 16:39:44');
INSERT INTO `system_message` VALUES (32, 2, 'QUESTION_APPROVED', '问题审核通过', '您的问题《计算机问题》已审核通过', 1, 'STUDY_QUESTION', 1, 0, '2025-12-06 13:02:33', '2025-12-06 13:05:49');
INSERT INTO `system_message` VALUES (33, 2, 'QUESTION_ANSWERED', '您的问题有新回答', '您的问题《计算机问题》收到了新的回答', 1, 'STUDY_QUESTION', 1, 0, '2025-12-06 13:04:23', '2025-12-06 13:05:49');
INSERT INTO `system_message` VALUES (34, 2, 'QUESTION_ANSWERED', '您的问题有新回答', '您的问题《计算机问题》收到了新的回答', 1, 'STUDY_QUESTION', 1, 0, '2025-12-06 13:04:44', '2025-12-06 13:05:49');
INSERT INTO `system_message` VALUES (35, 5, 'ANSWER_ACCEPTED', '您的回答被采纳', '您对问题《计算机问题》的回答已被采纳', 0, 'STUDY_QUESTION', 1, 0, '2025-12-06 13:05:11', '2025-12-06 13:05:11');
INSERT INTO `system_message` VALUES (36, 2, 'QUESTION_REJECTED', '问题审核未通过', '您的问题《我不会java》审核未通过，原因：发发发', 1, 'STUDY_QUESTION', 2, 0, '2025-12-06 13:05:44', '2025-12-06 13:05:49');
INSERT INTO `system_message` VALUES (37, 1, 'ADMIN_AUDIT_REQUIRED', '新的失物待审核', '有一条新的失物《舒舒服服》需要审核，触发原因：新注册用户；', 1, 'LOST_FOUND', 5, 0, '2025-12-06 13:08:28', '2025-12-06 16:39:44');
INSERT INTO `system_message` VALUES (38, 1, 'ADMIN_AUDIT_REQUIRED', '新的招领待审核', '有一条新的招领《阿飞萨芬萨芬》需要审核，触发原因：新注册用户；', 1, 'LOST_FOUND', 6, 0, '2025-12-06 13:08:58', '2025-12-06 16:39:44');
INSERT INTO `system_message` VALUES (39, 1, 'ADMIN_AUDIT_REQUIRED', '新的商品待审核', '有一条新的商品《无敌大耳机》需要审核，触发原因：新注册用户；', 1, 'GOODS', 3, 0, '2025-12-06 13:09:28', '2025-12-06 16:39:44');
INSERT INTO `system_message` VALUES (40, 1, 'ADMIN_AUDIT_REQUIRED', '新的商品待审核', '有一条新的商品《无敌梳子》需要审核，触发原因：新注册用户；', 1, 'GOODS', 4, 0, '2025-12-06 13:10:03', '2025-12-06 16:39:44');
INSERT INTO `system_message` VALUES (41, 1, 'ADMIN_AUDIT_REQUIRED', '新的学习问题待审核', '有一条新的学习问题《MysqlXXX》需要审核，触发原因：新注册用户；', 1, 'STUDY_QUESTION', 3, 0, '2025-12-06 13:10:36', '2025-12-06 16:39:44');
INSERT INTO `system_message` VALUES (42, 1, 'ADMIN_AUDIT_REQUIRED', '新的学习问题待审核', '有一条新的学习问题《我不会java》需要审核，触发原因：新注册用户；', 1, 'STUDY_QUESTION', 2, 0, '2025-12-06 13:18:14', '2025-12-06 16:39:44');
INSERT INTO `system_message` VALUES (43, 4, 'QUESTION_APPROVED', '问题审核通过', '您的问题《MysqlXXX》已审核通过', 1, 'STUDY_QUESTION', 3, 0, '2025-12-06 13:19:16', '2025-12-06 13:20:38');
INSERT INTO `system_message` VALUES (44, 4, 'GOODS_APPROVED', '商品审核通过', '您的商品《无敌梳子》审核通过，已上架', 1, 'GOODS', 4, 0, '2025-12-06 13:19:22', '2025-12-06 13:20:38');
INSERT INTO `system_message` VALUES (45, 4, 'GOODS_APPROVED', '商品审核通过', '您的商品《无敌大耳机》审核通过，已上架', 1, 'GOODS', 3, 0, '2025-12-06 13:19:24', '2025-12-06 13:20:38');
INSERT INTO `system_message` VALUES (46, 4, 'LOST_FOUND_APPROVED', '招领审核通过', '您发布的招领《阿飞萨芬萨芬》已通过审核，现已上线。', 1, 'LOST_FOUND', 6, 0, '2025-12-06 13:19:27', '2025-12-06 13:20:38');
INSERT INTO `system_message` VALUES (47, 4, 'LOST_FOUND_APPROVED', '失物审核通过', '您发布的失物《舒舒服服》已通过审核，现已上线。', 1, 'LOST_FOUND', 5, 0, '2025-12-06 13:19:28', '2025-12-06 13:20:38');
INSERT INTO `system_message` VALUES (48, 4, 'CLAIM_APPLY', '收到认领申请', '您发布的失物《舒舒服服》收到了认领申请，请及时查看', 1, 'LOST_FOUND', 5, 0, '2025-12-06 13:33:25', '2025-12-06 13:38:15');
INSERT INTO `system_message` VALUES (49, 4, 'CLAIM_APPLY', '收到认领申请', '您发布的失物《阿飞萨芬萨芬》收到了认领申请，请及时查看', 1, 'LOST_FOUND', 6, 0, '2025-12-06 13:33:34', '2025-12-06 13:38:15');
INSERT INTO `system_message` VALUES (50, 4, 'ORDER_CREATED', '收到新订单', '您的商品《无敌梳子》被购买了，订单号：GO176499928220102', 1, 'ORDER', 3, 0, '2025-12-06 13:34:42', '2025-12-06 13:38:15');
INSERT INTO `system_message` VALUES (51, 4, 'ORDER_CREATED', '收到新订单', '您的商品《无敌大耳机》被购买了，订单号：GO176499931047302', 1, 'ORDER', 4, 0, '2025-12-06 13:35:10', '2025-12-06 13:38:15');
INSERT INTO `system_message` VALUES (52, 4, 'ORDER_PAID', '订单已付款', '订单号：GO176499931047302 已付款，请及时发货', 1, 'ORDER', 4, 0, '2025-12-06 13:35:24', '2025-12-06 13:38:15');
INSERT INTO `system_message` VALUES (53, 4, 'ORDER_PAID', '订单已付款', '订单号：GO176499928220102 已付款（自提方式，无需发货），请等待买家自提', 1, 'ORDER', 3, 0, '2025-12-06 13:35:51', '2025-12-06 13:38:15');
INSERT INTO `system_message` VALUES (54, 4, 'ORDER_COMPLETED', '订单已完成', '订单号：GO176499928220102 买家已确认收货，订单完成', 1, 'ORDER', 3, 0, '2025-12-06 13:36:07', '2025-12-06 13:38:15');
INSERT INTO `system_message` VALUES (55, 1, 'ADMIN_AUDIT_REQUIRED', '新的商品待审核', '有一条新的商品《衣服衣服》需要审核，触发原因：新注册用户；', 1, 'GOODS', 5, 0, '2025-12-06 13:39:23', '2025-12-06 16:39:44');
INSERT INTO `system_message` VALUES (56, 4, 'GOODS_APPROVED', '商品审核通过', '您的商品《衣服衣服》审核通过，已上架', 1, 'GOODS', 5, 0, '2025-12-06 13:40:25', '2025-12-06 13:40:48');
INSERT INTO `system_message` VALUES (57, 2, 'ORDER_SHIPPED', '订单已发货', '订单号：GO176499931047302 已发货，快递单号：4124123123123', 1, 'ORDER', 4, 0, '2025-12-06 13:43:25', '2025-12-06 13:50:11');
INSERT INTO `system_message` VALUES (58, 4, 'ORDER_COMPLETED', '订单已完成', '订单号：GO176499931047302 买家已确认收货，订单完成', 1, 'ORDER', 4, 0, '2025-12-06 13:43:34', '2025-12-06 13:43:39');
INSERT INTO `system_message` VALUES (59, 2, 'CLAIM_CONFIRMED', '认领成功', '您认领的失物《阿飞萨芬萨芬》已被确认，请联系发布者取回物品', 1, 'LOST_FOUND', 6, 0, '2025-12-06 18:04:31', '2025-12-06 18:05:40');
INSERT INTO `system_message` VALUES (60, 2, 'CLAIM_CONFIRMED', '认领成功', '您认领的失物《舒舒服服》已被确认，请联系发布者取回物品', 0, 'LOST_FOUND', 5, 0, '2025-12-06 18:08:15', '2025-12-06 18:08:15');
INSERT INTO `system_message` VALUES (61, 1, 'ADMIN_VERIFICATION_REQUIRED', '新的实名认证待审核', '用户null（748495940@qq.com）提交了实名认证申请，请及时审核', 1, 'VERIFICATION', 5, 0, '2025-12-06 19:02:50', '2025-12-06 19:08:27');
INSERT INTO `system_message` VALUES (62, 5, 'VERIFICATION_APPROVED', '实名认证审核通过', '恭喜您！您的实名认证已通过审核。您现在可以发布闲置商品和跑腿任务，也可以参与商品交易和接单跑腿了。', 0, 'VERIFICATION', 5, 0, '2025-12-06 19:02:58', '2025-12-06 19:02:58');
INSERT INTO `system_message` VALUES (63, 4, 'GOODS_OFFSHELF', '您的商品已被下架', '您发布的商品《无敌大耳机》已被管理员下架，原因：测试', 0, 'GOODS', 3, 0, '2025-12-06 19:36:09', '2025-12-06 19:36:09');
INSERT INTO `system_message` VALUES (64, 2, 'QUESTION_ANSWERED', '您的问题有新回答', '您的问题《计算机问题》收到了新的回答', 0, 'STUDY_QUESTION', 1, 0, '2025-12-06 20:30:36', '2025-12-06 20:30:36');
INSERT INTO `system_message` VALUES (65, 2, 'QUESTION_ANSWERED', '您的问题有新回答', '您的问题《计算机问题》收到了新的回答', 0, 'STUDY_QUESTION', 1, 0, '2025-12-06 20:30:38', '2025-12-06 20:30:38');
INSERT INTO `system_message` VALUES (66, 4, 'QUESTION_ANSWERED', '您的问题有新回答', '您的问题《MysqlXXX》收到了新的回答', 0, 'STUDY_QUESTION', 3, 0, '2025-12-06 20:30:55', '2025-12-06 20:30:55');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱（唯一）',
  `gender` tinyint(1) NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年级',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号',
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '学生' COMMENT '用户类型：学生、教师',
  `is_verified` tinyint(1) NULL DEFAULT 0 COMMENT '是否实名认证：0-未认证，1-已认证（已认证用户信息优先展示，认证后自动获得接单权限）',
  `verification_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NOT_VERIFIED' COMMENT '认证状态：NOT_VERIFIED、PENDING、VERIFIED、REJECTED',
  `verification_proof` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '认证证明文件（JSON数组）',
  `verification_submit_time` datetime NULL DEFAULT NULL COMMENT '认证提交时间',
  `verification_audit_time` datetime NULL DEFAULT NULL COMMENT '认证审核时间',
  `verification_audit_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证拒绝原因',
  `verification_audit_admin_id` bigint NULL DEFAULT NULL COMMENT '审核管理员ID',
  `can_accept_task` tinyint(1) NULL DEFAULT 0 COMMENT '是否可以接单跑腿：0-不可接单，1-可接单（实名认证后自动设置为可接单）',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'USER' COMMENT '角色：USER-普通用户，ADMIN-管理员',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '账号状态：1-正常，2-已封禁',
  `ban_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封禁类型：TEMPORARY-临时，PERMANENT-永久',
  `ban_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封禁原因',
  `ban_days` int NULL DEFAULT NULL COMMENT '封禁天数',
  `ban_time` datetime NULL DEFAULT NULL COMMENT '封禁时间',
  `unban_time` datetime NULL DEFAULT NULL COMMENT '解封时间',
  `ban_admin_id` bigint NULL DEFAULT NULL COMMENT '封禁操作管理员ID',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_is_verified`(`is_verified` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '$2a$10$ZL4PFxqQs5JW8ZiBKs6pzeN14dgW.CyTB9Q1xWv/RiX4FIDOzY9bu', '超级管理员', '/uploads/avatar/2025/12/02/b14f2ccb-2496-4e1f-91e1-dda182b71d9a.jpg', 'admin@qq.com', 1, '2021级', '计算机', NULL, NULL, NULL, NULL, '学生', 0, 'NOT_VERIFIED', NULL, NULL, NULL, NULL, NULL, 0, 'ADMIN', 1, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-04 13:11:14', '2025-12-02 12:33:46', '2025-12-02 18:28:20', 0);
INSERT INTO `user` VALUES (2, '$2a$10$yVHgLaFhCmUjJORNm9Dyu.nvtUk/sCgFUL7vNAZ76gupghhWTJBqO', '测试用户', '/uploads/avatar/2025/12/02/735c0b4c-10ad-498d-8997-aad14304611d.jpg', 'test@qq.com', 1, NULL, NULL, NULL, '测试', '452235200105283716', '20240101', '学生', 1, 'VERIFIED', '[\"/uploads/verification/2025/12/02/34c6db6a-817a-4b0d-a211-79d7131f1dbe.jpg\"]', '2025-12-02 20:09:58', '2025-12-04 23:27:09', NULL, 1, 1, 'USER', 1, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-06 18:56:24', '2025-12-02 12:33:48', '2025-12-02 14:17:03', 0);
INSERT INTO `user` VALUES (4, '$2a$10$4THDJIelgGki0zxMZYqHdOcsTD/AeEw81XVV6aLSNs5dCxZs78yOW', '无敌了', '/uploads/avatar/2025/12/03/fc07a8e1-3aa0-4ca0-b777-b8b784a01036.jpg', '2644832053@qq.com', 1, '2021', NULL, NULL, '测试从', '456266200005223618', '123456789', '学生', 1, 'VERIFIED', '[\"/uploads/verification/2025/12/05/c7f11f69-f5bb-48e9-b5aa-af1097223e55.jpg\"]', '2025-12-05 11:07:47', '2025-12-05 11:07:54', NULL, 1, 1, 'USER', 1, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-05 14:34:14', '2025-12-02 14:21:08', '2025-12-02 14:21:08', 0);
INSERT INTO `user` VALUES (5, '$2a$10$zk6DG9WfT5sukHpUHSantejCofE20tvzo9rnudPw5qSsWZtIMkVcu', '哈哈', '/uploads/avatar/2025/12/06/b2f0b046-8e61-4a4e-b76f-218c33c2c49f.jpg', '748495940@qq.com', 0, NULL, NULL, NULL, '丰富', '453337200106132617', '1231231', '教师', 1, 'VERIFIED', '[\"/uploads/verification/2025/12/06/73ca7d4c-fbdd-468d-aa79-1b9eada0bd90.jpg\"]', '2025-12-06 19:02:50', '2025-12-06 19:03:00', NULL, 1, 1, 'USER', 1, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-04 13:11:35', '2025-12-04 11:57:02', '2025-12-04 11:57:02', 0);

SET FOREIGN_KEY_CHECKS = 1;
