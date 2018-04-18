
package cn.com.zach.tools.increment;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

/**
 * 使用本地文件保存自增序列值
 * @author zach
 */
public abstract class AbstractLocalSequenceMaxValueIncrementer
		implements DataFieldMaxValueIncrementer, InitializingBean, DisposableBean {

	/**
	 * 默认sequence存储文件在classpath目录下
	 */
	private static final String DEFAULT_LOCAL_FILE = "classpath:/";

	/**
	 * 每次写入文件增加块大小
	 */
	protected int blockSize = 1000;

	/**
	 * 当前值
	 */
	private volatile long currentId = 0;

	/**
	 * 前导0个数
	 */
	private int paddingLength = 0;

	/**
	 * 自增sequence值保存的本地文件
	 */
	private String localFile = null;

	/**
	 * 是否一直打开文件, false:否, 文件写入时打开, true:文件打开后永不关闭,持续写入
	 */
	private boolean alwaysOpenLocalFile = true;

	/**
	 * 写sequence文件
	 */
	private RandomAccessFile accessFile = null;

	/**
	 * 使用重入锁互斥实现互斥操作
	 */
	private final Lock lock = new ReentrantLock();

	public String getLocalFile() {
		return localFile;
	}

	public void setLocalFile(String localFile) {
		this.localFile = localFile;
	}

	public void setPaddingLength(int paddingLength) {
		this.paddingLength = paddingLength;
	}

	public int getPaddingLength() {
		return this.paddingLength;
	}

	/**
	 * @return the alwaysOpenLocalFile
	 */
	public boolean isAlwaysOpenLocalFile() {
		return alwaysOpenLocalFile;
	}

	/**
	 * @param alwaysOpenLocalFile
	 *            the alwaysOpenLocalFile to set
	 */
	public void setAlwaysOpenLocalFile(boolean alwaysOpenLocalFile) {
		this.alwaysOpenLocalFile = alwaysOpenLocalFile;
	}

	/**
	 * @return the blockSize
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * @param blockSize
	 *            the blockSize to set
	 */
	public void setBlockSize(int blockSize) {
		if (blockSize > 5) {
			this.blockSize = blockSize;
		}
	}

	/**
	 * 获取下一个序列块
	 * 
	 * @param blockSize
	 * @return
	 */
	protected final long applyNextBlock(int blockSize) {
		long nextBlock = 0;
		try {
			if (accessFile == null) {
				// 读写方式打开文件
				accessFile = new RandomAccessFile(localFile, "rw");
			}
			// 文件指针偏移到0位置
			accessFile.seek(0);
			if (accessFile.length() == 0) {
				accessFile.writeLong(blockSize);
				nextBlock = blockSize;
			} else {
				// 读出当前文件sequence值,在此基础上增加指定的块大小
				nextBlock = accessFile.readLong() + blockSize;
				// 把新的sequence写入文件
				accessFile.seek(0);
				accessFile.writeLong(nextBlock);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!alwaysOpenLocalFile && accessFile != null) {
				try {
					accessFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				accessFile = null;
			}
		}
		return nextBlock;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 查找sequence文件位置,找不到使用默认文件路径
		ResourceLoader loader = new DefaultResourceLoader();
		File file = null;
		if (localFile == null || localFile.length() == 0) {
			localFile = DEFAULT_LOCAL_FILE;
		}
		Resource resource = loader.getResource(localFile);
		if (!resource.exists()) {
			resource = loader.getResource(DEFAULT_LOCAL_FILE);
		}
		file = resource.getFile();
		// 如果是目录,在目录下新建文件
		if (file.isDirectory()) {
			file = new File(file, "incrementer.sequence.dat");
		}
		// 文件不存在创建空文件
		if (!file.exists()) {
			file.createNewFile();
		}
		this.localFile = file.getPath();
	}

	@Override
	public void destroy() {
		// 关闭打开文件
		if (accessFile != null) {
			try {
				accessFile.close();
				accessFile = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public long nextLongValue() {
		long answer = 0;
		lock.lock();
		try {
			answer = this.currentId = getNextKey();
		} finally {
			lock.unlock();
		}
		return answer;
	}

	@Override
	public int nextIntValue() throws DataAccessException {
		return (int) nextLongValue();
	}

	@Override
	public String nextStringValue() throws DataAccessException {
		String s = Long.toString(nextLongValue());
		int len = s.length();
		if (len < this.paddingLength) {
			StringBuilder sb = new StringBuilder(this.paddingLength);
			for (int i = 0; i < this.paddingLength - len; i++) {
				sb.append('0');
			}
			sb.append(s);
			s = sb.toString();
		}
		return s;
	}

	/**
	 * 获取当前值
	 * 
	 * @return
	 * @throws DataAccessException
	 */
	public long currentLongValue() throws DataAccessException {
		return this.currentId;
	}

	protected abstract long getNextKey();
}
