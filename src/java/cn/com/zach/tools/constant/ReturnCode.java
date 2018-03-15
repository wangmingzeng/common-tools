package cn.com.zach.tools.constant;

/**
 * 现有项目都是前后端分离，前后端数据交互都通过http/https请求
 * 由此定义一些常用的返回码
 * @author zach
 */
public class ReturnCode {
	
	/**请求成功*/
	public static final String SUCCESS = "0";
	
	/**服务故障*/
	public static final String SYSTEM_ERROR = "1";
	
	/**参数错误*/
	public static final String PARAM_ERROR = "2";
	
	
	/**1000~以上为业务错误码*/
}
