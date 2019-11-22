package cn.com.zach.tools.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class NIOUtils {
	
	/**
	 * 流结束EOF标记
	 */
	private final static int EOF = -1;
	
	/**
	 * 1KB大小
	 */
	public final static int ONE_KB = 1024;
	
	/**
	 * 1MB大小
	 */
	public final static int ONE_MB = ONE_KB * ONE_KB;

	/**
	 * 默认bio缓冲区大小100K
	 */
	public final static int DEFAULT_BUFFER_SIZE = 100 * ONE_KB;
	
	/**
	 * 默认nio缓冲区大小
	 */
	public final static int		FILE_COPY_BUFFER_SIZE	= ONE_MB * 30;
	
	/**
	 * 把ByteBuffer转换输入流
	 * 
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public final static InputStream toInputStream(final ByteBuffer buffer) throws IOException {
		InputStream answer = new InputStream() {
			public int available() {
				return buffer.remaining();
			}

			public synchronized int read() throws IOException {
				if (!buffer.hasRemaining()) {
					return -1;
				}
				return buffer.get();
			}

			public synchronized int read(byte[] bytes, int off, int len) throws IOException {
				if (!buffer.hasRemaining())
					return -1;
				len = Math.min(len, buffer.remaining());
				buffer.get(bytes, off, len);
				return len;
			}
		};
		return answer;
	}
	
	/**
	 * 把内存缓冲区写入到输出流中, 并且关闭输出流.
	 * @param input 源输入流
	 * @param output 目标输出流
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(ByteBuffer input, OutputStream output) throws IOException {
		return write(toInputStream(input), output);
	}
	
	/**
	 * 把字节缓冲区写入文件
	 * @param input 缓冲区
	 * @param output 文件
	 * @return
	 * @throws IOException
	 */
	public final static long write(ByteBuffer input, File output) throws IOException {
		return write(toInputStream(input), output, true);
	}
	
	/**
	 * 把输入流写入文件
	 * @param input 输入流
	 * @param output 目标文件
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(InputStream input, File output, boolean autoClose) throws IOException {
		output.getParentFile().mkdirs();
		return write(input, new FileOutputStream(output), autoClose);
	}
	
	/**
	 * 把输入写入到输出流,同时自动关闭输入和输出流
	 * @param input 源输入流
	 * @param output 目标输出流
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(InputStream input, OutputStream output) throws IOException {
		long count = write(input, output, new byte[DEFAULT_BUFFER_SIZE], true, true);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return count;
	}

	/**
	 * 把输入流写入到输出流
	 * @param input 源输入流
	 * @param output 目标输出流
	 * @param autoClose 是否自动关闭输入和输出流. true:自动关闭输入输出流; false:不关闭输入输出流;
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(InputStream input, OutputStream output, boolean autClose) throws IOException {
		long count = write(input, output, new byte[DEFAULT_BUFFER_SIZE], autClose, autClose);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return count;
	}
	
	/**
	 * 把输入写入到输出流
	 * @param input 输入流
	 * @param output 输出流
	 * @param buffer 缓冲区
	 * @param closeInputStream 是否自动关闭输出流
	 * @param closeOutputStream 是否自动关闭输出流
	 * @return 返回写入长度
	 * @throws IOException
	 */
	public final static long write(InputStream input, OutputStream output, byte[] buffer, boolean closeInputStream, boolean closeOutputStream) throws IOException {
		long count = 0;
		int n = 0;
		try {
			while (EOF != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
			}
		} finally {
			if (closeInputStream) {
				input.close();
			}
			if (closeOutputStream) {
				output.close();
			}
		}
		return count;
	}
	
	/**
	 * 大文件复制
	 * @param src 只读管道
	 * @param dest 可写入管道
	 * @throws IOException
	 */
	public final static void save(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
		save(src, dest, ONE_MB, true);
	}

	/*10M申请对外内存, 主要用于复制大文件, 比如: 10M以上流, 1G文件
	 * <pre>
	 * 	 //src.txt大小超过10M
	 * 	 File src = new File("/tmp/src.txt");
	 * 	 ReadableByteChannel rc = new FileInputStream(src).getChannel();
	 *
	 * 	 File dest = new File("/tmp/dest.txt");
	 * 	 WritableByteChannel wc = new FileInputStream(dest).getChannel();
	 * 	 save(rc , wc , ONE_MB, true)
	 * </pre>
	 * @param src 只读管道
	 * @param dest 可写入管道
	 * @param bufferSize 缓冲大小, 如果申请的缓冲区大小超过10M, 使用堆外内存(即:向操作系统申请内存), 否则使用堆内内存(即:向JVM申请内存)
	 * @param autoClose 是否自动关闭管道
	 * @throws IOException
	 */
	public final static void save(ReadableByteChannel src, WritableByteChannel dest, int bufferSize, boolean autoClose) throws IOException{
		//如果申请的缓冲区大小超过10M, 使用堆外内存(即:向操作系统申请内存), 否则使用堆内内存(即:向JVM申请内存)
		//注意如果申请DirectBuffer, GC无法回收这部分内存. 使用不当可能造成DirectBuffer溢出.
		//使用-XX:MaxDirectMemorySize来指定最大的堆外内存大小.默认是64M
		//建议在大文件复制时使用DirectBuffer
		ByteBuffer buffer = bufferSize >= 10 * ONE_MB ? ByteBuffer.allocateDirect( bufferSize ) : ByteBuffer.allocate( bufferSize );
		try {
			while (src.read(buffer) != EOF) {
				buffer.flip();
				// 保证缓冲区的数据全部写入
				while (buffer.hasRemaining()) {
					dest.write(buffer);
				}
				buffer.clear();
			}
			buffer.clear();
			// 如果申请堆外内存,这部分内存需要手动释放, GC无法回收这部分内存
			if (buffer.isDirect()) {
				privileged(buffer);
			}
			buffer = null;
		} finally {
			if (autoClose) {
				src.close();
				dest.close();
			}
		}
	}
	
	/**
	 * 释放堆外内存
	 * @param byteBuffer
	 */
	private final static void privileged(final ByteBuffer byteBuffer) {
		try {
			// 释放堆外内存,jdk是通过sun.misc.Cleaner释放堆外内存,这是一个内部类外部不能访问
			// 通过反射方式调用
			java.lang.reflect.Method cleanerMethod = byteBuffer.getClass().getMethod("cleaner", new Class[0]);
			cleanerMethod.setAccessible(true);
			Object cleaner = cleanerMethod.invoke(byteBuffer, new Object[0]);
			java.lang.reflect.Method cleanMethod = cleaner.getClass().getMethod("clean", new Class[0]);
			cleanMethod.setAccessible(true);
			cleanMethod.invoke(cleaner, new Object[0]);
			// 内部方法不能直接调用
			// sun.misc.Cleaner cleaner =
			// (sun.misc.Cleaner)getCleanerMethod.invoke(byteBuffer, new Object[0]);
			// cleaner.clean();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 使用nio复制文件
	 * @param srcFile 源文件
	 * @param destFile 目标文件
	 * @param preserveFileDate 是或否修改文件最后一次修改时间
	 * @throws IOException
	 */
	public final static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
		try(FileInputStream fis = new FileInputStream(srcFile);
				FileOutputStream fos = new FileOutputStream(destFile);
				FileChannel input = fis.getChannel();
				FileChannel output = fos.getChannel()){
			long size = input.size();
			long position = 0;
			long count = 0;
			while(position < size) {
				count = ((size - position) > FILE_COPY_BUFFER_SIZE)? FILE_COPY_BUFFER_SIZE : (size - position);
				position += output.transferFrom(input, position, count);
			}
		}
		if (srcFile.length() != destFile.length()) {
			throw new IOException("文件复制失败.");
		}
		if (preserveFileDate) {
			destFile.setLastModified(srcFile.lastModified());
		}
	}
	
	/**
	 * 使用utf-8字符集把字符串转换ByteBuffer对象.
	 * @param input 输入流
	 * @return
	 * @throws IOException
	 */
	public final static ByteBuffer toByteBuffer(String input) throws IOException {
		return ByteBuffer.wrap(input.getBytes("utf-8"));
	}
	
	/**
	 * 把文件转换ByteBuffer对象
	 * @param input 输入流
	 * @return
	 * @throws IOException
	 */
	public final static ByteBuffer toByteBuffer(File input) throws IOException {
		return toByteBuffer(new FileInputStream(input), new byte[DEFAULT_BUFFER_SIZE]);
	}
	
	/**
	 * 把网络地址内容转换ByteBuffer对象
	 * @param input 输入流
	 * @return
	 * @throws IOException
	 */
	public final static ByteBuffer toByteBuffer(URL input) throws IOException {
		return toByteBuffer(input.openStream(), new byte[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * 把输入流转换ByteBuffer对象
	 * @param input 输入流
	 * @return
	 * @throws IOException
	 */
	public final static ByteBuffer toByteBuffer(InputStream input) throws IOException {
		return toByteBuffer(input, new byte[DEFAULT_BUFFER_SIZE]);
	}
	
	/**
	 * 把输入流转换ByteBuffer对象
	 * @param input 输入流
	 * @param buffer 转换使用缓冲区
	 * @return
	 * @throws IOException
	 */
	public final static ByteBuffer toByteBuffer(InputStream input, byte[] buffer) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		write(input, output, buffer, true, true);
		// 复用ByteArrayOutputStream内部缓冲区, 直接构建ByteBuffer
		return ByteBuffer.wrap(output.toByteArray(), 0, output.size());
	}
}
