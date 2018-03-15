
package cn.com.zach.tools.increment;

/**
 * 自增加1序列生成
 * 
 * @author zach
 */
public class SequenceMaxValueIncrementer extends AbstractLocalSequenceMaxValueIncrementer {

	/**
	 * 获取下一个值自增步长
	 */
	private int delta = 1;

	/**
	 * 当前值
	 */
	private volatile long nextId = 0;

	/**
	 * 增加到最大值
	 */
	private volatile long maxId = 0;

	@Override
	protected long getNextKey() {
		/**
		 * 没有使用同步锁是因为:父类进行了同步,在使用时请注意.
		 */
		long answer = 0;
		answer = nextId = nextId + delta;
		if (nextId >= maxId) {
			long nextBlock = applyNextBlock(blockSize);
			this.maxId = nextBlock;
		}
		return answer;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 判断设置步长是否超过块大小
		if (delta >= blockSize) {
			delta = 1;
		}
		super.afterPropertiesSet();
		long nextBlock = applyNextBlock(blockSize);
		this.maxId = nextBlock;
		this.nextId = maxId - blockSize + delta;
	}
}
