
package cn.com.zach.tools.increment;

import javax.sql.DataSource;

import org.springframework.jdbc.support.incrementer.AbstractSequenceMaxValueIncrementer;

/**
 * 
 * 使用mysql实现类似oracle增长序列, mysql库中创建sequence函数: --
 * ------------------------------------------- -- -- mysql模拟oacle的sequence --
 * 下一个值(按照设定步长增长): select nextval('seqName'); -- 下一块(直接指定增长值) : select
 * nextblock('seqName',1000); -- 当前值: select currval('seqName'); -- 设置值: SELECT
 * setval('seqName', 1000); -- ------------------------------------------- --
 * 
 * -- mysql创建函数需要打开创建权限配置 -- set global log_bin_trust_function_creators=1;
 * 
 * -- ------------------------------------------- -- -- 创建sequence表 --
 * mysql模拟oacle的sequence生成方式 -- ------------------------------------------- --
 * DROP table if exists sequence; CREATE TABLE sequence ( name varchar(64) NOT
 * NULL COMMENT '序列名称', current_value BIGINT UNSIGNED NOT NULL DEFAULT 1000
 * COMMENT '初始值', increment INT NOT NULL DEFAULT 1 COMMENT '增长步长', PRIMARY KEY
 * (name) ) ENGINE=InnoDB DEFAULT CHARSET=utf8; -- name建立hash索引达到高效查询 ALTER
 * TABLE `sequence` ADD UNIQUE INDEX (`name`) USING HASH ;
 * 
 * -- ------------------------------------------- -- -- 功能 : 获取下一个序列值 -- 名称 :
 * nextval(seq_name) -- 参数 : seq_name 表示序列名称 -- 返回 : 下一个序列值 -- 事例 : select
 * nextval('sequence'); -- ------------------------------------------- -- DROP
 * FUNCTION IF EXISTS nextval; DELIMITER $ CREATE FUNCTION nextval(seq_name
 * VARCHAR(64)) RETURNS BIGINT UNSIGNED CONTAINS SQL BEGIN UPDATE sequence SET
 * current_value = last_insert_id(current_value+increment) WHERE name =
 * seq_name; RETURN last_insert_id(); END $ DELIMITER ;
 * 
 * -- ------------------------------------------- -- -- 功能 : 获取下一个序列值 -- 名称 :
 * nextblock(seq_name,block) -- 参数 : seq_name 表示序列名称 -- 参数 : block 表示增加序列块 -- 返回
 * : 下一个序列值 -- 事例 : select nextblock('sequence',1000); --
 * ------------------------------------------- -- DROP FUNCTION IF EXISTS
 * nextblock; DELIMITER $ CREATE FUNCTION nextblock(seq_name VARCHAR(64), block
 * INTEGER) RETURNS BIGINT UNSIGNED CONTAINS SQL BEGIN UPDATE sequence SET
 * current_value = last_insert_id(current_value+block) WHERE name = seq_name;
 * RETURN last_insert_id(); END $ DELIMITER ;
 * 
 * 
 * -- ------------------------------------------- -- -- 功能 : 获取当前序列值 -- 名称 :
 * currval(seq_name) -- 参数 : seq_name 表示序列名称 -- 返回 : 当前序列值 -- 事例 : select
 * currval('sequence'); -- ------------------------------------------- -- DROP
 * FUNCTION IF EXISTS currval; DELIMITER $ CREATE FUNCTION currval (seq_name
 * VARCHAR(64)) RETURNS BIGINT UNSIGNED CONTAINS SQL BEGIN DECLARE value
 * INTEGER; SET value = 0; SELECT current_value INTO value FROM sequence WHERE
 * name = seq_name; RETURN value; END $ DELIMITER ;
 * 
 * -- ------------------------------------------- -- -- 功能 : 设置序列起始值 -- 名称 :
 * setval(seq_name ,value) -- 参数 : seq_name 表示序列名称, value:表示初始值 -- 返回 : value值
 * -- 事例 : SELECT setval('sequence', 1000); --
 * ------------------------------------------- -- DROP FUNCTION IF EXISTS
 * setval; DELIMITER $ CREATE FUNCTION setval (seq_name VARCHAR(50), value
 * INTEGER) RETURNS BIGINT UNSIGNED CONTAINS SQL BEGIN UPDATE sequence SET
 * current_value = value WHERE name = seq_name; RETURN currval(seq_name); END $
 * DELIMITER ;
 * 
 * 
 * 
 * 
 * -- 函数创建完成后权限还原 set global log_bin_trust_function_creators=0;
 * 
 * -- 初始化 INSERT INTO sequence VALUES('sequence',1000,1); COMMIT;
 * 
 * -- 添加新sequence -- INSERT INTO sequence VALUES('seqName',1000,10);
 * 
 * 
 * @author zach
 */
public class MysqlSequenceIncrementer extends AbstractSequenceMaxValueIncrementer {

	public MysqlSequenceIncrementer() {
	}

	/**
	 * 构造函数
	 * 
	 * @param dataSource
	 * @param incrementerName
	 */
	public MysqlSequenceIncrementer(DataSource dataSource, String incrementerName) {
		super(dataSource, incrementerName);
	}

	@Override
	protected String getSequenceQuery() {
		// 这里的getIncrementerName是sequence表中name
		// 按照sequence表中increment字段设定的步长进行增长,默认是1
		// 只是模拟oracle序列
		return "SELECT nextval( '" + getIncrementerName() + "' ) AS nextkey";
	}
}
