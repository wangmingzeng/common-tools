
package cn.com.zach.tools.increment;

import java.net.InetAddress;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

/**
 * uuid主键生成方式, 生成算法: IP + JVM + Time + counter
 * 
 * @author zach
 */
public class UUIDMaxValueIncrementer implements DataFieldMaxValueIncrementer {

	/**
	 * 本机IP地址
	 */
	private static final int IP;
	/**
	 * 获取本机ip地址
	 */
	static {
		int ipadd;
		try {
			ipadd = IptoInt(InetAddress.getLocalHost().getAddress());
		} catch (Exception e) {
			ipadd = 0;
		}
		IP = ipadd;
	}

	/**
	 * 模拟进程
	 */
	private static final int JVM = (int) (System.currentTimeMillis() >>> 8);

	/**
	 * 计数器
	 */
	private static short counter = (short) 0;

	public static int IptoInt(byte[] bytes) {
		int result = 0;
		for (int i = 0; i < 4; i++) {
			result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
		}
		return result;
	}

	/**
	 * 获取jvm进程id(模拟id)
	 * 
	 * @return
	 */
	protected int getJVM() {
		return JVM;
	}

	/**
	 * 获取自增
	 * 
	 * @return
	 */
	protected short getCount() {
		synchronized (UUIDMaxValueIncrementer.class) {
			if (counter < 0)
				counter = 0;
			return counter++;
		}
	}

	/**
	 * ip
	 * 
	 * @return
	 */
	protected int getIP() {
		return IP;
	}

	/**
	 * 时间高32位
	 * 
	 * @return
	 */
	protected short getHiTime() {
		return (short) (System.currentTimeMillis() >>> 32);
	}

	/**
	 * 时间低32位
	 * 
	 * @return
	 */
	protected int getLoTime() {
		return (int) System.currentTimeMillis();
	}

	protected String format(int intval) {
		String formatted = Integer.toHexString(intval);
		StringBuffer buf = new StringBuffer("00000000");
		buf.replace(8 - formatted.length(), 8, formatted);
		return buf.toString();
	}

	protected String format(short shortval) {
		String formatted = Integer.toHexString(shortval);
		StringBuffer buf = new StringBuffer("0000");
		buf.replace(4 - formatted.length(), 4, formatted);
		return buf.toString();
	}

	@Override
	public int nextIntValue() {
		throw new RuntimeException("uuid主键不支持int类型生成");
	}

	@Override
	public long nextLongValue() {
		throw new RuntimeException("uuid主键不支持long类型生成");
	}

	@Override
	public String nextStringValue() {
		return new StringBuffer(36).append(format(getIP())).append(format(getJVM())).append(format(getHiTime()))
				.append(format(getLoTime())).append(format(getCount())).toString();
	}
}
