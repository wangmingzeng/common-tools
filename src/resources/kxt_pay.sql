/*
Navicat MySQL Data Transfer

Source Server         : vm2
Source Server Version : 50637
Source Host           : 10.10.20.32:3306
Source Database       : kxt_pay

Target Server Type    : MYSQL
Target Server Version : 50637
File Encoding         : 65001

Date: 2017-12-26 09:10:10
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for pay_account
-- ----------------------------
DROP TABLE IF EXISTS `pay_account`;
CREATE TABLE `pay_account` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `account_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '资金账户内部编号',
  `owner_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '所有人类型，枚举：\nCODYY,\nSP,\nORG,\nUSER',
  `sp_id` bigint(20) unsigned NOT NULL COMMENT '所属运营商',
  `owner_id` bigint(20) unsigned NOT NULL COMMENT '下面几类值；\n001（阔地）\ncms_sp.id（运营商）\ncms_org_sp.id（机构）\ncms_user.id（学员、教师）',
  `currency` varchar(6) COLLATE utf8mb4_bin NOT NULL DEFAULT 'CNY' COMMENT '币种，枚举：CNY(人民币)',
  `total_balance` bigint(20) NOT NULL DEFAULT '0' COMMENT '累计总收入，扣除分润的部分，单位分。total_balance = available_balance + frozen_balance + settle_balance',
  `available_balance` bigint(20) NOT NULL DEFAULT '0' COMMENT '可用余额，表示可结算的金额，单位分。',
  `frozen_balance` bigint(20) NOT NULL DEFAULT '0' COMMENT '冻结余额，不可结算的金额, 单位分。',
  `settle_balance` bigint(20) NOT NULL DEFAULT '0' COMMENT '已结算余额, 单位分。累计代付到运营商、机构、教师银行卡的金额。',
  `total_spend` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '累计支出',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint(20) DEFAULT NULL COMMENT '最后更新人编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_no` (`account_number`),
  KEY `idx_fk_sp_id` (`sp_id`),
  KEY `idx_sp_owner` (`sp_id`,`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='资金账户表，一个用户根据不同的币种、所属不同的运营商可以创建多个资金账户。';

-- ----------------------------
-- Records of pay_account
-- ----------------------------

-- ----------------------------
-- Table structure for pay_account_seq
-- ----------------------------
DROP TABLE IF EXISTS `pay_account_seq`;
CREATE TABLE `pay_account_seq` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `account_seq_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '资金流水号',
  `title` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '流水备注',
  `account_id` bigint(20) unsigned NOT NULL COMMENT '账户ID',
  `fund_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '资金操作类型，枚举：\nINCOME(收入),\nPAY(支出),\nFREEZE(冻结),\nUNFREEZE(解冻)',
  `order_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '订单号，pay_order.order_number\n\n',
  `trade_number` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '交易单号，pay_trade.trade_number\n',
  `currency` varchar(6) COLLATE utf8mb4_bin NOT NULL DEFAULT 'CNY',
  `amount` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '金额',
  `last_total_balance` bigint(20) unsigned DEFAULT NULL COMMENT '处理前资金余额',
  `last_avail_balance` bigint(20) unsigned DEFAULT NULL COMMENT '处理期可用资金余额',
  `last_fozen_balance` bigint(20) unsigned DEFAULT NULL COMMENT '处理前资金冻结余额',
  `last_settle_balance` bigint(20) unsigned DEFAULT NULL COMMENT '处理前资金结算余额',
  `last_total_spend` bigint(20) unsigned DEFAULT NULL COMMENT '处理前支出总额',
  `state` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '资金流水状态，枚举:\nREADY(等待处理),\nSUCCESSFUL(成功),\nFAILED(失败),\nPROCESSING(处理中)',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint(20) DEFAULT NULL COMMENT '最后更新人编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_seq_no_UNIQUE` (`account_seq_number`),
  KEY `fk_account_id_idx` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='资金变化流水表, 清算分润时创建，一条交易单可能创建多条资金流水';

-- ----------------------------
-- Records of pay_account_seq
-- ----------------------------

-- ----------------------------
-- Table structure for pay_accounting_record
-- ----------------------------
DROP TABLE IF EXISTS `pay_accounting_record`;
CREATE TABLE `pay_accounting_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `business_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `channel_type` varchar(20) NOT NULL COMMENT '渠道类型:ALIPAY、WECHATPAY',
  `accounting_time` varchar(32) NOT NULL COMMENT '对账日期',
  `accounting_start_time` datetime NOT NULL COMMENT '对账开始时间',
  `accounting_end_time` datetime NOT NULL COMMENT '对账结束时间',
  `accounting_result` varchar(32) NOT NULL COMMENT '对账结果（全部成功、部分成功、全部失败）',
  `failure_num` int(11) NOT NULL COMMENT '失败笔数',
  `total_accounting_num` int(11) NOT NULL COMMENT '总处理笔数',
  `success_num` int(11) NOT NULL COMMENT '总成功笔数',
  `accounting_state` varchar(32) NOT NULL COMMENT '对账状态（已完成、未完成）',
  `delete_flag` varchar(5) NOT NULL COMMENT '删除标记',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_by` bigint(20) unsigned DEFAULT NULL COMMENT '创建人',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账记录表';

-- ----------------------------
-- Records of pay_accounting_record
-- ----------------------------

-- ----------------------------
-- Table structure for pay_alipay_seq
-- ----------------------------
DROP TABLE IF EXISTS `pay_alipay_seq`;
CREATE TABLE `pay_alipay_seq` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `alipay_trade_no` varchar(128) DEFAULT NULL COMMENT '支付宝交易号',
  `business_order_no` varchar(128) DEFAULT NULL COMMENT '商户订单号',
  `business_type` varchar(20) DEFAULT NULL COMMENT '业务类型',
  `commodity_name` varchar(1024) DEFAULT NULL COMMENT '商品名称',
  `create_time` varchar(32) DEFAULT NULL COMMENT '创建时间',
  `complete_time` varchar(32) DEFAULT NULL COMMENT '完成时间',
  `stores_number` varchar(32) DEFAULT NULL COMMENT '门店编号',
  `stores_name` varchar(32) DEFAULT NULL COMMENT '门店名称',
  `operator` varchar(32) DEFAULT NULL COMMENT '操作员',
  `terminal_no` varchar(128) DEFAULT NULL COMMENT '终端号',
  `other_account` varchar(128) DEFAULT NULL COMMENT '对方账户',
  `order_amount` varchar(16) DEFAULT NULL COMMENT '订单金额（元）',
  `merchant_receive` varchar(16) DEFAULT NULL COMMENT '商家实收（元）',
  `alipay_red_envelope` varchar(16) DEFAULT NULL COMMENT '支付宝红包（元）',
  `collect_point_treasure` varchar(16) DEFAULT NULL COMMENT '集分宝（元）',
  `alipay_preferential` varchar(16) DEFAULT NULL COMMENT '支付宝优惠（元）',
  `merchant_preferential` varchar(16) DEFAULT NULL COMMENT '商家优惠（元）',
  `voucher_cancel_out_amount` varchar(16) DEFAULT NULL COMMENT '券核销金额（元）',
  `voucher_name` varchar(128) DEFAULT NULL COMMENT '券名称',
  `merchant_red_envelope_amount` varchar(16) DEFAULT NULL COMMENT '商家红包消费金额（元）',
  `card_buy_amount` varchar(16) DEFAULT NULL COMMENT '卡消费金额（元）',
  `refund_batch_no` varchar(128) DEFAULT NULL COMMENT '退款批次号/请求号',
  `service_fee` varchar(16) DEFAULT NULL COMMENT '服务费（元）',
  `share_profit` varchar(16) DEFAULT NULL COMMENT '分润（元）',
  `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付宝对账单数据表';

-- ----------------------------
-- Records of pay_alipay_seq
-- ----------------------------

-- ----------------------------
-- Table structure for pay_cardbind
-- ----------------------------
DROP TABLE IF EXISTS `pay_cardbind`;
CREATE TABLE `pay_cardbind` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `owner_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '所有人类型，枚举：\nCODYY,\nSP,\nORG,\nUSER',
  `sp_id` bigint(20) unsigned NOT NULL COMMENT '所属运营商',
  `owner_id` bigint(20) unsigned NOT NULL COMMENT '下面几类值；\n001（阔地）\ncms_sp.id（运营商）\ncms_org_sp.id（机构）\ncms_user.id（学员、教师）',
  `card_index` varchar(35) COLLATE utf8mb4_bin NOT NULL COMMENT '银行卡ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint(20) DEFAULT NULL COMMENT '最后更新人编号',
  `del_flag` varchar(6) COLLATE utf8mb4_bin NOT NULL DEFAULT 'NO' COMMENT '已删除标记，枚举：YES、NO',
  PRIMARY KEY (`id`),
  KEY `idx_fk_sp_id` (`sp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='银行卡绑定表';

-- ----------------------------
-- Records of pay_cardbind
-- ----------------------------

-- ----------------------------
-- Table structure for pay_clean
-- ----------------------------
DROP TABLE IF EXISTS `pay_clean`;
CREATE TABLE `pay_clean` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `order_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '订单号',
  `trade_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '交易号',
  `clean_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '清算业务类型，枚举：CHANNEL_FEE(通道手续费)，SHARE_PROFIT(分润)',
  `clean_rate` int(10) NOT NULL DEFAULT '0' COMMENT '清算比例，单位百分之一',
  `clean_amount` bigint(20) NOT NULL DEFAULT '0' COMMENT '清算金额，单位分',
  `account_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '资金账户号，指明清算资金给谁， pay_account.account_number\n',
  `state` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '状态，枚举：\nPENDING(待处理), \nPROCESSED(已处理)',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint(20) DEFAULT NULL COMMENT '最后更新人编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='交易清算表，记录订单清算分润的情况。';

-- ----------------------------
-- Records of pay_clean
-- ----------------------------

-- ----------------------------
-- Table structure for pay_fund_clean_record
-- ----------------------------
DROP TABLE IF EXISTS `pay_fund_clean_record`;
CREATE TABLE `pay_fund_clean_record` (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `business_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `clean_cycle` varchar(12) NOT NULL COMMENT '清算周期（一个月）',
  `clean_start_time` datetime NOT NULL COMMENT '清算内容开始时间',
  `clean_end_time` datetime NOT NULL COMMENT '清算内容结束时间',
  `clean_result` varchar(12) NOT NULL COMMENT '清算结果',
  `clean_num` int(11) NOT NULL DEFAULT '0' COMMENT '清算笔数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_by` bigint(20) unsigned DEFAULT NULL COMMENT '创建人',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金清算记录表';

-- ----------------------------
-- Records of pay_fund_clean_record
-- ----------------------------

-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `order_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '订单号',
  `order_type` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单类型，枚举：\nBUY_COURSE(购买课程),\nREFUND(申请退款),\nRECHARGE(充值),\nWITHDRAW(提现),\nTRANSFER(转账),\nWITHHOLD(代扣),\nAGENT_PAY(代付)\n\n\n\n\n',
  `title` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单备注',
  `course_id` bigint(20) unsigned DEFAULT NULL COMMENT '课程ID，购买课程和退款的时候，此字段不为空。',
  `course_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '课程名称',
  `currency` varchar(3) COLLATE utf8mb4_bin NOT NULL DEFAULT 'CNY' COMMENT '币种，枚举，三位字母缩写',
  `course_price` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '生成订单时的课程标价,单位分',
  `amount` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '实际总成交价格，单位分',
  `pay_way` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '交易方式，枚举：MIX(混合模式),\nCASH(现金),\nCOUPON(优惠券),\nACCOUNT(现金账户),\nCREDIT_CARD(信用卡),\nDEBIT_CARD(借记卡),\nBIZ_ACCOUNT(对公账户),\nALIPAY(支付宝),\nWECHAT(微信),\n',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '订购人ID',
  `user_name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订购人姓名',
  `description` varchar(512) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '描述',
  `state` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT 'READY' COMMENT '订单状态，枚举:\nREADY(就绪),\nSUCCESSFUL(成功),\nPROCESSING(处理中),\nCANCELLED(取消),\nREFUNDING(申请退款中)，REFUND_REFUNED（拒绝退款），PART_REFUND（部分退款），FULL_REFUND（全额退款），\nCLOSED（关闭）',
  `order_source` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单来源, 枚举：\nWEB(web),\nAPP(app),\nWECHAT(wechat),\nTHIRDPARTY(第三方应用),\nINTERNAL(内部生成)',
  `settle_state` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT 'NO_START' COMMENT '清结算状态，枚举：\nNO_START(未开始),\nCHECKED(已对账),\nCLEANED(已清算-订单清算),WAIT_SETTLED(待结算),PRE_SETTLED(预结算),\nSETTLED(已结算)',
  `related_order_no` varchar(45) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '相关订单号，退款时，填写被退款的订单号。',
  `refund_desc` varchar(2048) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '退款发生时的退款描述',
  `complete_time` datetime DEFAULT NULL COMMENT '订单完成时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint(20) DEFAULT NULL COMMENT '最后更新人编号',
  `teacher_id` bigint(20) unsigned DEFAULT NULL COMMENT '课程发布的教师ID，冗余字段',
  `teacher_name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '发布课程的教师姓名',
  `org_id` bigint(20) unsigned DEFAULT NULL COMMENT '课程所属的机构ID或发布的机构ID，冗余字段，不为空',
  `org_name` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '机构名称',
  `sp_id` bigint(20) unsigned NOT NULL COMMENT '课程所属运营商ID，冗余字段',
  `sp_name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '运营商名称',
  `user_number` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL,
  `teacher_number` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL,
  `close_type` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '关闭类型',
  `close_reason` varchar(2048) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '关闭原因',
  `kd_settle_state` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '阔地结算状态：已清算CLEAND、待结算WAIT_SETTLED、预结算PRE_SETTLED、已结算SETTLED',
  `sp_settle_state` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '运营商结算状态：已清算CLEAND、待结算WAITSETTLED、预结算PRESETTLED、已结算SETTLED',
  `org_settle_state` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '机构结算状态：已清算CLEAND、待结算WAIT_SETTLED、预结算PRE_SETTLED、已结算SETTLED',
  `teacher_settle_state` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '教师结算状态：已清算CLEAND、待结算WAIT_SETTLED、预结算PRE_SETTLED、已结算SETTLED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_number`),
  KEY `idx_fk_course_id` (`course_id`),
  KEY `idx_fk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='订单表';

-- ----------------------------
-- Records of pay_order
-- ----------------------------
INSERT INTO `pay_order` VALUES ('1', '010001002017102971310517000290', 'BUY_COURSE', null, '118', '1234', 'CNY', '0', '0', 'FREE', '643', '普通学员02', null, 'SUCCESSFUL', null, 'NO_START', null, null, null, '2017-12-20 19:54:16', '2017-12-25 20:04:01', null, null, null, null, '317', null, '2', '百度运营商', '10100000000047097962', null, null, null, null, null, null, null);
INSERT INTO `pay_order` VALUES ('2', '010001002017102971310517000291', 'BUY_COURSE', null, '141', '机构收费直播语文', 'CNY', '900', '900', 'ALIPAY', '643', '普通学员02', null, 'PROCESSING', null, 'NO_START', null, null, null, '2017-12-20 21:06:02', '2017-12-25 21:08:38', null, null, null, null, '327', null, '2', '百度运营商', '10100000000047097962', null, null, null, null, null, null, null);
INSERT INTO `pay_order` VALUES ('3', '010001002017102971310517000292', 'BUY_COURSE', null, '1', '测试机构发布直播课程', 'CNY', '0', '0', 'FREE', '643', '普通学员02', null, 'SUCCESSFUL', null, 'NO_START', null, null, null, '2017-12-25 15:53:57', '2017-12-25 20:07:45', null, null, null, null, '312', null, '2', '百度运营商', '10100000000047097962', null, null, null, null, null, null, null);

-- ----------------------------
-- Table structure for pay_order_clean_record
-- ----------------------------
DROP TABLE IF EXISTS `pay_order_clean_record`;
CREATE TABLE `pay_order_clean_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `business_type` varchar(255) DEFAULT NULL COMMENT '业务类型',
  `clean_date` varchar(32) NOT NULL COMMENT '清算日期',
  `clean_start_time` datetime NOT NULL COMMENT '开始时间',
  `clean_end_time` datetime NOT NULL COMMENT '结束时间',
  `clean_result` varchar(32) NOT NULL COMMENT '清算结果',
  `clean_num` int(11) NOT NULL COMMENT '清算笔数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_by` bigint(20) unsigned DEFAULT NULL COMMENT '创建人',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单清算记录表';

-- ----------------------------
-- Records of pay_order_clean_record
-- ----------------------------

-- ----------------------------
-- Table structure for pay_refund_seq
-- ----------------------------
DROP TABLE IF EXISTS `pay_refund_seq`;
CREATE TABLE `pay_refund_seq` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `order_number` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单号',
  `action_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '操作类型，枚举：\nREQUEST（请求）、\nAUDIT（审计）、CANCEL（取消）',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '当action_type为REQUEST时填写申请退款人的用户id；当action_type为AUDIT时，根据审核人员主体，可以是机构管理员用户id，也可以是独立教师的用户id',
  `request_cause` varchar(2048) COLLATE utf8mb4_bin NOT NULL COMMENT '申请退款原因，申请人填写',
  `operator_id` bigint(20) unsigned DEFAULT NULL COMMENT '审计退款的操作员',
  `action` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '审计结果，枚举：\nAGREE（同意）、\nREFUSE（拒绝）',
  `refund_amount` bigint(20) unsigned DEFAULT NULL COMMENT '退款金额',
  `refund_desc` varchar(2048) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '退款描述, 审核的人填写',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint(20) DEFAULT NULL COMMENT '最后更新人编号',
  PRIMARY KEY (`id`),
  KEY `idx_fk_user_Id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='记录退款处理过程';

-- ----------------------------
-- Records of pay_refund_seq
-- ----------------------------

-- ----------------------------
-- Table structure for pay_setting
-- ----------------------------
DROP TABLE IF EXISTS `pay_setting`;
CREATE TABLE `pay_setting` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint(20) DEFAULT NULL COMMENT '最后更新人编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='支付设置表。支付宝、微信支付的设置信息等';

-- ----------------------------
-- Records of pay_setting
-- ----------------------------

-- ----------------------------
-- Table structure for pay_trade
-- ----------------------------
DROP TABLE IF EXISTS `pay_trade`;
CREATE TABLE `pay_trade` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `trade_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '交易号',
  `title` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  `order_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '订单号',
  `trade_type` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '交易单类型，枚举：\nPAY(支付),\nREFUND(退款),\nRECHARGE(充值),\nWITHDRAW(提现),\nTRANSFER(转账)，\nWITHHOLD(代扣),\nAGENT_PAY(代付)\n',
  `currency` varchar(3) COLLATE utf8mb4_bin NOT NULL DEFAULT 'CNY' COMMENT '实际支付币种',
  `amount` bigint(20) unsigned NOT NULL COMMENT '交易价格',
  `pay_way` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '交易方式，枚举：MIX(混合模式),\nCASH(现金),\nCOUPON(优惠券),\nACCOUNT(现金账户),\nCREDIT_CARD(信用卡),\nDEBIT_CARD(借记卡),\nBIZ_ACCOUNT(对公账户),\nALIPAY(支付宝),\nWECHAT(微信),\n',
  `from_account` varchar(45) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '源账号，银行卡为card_index，支付宝、微信为account key',
  `to_account` varchar(45) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '目标账号，银行卡为card_index，支付宝、微信为account key',
  `state` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT 'READY' COMMENT '订单状态，枚举:\nREADY(就绪),\nSUCCESSFUL(成功),\nPROCESSING(处理中),\nCANCELLED(取消),\nCLOSED(关闭)',
  `settle_state` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT 'NO_START' COMMENT '清结算状态，枚举：\nNO_START(未开始),\nCHECKED(已对账),\nCLEANED(已清算),\nSETTLED(已结算),\nREFUND(已退款)',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint(20) DEFAULT NULL COMMENT '最后更新人编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trade_no` (`trade_number`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='交易单表，一个订单可以拆分成多笔交易，如一笔订单包括现金支付、红包支付等等；再如一笔订单分多次支付等等。';

-- ----------------------------
-- Records of pay_trade
-- ----------------------------
INSERT INTO `pay_trade` VALUES ('1', '2017113251311141000058', null, '010001002017102971310517000290', null, 'CNY', '0', 'FREE', null, null, 'SUCCESSFUL', 'NO_START', '2017-12-20 19:54:17', '2017-12-25 20:04:01', null, null);
INSERT INTO `pay_trade` VALUES ('2', '2017113251311141000059', null, '010001002017102971310517000292', null, 'CNY', '0', 'FREE', null, null, 'SUCCESSFUL', 'NO_START', '2017-12-25 15:53:57', '2017-12-25 20:07:45', null, null);
INSERT INTO `pay_trade` VALUES ('3', '2017113251311141000085', null, '010001002017102971310517000291', null, 'CNY', '900', 'ALIPAY', null, null, 'READY', 'NO_START', '2017-12-25 21:08:27', '2017-12-25 21:08:27', null, null);

-- ----------------------------
-- Table structure for pay_trade_seq
-- ----------------------------
DROP TABLE IF EXISTS `pay_trade_seq`;
CREATE TABLE `pay_trade_seq` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `seq_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '流水号',
  `trade_number` varchar(30) COLLATE utf8mb4_bin NOT NULL COMMENT '交易编号',
  `currency` varchar(3) COLLATE utf8mb4_bin NOT NULL DEFAULT 'CNY' COMMENT '实际支付币种',
  `amount` bigint(20) unsigned NOT NULL COMMENT '交易价格',
  `channel_type` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '渠道类型:ALIPAY、WECHATPAY',
  `state` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT 'PROCESSING' COMMENT '流水单状态，枚举:\nSUCCESSFUL(成功),\nFAILED(失败),\nPROCESSING(处理中)',
  `check_flag` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT 'NO' COMMENT '对账状态，枚举：YES（已对账）、NO（未对账）',
  `check_result` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '对账结果，枚举：RIGHT(与渠道结果一致),WRONG(与渠道结果不一致)',
  `check_comment` varchar(512) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '对账结果描述，对账与渠道结果不一致时填写原因',
  `channel_seq_number` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '渠道的交易流水号',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人编号',
  `update_by` bigint(20) DEFAULT NULL COMMENT '最后更新人编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sequence_no` (`seq_number`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='交易流水表，记录一个交易单调用支付渠道的一次支付过程，一个交易单可以多次调用支付渠道支付，直到成功或失败。';

-- ----------------------------
-- Records of pay_trade_seq
-- ----------------------------
INSERT INTO `pay_trade_seq` VALUES ('1', '2017102971310568000244', '2017113251311141000058', 'CNY', '0', 'KXT', 'SUCCESSFUL', 'NO', null, null, null, '2017-12-25 20:04:06', '2017-12-25 20:04:06', null, null);
INSERT INTO `pay_trade_seq` VALUES ('2', '2017102971310568000245', '2017113251311141000059', 'CNY', '0', 'KXT', 'SUCCESSFUL', 'NO', null, null, null, '2017-12-25 20:07:36', '2017-12-25 20:07:36', null, null);
INSERT INTO `pay_trade_seq` VALUES ('3', '2017102971310568000246', '2017113251311141000085', 'CNY', '900', 'ALIPAY', 'PROCESSING', 'NO', null, null, null, '2017-12-25 21:08:29', '2017-12-25 21:08:29', null, null);

-- ----------------------------
-- Table structure for pay_wechatpay_seq
-- ----------------------------
DROP TABLE IF EXISTS `pay_wechatpay_seq`;
CREATE TABLE `pay_wechatpay_seq` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `trade_time` varchar(32) DEFAULT NULL COMMENT '交易时间',
  `official_accounts_id` varchar(32) DEFAULT NULL COMMENT '公众账号ID',
  `business_no` varchar(128) DEFAULT NULL COMMENT '商户号',
  `sub_business_no` varchar(128) DEFAULT NULL COMMENT '子商户号',
  `equipment_no` varchar(32) DEFAULT NULL COMMENT '设备号',
  `wx_order_no` varchar(128) DEFAULT NULL COMMENT '微信订单号',
  `business_order_no` varchar(64) DEFAULT NULL COMMENT '商户订单号',
  `user_identification` varchar(10) DEFAULT NULL COMMENT '用户标识',
  `trade_type` varchar(32) DEFAULT NULL COMMENT '交易类型',
  `trade_state` varchar(16) DEFAULT NULL COMMENT '交易状态',
  `pay_bank` varchar(64) DEFAULT NULL COMMENT '付款银行',
  `currency` varchar(32) DEFAULT NULL COMMENT '货币种类',
  `total_amount` varchar(16) DEFAULT NULL COMMENT '总金额',
  `deduction` varchar(16) DEFAULT NULL COMMENT '代金券或立减优惠金额',
  `commodity_name` varchar(128) DEFAULT NULL COMMENT '商品名称',
  `business_data` varchar(2048) DEFAULT NULL COMMENT '商户数据包',
  `fee` varchar(10) DEFAULT NULL COMMENT '手续费',
  `rate` varchar(10) DEFAULT NULL COMMENT '费率',
  `wx_refund_number` varchar(255) DEFAULT NULL COMMENT '微信退款单号',
  `business_refund_number` varchar(255) DEFAULT NULL COMMENT '商户退款单号',
  `refund_amount` varchar(16) DEFAULT NULL COMMENT '退款金额',
  `deduction_refund_amount` varchar(16) DEFAULT NULL COMMENT '代金券或立减优惠退款金额',
  `refund_type` varchar(32) DEFAULT NULL COMMENT '退款类型',
  `refund_state` varchar(16) DEFAULT NULL COMMENT '退款状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信对账单数据表';

-- ----------------------------
-- Records of pay_wechatpay_seq
-- ----------------------------
