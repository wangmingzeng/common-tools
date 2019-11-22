package cn.com.zach.tools.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * IO工具操作类
 * @author zach
 *
 */
public class IOUtils {

	/**
	 * 流结束EOF标记
	 */
	private final static int EOF = -1;
	
	/**
	 * Linux系统文件夹分隔符
	 */
	public static final char DIR_SEPARATOR_UNIX = '/';
	
	/**
	 * windows系统文件夹分隔符
	 */
	public static final char DIR_SEPARATOR_WINDOWS = '\\';
	
	/**
     * 系统文件夹分隔符
     */
    public static final char DIR_SEPARATOR = File.separatorChar;
    
    /**
     * Linux系统换行符
     */
    public static final String LINE_SEPARATOR_UNIX = "\n";
    /**
     * Windows系统换行符
     */
    public static final String LINE_SEPARATOR_WINDOWS = "\r\n";
    
    /**
     * 默认的缓冲区大小
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    
    /**
     * 默认skip()方法跳过的缓存大小
     */
    private static final int SKIP_BUFFER_SIZE = 2048;
    
    private static byte[] SKIP_BYTE_BUFFER;
    
    /**
     * 关闭一个URLConnection.
     */
    public static void close(final URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }
    
    /**
     * 从URI中获取内容，转换为byte数组
     * @param uri
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(final URI uri) throws IOException {
        return toByteArray(uri.toURL());
    }
    
    /**
     * 从URL中获取内容，转换为byte数组
     * @param url
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(final URL url) throws IOException {
        final URLConnection conn = url.openConnection();
        try {
            return toByteArray(conn);
        } finally {
            close(conn);
        }
    }
    
    /**
     * 从URLConnection中获取内容，转换为byte数组
     * @param url
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(final URLConnection urlConn) throws IOException {
        try (InputStream inputStream = urlConn.getInputStream()) {
            return toByteArray(inputStream);
        }
    }
    
    /**
     * 将InputStream的内容转换为byte数组
     * @param input
     * @param size	InputStream的大小
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(final InputStream input, final long size) throws IOException {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
        }
        return toByteArray(input, (int)size);
    }
    
    /**
     * 将InputStream的内容转换为byte数组
     * @param input
     * @param size	InputStream的大小
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(final InputStream input, final int size) throws IOException {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
        }
        if (size == 0) {
            return new byte[0];
        }
        final byte[] data = new byte[size];
        int offset = 0;
        int read;
        while (offset < size && (read = input.read(data, offset, size - offset)) != EOF) {
            offset += read;
        }
        if (offset != size) {
            throw new IOException("Unexpected read size. current: " + offset + ", expected: " + size);
        }
        return data;
    }
    
    /**
     * 将InputStream的内容转换为byte数组
     * @param input	输入流
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(final InputStream input) throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            copy(input, output);
            return output.toByteArray();
        }
    }
    
    /**
     * 将InputStream复制成OutputStream
     * @param input	输入流
     * @param output 输出流
     * @return
     * @throws IOException
     */
    public static int copy(final InputStream input, final OutputStream output) throws IOException {
        final long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }
  
    /**
     * 从一个大InputStream（over 2GB）中复制内容到输出流
     * @param input
     * @param output
     * @return
     * @throws IOException
     */
    public static long copyLarge(final InputStream input, final OutputStream output) throws IOException {
        return copy(input, output, DEFAULT_BUFFER_SIZE);
    }
    
