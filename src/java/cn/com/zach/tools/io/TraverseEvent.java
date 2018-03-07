package cn.com.zach.tools.io;

import java.io.File;

/**
 * traverse 遍历文件事件
 * @author zach
 *
 */
public interface TraverseEvent
{

	/**
	 * 执行遍历时间
	 * @param file
	 */
	public void doEvent(int number, File node);

	/**
	 * 是否终止当前递归
	 * @return
	 */
	public boolean isAbort();
}
