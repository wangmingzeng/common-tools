
package cn.com.zach.tools.increment;

/**
 * hilo算法: id=hi*(max_lo+1)+lo, lo值在1到max_lo直接循环, lo值每循环一圈，hi值就增一.
 * 
 * @author zach
 */
public class HiLoMaxValueIncrementer extends AbstractLocalSequenceMaxValueIncrementer {

	private long hi = 1;

	private long lo = 1;

	public long getNextKey() {
		long answer = hi * (blockSize + 1) + lo;
		if (lo > blockSize) {
			lo = 1;
			hi++;
			applyNextBlock(1);
		} else {
			lo++;
		}
		return answer;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 判断设置步长是否超过块大小
		super.afterPropertiesSet();
		hi = applyNextBlock(blockSize);
	}
}
