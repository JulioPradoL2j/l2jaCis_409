/*
MySQL Data Transfer
Source Host: localhost
Source Database: acis
Target Host: localhost
Target Database: acis
Date: 04/06/2025 06:31:29
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for gameservers
-- ----------------------------
DROP TABLE IF EXISTS `gameservers`;
CREATE TABLE `gameservers` (
  `server_id` int(11) NOT NULL DEFAULT 0,
  `hexid` varchar(50) NOT NULL DEFAULT '',
  `host` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `gameservers` VALUES ('1', '-799cd2897113eca3680c51a702a74c7a', '');