    public static long copy(final InputStream input, final OutputStream output, final int bufferSize) throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }
    
    /**
     * 从一个大InputStream（over 2GB）中复制内容到输出流
     * @param input
     * @param output
     * @param buffer	缓冲区大小
     * @return
     * @throws IOException
     */
    public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
    
    
    /**
     * 转换字符集
     * @param charset
     * @return
     */
    public static Charset toCharset(final String charset) {
        return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
    }
    
    /**
     * 转换成字符串
     * @param input
     * @param encoding
     * @return
     * @throws IOException
     */
    public static String toString(final byte[] input, final String encoding) throws IOException {
        return new String(input, toCharset(encoding));
    }
    
    /**
     * 转换成字符串
     * @param input
     * @return
     * @throws IOException
     */
    public static String toString(final Reader input) throws IOException {
        try (final StringWriter sw = new StringWriter()) {
            copy(input, sw);
            return sw.toString();
        }
    }
    
    /**
     * 转换成字符串
     * @param uri
     * @param encoding
     * @return
     * @throws IOException
     */
    public static String toString(final URI uri, final String encoding) throws IOException {
        return toString(uri.toURL(), encoding);
    }
    
    /**
     * 转换成字符串
     * @param url
     * @param encoding
     * @return
     * @throws IOException
     */
    public static String toString(final URL url, final String encoding) throws IOException {
    	 	try (InputStream inputStream = url.openStream()) {
             return toString(inputStream, encoding);
         }
    }
    
    /**
     * 将InputStream的内容转换成字符串
     * @param input
     * @param encoding
     * @return
     * @throws IOException
     */
    public static String toString(final InputStream input, final String encoding) throws IOException {
        try (final StringWriter sw = new StringWriter()) {
        		Charset charset = toCharset(encoding);
            copy(input, sw, charset);
            return sw.toString();
        }
    }
    
    /**
     * 将InputStream的内容转换成Writer对象
     * @param input
     * @param output
     * @param inputEncoding
     * @throws IOException
     */
    public static void copy(final InputStream input, final Writer output, final Charset inputEncoding) throws IOException {
    		Charset	charset = (inputEncoding == null)? Charset.defaultCharset() : inputEncoding;
        final InputStreamReader in = new InputStreamReader(input, charset);
        copy(in, output);
    }
    
    /**
     * 将Reader的内容转换成Writer对象
     * @param input
     * @param output
     * @return
     * @throws IOException
     */
    public static int copy(final Reader input, final Writer output) throws IOException {
        final long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }
    
    public static long copyLarge(final Reader input, final Writer output) throws IOException {
        return copyLarge(input, output, new char[DEFAULT_BUFFER_SIZE]);
    }
    
    /**
     * 将Reader的内容写入到output中
     * @param input
     * @param output
     * @param buffer
     * @return
     * @throws IOException
     */
    public static long copyLarge(final Reader input, final Writer output, final char[] buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
    
    /**
     * 将资源转换成字符串
     * @param name
     * @param encoding
     * @return
     * @throws IOException
     */
    public static String resourceToString(final String name, final String encoding) throws IOException {
        return resourceToString(name, encoding, null);
    }
    
    /**
     * 将资源转换成字符串
     * @param name
     * @param encoding
     * @param classLoader
     * @return
     * @throws IOException
     */
    public static String resourceToString(final String name, final String encoding, final ClassLoader classLoader) throws IOException {
        return toString(resourceToURL(name, classLoader), encoding);
    }
    
    /**
     * 将资源转换成byte数组
     * @param name
     * @return
     * @throws IOException
     */
    public static byte[] resourceToByteArray(final String name) throws IOException {
        return resourceToByteArray(name, null);
    }
    
    /**
     * 将资源转换成byte数组
     * @param name
     * @param classLoader
     * @return
     * @throws IOException
     */
    public static byte[] resourceToByteArray(final String name, final ClassLoader classLoader) throws IOException {
        return toByteArray(resourceToURL(name, classLoader));
    }
    
    /**
     * 获取资源URL
     * @param name
     * @return
     * @throws IOException
     */
    public static URL resourceToURL(final String name) throws IOException {
        return resourceToURL(name, null);
    }
    
    /**
     * 根据“资源全路径名称”通过类加载器获取资源url
     * @param name
     * @param classLoader
     * @return
     * @throws IOException
     */
    public static URL resourceToURL(final String name, final ClassLoader classLoader) throws IOException {
        // What about the thread context class loader?
        // What about the system class loader?
        final URL resource = classLoader == null ? IOUtils.class.getResource(name) : classLoader.getResource(name);
        if (resource == null) {
            throw new IOException("Resource not found: " + name);
        }
        return resource;
    }
    
    /**
     * 将InputStream中的内容读取到List<String>中
     * @param input
     * @param encoding
     * @return
     * @throws IOException
     */
    public static List<String> readLines(final InputStream input, final String encoding) throws IOException {
        final InputStreamReader reader = new InputStreamReader(input, toCharset(encoding));
        return readLines(reader);
    }
    
    /**
     * 按行读取
     * @param input
     * @return
     * @throws IOException
     */
    public static List<String> readLines(final Reader input) throws IOException {
        final BufferedReader reader = toBufferedReader(input);
        final List<String> list = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }
    
    /**
     * 将Reader包装成BufferedReader，带缓存区
     * @param reader
     * @return
     */
    public static BufferedReader toBufferedReader(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }
    
    /**
     * 将Writer包装成BufferedWriter，带缓存区
     * @param writer
     * @return
     */
    public static BufferedWriter toBufferWriter(final Writer writer) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
    }
    
    public static BufferedOutputStream buffer(final OutputStream outputStream) {
        if (outputStream == null) { // not checked by BufferedOutputStream
            throw new NullPointerException();
        }
        return outputStream instanceof BufferedOutputStream ?
                (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream);
    }
    
    public static BufferedInputStream buffer(final InputStream inputStream) {
        if (inputStream == null) { // not checked by BufferedInputStream
            throw new NullPointerException();
        }
        return inputStream instanceof BufferedInputStream ?
                (BufferedInputStream) inputStream : new BufferedInputStream(inputStream);
    }
    
    /**
     * 将InputStream中的内容转换成char[]
     * @param is
     * @param encoding
     * @return
     * @throws IOException
     */
    public static char[] toCharArray(final InputStream is, final Charset encoding) throws IOException {
        final CharArrayWriter output = new CharArrayWriter();
        copy(is, output, encoding);
        return output.toCharArray();
    }
    
    /**
     * 将Reader中内容转换成char[]
     * @param input
     * @return
     * @throws IOException
     */
    public static char[] toCharArray(final Reader input) throws IOException {
        final CharArrayWriter sw = new CharArrayWriter();
        copy(input, sw);
        return sw.toCharArray();
    }
    
    /**
     * 将字符转换成InputStream
     * @param input
     * @param encoding
     * @return
     * @throws IOException
     */
    public static InputStream toInputStream(final CharSequence input, final String encoding) throws IOException {
        return toInputStream(input.toString(), encoding);
    }
    
    public static InputStream toInputStream(final String input, final String encoding) throws IOException {
        final byte[] bytes = input.getBytes(toCharset(encoding));
        return new ByteArrayInputStream(bytes);
    }
    
    /**
     * 将byte写入OutputStream
     * @param data
     * @param output
     * @throws IOException
     */
    public static void write(final byte[] data, final OutputStream output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }
    
    /**
     * 将byte写入OutputStream
     * @param data
     * @param output
     * @param encoding
     * @throws IOException
     */
    public static void write(final char[] data, final OutputStream output, final String encoding) throws IOException {
        if (data != null) {
            output.write(new String(data).getBytes(toCharset(encoding)));
        }
    }
    
    /**
     * 将byte写入Writer
     * @param data
     * @param output
     * @param encoding
     * @throws IOException
     */
    public static void write(final byte[] data, final Writer output, final String encoding) throws IOException {
        if (data != null) {
            output.write(new String(data, toCharset(encoding)));
        }
    }
    
    /**
     * 写字符串
     * @param data
     * @param output
     * @throws IOException
     */
    public static void write(final CharSequence data, final Writer output) throws IOException {
        if (data != null) {
            write(data.toString(), output);
        }
    }
    
    /**
     * 写字符串
     * @param data
     * @param output
     * @throws IOException
     */
    public static void write(final String data, final Writer output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }
    
    /**
     * 写字符串
     * @param data
     * @param output
     * @param encoding
     * @throws IOException
     */
    public static void write(final CharSequence data, final OutputStream output, final String encoding) throws IOException {
        if (data != null) {
            write(data.toString(), output, encoding);
        }
    }
    
    /**
     * 写字符串
     * @param data
     * @param output
     * @param encoding
     * @throws IOException
     */
    public static void write(final String data, final OutputStream output, final String encoding) throws IOException {
        if (data != null) {
            output.write(data.getBytes(toCharset(encoding)));
        }
    }
    
    
    
    /**
     * 将byte分块写入OutputStream(针对文件过大)
     * @param data
     * @param output
     * @throws IOException
     */
    public static void writeChunked(final byte[] data, final OutputStream output) throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                final int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
                output.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }
    
    /**
     * 将byte分块写入Writer(针对文件过大)
     * @param data
     * @param output
     * @throws IOException
     */
    public static void writeChunked(final char[] data, final Writer output) throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                final int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
                output.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }
    
    /**
     * 复制
     * @param input
     * @param output
     * @param inputOffset
     * @param length
     * @return
     * @throws IOException
     */
	public static long copyLarge(final InputStream input, final OutputStream output, final long inputOffset,
			final long length) throws IOException {
		return copyLarge(input, output, inputOffset, length, new byte[DEFAULT_BUFFER_SIZE]);
	}
	
	public static long copyLarge(final InputStream input, final OutputStream output, final long inputOffset,
			final long length, final byte[] buffer) throws IOException {
		if (inputOffset > 0) {
			skipFully(input, inputOffset);
		}
		if (length == 0) {
			return 0;
		}
		final int bufferLength = buffer.length;
		int bytesToRead = bufferLength;
		if (length > 0 && length < bufferLength) {
			bytesToRead = (int) length;
		}
		int read;
		long totalRead = 0;
		while (bytesToRead > 0 && EOF != (read = input.read(buffer, 0, bytesToRead))) {
			output.write(buffer, 0, read);
			totalRead += read;
			if (length > 0) { // only adjust length if not reading to the end
				// Note the cast must work because buffer.length is an integer
				bytesToRead = (int) Math.min(length - totalRead, bufferLength);
			}
		}
		return totalRead;
	}
	
	public static void skipFully(final InputStream input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        final long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }
	
	public static long skip(final InputStream input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        if (SKIP_BYTE_BUFFER == null) {
            SKIP_BYTE_BUFFER = new byte[SKIP_BUFFER_SIZE];
        }
        long remain = toSkip;
        while (remain > 0) {
            // See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
            final long n = input.read(SKIP_BYTE_BUFFER, 0, (int) Math.min(remain, SKIP_BUFFER_SIZE));
            if (n < 0) { // EOF
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }
	
	/**
	 * 内容比较
	 * @param input1
	 * @param input2
	 * @return
	 * @throws IOException
	 */
	public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }
        int ch = input1.read();
        while (EOF != ch) {
            final int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }

        final int ch2 = input2.read();
        return ch2 == EOF;
    }

	/**
	 * 内容比较
	 * @param input1
	 * @param input2
	 * @return
	 * @throws IOException
	 */
	public static boolean contentEquals(Reader input1, Reader input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        input1 = toBufferedReader(input1);
        input2 = toBufferedReader(input2);
        int ch = input1.read();
        while (EOF != ch) {
            final int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }
        final int ch2 = input2.read();
        return ch2 == EOF;
    }
	
	/**
	 * 内容比较
	 * @param input1
	 * @param input2
	 * @return
	 * @throws IOException
	 */
	public static boolean contentEqualsIgnoreEOL(final Reader input1, final Reader input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        final BufferedReader br1 = toBufferedReader(input1);
        final BufferedReader br2 = toBufferedReader(input2);
        String line1 = br1.readLine();
        String line2 = br2.readLine();
        while (line1 != null && line2 != null && line1.equals(line2)) {
            line1 = br1.readLine();
            line2 = br2.readLine();
        }
        return line1 == null ? line2 == null ? true : false : line1.equals(line2);
    }
	
	/**
	 * 触摸文件, 文件不存在则新建文件,文件存在修改文件最后的修改时间
	 * @param file 目标文件
	 * @throws IOException
	 */
	public static void touch(File file) throws IOException {
		if (!file.exists()) {
			// 文件不存在新建文件
			file.getParentFile().mkdirs();
			file.createNewFile();
		} else {
			// 修改最后的修改时间
			file.setLastModified(System.currentTimeMillis());
		}
	}
	
	/**
	 * 获取文件后缀名称
	 * @param file 文件名称,或者文件路径
	 * @return 后缀名称
	 */
	public static String suffix(File file) {
		return suffix(file.getName());
	}
	
	/**
	 * 获取文件后缀名称
	 * @param filename 文件名称,或者文件路径
	 * @return 后缀名称
	 */
	public static String suffix(String filename) {
		String answer = null;
		// 取得最后一个点号的位置
		int pos = filename.lastIndexOf('.');
		if (pos > 0) {
			answer = filename.substring(pos + 1, filename.length());
			// 判断后缀名不能包含路径分隔符
			if (answer.length() == 0 || answer.contains("/") || answer.contains("\\")) {
				answer = null;
			}
		}
		return answer;
	}
	
	/**
	 * 移动文件到目标文件或者文件夹,如果目标文件存在覆盖文件
	 * @param src 源文件
	 * @param dest 目标文件或者文件夹
	 * @return 移动是否成功
	 * @throws IOException
	 */
	public static boolean move(File src, File dest) throws IOException {
		return move(src, dest, true);
	}
	
	/**
	 * 移动文件到目标文件或者文件夹
	 * @param src 源文件
	 * @param dest 目标文件或者文件夹
	 * @param isOverride 是否覆盖目标文件
	 * @return 移动是否成功
	 * @throws IOException
	 */
	public static boolean move(File src, File dest, boolean isOverride) throws IOException {
		boolean answer = false;
		if (dest.isFile()) {
			// 判断目标文件是否是文件夹, 如果是在文件夹下建立同名文件
			if (dest.isDirectory()) {
				dest = new File(dest, src.getName());
			}
			// 判断是否覆盖目标文件
			if (dest.exists() && isOverride) {
				dest.delete();
			}
			// 在文件系统不同的情况下renameTo会失败, 失败后使用copy方法复制原文件, 然后删除原文件.
			if (!src.renameTo(dest)) {	//renameTo方法如果源文件已存在，则不会成功
				touch(dest);
				copyFile(src, dest);
				src.delete();
				answer = true;
			}
		}
		return answer;
	}
	
	/**
	 * 使用nio复制文件
	 * @param srcFile 源文件
	 * @param destFile 目标文件或者文件夹
	 * @throws IOException
	 */
	public final static void copyFile(File srcFile, File destFile) throws IOException {
		if (destFile.isFile()) {
			copyFile(srcFile, destFile, true);
		} else {
			copyFileToDirectory(srcFile, destFile);
		}
	}
	
	/**
	 * 使用io复制文件
	 * @param srcFile 源文件
	 * @param destFile 目标文件
	 * @param preserveFileDate 是或否修改文件最后一次修改时间
	 * @throws IOException
	 */
	public final static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));){
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			while(bis.read(buffer) != EOF){
				bos.write(buffer);
			}
			if(preserveFileDate) {
				destFile.setLastModified(srcFile.lastModified());
			}
		}
	}
	
	/**
	 * 使用io复制文件到文件夹
	 * @param srcFile 源文件
	 * @param destDir 目标文件夹
	 * @throws IOException
	 */
	public final static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
		File destFile = new File(destDir, srcFile.getName());
		copyFile(srcFile, destFile, true);
	}
	
	/**
	 * 使用io复制文件夹
	 * @param srcDir 源文件夹
	 * @param destDir 目标文件夹
	 * @throws IOException
	 */
	public final static void copyDirectory(File srcDir, File destDir) throws IOException {
		copyDirectory(srcDir, destDir, null, true);
	}
	
	/**
	 * 使用io复制文件夹
	 * @param srcDir 源文件夹
	 * @param destDir 目标文件夹
	 * @param filter 文件过滤器
	 * @param preserveFileDate 是或否修改文件夹最后一次修改时间
	 * @throws IOException
	 */
	public final static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate) throws IOException {
		List<String> exclusionList = null;
		if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
			File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
			if (srcFiles != null && srcFiles.length > 0) {
				exclusionList = new ArrayList<String>(srcFiles.length);
				for (File srcFile : srcFiles) {
					File copiedFile = new File(destDir, srcFile.getName());
					exclusionList.add(copiedFile.getCanonicalPath());
				}
			}
		}
		copyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
	}
	
	/**
	 * 使用nio复制文件夹
	 * @param srcDir 源文件夹
	 * @param destDir 目标文件夹
	 * @param filter 文件过滤器
	 * @param preserveFileDate 是或否修改文件夹最后一次修改时间
	 * @param exclusionList 被排除文件列表
	 * @throws IOException
	 */
	private final static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate,
			List<String> exclusionList) throws IOException {
		File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
		for (File srcFile : srcFiles) {
			File dstFile = new File(destDir, srcFile.getName());
			if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
				if (srcFile.isDirectory()) {
					copyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList);
				} else {
					copyFile(srcFile, dstFile, preserveFileDate);
				}
			}
		}
		if (preserveFileDate) {
			destDir.setLastModified(srcDir.lastModified());
		}
	}
	
	/**
	 * 使用post请求把输入流写入网络地址
	 * @param input 输入流
	 * @param output 网络地址
	 * @param header 请求头部
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String send(InputStream input, URL url, Map<String, String> header) throws IOException {
		return send(input, url, header, true);
	}
	
	/**
	 * 使用post请求把输入流写入网络地址
	 * @param input 输入流
	 * @param output 网络地址
	 * @param header 请求头部
	 * @param autoclose 是否自动关闭输入流
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String send(InputStream input, URL url, Map<String, String> header, boolean autoclose) throws IOException {
		String answer = null;
		HttpURLConnection conn = null;
		OutputStream output = null;
		InputStream response = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod( "POST" );
			conn.setConnectTimeout( 10 * 1000 );	//10s
			conn.setReadTimeout( 10 * 1000 );
			conn.setDoInput( true );
			conn.setDoOutput( true );
			if (header != null && header.size() > 0) {
				for (Map.Entry<String, String> en : header.entrySet()) {
					conn.setRequestProperty(en.getKey(), en.getValue());
				}
			}
			conn.connect();
			if(input != null) {
				//输出流
				output = conn.getOutputStream();
				//将输入流的内容写入到输出流，发送给请求
				copy(input, output);
			}
			int responseCode = conn.getResponseCode();
			response = conn.getInputStream();
			answer = toString(response, "UTF-8");
			if ( responseCode < 200 && responseCode >= 400 ){
				throw new IOException(answer);
			}
			return answer;
		}finally {
			if(autoclose) {
				if(input != null)
					input.close();
			}
			if(output != null)
				output.close();
			response.close();
			conn.disconnect();
		}
	}
	
	/**
	 * 在指定目录以及子目录中查找文件
	 * @param dir 指定目录
	 * @param match 匹配文件关键字
	 * @return
	 */
	public static List<File> search(File dir, String match) {
		List<File> answer = new ArrayList<File>();
		search(dir, answer, match, false);
		return answer;
	}

	/**
	 * 在指定目录以及子目录中查找文件或者文件目录
	 * @param dir 指定目录
	 * @param match 匹配文件关键字
	 * @param isMatchDirectory 是否匹配文件目录
	 * @return
	 */
	public static List<File> search(File dir, String match, boolean isMatchDirectory) {
		List<File> answer = new ArrayList<File>();
		search(dir, answer, match, isMatchDirectory);
		return answer;
	}
	
	/**
	 * 在指定目录以及子目录中查找文件或者文件目录
	 * @param dir 指定目录
	 * @param array 匹配文件存放列表
	 * @param match 匹配文件关键字
	 * @param isMatchDirectory 是否匹配文件目录
	 */
	public static void search(File dir, List<File> array, String match, boolean isMatchDirectory) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				if (isMatchDirectory && file.getName().indexOf(match) >= 0) {
					array.add(file);
				}
				search(file, array, match, isMatchDirectory);
			} else if (file.getName().indexOf(match) >= 0) {
				array.add(file);
			}
		}
	}
	
	/**
	 * 递归遍历目录下所有的文件,并且执行事件.
	 * @param aNode 指定目录
	 * @param filter 文件名过滤, 过滤文件名中是否包含指定的关键字.如果为null遍历所有文件子文件夹
	 * @param event 触发遍历事件
	 */
	public final static boolean traverse(File aNode, String[] filters, TraverseEvent event) {
		if (aNode.canRead()) {
			if (aNode.isDirectory()) {
				final File[] nodes = aNode.listFiles();
				boolean isAbort = false;
				for (File element : nodes) {
					// traverse( element,filters ,aFiles );
					isAbort = traverse(element, filters, event);
					if (isAbort) {
						break;
					}
				}
			} else if (aNode.isFile() && aNode.length() > 0) {
				String nodename = aNode.getName();
				if (filters != null && filters.length > 0) {
					for (String filter : filters) {
						if (nodename.indexOf(filter) >= 0) {
							event.doEvent(0, aNode);
							return event.isAbort();
						}
					}
				} else {
					event.doEvent(0, aNode);
					return event.isAbort();
				}
			}
		}
		return false;
	}
	
	/**
	 * 删除目录文件下所有子文件和子目录,目录本身不删除
	 * @param file 目录
	 */
	public final static void clean(File dir) {
		del(dir, true);
	}

	/**
	 * 删除目录文件下所有子文件和子目录,同时删除目录本身
	 * @param file
	 */
	public final static void del(File file) {
		del(file, false);
	}
	
	/**
	 * 删除文件或者文件目录,如果是目录删除目录下的所有内容
	 * @param file 文件或者文件夹
	 * @param clean 如果是目录是否删除目录本身, true: 删除自身; false:保留自身
	 */
	public final static void del(File file, boolean clean) {
		if (file.exists()) {
			if (file.isFile()) {
				// 文件直接删除
				file.delete();
			} else if (file.isDirectory()) {
				File fiels[] = file.listFiles();
				int length = fiels.length;
				if (length == 0 && !clean) {
					// 若目录下没有文件则直接删除
					file.delete();
				} else if (length >= 0) {
					// 遍历文件数组删除文件,如果是目录递归删除
					for (int i = 0; i < length; i++) {
						if (fiels[i].isDirectory()) {
							del(fiels[i], false);
						} else {
							fiels[i].delete();
						}
					}
					if (!clean) {
						file.delete();
					}
				}
			}
		}
	}
	
	/**
	 * 使用zlib(压缩算法)压缩输入到输出流中,自动关闭输入/输出流
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @throws IOException
	 */
	public final static void deflater(InputStream input, OutputStream output) throws IOException {
		deflater(input, output, true, true);
	}
	
	/**
	 * 使用zlib(压缩算法)压缩输入到输出流中
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @param closeInputStream 是否自动关闭输出流
	 * @param closeOutputStream 是否自动关闭输出流
	 * @throws IOException
	 */
	public final static void deflater(InputStream input, OutputStream output, boolean closeInputStream,
			boolean closeOutputStream) throws IOException {
		Deflater def = new Deflater(Deflater.BEST_COMPRESSION, true);
		DeflaterOutputStream dos = new DeflaterOutputStream(output, def);
		write(input, dos, new byte[DEFAULT_BUFFER_SIZE], closeInputStream, closeOutputStream);
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
	 * 使用zlib(压缩算法)解压缩输入到输出流中
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 * @param autoCloseStream 是否自动关闭输入/输出流
	 * @throws IOException
	 */
	public final static void inflater(InputStream input, OutputStream output, boolean autoCloseStream) throws IOException {
		inflater(input, output, autoCloseStream, autoCloseStream);
	}

	/**
	 * 使用zlib(压缩算法)解压缩输入到输出流中
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 * @param closeInputStream 是否自动关闭输出流
	 * @param closeOutputStream 是否自动关闭输出流
	 * @throws IOException
	 */
	public final static void inflater(InputStream input, OutputStream output, boolean closeInputStream, boolean closeOutputStream) throws IOException {
		Inflater inf = new Inflater(true);
		InflaterInputStream iis = new InflaterInputStream(input, inf);
		write(iis, output, new byte[DEFAULT_BUFFER_SIZE], closeInputStream, closeOutputStream);
	}
	
	/**
	 * 使用gzip(压缩算法)压缩输入到输出流中,自动关闭输入/输出流
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @throws IOException
	 */
	public final static void gzip(InputStream input, OutputStream output) throws IOException {
		gzip(input, output, true, true);
	}

	/**
	 * 使用gzip(压缩算法)压缩输入到输出流中
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @param autoCloseStream 是否自动关闭输出流
	 * @throws IOException
	 */
	public final static void gzip(InputStream input, OutputStream output, boolean autoCloseStream) throws IOException {
		gzip(input, output, autoCloseStream, autoCloseStream);
	}

	/**
	 * 使用gzip(压缩算法)压缩输入到输出流中
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @param closeInputStream 是否自动关闭输出流
	 * @param closeOutputStream 是否自动关闭输出流
	 * @throws IOException
	 */
	public final static void gzip(InputStream input, OutputStream output, boolean closeInputStream, boolean closeOutputStream) throws IOException {
		GZIPOutputStream gos = new GZIPOutputStream(output);
		write(input, gos, new byte[DEFAULT_BUFFER_SIZE], closeInputStream, closeOutputStream);
	}

	/**
	 * 使用gzip(压缩算法)解压缩输入到输出流中,自动关闭输入/输出流
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 */
	public final static void ungzip(InputStream input, OutputStream output) throws IOException {
		ungzip(input, output, true, true);
	}

	/**
	 * 使用gzip(压缩算法)解压缩输入到输出流中
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 * @param autoCloseStream 是否自动关闭输入/输出流
	 */
	public final static void ungzip(InputStream input, OutputStream output, boolean autoCloseStream) throws IOException {
		ungzip(input, output, autoCloseStream, autoCloseStream);
	}

	/**
	 * 使用gzip(压缩算法)解压缩输入到输出流中
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 * @param closeInputStream 是否自动关闭输出流
	 * @param closeOutputStream 是否自动关闭输出流
	 */
	public final static void ungzip(InputStream input, OutputStream output, boolean closeInputStream, boolean closeOutputStream) throws IOException {
		GZIPInputStream gis = new GZIPInputStream(input);
		write(gis, output, new byte[DEFAULT_BUFFER_SIZE], closeInputStream, closeOutputStream);
	}
	
	/**
	 * 使用zip压缩(归档算法)文件或者文件夹, 目标文件在当前文件所在目录中.
	 * jdk内置的zip算法不支持中文文件名称, 请使用ant提供的zip包压缩中文文件.
	 * @param src 被压缩文件
	 * @throws IOException
	 */
	public final static void zip(File src) throws IOException {
		String basePath = src.getParent();
		String destPath = basePath + File.separator + src.getName() + ".zip";
		zip(src, new File(destPath));
	}
	
	/**
	 * 使用zip压缩(归档算法)文件或者文件夹,jdk内置的zip算法不支持中文文件名称, 请使用ant提供的zip包压缩中文文件.
	 * @param src 被压缩文件
	 * @param dest 目标路径
	 * @throws IOException
	 */
	public final static void zip(File src, File dest) throws IOException {
		// 对输出文件做CRC32校验
		CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(dest), new CRC32());
		ZipOutputStream zos = new ZipOutputStream(cos);
		zip(src, zos, "");
		zos.flush();
		zos.close();
	}
	
	/**
	 * 使用zip压缩(归档算法)文件或者文件夹,jdk内置的zip算法不支持中文文件名称, 请使用ant提供的zip包压缩中文文件.
	 * @param src  源路径
	 * @param zoutput  解压缩输出流
	 * @param basePath  压缩包内相对路径
	 * @throws IOException
	 */
	public final static void zip(File src, ZipOutputStream zoutput, String basePath) throws IOException {
		if (src.isDirectory()) {
			File[] files = src.listFiles();
			if (files.length < 1) { // 构建空目录
				ZipEntry entry = new ZipEntry(basePath + src.getName() + File.separator);
				zoutput.putNextEntry(entry);
				zoutput.closeEntry();
			}
			for (File file : files) { // 递归压缩
				zip(file, zoutput, basePath + src.getName() + File.separator);
			}
		} else {
			ZipEntry entry = new ZipEntry(basePath + src.getName());
			zip(entry, new FileInputStream(src), zoutput);
		}
	}
	
	/**
	 * 使用zip压缩(归档算法)文件或者文件夹,jdk内置的zip算法不支持中文文件名称, 请使用ant提供的zip包压缩中文文件.
	 * @param entry 压缩实体
	 * @param input 被压缩流
	 * @param zoutput 压缩目标流
	 * @throws IOException
	 */
	public final static void zip(ZipEntry entry, InputStream input, ZipOutputStream zoutput) throws IOException {
		zoutput.putNextEntry(entry);
		write(input, zoutput, new byte[512], true, false);
		zoutput.closeEntry();
	}
	
	/**
	 * 解压缩的zip(归档算法)流
	 * @param input zip压缩流
	 * @param dir 解压缩的目录
	 * @throws IOException
	 */
	public final static void unzip(InputStream input, File dir) throws IOException {
		// 防止文件名中有中文时出错
		System.setProperty("sun.zip.encoding", System.getProperty("sun.jnu.encoding"));
		dir.mkdirs();
		ZipInputStream zinput = new ZipInputStream(input);
		ZipEntry ze = null;
		while ((ze = zinput.getNextEntry()) != null) {
			if (ze.isDirectory()) {
				new File(dir.getPath(), ze.getName()).mkdirs();
			} else {
				append(zinput, new File(dir.getPath(), ze.getName()), false);
			}
			zinput.closeEntry();
		}
		zinput.closeEntry();
		zinput.close();
	}
	
	/**
	 * 把输入流追加到文件. 如果文件不存在新建文件并写入, 如果文件存在追加到文件尾部.
	 * @param input 输入流
	 * @param out 写入文件
	 * @param closeInputStream 是否自动关闭输入流
	 * @throws IOException
	 */
	public final static void append(InputStream input, File out, boolean closeInputStream) throws IOException {
		OutputStream output = null;
		// 如果文件父目录不存在创建目录
		out.getParentFile().mkdirs();
		output = new FileOutputStream(out, true);
		write(input, output, new byte[DEFAULT_BUFFER_SIZE], closeInputStream, true);
	}
	
	/**
	 * 格式化大小, 单位K 或者M 或者G
	 * @param size
	 * @return
	 */
	public final static String size(long size) {
		String answer = null;
		if (size < 1024) { // 1K
			answer = Long.toString(size) + "Byte";
		} else if (size < 1048576) { // 1024*1024=1M
			answer = String.format("%.2f", size / 1024.0) + "K";
		} else if (size < 1073741824) { // 1024*1024*1024=1G
			answer = String.format("%.2f", size / (1048576.0)) + "M";
		} else if (size < 1099511627776L) { // 1024*1024*1024*11024=1T
			answer = String.format("%.2f", size / (1073741824.0)) + "G";
		} else if (size > 1099511627776L) {
			answer = "";
		}
		return answer;
	}
	
    public static void main(String[] args) throws IOException {
    		/*InputStream input = new FileInputStream(new File("/Users/zengwangming/Downloads/timg.jpg"));
    		byte[] bytes = toByteArray(input, 1024);
    		System.out.println(bytes.length);
    		System.out.println(new String(bytes));*/
    		/*URL url = new URL("http://2188856.com/lemeng");
    		byte[] bytes = toByteArray(url);
    		System.out.println(bytes.length);
		System.out.println(new String(bytes));*/
    		/*URL url = new URL("https://www.baidu.com");
    		URLConnection conn = url.openConnection();
    		InputStream input = conn.getInputStream();
    		String str = toString(input, null);
    		System.out.println(str);*/
    		/*String sysName = System.getProperty("user.dir");
    		URL url = resourceToURL(sysName + "/target/classes/cn/com/zach/tools/io/IOUtils.class");
    		System.out.println(url.toString());*/
    		
    		/*URL url = new URL("http://2188856.com/lemeng");
    		URLConnection conn = url.openConnection();
    		InputStream input = conn.getInputStream();
		char[] chars = toCharArray(input, null);
		System.out.println(chars.length);
		System.out.println(new String(chars));*/
    		URL url = new URL("http://2188856.com/lemeng");
    		String str = send(null, url, null);
    		System.out.println(str);
    }
}
