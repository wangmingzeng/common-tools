
package cn.com.zach.tools.increment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.incrementer.AbstractColumnMaxValueIncrementer;

/**
 * mysql库中创建sequence函数: -- ------------------------------------------- -- --
 * mysql模拟oacle的sequence -- 下一个值(按照设定步长增长): select nextval('seqName'); --
 * 下一块(直接指定增长值) : select nextblock('seqName',1000); -- 当前值: select
 * currval('seqName'); -- 设置值: SELECT setval('seqName', 1000); --
 * ------------------------------------------- --
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
 * -- 函数创建完成后权限还原 set global log_bin_trust_function_creators=0;
 * 
 * -- 初始化 INSERT INTO sequence VALUES('sequence',1000,1); COMMIT;
 * 
 * -- 添加新sequence -- INSERT INTO sequence VALUES('seqName',1000,10);
 * 
 * 
 * 
 * @author zach
 *
 */
public class MySQLSequenceMaxValueIncrementer extends AbstractColumnMaxValueIncrementer {

	/** 下一个值 */
	private long nextId = 0;

	/** 最大值 */
	private long maxId = 0;

	/**
	 * 默认构造函数
	 */
	public MySQLSequenceMaxValueIncrementer() {
	}

	/**
	 * 带参数构造函数
	 * 
	 * @param dataSource
	 * @param incrementerName
	 * @param columnName
	 */
	public MySQLSequenceMaxValueIncrementer(DataSource dataSource, String incrementerName, String columnName) {
		super(dataSource, incrementerName, columnName);
	}

	@Override
	public void setColumnName(String columnName) {
		super.setColumnName(columnName);
		super.setIncrementerName(columnName);
	}

	@Override
	public void setIncrementerName(String incrementerName) {
		super.setColumnName(incrementerName);
		super.setIncrementerName(incrementerName);
	}

	@Override
	protected synchronized long getNextKey() throws DataAccessException {
		if (this.maxId == this.nextId) {
			Connection con = DataSourceUtils.getConnection(getDataSource());
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
				ResultSet rs = stmt.executeQuery(
						"SELECT nextblock( '" + getColumnName() + "'," + getCacheSize() + " ) AS nextkey");
				try {
					this.maxId = rs.getLong(1);
				} finally {
					JdbcUtils.closeResultSet(rs);
				}
				this.nextId = this.maxId - getCacheSize() + 1;
			} catch (SQLException ex) {
				throw new DataAccessResourceFailureException("自增列获取失败", ex);
			} finally {
				JdbcUtils.closeStatement(stmt);
				DataSourceUtils.releaseConnection(con, getDataSource());
			}
		} else {
			this.nextId++;
		}
		return this.nextId;
	}
}
