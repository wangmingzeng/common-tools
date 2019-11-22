package cn.com.zach.tools.object;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
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

import cn.com.zach.tools.io.TraverseEvent;

/**
 * io操作工具类
 * @author THINK
 *
 */
public final class IOUtils
{

	/**
	 * 流结束EOF标记
	 */
	private final static int	EOF						= -1;

	/**
	 * 1KB大小
	 */
	public final static int		ONE_KB					= 1024;

	/**
	 * 1MB大小
	 */
	public final static int		ONE_MB					= ONE_KB * ONE_KB;

	/**
	 * 默认nio缓冲区大小
	 */
	public final static int		FILE_COPY_BUFFER_SIZE	= ONE_MB * 30;

	/**
	 * 默认bio缓冲区大小100K
	 */
	public final static int		DEFAULT_BUFFER_SIZE		= 100 * ONE_KB;

	/**
	 * 编码字符串
	 * @param str 字符串
	 * @param charset 字符集, 如果此字段为空, 使用系统默认字符集编码
	 * @return 编码后的字节码
	 */
	public static byte[] charset(String str, String charset)
	{
		byte[] answer = null;
		if ( charset == null || charset.length() == 0 )
		{
			answer = str.getBytes();
		}
		else
		{
			try
			{
				answer = str.getBytes( charset );
			}
			catch ( UnsupportedEncodingException e )
			{
				throw new RuntimeException( charset + "字符集不被支持" );
			}
		}
		return answer;
	}

	/**
	 * 解码字节码
	 * @param data 字符串
	 * @param charset 字符集, 如果此字段为空, 使用系统默认字符集编码
	 * @return 解码后的字符串
	 */
	public static String charset(byte[] data, String charset)
	{
		String answer = null;
		if ( charset == null || charset.length() == 0 )
		{
			answer = new String( data );
		}
		else
		{
			try
			{
				answer = new String( data, charset );
			}
			catch ( UnsupportedEncodingException e )
			{
				throw new RuntimeException( charset + "字符集不被支持" );
			}
		}
		return answer;
	}

	/**
	 * 字符串编码转换
	 * @param str 字符串
	 * @param srcCharset 字符串原始字符集, 如果此字段为空, 使用系统默认字符集编码
	 * @param destCharset 转换目标字符集, 如果此字段为空, 使用系统默认字符集编码
	 * @return 解码后的字符串
	 */
	public static String charset(String str, String srcCharset, String destCharset)
	{
		String answer = null;
		answer = charset( charset( str ,srcCharset ) ,destCharset );
		return answer;
	}

	/**
	 * 获取文件后缀名称
	 * @param file 文件名称,或者文件路径
	 * @return 后缀名称
	 */
	public static String suffix(File file)
	{
		return suffix( file.getName() );
	}

	/**
	 * 获取文件后缀名称
	 * @param file 文件名称,或者文件路径
	 * @return 后缀名称
	 */
	public static String suffix(URL url)
	{
		return suffix( url.getPath() );
	}

	/**
	 * 获取文件后缀名称
	 * @param filename 文件名称,或者文件路径
	 * @return 后缀名称
	 */
	public static String suffix(String filename)
	{
		String answer = null;
		//取得最后一个点号的位置
		int pos = filename.lastIndexOf( '.' );
		if ( pos > 0 )
		{
			answer = filename.substring( pos + 1 ,filename.length() );
			//判断后缀名不能包含路径分隔符
			if ( answer.length() == 0 || answer.contains( "/" ) || answer.contains( "\\" ) )
			{
				answer = null;
			}
		}
		return answer;
	}

	/**
	 * 把文件路径或者url地址连续多个/或者\统一替换成一个/,例如: a\\\\b\\\\\\c//d 替换成a/b/c/d
	 * @param path 文件路径或者url地址
	 * @return 归一化后的结果
	 */
	public static String normalize(String path)
	{
		return path.replaceAll( "[/\\\\]{1,}" ,"/" );
	}

	/**
	 * 移动文件到目标文件或者文件夹,如果目标文件存在覆盖文件
	 * @param src 源文件
	 * @param dest 目标文件或者文件夹
	 * @return 移动是否成功
	 * @throws IOException
	 */
	public static boolean move(File src, File dest) throws IOException
	{
		return move( src ,dest ,true );
	}

	/**
	 * 移动文件到目标文件或者文件夹
	 * @param src 源文件
	 * @param dest 目标文件或者文件夹
	 * @param isOverride 是否覆盖目标文件
	 * @return 移动是否成功
	 * @throws IOException
	 */
	public static boolean move(File src, File dest, boolean isOverride) throws IOException
	{
		boolean answer = false;
		if ( dest.isFile() )
		{
			//判断目标文件是否是文件夹, 如果是在文件夹下建立同名文件
			if ( dest.isDirectory() )
			{
				dest = new File( dest, src.getName() );
			}
			//判断是否覆盖目标文件
			if ( dest.exists() && isOverride )
			{
				dest.delete();
			}
			//在文件系统不同的情况下renameTo会失败, 失败后使用copy方法复制原文件, 然后删除原文件.
			if ( !src.renameTo( dest ) )
			{
				touch( dest );
				copyFile( src ,dest );
				src.delete();
				answer = true;
			}
		}
		return answer;
	}

	/**
	 * 触摸文件, 文件不存在则新建文件,文件存在修改文件最后的修改时间
	 * @param file 目标文件
	 * @throws IOException
	 */
	public static void touch(File file) throws IOException
	{
		if ( !file.exists() )
		{
			//文件不存在新建文件
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		else
		{
			//修改最后的修改时间
			file.setLastModified( System.currentTimeMillis() );
		}
	}

	/**
	 * URL读取内容
	 * @param input url地址
	 * @param encoding URL内容字符集,默认utf-8
	 * @return 返回URL中字符串
	 * @throws IOException
	 */
	public final static String toString(URL input, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		StringWriter output = new StringWriter();
		write( new InputStreamReader( input.openStream(), encoding ) ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,true );
		return output.toString();
	}

	/**
	 * 读取文件
	 * @param input 文件
	 * @param encoding 文件字符集,默认utf-8
	 * @return 返回读取字符串
	 * @throws IOException
	 */
	public final static String toString(File input, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		StringWriter output = new StringWriter();
		write( new InputStreamReader( new FileInputStream( input ), encoding ) ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,true );
		return output.toString();
	}

	/**
	 * 缓冲区转换字符串
	 * @param input
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public final static String toString(ByteBuffer input, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		StringWriter output = new StringWriter();
		write( new InputStreamReader( toInputStream( input ), encoding ) ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,true );
		return output.toString();
	}

	/**
	 * 从输入流中读取字符窜内容
	 * @param input 输入流
	 * @param encoding 输入流字符集,默认utf-8
	 * @return 返回读取字符串
	 * @throws IOException
	 */
	public final static String toString(InputStream input, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		StringWriter output = new StringWriter();
		write( new InputStreamReader( input, encoding ) ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,true );
		return output.toString();
	}

	/**
	 * 获取带缓冲区的输出流, 默认缓冲区大小8K.
	 * @param file 文件
	 * @return 缓冲输出流
	 * @throws IOException
	 */
	public final static BufferedOutputStream getBufferedOutputStream(File file) throws IOException
	{
		return getBufferedOutputStream( new FileOutputStream( file ) ,DEFAULT_BUFFER_SIZE * 8 );
	}

	/**
	 * 获取带缓冲区的输出流.
	 * @param file 文件
	 * @param bufferSize 缓冲区大小
	 * @return 缓冲输出流
	 * @throws IOException
	 */
	public final static BufferedOutputStream getBufferedOutputStream(File file, int bufferSize) throws IOException
	{
		return getBufferedOutputStream( new FileOutputStream( file ) ,bufferSize );
	}

	/**
	 * 获取带缓冲区的输出流, 默认缓冲区大小8K.
	 * @param out 输出流
	 * @return 缓冲输出流
	 * @throws IOException
	 */
	public final static BufferedOutputStream getBufferedOutputStream(OutputStream out) throws IOException
	{
		return getBufferedOutputStream( out ,DEFAULT_BUFFER_SIZE * 8 );
	}

	/**
	 * 获取带缓冲区的输出流
	 * @param file 文件
	 * @param bufferSize 缓冲区大小
	 * @return 缓冲输出流
	 * @throws IOException
	 */
	public final static BufferedOutputStream getBufferedOutputStream(OutputStream output, int bufferSize) throws IOException
	{
		BufferedOutputStream answer = null;
		if ( !(output instanceof BufferedOutputStream) )
		{
			answer = new BufferedOutputStream( output, bufferSize );
		}
		else
		{
			answer = (BufferedOutputStream) output;
		}
		return answer;
	}

	/**
	 * 获取带缓冲区的输入流, 默认缓冲区大小8K.
	 * @param file 文件
	 * @return 缓冲输入流
	 * @throws IOException
	 */
	public final static BufferedInputStream getBufferedInputStream(File file) throws IOException
	{
		return getBufferedInputStream( new FileInputStream( file ) ,DEFAULT_BUFFER_SIZE * 8 );
	}

	/**
	 * 获取带缓冲区的输入流.
	 * @param file 文件
	 * @param bufferSize 缓冲区大小
	 * @return 缓冲输入流
	 * @throws IOException
	 */
	public final static BufferedInputStream getBufferedInputStream(File file, int bufferSize) throws IOException
	{
		return getBufferedInputStream( new FileInputStream( file ) ,bufferSize );
	}

	/**
	 * 获取带缓冲区的输入流, 默认缓冲区大小8K.
	 * @param input 输入流
	 * @return 缓冲输入流
	 * @throws IOException
	 */
	public final static BufferedInputStream getBufferedInputStream(InputStream input) throws IOException
	{
		return getBufferedInputStream( input ,DEFAULT_BUFFER_SIZE * 8 );
	}

	/**
	 * 获取带缓冲区的输入流
	 * @param file 文件
	 * @param bufferSize 缓冲区大小
	 * @return 缓冲输入流
	 * @throws IOException
	 */
	public final static BufferedInputStream getBufferedInputStream(InputStream inputput, int bufferSize) throws IOException
	{
		BufferedInputStream answer = null;
		if ( !(inputput instanceof BufferedInputStream) )
		{
			answer = new BufferedInputStream( inputput, bufferSize );
		}
		else
		{
			answer = (BufferedInputStream) inputput;
		}
		return answer;
	}

	//写入
	/**
	 * 字符串追加到文件尾部(不覆盖文件内容). 如果文件不存在新建文件并写入
	 * <pre>
	 * 		File file = new File("/tmp/1.txt");
	 * 		String str="Hello word!";
	 * 		IOUtils.appendUTF8(str, file);
	 * </pre>
	 * @param data 字符串
	 * @param output 写入文件
	 * @param encoding 字符串字符集,默认使用utf-8
	 * @throws IOException
	 */
	public final static void appendUTF8(String data, File output) throws IOException
	{
		OutputStream out = null;
		try
		{
			//如果文件父目录不存在创建目录
			output.getParentFile().mkdirs();
			out = new FileOutputStream( output, true );
			out.write( data.getBytes( "utf-8" ) );
		}
		finally
		{
			close( out );
		}
	}

	/**
	 * 把URL中内容追加到文件尾部. 如果文件不存在新建文件并写入
	 * <pre>
	 * 		File img = new File("/tmp/img.jpg");
	 * 		URL url = new URL("http://f.hiphotos.baidu.com/image/pic/item/cefc1e178a82b901bfa9f4b47f8da9773912ef35.jpg");
	 * 		IOUtils.append(url, img);
	 * </pre>
	 * @param url url地址
	 * @param out 写入文件
	 * @throws IOException
	 */
	public final static void append(URL url, File out) throws IOException
	{
		append( url.openStream() ,out ,true );
	}

	/**
	 * 把内存缓冲区追加到文件, 如果文件不存在新建文件并写入
	 * <pre>
	 * 	事例1:
	 * 		//把文件建立内存映射,进行文件复制
	 * 		File src = new File("/tmp/img.jpg");
	 * 		File dest = new File("/tmp/img1.jpg");
	 * 		//获取文件管道
	 * 		FileChannel fc = new FileInputStream(src).getChannel();
	 * 		//建立文件内存映射, 这里使用堆外内存.GC无法回收这里的内存慎用.
	 * 		MappedByteBuffer buffer=fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
	 * 		IOUtils.append(buffer, dest);
	 *
	 * 	事例2:
	 * 		ByteBuffer  buffer = ByteBuffer.wrap("Hello word!".getBytes());
	 * 		File file =   new File("/tmp/txt.txt");
	 * 		IOUtils.append( buffer , file );
	 *
	 * </pre>
	 * @param input 输入流
	 * @param out 写入文件
	 * @throws IOException
	 */
	public final static void append(ByteBuffer input, File out) throws IOException
	{
		append( toInputStream( input ) ,out ,true );
	}

	/**
	 * 把输入流追加到文件,并且自动关闭输入流. 如果文件不存在新建文件并写入
	 * @param input 输入流
	 * @param out 写入文件
	 * @throws IOException
	 */
	public final static void append(InputStream input, File out) throws IOException
	{
		append( input ,out ,true );
	}

	/**
	 * 把输入流追加到文件. 如果文件不存在新建文件并写入, 如果文件存在追加到文件尾部.
	 * @param input 输入流
	 * @param out 写入文件
	 * @param closeInputStream 是否自动关闭输入流
	 * @throws IOException
	 */
	public final static void append(InputStream input, File out, boolean closeInputStream) throws IOException
	{
		OutputStream output = null;
		//如果文件父目录不存在创建目录
		out.getParentFile().mkdirs();
		output = new FileOutputStream( out, true );
		write( input ,output ,new byte[DEFAULT_BUFFER_SIZE] ,closeInputStream ,true );
	}

	/**
	 * 使用utf8字符集把字符串追加到输出流尾部, 不关闭输出流
	 * @param input 网络地址
	 * @param output 输出流
	 * @throws IOException
	 */
	public final static void appendUTF8(String input, OutputStream output) throws IOException
	{
		output.write( input.getBytes( "utf-8" ) );
	}

	/**
	 * 把缓冲区内容追加到输出流尾部, 不关闭输出流
	 * @param input 网络地址
	 * @param output 输出流
	 * @throws IOException
	 */
	public final static void append(ByteBuffer input, OutputStream output) throws IOException
	{
		append( toInputStream( input ) ,output );
	}

	/**
	 * 把网络地址内容追加到输出流尾部, 不关闭输出流
	 * @param input 网络地址
	 * @param output 输出流
	 * @throws IOException
	 */
	public final static void append(URL input, OutputStream output) throws IOException
	{
		append( input.openStream() ,output );
	}

	/**
	 * 把文件追加到输出流尾部, 不关闭输出流
	 * @param input 文件
	 * @param output 输出流
	 * @throws IOException
	 */
	public final static void append(File input, OutputStream output) throws IOException
	{
		append( new FileInputStream( input ) ,output );
	}

	/**
	 * 把输入流追加到输出流尾部,关闭输入流,不关闭输出流
	 * @param input 输入流
	 * @param output 输出流
	 * @throws IOException
	 */
	public final static void append(InputStream input, OutputStream output) throws IOException
	{
		write( input ,output ,new byte[DEFAULT_BUFFER_SIZE] ,true ,false );
	}

	/**
	 * 把内存缓冲区写入到writer中,不关闭writer.
	 * @param input 内存缓冲区
	 * @param output 输出writer
	 * @param charset 内存缓冲区字符集编码, 默认utf-8字符集
	 * @return 写入字符个数
	 * @throws IOException
	 */
	public final static int append(ByteBuffer input, Writer output, String charset) throws IOException
	{
		if ( charset == null || charset.length() == 0 )
		{
			charset = "utf-8";
		}
		return write( new InputStreamReader( toInputStream( input ), charset ) ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,false );
	}

	/**
	 * 把网络地址写入到writer中,不关闭writer.
	 * @param input 网络地址
	 * @param output 输出writer
	 * @param charset 网络地址字符集编码, 默认utf-8字符集
	 * @return 写入字符个数
	 * @throws IOException
	 */
	public final static int append(URL input, Writer output, String charset) throws IOException
	{
		if ( charset == null || charset.length() == 0 )
		{
			charset = "utf-8";
		}
		return write( new InputStreamReader( input.openStream(), charset ) ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,false );
	}

	/**
	 * 把文件写入到writer中,不关闭writer.
	 * @param input 文件
	 * @param output 输出writer
	 * @param charset 文件字符集编码, 默认utf-8字符集
	 * @return 写入字符个数
	 * @throws IOException
	 */
	public final static int append(File input, Writer output, String charset) throws IOException
	{
		if ( charset == null || charset.length() == 0 )
		{
			charset = "utf-8";
		}
		return write( new InputStreamReader( new FileInputStream( input ), charset ) ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,false );
	}

	/**
	 * 把reader写入到writer中,关闭input,不关闭writer.
	 * @param input 输入流
	 * @param output 输出writer
	 * @param charset 输入流字符集编码, 默认utf-8字符集
	 * @return 写入字符个数
	 * @throws IOException
	 */
	public final static int append(InputStream input, Writer output, String charset) throws IOException
	{
		if ( charset == null || charset.length() == 0 )
		{
			charset = "utf-8";
		}
		return write( new InputStreamReader( input, charset ) ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,false );
	}

	/**
	 * 把reader写入到writer中,关闭reader,不关闭writer.
	 * @param input 输入reader
	 * @param output 输出writer
	 * @return 写入字符个数
	 * @throws IOException
	 */
	public final static int append(Reader input, Writer output) throws IOException
	{
		return write( input ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,false );
	}

	//
	//	/**
	//	 * 把Reader写入到文件,并且自动关闭reader. 如果文件不存在新建文件并写入, 如果文件存在追加到文件尾部.
	//	 * @param reader  reader
	//	 * @param output 写入文件
	//	 * @throws IOException
	//	 */
	//	public final static void append(Reader reader, File output) throws IOException
	//	{
	//		append( reader ,output ,true );
	//	}
	//
	//	/**
	//	 * 把Reader写入到文件. 如果文件不存在新建文件并写入, 如果文件存在追加到文件尾部.
	//	 * @param input reader
	//	 * @param output 写入文件
	//	 * @param closeInputStream 是否自动关闭输入流
	//	 * @throws IOException
	//	 */
	//	public final static void append(Reader reader, File output, boolean closeReader) throws IOException
	//	{
	//		Writer writer = null;
	//		//如果文件父目录不存在创建目录
	//		output.getParentFile().mkdirs();
	//		writer = new FileWriter( output, true );
	//		write( reader ,writer ,new char[DEFAULT_BUFFER_SIZE] ,closeReader ,true );
	//	}
	//
	//	/**
	//	 * 使用nio追加两个文件
	//	 * @param srcFile 源文件
	//	 * @param destFile 目标文件
	//	 * @throws IOException
	//	 */
	//	public final static void appendFile(File srcFile, File destFile) throws IOException
	//	{
	//		appendFile( srcFile ,destFile ,true );
	//	}
	//
	//	/**
	//	 * 使用nio追加两个文件
	//	 * @param srcFile 源文件
	//	 * @param destFile 目标文件
	//	 * @param preserveFileDate 是或否修改文件最后一次修改时间
	//	 * @throws IOException
	//	 */
	//	public final static void appendFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException
	//	{
	//		FileInputStream fis = null;
	//		FileOutputStream fos = null;
	//		FileChannel input = null;
	//		FileChannel output = null;
	//		try
	//		{
	//			//如果文件父目录不存在创建目录
	//			destFile.getParentFile().mkdirs();
	//			fis = new FileInputStream( srcFile );
	//			fos = new FileOutputStream( destFile, true );
	//			input = fis.getChannel();
	//			output = fos.getChannel();
	//			long bytesTransferred = 0;
	//			while ( bytesTransferred < input.size() )
	//			{
	//				bytesTransferred += input.transferTo( 0 ,input.size() ,output );
	//			}
	//			if ( preserveFileDate )
	//			{
	//				destFile.setLastModified( srcFile.lastModified() );
	//			}
	//		}
	//		finally
	//		{
	//			close( output );
	//			close( fos );
	//			close( input );
	//			close( fis );
	//		}
	//	}
	//复制方法
	/**
	 * 使用nio复制文件
	 * @param srcFile 源文件
	 * @param destFile 目标文件或者文件夹
	 * @throws IOException
	 */
	public final static void copyFile(File srcFile, File destFile) throws IOException
	{
		if ( destFile.isFile() )
		{
			copyFile( srcFile ,destFile ,true );
		}
		else
		{
			copyFileToDirectory( srcFile ,destFile );
		}
	}

	/**
	 * 使用nio复制文件到文件夹
	 * @param srcFile 源文件
	 * @param destDir 目标文件夹
	 * @throws IOException
	 */
	public final static void copyFileToDirectory(File srcFile, File destDir) throws IOException
	{
		File destFile = new File( destDir, srcFile.getName() );
		copyFile( srcFile ,destFile ,true );
	}

	/**
	 * 使用nio复制文件夹
	 * @param srcDir 源文件夹
	 * @param destDir 目标文件夹
	 * @throws IOException
	 */
	public final static void copyDirectory(File srcDir, File destDir) throws IOException
	{
		copyDirectory( srcDir ,destDir ,null ,true );
	}

	/**
	 * 使用nio复制文件夹
	 * @param srcDir 源文件夹
	 * @param destDir 目标文件夹
	 * @param filter 文件过滤器
	 * @param preserveFileDate 是或否修改文件夹最后一次修改时间
	 * @throws IOException
	 */
	public final static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate) throws IOException
	{
		List<String> exclusionList = null;
		if ( destDir.getCanonicalPath().startsWith( srcDir.getCanonicalPath() ) )
		{
			File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles( filter );
			if ( srcFiles != null && srcFiles.length > 0 )
			{
				exclusionList = new ArrayList<String>( srcFiles.length );
				for ( File srcFile : srcFiles )
				{
					File copiedFile = new File( destDir, srcFile.getName() );
					exclusionList.add( copiedFile.getCanonicalPath() );
				}
			}
		}
		copyDirectory( srcDir ,destDir ,filter ,preserveFileDate ,exclusionList );
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
	private final static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate, List<String> exclusionList) throws IOException
	{
		File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles( filter );
		for ( File srcFile : srcFiles )
		{
			File dstFile = new File( destDir, srcFile.getName() );
			if ( exclusionList == null || !exclusionList.contains( srcFile.getCanonicalPath() ) )
			{
				if ( srcFile.isDirectory() )
				{
					copyDirectory( srcFile ,dstFile ,filter ,preserveFileDate ,exclusionList );
				}
				else
				{
					copyFile( srcFile ,dstFile ,preserveFileDate );
				}
			}
		}
		if ( preserveFileDate )
		{
			destDir.setLastModified( srcDir.lastModified() );
		}
	}

	/**
	 * 使用nio复制文件
	 * @param srcFile 源文件
	 * @param destFile 目标文件
	 * @param preserveFileDate 是或否修改文件最后一次修改时间
	 * @throws IOException
	 */
	public final static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel input = null;
		FileChannel output = null;
		try
		{
			destFile.getParentFile().mkdirs();
			fis = new FileInputStream( srcFile );
			fos = new FileOutputStream( destFile );
			input = fis.getChannel();
			output = fos.getChannel();
			long size = input.size();
			long pos = 0;
			long count = 0;
			while ( pos < size )
			{
				count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
				pos += output.transferFrom( input ,pos ,count );
			}
		}
		finally
		{
			close( output );
			close( fos );
			close( input );
			close( fis );
		}
		if ( srcFile.length() != destFile.length() )
		{
			throw new IOException( "文件复制失败." );
		}
		if ( preserveFileDate )
		{
			destFile.setLastModified( srcFile.lastModified() );
		}
	}

	//
	//	/**
	//	 * 把byte数组写入Writer中
	//	 * @param data 输入byte数组
	//	 * @param output 输出Writer
	//	 * @param encoding 字符串字符集,默认utf-8
	//	 * @throws IOException
	//	 */
	//	public final static void write(byte[] data, Writer output, String encoding) throws IOException
	//	{
	//		if ( encoding == null || encoding.length() == 0 )
	//		{
	//			encoding = "utf-8";
	//		}
	//		if ( data != null )
	//		{
	//			output.write( new String( data, encoding ) );
	//		}
	//	}
	//
	//	/**
	//	 * 把URL中内容复制到Writer中
	//	 * @param input 源URL
	//	 * @param output 目标writer
	//	 * @param encoding URL中内容字符集,默认utf-8
	//	 * @return 返回复制长度
	//	 * @throws IOException
	//	 */
	//	public final static int write(URL input, Writer output, String encoding) throws IOException
	//	{
	//		int count = 0;
	//		Reader in = null;
	//		if ( encoding == null || encoding.length() == 0 )
	//		{
	//			encoding = "utf-8";
	//		}
	//		try
	//		{
	//			in = new InputStreamReader( input.openStream(), encoding );
	//			count = write( in ,output );
	//		}
	//		finally
	//		{
	//			close( in );
	//		}
	//		return count;
	//	}
	//
	//	/**
	//	 * Reader复制到Writer, 并且自动关闭输入和输出流
	//	 * @param input 源reader
	//	 * @param output 目标write
	//	 * @param autoCloseStream 自动关闭输入和输出流
	//	 * @return 返回复制长度
	//	 * @throws IOException
	//	 */
	//	public final static int write(Reader input, Writer output) throws IOException
	//	{
	//		long count = write( input ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,true );
	//		if ( count > Integer.MAX_VALUE )
	//		{
	//			return -1;
	//		}
	//		return (int) count;
	//	}
	//
	//	/**
	//	 * Reader复制到Writer
	//	 * @param input 源reader
	//	 * @param output 目标write
	//	 * @param autoCloseStream 自动关闭输入和输出流
	//	 * @return 返回复制长度
	//	 * @throws IOException
	//	 */
	//	public final static int write(Reader input, Writer output, boolean autoCloseStream) throws IOException
	//	{
	//		long count = write( input ,output ,new char[DEFAULT_BUFFER_SIZE] ,autoCloseStream ,autoCloseStream );
	//		if ( count > Integer.MAX_VALUE )
	//		{
	//			return -1;
	//		}
	//		return (int) count;
	//	}
	//
	/**
	 * 输入流复制到Writer
	 * @param input 源输入流
	 * @param output 目标writer
	 * @param charset 输入流字符集字符集,默认utf-8
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static int write(InputStream input, Writer output, String charset) throws IOException
	{
		if ( charset == null || charset.length() == 0 )
		{
			charset = "utf-8";
		}
		return write( new InputStreamReader( input, charset ) ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,true );
	}

	/**
	 * 把reader写入到writer中,并且关闭reader和writer
	 * @param input
	 * @param output
	 * @return
	 * @throws IOException
	 */
	public final static int write(Reader input, Writer output) throws IOException
	{
		return write( input ,output ,new char[DEFAULT_BUFFER_SIZE] ,true ,true );
	}

	/**
	 * Reader复制到Writer
	 * @param input 输入
	 * @param output 输出
	 * @param buffer 缓冲区
	 * @param closeInputStream 是否自动关闭输出流
	 * @param closeOutputStream 是否自动关闭输出流
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static int write(Reader input, Writer output, char[] buffer, boolean closeInputStream, boolean closeOutputStream) throws IOException
	{
		int count = 0;
		int n = 0;
		try
		{
			while ( EOF != (n = input.read( buffer )) )
			{
				output.write( buffer ,0 ,n );
				count += n;
			}
		}
		finally
		{
			if ( closeInputStream )
			{
				close( input );
			}
			if ( closeOutputStream )
			{
				//output.flush();
				close( output );
			}
		}
		return count;
	}

	/**
	 * Reader写入到OutputStream
	 * @param input 源reader
	 * @param output 目标OutputStream,
	 * @parm charset 使用字符集, 默认utf-8
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static int write(Reader input, OutputStream output, String charset) throws IOException
	{
		if ( charset == null || charset.length() == 0 )
		{
			charset = "utf-8";
		}
		return write( input ,new OutputStreamWriter( output, charset ) ,new char[DEFAULT_BUFFER_SIZE] ,true ,true );
	}

	/**
	 * 使用utf8字符集把字符串写入输出流中, 并且关闭输出流
	 * @param data 字符串
	 * @param output 输出流
	 * @throws IOException
	 */
	public final static void writeUtf8(String data, OutputStream output) throws IOException
	{
		if ( data != null )
		{
			output.write( data.getBytes( "utf-8" ) );
		}
		output.close();
	}

	/**
	 * 把网络地址复制到输出流中,并且关闭输出流.
	 * @param input 源RUL
	 * @param output 目标输出流
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(URL input, OutputStream output) throws IOException
	{
		return write( input.openStream() ,output ,true );
	}

	/**
	 * 把文件写入到输出流中, 并且关闭输出流.
	 * @param input 文件
	 * @param output 目标输出流
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(File input, OutputStream output) throws IOException
	{
		long count = write( getBufferedInputStream( input ) ,output ,new byte[DEFAULT_BUFFER_SIZE] ,true ,true );
		if ( count > Integer.MAX_VALUE )
		{
			return -1;
		}
		return count;
	}

	/**
	 * 把内存缓冲区写入到输出流中, 并且关闭输出流.
	 * @param input 源输入流
	 * @param output 目标输出流
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(ByteBuffer input, OutputStream output) throws IOException
	{
		return write( toInputStream( input ) ,output );
	}

	/**
	 * 把输入写入到输出流,同时自动关闭输入和输出流
	 * @param input 源输入流
	 * @param output 目标输出流
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(InputStream input, OutputStream output) throws IOException
	{
		long count = write( input ,output ,new byte[DEFAULT_BUFFER_SIZE] ,true ,true );
		if ( count > Integer.MAX_VALUE )
		{
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
	public final static long write(InputStream input, OutputStream output, boolean autClose) throws IOException
	{
		long count = write( input ,output ,new byte[DEFAULT_BUFFER_SIZE] ,autClose ,autClose );
		if ( count > Integer.MAX_VALUE )
		{
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
	public final static long write(InputStream input, OutputStream output, byte[] buffer, boolean closeInputStream, boolean closeOutputStream) throws IOException
	{
		long count = 0;
		int n = 0;
		try
		{
			while ( EOF != (n = input.read( buffer )) )
			{
				output.write( buffer ,0 ,n );
				count += n;
			}
		}
		finally
		{
			if ( closeInputStream )
			{
				close( input );
			}
			if ( closeOutputStream )
			{
				//output.flush();
				close( output );
			}
		}
		return count;
	}

	/**
	 * 使用post请求把字符串写入网络地址
	 * @param input 输入字符串
	 * @param output 网络地址
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String writeUTF8(String input, URL output ) throws IOException
	{
		byte [] b =  input.getBytes("utf-8") ;
		Map<String, String> header = new HashMap<>();
		header.put("Content-Length", String.valueOf(b.length));
		header.put("Content-Type","text/plain");
		return write(new ByteArrayInputStream(b ), output, header, true);
	}

	/**
	 * 使用post请求把文件写入网络地址
	 * @param input 文件
	 * @param output 网络地址
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String write(File input, URL output ) throws IOException
	{
		Map<String, String> header = new HashMap<>();
		header.put("Content-Length", String.valueOf(input.length()));
		header.put("Content-Type","application/octet-stream");
		return write(new FileInputStream(input), output, header, true);
	}

	/**
	 * 使用post请求把字符串写入网络地址
	 * @param input 字符串
	 * @param output 网络地址
	 * @param contentType 字符串类型, 例如: 文本使用text/plain, json对象使用:application/json
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String write(String input, URL output, String contentType ) throws IOException
	{
		byte [] b =  input.getBytes("utf-8") ;
		Map<String, String> header = new HashMap<>();
		header.put("Content-Length", String.valueOf(b.length));
		header.put("Content-Type",contentType== null ? "text/plain":contentType  );
		return write(new ByteArrayInputStream(b ), output, header, true);
	}

	/**
	 * 使用post请求把内存缓冲区写入网络地址
	 * @param input 内存缓冲区
	 * @param output 网络地址
	 * @param header 请求头部
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String write(ByteBuffer input, URL output, Map<String, String> header) throws IOException
	{
		return write( toInputStream( input ) ,output ,header ,true );
	}

	/**
	 * 使用post请求把字符串写入网络地址
	 * @param input 输入字符串
	 * @param output 网络地址
	 * @param header 请求头部
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String writeUTF8(String input, URL output, Map<String, String> header) throws IOException
	{
		return write( new ByteArrayInputStream( input.getBytes( "utf-8" ) ) ,output ,header ,true );
	}

	/**
	 * 使用post请求把byte数组写入网络地址
	 * @param input 输入字节数组
	 * @param output 网络地址
	 * @param header 请求头部
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String write(byte[] input, URL output, Map<String, String> header) throws IOException
	{
		return write( new ByteArrayInputStream( input ) ,output ,header ,true );
	}

	/**
	 * 使用post请求把文件写入网络地址
	 * @param input 输入文件
	 * @param output 网络地址
	 * @param header 请求头部
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String write(File input, URL output, Map<String, String> header) throws IOException
	{
		return write( new FileInputStream( input ) ,output ,header ,true );
	}

	/**
	 * 使用post请求把输入流写入网络地址
	 * @param input 输入流
	 * @param output 网络地址
	 * @param header 请求头部
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String write(InputStream input, URL output, Map<String, String> header) throws IOException
	{
		return write( input ,output ,header ,true );
	}

	/**
	 * 使用post请求把输入流写入网络地址
	 * @param input 输入流
	 * @param output 网络地址
	 * @param header 请求头部
	 * @autoclose 是否自动关闭输入流
	 * @return 返回结果
	 * @throws IOException
	 */
	public final static String write(InputStream input, URL output, Map<String, String> header, boolean autoclose) throws IOException
	{
		String answer = null;
		OutputStream out = null;
		InputStream res = null;
		try
		{
			HttpURLConnection conn = (HttpURLConnection) output.openConnection();
			conn.setRequestMethod( "POST" );
			conn.setConnectTimeout( 10 * 1000 );
			conn.setReadTimeout( 10 * 1000 );
			conn.setDoInput( true );
			conn.setDoOutput( true );
			if ( header != null && header.size() > 0 )
			{
				for ( Map.Entry<String, String> en : header.entrySet() )
				{
					conn.setRequestProperty( en.getKey() ,en.getValue() );
				}
			}
			conn.connect();
			out = conn.getOutputStream();
			write( input ,out );
			int responseCode = conn.getResponseCode();
			res = conn.getInputStream();
			answer = toString( res ,null );
			if ( responseCode < 200 && responseCode >= 400 )
			{
				throw new IOException( answer );
			}
		}
		finally
		{
			if ( autoclose )
			{
				close( input );
			}
			close( res );
			close( out );
		}
		return answer;
	}

	/**
	 * 字符串写入文件
	 * @param input 字符串,默认utf-8字符集
	 * @param output 文件
	 * @param encoding
	 * @throws IOException
	 */
	public final static long writeUTF8(String input, File output) throws IOException
	{
		return write( input.getBytes( "utf-8" ) ,output );
	}

	/**
	 * 把byte数组写入文件
	 * @param input byte数组
	 * @param output 目标文件
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(byte[] input, File output) throws IOException
	{
		return write( new ByteArrayInputStream( input ) ,output ,true );
	}

	/**
	 * 把URL中内容写入到文件
	 * @param input 源url
	 * @param output 目标文件
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(URL input, File output) throws IOException
	{
		return write( input.openStream() ,output ,true );
	}

	/**
	 * 把文件写入文件
	 * @param input 源文件
	 * @param output 目标文件
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(File input, File output) throws IOException
	{
		return write( new FileInputStream( input ) ,output ,true );
	}

	/**
	 * 把字节缓冲区写入文件
	 * @param input 缓冲区
	 * @param output 文件
	 * @return
	 * @throws IOException
	 */
	public final static long write(ByteBuffer input, File output) throws IOException
	{
		return write( toInputStream( input ) ,output ,true );
	}

	/**
	 * 把输入流写入文件
	 * @param input 输入流
	 * @param output 目标文件
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public final static long write(InputStream input, File output, boolean autoClose) throws IOException
	{
		output.getParentFile().mkdirs();
		return write( input ,new FileOutputStream( output ) ,autoClose );
	}

	/**
	 * 大文件复制
	 * @param src 只读管道
	 * @param dest 可写入管道
	 * @throws IOException
	 */
	public final static void save(ReadableByteChannel src, WritableByteChannel dest) throws IOException
	{
		save( src ,dest ,ONE_MB ,true );
	}

	/**
	 * 把只读管道写入可写管道中, 缓冲如果超过10M申请对外内存, 主要用于复制大文件, 比如: 10M以上流, 1G文件
	 * <pre>
	 * 	 //src.txt大小超过10M
	 * 	 File src = new File("/tmp/src.txt");
	 * 	 ReadableByteChannel rc = new FileInputStream(src).getChannel();
	 *
	 * 	 File dest = new File("/tmp/dest.txt");
	 * 	 WritableByteChannel wc = new FileInputStream(dest).getChannel();
	 *
	 * 	 save(rc , wc , ONE_MB, true)
	 * </pre>
	 * @param src 只读管道
	 * @param dest 可写入管道
	 * @param bufferSize 缓冲大小, 如果申请的缓冲区大小超过10M, 使用堆外内存(即:向操作系统申请内存), 否则使用堆内内存(即:向JVM申请内存)
	 * @param autoClose 是否自动关闭管道
	 * @throws IOException
	 */
	public final static void save(ReadableByteChannel src, WritableByteChannel dest, int bufferSize, boolean autoClose) throws IOException
	{
		//如果申请的缓冲区大小超过10M, 使用堆外内存(即:向操作系统申请内存), 否则使用堆内内存(即:向JVM申请内存)
		//注意如果申请DirectBuffer, GC无法回收这部分内存. 使用不当可能造成DirectBuffer溢出.
		//使用-XX:MaxDirectMemorySize来指定最大的堆外内存大小.默认是64M
		//建议在大文件复制时使用DirectBuffer
		ByteBuffer buffer = bufferSize >= 10 * ONE_MB ? ByteBuffer.allocateDirect( bufferSize ) : ByteBuffer.allocate( bufferSize );;
		try
		{
			while ( src.read( buffer ) != EOF )
			{
				buffer.flip();
				// 保证缓冲区的数据全部写入
				while ( buffer.hasRemaining() )
				{
					dest.write( buffer );
				}
				buffer.clear();
			}
			buffer.clear();
			//如果申请堆外内存,这部分内存需要手动释放, GC无法回收这部分内存
			if ( buffer.isDirect() )
			{
				privileged( buffer );
			}
			buffer = null;
		}
		finally
		{
			if ( autoClose )
			{
				close( src );
				close( dest );
			}
		}
	}

	/**
	 * 把网络地址保存到文件中, 并且计算网络输入流的MD5值. 即: 一边保存文件一边计算文件MD5值
	 * 这样计算文件MD5比分两步(一步保存文件,另一步计算文件MD5值)要快很多.特别是大文件.
	 * @param input 网络地址
	 * @param output 输出文件,如果文件不存在新建文件
	 * @return 输入流md5值,也就是文件的md5值
	 * @throws IOException
	 */
	public final static String saveAndMd5(byte[] input, File output) throws IOException
	{
		return saveAndMd5( new ByteArrayInputStream( input ) ,output );
	}

	/**
	 * 把网络地址保存到文件中, 并且计算网络输入流的MD5值. 即: 一边保存文件一边计算文件MD5值
	 * 这样计算文件MD5比分两步(一步保存文件,另一步计算文件MD5值)要快很多.特别是大文件.
	 * @param input 网络地址
	 * @param output 输出文件,如果文件不存在新建文件
	 * @return 输入流md5值,也就是文件的md5值
	 * @throws IOException
	 */
	public final static String saveAndMd5(URL input, File output) throws IOException
	{
		return saveAndMd5( input.openStream() ,output );
	}

	/**
	 * 把输入流保存到文件中, 并且计算输入流的MD5值. 即: 一边保存文件一边计算文件MD5值
	 * 这样计算文件MD5比分两步(一步保存文件,另一步计算文件MD5值)要快很多.特别是大文件.
	 * @param input 输入流
	 * @param output 输出文件,如果文件不存在新建文件
	 * @return 输入流md5值,也就是文件的md5值
	 * @throws IOException
	 */
	public final static String saveAndMd5(InputStream input, File output) throws IOException
	{
		output.getParentFile().mkdirs();
		return saveAndMd5( input ,new FileOutputStream( output ) ,new byte[DEFAULT_BUFFER_SIZE] ,true ,true );
	}

	/**
	 * 使用bio把输入流复制到输出流中, 并且计算输入流的MD5值
	 * @param input 输入流
	 * @param output 输出流
	 * @param buffer 缓冲区
	 * @param closeInputStream 是否自动输入流
	 * @param closeOutputStream 是否自动关闭输出流
	 * @return
	 * @throws IOException
	 */
	public final static String saveAndMd5(InputStream input, OutputStream output, byte[] buffer, boolean closeInputStream, boolean closeOutputStream) throws IOException
	{
		MessageDigest digest = null;
		int n = 0;
		try
		{
			digest = MessageDigest.getInstance( "MD5" );
			while ( EOF != (n = input.read( buffer )) )
			{
				output.write( buffer ,0 ,n );
				digest.update( buffer ,0 ,n );
			}
		}
		catch ( Exception e )
		{
			throw new IOException( e );
		}
		finally
		{
			if ( closeInputStream )
			{
				close( input );
			}
			if ( closeOutputStream )
			{
				close( output );
			}
		}
		return new BigInteger( 1, digest.digest() ).toString( 16 );
	}

	/**
	 * 把只读管道写入可写管道中, 并且计算输入流的MD5值.默认缓冲区1M
	 * @param src 只读管道
	 * @param dest 可写管道
	 * @return 返回md5值
	 * @throws IOException
	 */
	public final static String saveAndMd5(ReadableByteChannel src, WritableByteChannel dest) throws IOException
	{
		return saveAndMd5( src ,dest ,ONE_MB ,true );
	}

	/**
	 * 把只读管道写入可写管道中, 缓冲如果超过10M申请堆外内存, 并且计算输入流的MD5值. 比如: 10M以上流, 1G文件
	 * @param src 只读管道
	 * @param dest 可写管道
	 * @param bufferSize 缓冲区大小
	 * @param autoClose 是否自动关闭管道
	 * @return 返回md5值
	 * @throws IOException
	 */
	public final static String saveAndMd5(ReadableByteChannel src, WritableByteChannel dest, int bufferSize, boolean autoClose) throws IOException
	{
		//如果申请的缓冲区大小超过10M, 使用堆外内存(即:向操作系统申请内存), 否则使用堆内内存(即:向JVM申请内存)
		//注意如果申请DirectBuffer, GC无法回收这部分内存. 使用不当可能造成DirectBuffer溢出.
		//使用-XX:MaxDirectMemorySize来指定最大的堆外内存大小.默认是64M
		//建议在大文件复制时使用DirectBuffer
		ByteBuffer buffer = bufferSize >= 10 * ONE_MB ? ByteBuffer.allocateDirect( bufferSize ) : ByteBuffer.allocate( bufferSize );;
		MessageDigest digest = null;
		try
		{
			digest = MessageDigest.getInstance( "MD5" );
			while ( src.read( buffer ) != EOF )
			{
				buffer.flip();
				while ( buffer.hasRemaining() )
				{
					dest.write( buffer );
				}
				buffer.flip();
				digest.update( buffer );
				buffer.clear();
			}
			buffer.clear();
			//如果申请堆外内存,这部分内存需要手动释放, GC无法回收这部分内存
			if ( buffer.isDirect() )
			{
				privileged( buffer );
			}
			buffer = null;
		}
		catch ( Exception e )
		{
			throw new IOException( e );
		}
		finally
		{
			if ( autoClose )
			{
				close( src );
				close( dest );
			}
		}
		return new BigInteger( 1, digest.digest() ).toString( 16 );
	}

	/**
	 * 释放堆外内存
	 * @param byteBuffer
	 */
	public final static void unmap(final ByteBuffer buffer)
	{
		if ( buffer != null && buffer.isDirect() )
		{
			privileged( buffer );
		}
	}

	/**
	 * 释放堆外内存
	 * @param byteBuffer
	 */
	private final static void privileged(final ByteBuffer byteBuffer)
	{
		try
		{
			//释放堆外内存,jdk是通过sun.misc.Cleaner释放堆外内存,这是一个内部类外部不能访问
			//通过反射方式调用
			java.lang.reflect.Method cleanerMethod = byteBuffer.getClass().getMethod( "cleaner" ,new Class[0] );
			cleanerMethod.setAccessible( true );
			Object cleaner = cleanerMethod.invoke( byteBuffer ,new Object[0] );
			java.lang.reflect.Method cleanMethod = cleaner.getClass().getMethod( "clean" ,new Class[0] );
			cleanMethod.setAccessible( true );
			cleanMethod.invoke( cleaner ,new Object[0] );
			//内部方法不能直接调用
			//sun.misc.Cleaner cleaner = (sun.misc.Cleaner)getCleanerMethod.invoke(byteBuffer, new Object[0]);
			//cleaner.clean();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * 使用nio把byte数组保存到文件中
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public final static void save(byte[] input, File output) throws IOException
	{
		save( new ByteArrayInputStream( input ) ,input.length ,output );
	}

	/**
	 * 使用nio把文件保存到文件中
	 * @param input 输入流
	 * @param inputSize 输入流大小, 注意不是available大小
	 * @param output 文件
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public final static void save(File input, File output) throws IOException
	{
		save( new FileInputStream( input ).getChannel() ,input.length() ,new FileOutputStream( output ).getChannel() ,true );
	}

	/**
	 * 使用nio把内存缓冲区保存到文件中
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public final static void save(ByteBuffer input, File output) throws IOException
	{
		FileChannel channel = null;
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream( output );
			channel = out.getChannel();
			while ( input.hasRemaining() )
			{
				channel.write( input );
			}
			channel.force( false );
		}
		finally
		{
			close( channel );
			close( out );
		}
	}

	/**
	 * 使用nio把网络地址保存到文件中
	 * <pre>
	 * 		//异步把图片保存到本地
	 * 		URL url = new URL("http://img.taopic.com/uploads/allimg/140816/235034-140Q60K05695.jpg");
	 *  		File file = new File("/tmp/img3.jpg");
	 *  		Executors.execute(IOUtils.class, "save", new Object[ ] {url,file});
	 * </pre>
	 *
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public final static void save(URL input, File output) throws IOException
	{
		URLConnection conn = input.openConnection();
		save( conn.getInputStream() ,conn.getContentLength() ,output );
	}

	/**
	 * 使用nio把流保存到文件中
	 * @param input 输入流
	 * @param inputSize 输入流大小, 注意不是available大小
	 * @param output 文件
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public final static void save(InputStream input, long inputSize, File output) throws IOException
	{
		output.getParentFile().mkdirs();
		//尝试获取输入流管道, 如果文件文件输入流, 直接获取文件输入管道
		//如果其他输入流内建8k缓冲区模拟管道, 针对现在计算内存来说8K的缓冲区有点小
		//详细参见Channels类
		save( Channels.newChannel( input ) ,inputSize ,new FileOutputStream( output ).getChannel() ,true );
	}

	/**
	 * 使用nio把输入通道保存到文件通道中, 这里需要指定inputSize也就是输入流的大小. 不能使用流的available表示输入流大小
	 * 这一点需要注意.特别是URL获得输入流available表示当前缓冲区中大小,根本不是流大小.
	 * @param input
	 * @param size
	 * @param output
	 * @param autoClose
	 * @throws IOException
	 */
	public final static void save(ReadableByteChannel input, long inputSize, FileChannel output, boolean autoClose) throws IOException
	{
		try
		{
			output.transferFrom( input ,0 ,inputSize );
		}
		finally
		{
			if ( autoClose )
			{
				close( input );
				close( output );
			}
		}
	}

	/**
	 * 计算字符串md5的值,使用16进制编码
	 * @param string 字符串
	 * @return
	 * @throws IOException
	 */
	public final static String md5(String string) throws IOException
	{
		return md5( new ByteArrayInputStream( string.getBytes() ) ,true );
	}

	/**
	 * 计算字符串md5的值,使用16进制编码
	 * @param string 字符串
	 * @param charset 字符集编码
	 * @return
	 * @throws IOException
	 */
	public final static String md5(String string, String charset) throws IOException
	{
		return md5( new ByteArrayInputStream( string.getBytes( charset ) ) ,true );
	}

	/**
	 * 计算网络地址对应流md5值, 使用16进制编码
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public final static String md5(URL url) throws IOException
	{
		return md5( url.openStream() ,true );
	}

	/**
	 * 计算文件md5值,使用16进制编码
	 * @param file 文件
	 * @return
	 * @throws IOException
	 */
	public final static String md5(File file) throws IOException
	{
		return md5( new FileInputStream( file ) ,true );
	}

	/**
	 * 计算输入流md5值, 使用16进制编码
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public final static String md5(InputStream input) throws IOException
	{
		return md5( input ,true );
	}

	/**
	 * 计算输入流md5值, 使用16进制编码
	 * @param input 输入流
	 * @param closeInputStream 是否自动关闭输入流
	 * @return 返回流MD5值
	 * @throws IOException
	 */
	public final static String md5(InputStream input, boolean closeInputStream) throws IOException
	{
		return md5( input ,new byte[DEFAULT_BUFFER_SIZE] ,closeInputStream );
	}

	/**
	 * 计算输入流md5值, 使用16进制编码
	 * @param input 输入流
	 * @param buffer 缓冲大小
	 * @param closeInputStream 是否自动关闭输入流
	 * @return 返回流MD5值
	 * @throws IOException
	 */
	public final static String md5(InputStream input, byte buffer[], boolean closeInputStream) throws IOException
	{
		MessageDigest digest = null;
		int n = 0;
		try
		{
			digest = MessageDigest.getInstance( "MD5" );
			while ( EOF != (n = input.read( buffer )) )
			{
				digest.update( buffer ,0 ,n );
			}
		}
		catch ( Exception e )
		{
			throw new IOException( e );
		}
		finally
		{
			if ( closeInputStream )
			{
				close( input );
			}
		}
		return new BigInteger( 1, digest.digest() ).toString( 16 );
	}

	/**
	 * 把reader转换成BufferedReader
	 * @param reader
	 * @return
	 */
	public final static BufferedReader toBufferedReader(Reader reader)
	{
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader( reader );
	}

	/**
	 * 把字符换转换输入流
	 * @param input 源字符串
	 * @param encoding 字符串字符集,默认utf-8
	 * @return 返回输入流
	 * @throws IOException
	 */
	public final static InputStream toInputStream(String input, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		byte[] bytes = input.getBytes( encoding );
		return new ByteArrayInputStream( bytes );
	}

	/**
	 * 把ByteBuffer转换输入流
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public final static InputStream toInputStream(final ByteBuffer buffer) throws IOException
	{
		InputStream answer = new InputStream()
		{

			public int available()
			{
				return buffer.remaining();
			}

			public synchronized int read() throws IOException
			{
				if ( !buffer.hasRemaining() )
				{
					return -1;
				}
				return buffer.get();
			}

			public synchronized int read(byte[] bytes, int off, int len) throws IOException
			{
				if ( !buffer.hasRemaining() ) return -1;
				len = Math.min( len ,buffer.remaining() );
				buffer.get( bytes ,off ,len );
				return len;
			}
		};
		return answer;
	}

	/**
	 * 把ByteBuffer转换输出流
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public final static OutputStream toOutputStream(final ByteBuffer buffer) throws IOException
	{
		OutputStream answer = new OutputStream()
		{

			@Override
			public void write(int b) throws IOException
			{
				buffer.put( (byte) b );
			}

			@Override
			public void write(byte[] bytes, int off, int len) throws IOException
			{
				buffer.put( bytes ,off ,len );
			}
		};
		return answer;
	}

	/**
	 * 使用utf-8字符集把字符串转换ByteBuffer对象.
	 * @param input 输入流
	 * @return
	 * @throws IOException
	 */
	public final static ByteBuffer toByteBuffer(String input) throws IOException
	{
		return ByteBuffer.wrap( input.getBytes( "utf-8" ) );
	}

	/**
	 * 把文件转换ByteBuffer对象
	 * @param input 输入流
	 * @return
	 * @throws IOException
	 */
	public final static ByteBuffer toByteBuffer(File input) throws IOException
	{
		return toByteBuffer( new FileInputStream( input ) ,new byte[DEFAULT_BUFFER_SIZE] );
	}

	/**
	 * 把网络地址内容转换ByteBuffer对象
	 * @param input 输入流
	 * @return
	 * @throws IOException
	 */
	public final static ByteBuffer toByteBuffer(URL input) throws IOException
	{
		return toByteBuffer( input.openStream() ,new byte[DEFAULT_BUFFER_SIZE] );
	}

	/**
	 * 把输入流转换ByteBuffer对象
	 * @param input 输入流
	 * @return
	 * @throws IOException
	 */
	public final static ByteBuffer toByteBuffer(InputStream input) throws IOException
	{
		return toByteBuffer( input ,new byte[DEFAULT_BUFFER_SIZE] );
	}

	/**
	 * 把输入流转换ByteBuffer对象
	 * @param input 输入流
	 * @param buffer 转换使用缓冲区
	 * @return
	 * @throws IOException
	 */
	public final static ByteBuffer toByteBuffer(InputStream input, byte[] buffer) throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream()
		{

			@Override
			public synchronized byte[] toByteArray()
			{
				//直接返回内部buffer数组, 详细参见ByteArrayOutputStream源码
				return buf;
			}
		};
		write( input ,output ,buffer ,true ,true );
		//复用ByteArrayOutputStream内部缓冲区, 直接构建ByteBuffer
		return ByteBuffer.wrap( output.toByteArray() ,0 ,output.size() );
	}

	/**
	 * 递归遍历目录下所有的文件,并且执行事件.
	 * @param aNode 指定目录
	 * @param filter 文件名过滤, 过滤文件名中是否包含指定的关键字.如果为null遍历所有文件子文件夹
	 * @param event 触发遍历事件
	 */
	public final static boolean traverse(File aNode, String[] filters, TraverseEvent event)
	{
		if ( aNode.canRead() )
		{
			if ( aNode.isDirectory() )
			{
				final File[] nodes = aNode.listFiles();
				boolean isAbort = false;
				for ( File element : nodes )
				{
					//traverse( element,filters ,aFiles );
					isAbort = traverse( element ,filters ,event );
					if ( isAbort )
					{
						break;
					}
				}
			}
			else if ( aNode.isFile() && aNode.length() > 0 )
			{
				String nodename = aNode.getName();
				if ( filters != null && filters.length > 0 )
				{
					for ( String filter : filters )
					{
						if ( nodename.indexOf( filter ) >= 0 )
						{
							event.doEvent( 0 ,aNode );
							return event.isAbort();
						}
					}
				}
				else
				{
					event.doEvent( 0 ,aNode );
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
	public final static void clean(File dir)
	{
		del( dir ,true );
	}

	/**
	 * 删除目录文件下所有子文件和子目录,同时删除目录本身
	 * @param file
	 */
	public final static void del(File file)
	{
		del( file ,false );
	}

	/**
	 * 删除文件或者文件目录,如果是目录删除目录下的所有内容
	 * @param file 文件或者文件夹
	 * @param clean 如果是目录是否删除目录本身, true: 删除自身; false:保留自身
	 */
	public final static void del(File file, boolean clean)
	{
		if ( file.exists() )
		{
			if ( file.isFile() )
			{
				//文件直接删除
				file.delete();
			}
			else if ( file.isDirectory() )
			{
				File fiels[] = file.listFiles();
				int length = fiels.length;
				if ( length == 0 && !clean )
				{
					// 若目录下没有文件则直接删除
					file.delete();
				}
				else if ( length >= 0 )
				{
					//遍历文件数组删除文件,如果是目录递归删除
					for ( int i = 0 ; i < length ; i++ )
					{
						if ( fiels[i].isDirectory() )
						{
							del( fiels[i] ,false );
						}
						else
						{
							fiels[i].delete();
						}
					}
					if ( !clean )
					{
						file.delete();
					}
				}
			}
		}
	}

	/**
	 * MacOS 文件目录下自动生成__MACOSX目录, 在search时很经常会检索到这个目录下的文件.
	 * 本方法清理__MACOSX目录
	 * @param dir 指定目录
	 * @return
	 */
	public static void cleanMACOSX(File dir)
	{
		List<File> found = search( dir ,"__MACOSX" ,true );
		if ( found != null )
		{
			for ( File file : found )
			{
				del( file ,true );
			}
		}
	}

	/**
	 * 在指定目录以及子目录中查找文件
	 * @param dir 指定目录
	 * @param match 匹配文件关键字
	 * @return
	 */
	public static List<File> search(File dir, String match)
	{
		List<File> answer = new ArrayList<File>();
		search( dir ,answer ,match ,false );
		return answer;
	}

	/**
	 * 在指定目录以及子目录中查找文件或者文件目录
	 * @param dir 指定目录
	 * @param match 匹配文件关键字
	 * @param isMatchDirectory 是否匹配文件目录
	 * @return
	 */
	public static List<File> search(File dir, String match, boolean isMatchDirectory)
	{
		List<File> answer = new ArrayList<File>();
		search( dir ,answer ,match ,isMatchDirectory );
		return answer;
	}

	/**
	 * 在指定目录以及子目录中查找文件或者文件目录
	 * @param dir 指定目录
	 * @param array 匹配文件存放列表
	 * @param match 匹配文件关键字
	 * @param isMatchDirectory 是否匹配文件目录
	 */
	public static void search(File dir, List<File> array, String match, boolean isMatchDirectory)
	{
		File[] files = dir.listFiles();
		for ( File file : files )
		{
			if ( file.isDirectory() )
			{
				if ( isMatchDirectory && file.getName().indexOf( match ) >= 0 )
				{
					array.add( file );
				}
				search( file ,array ,match ,isMatchDirectory );
			}
			else if ( file.getName().indexOf( match ) >= 0 )
			{
				array.add( file );
			}
		}
	}

	/**
	 * 使用zlib(压缩算法)压缩输入到输出流中,自动关闭输入/输出流
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @throws IOException
	 */
	public final static void deflater(InputStream input, OutputStream output) throws IOException
	{
		deflater( input ,output ,true ,true );
	}

	/**
	 * 使用zlib(压缩算法)压缩输入到输出流中
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @param autoCloseStream 是否自动关闭输入/输出流
	 * @throws IOException
	 */
	public final static void deflater(InputStream input, OutputStream output, boolean autoCloseStream) throws IOException
	{
		deflater( input ,output ,autoCloseStream ,autoCloseStream );
	}

	/**
	 * 使用zlib(压缩算法)压缩输入到输出流中
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @param closeInputStream 是否自动关闭输出流
	 * @param closeOutputStream 是否自动关闭输出流
	 * @throws IOException
	 */
	public final static void deflater(InputStream input, OutputStream output, boolean closeInputStream, boolean closeOutputStream) throws IOException
	{
		Deflater def = new Deflater( Deflater.BEST_COMPRESSION, true );
		DeflaterOutputStream dos = new DeflaterOutputStream( output, def );
		write( input ,dos ,new byte[DEFAULT_BUFFER_SIZE] ,closeInputStream ,closeOutputStream );
	}

	/**
	 * 使用zlib(压缩算法)解压缩输入到输出流中,自动关闭输入/输出流
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 * @throws IOException
	 */
	public final static void inflater(InputStream input, OutputStream output) throws IOException
	{
		inflater( input ,output ,true ,true );
	}

	/**
	 * 使用zlib(压缩算法)解压缩输入到输出流中
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 * @param autoCloseStream 是否自动关闭输入/输出流
	 * @throws IOException
	 */
	public final static void inflater(InputStream input, OutputStream output, boolean autoCloseStream) throws IOException
	{
		inflater( input ,output ,autoCloseStream ,autoCloseStream );
	}

	/**
	 * 使用zlib(压缩算法)解压缩输入到输出流中
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 * @param closeInputStream 是否自动关闭输出流
	 * @param closeOutputStream 是否自动关闭输出流
	 * @throws IOException
	 */
	public final static void inflater(InputStream input, OutputStream output, boolean closeInputStream, boolean closeOutputStream) throws IOException
	{
		Inflater inf = new Inflater( true );
		InflaterInputStream iis = new InflaterInputStream( input, inf );
		write( iis ,output ,new byte[DEFAULT_BUFFER_SIZE] ,closeInputStream ,closeOutputStream );
	}

	/**
	 * 使用gzip(压缩算法)压缩输入到输出流中,自动关闭输入/输出流
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @throws IOException
	 */
	public final static void gzip(InputStream input, OutputStream output) throws IOException
	{
		gzip( input ,output ,true ,true );
	}

	/**
	 * 使用gzip(压缩算法)压缩输入到输出流中
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @param autoCloseStream 是否自动关闭输出流
	 * @throws IOException
	 */
	public final static void gzip(InputStream input, OutputStream output, boolean autoCloseStream) throws IOException
	{
		gzip( input ,output ,autoCloseStream ,autoCloseStream );
	}

	/**
	 * 使用gzip(压缩算法)压缩输入到输出流中
	 * @param input 未压缩输入流
	 * @param output 压缩后的输出流
	 * @param closeInputStream 是否自动关闭输出流
	 * @param closeOutputStream 是否自动关闭输出流
	 * @throws IOException
	 */
	public final static void gzip(InputStream input, OutputStream output, boolean closeInputStream, boolean closeOutputStream) throws IOException
	{
		GZIPOutputStream gos = new GZIPOutputStream( output );
		write( input ,gos ,new byte[DEFAULT_BUFFER_SIZE] ,closeInputStream ,closeOutputStream );
	}

	/**
	 * 使用gzip(压缩算法)解压缩输入到输出流中,自动关闭输入/输出流
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 */
	public final static void ungzip(InputStream input, OutputStream output) throws IOException
	{
		ungzip( input ,output ,true ,true );
	}

	/**
	 * 使用gzip(压缩算法)解压缩输入到输出流中
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 * @param autoCloseStream 是否自动关闭输入/输出流
	 */
	public final static void ungzip(InputStream input, OutputStream output, boolean autoCloseStream) throws IOException
	{
		ungzip( input ,output ,autoCloseStream ,autoCloseStream );
	}

	/**
	 * 使用gzip(压缩算法)解压缩输入到输出流中
	 * @param input 压缩输入流
	 * @param output 解压缩后的输出流
	 * @param closeInputStream 是否自动关闭输出流
	 * @param closeOutputStream 是否自动关闭输出流
	 */
	public final static void ungzip(InputStream input, OutputStream output, boolean closeInputStream, boolean closeOutputStream) throws IOException
	{
		GZIPInputStream gis = new GZIPInputStream( input );
		write( gis ,output ,new byte[DEFAULT_BUFFER_SIZE] ,closeInputStream ,closeOutputStream );
	}

	/**
	 * 使用zip压缩(归档算法)文件或者文件夹, 目标文件在当前文件所在目录中.
	 * jdk内置的zip算法不支持中文文件名称, 请使用ant提供的zip包压缩中文文件.
	 * @param src 被压缩文件
	 * @throws IOException
	 */
	public final static void zip(File src) throws IOException
	{
		String basePath = src.getParent();
		String destPath = basePath + File.separator + src.getName() + ".zip";
		zip( src ,new File( destPath ) );
	}

	/**
	 * 使用zip压缩(归档算法)文件或者文件夹,jdk内置的zip算法不支持中文文件名称, 请使用ant提供的zip包压缩中文文件.
	 * @param src 被压缩文件
	 * @param dest 目标路径
	 * @throws IOException
	 */
	public final static void zip(File src, File dest) throws IOException
	{
		// 对输出文件做CRC32校验
		CheckedOutputStream cos = new CheckedOutputStream( new FileOutputStream( dest ), new CRC32() );
		ZipOutputStream zos = new ZipOutputStream( cos );
		zip( src ,zos ,"" );
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
	public final static void zip(File src, ZipOutputStream zoutput, String basePath) throws IOException
	{
		if ( src.isDirectory() )
		{
			File[] files = src.listFiles();
			if ( files.length < 1 )
			{ // 构建空目录
				ZipEntry entry = new ZipEntry( basePath + src.getName() + File.separator );
				zoutput.putNextEntry( entry );
				zoutput.closeEntry();
			}
			for ( File file : files )
			{ // 递归压缩
				zip( file ,zoutput ,basePath + src.getName() + File.separator );
			}
		}
		else
		{
			ZipEntry entry = new ZipEntry( basePath + src.getName() );
			zip( entry ,new FileInputStream( src ) ,zoutput );
		}
	}

	/**
	 * 向zip流中追加压缩实体
	 * @param entryPath 路径,文件压缩路径.jdk内置的zip算法不支持中文文件名称, 请使用ant提供的zip包压缩中文文件.
	 * @param data 内容
	 * @param zoutput zip流
	 * @throws IOException
	 */
	public final static void zip(String entryPath, String data, ZipOutputStream zoutput) throws IOException
	{
		zip( entryPath ,data.getBytes() ,zoutput );
	}

	/**
	 * 向zip流中追加压缩实体
	 * @param entryPath 路径,文件压缩路径. jdk内置的zip算法不支持中文文件名称, 请使用ant提供的zip包压缩中文文件.
	 * @param data 内容
	 * @param zoutput zip流
	 * @throws IOException
	 */
	public final static void zip(String entryPath, byte[] data, ZipOutputStream zoutput) throws IOException
	{
		ZipEntry sheetEntry = new ZipEntry( entryPath );
		zoutput.putNextEntry( sheetEntry );
		zoutput.write( data ,0 ,data.length );
		zoutput.closeEntry();
	}

	/**
	 * 使用zip压缩(归档算法)文件或者文件夹,jdk内置的zip算法不支持中文文件名称, 请使用ant提供的zip包压缩中文文件.
	 * @param entry 压缩实体
	 * @param input 被压缩流
	 * @param zoutput 压缩目标流
	 * @throws IOException
	 */
	public final static void zip(ZipEntry entry, InputStream input, ZipOutputStream zoutput) throws IOException
	{
		zoutput.putNextEntry( entry );
		write( input ,zoutput ,new byte[512] ,true ,false );
		zoutput.closeEntry();
	}

	/**
	 * 解压缩的zip(归档算法)流
	 * @param input zip压缩流
	 * @param dir 解压缩的目录
	 * @throws IOException
	 */
	public final static void unzip(InputStream input, File dir) throws IOException
	{
		// 防止文件名中有中文时出错
		System.setProperty( "sun.zip.encoding" ,System.getProperty( "sun.jnu.encoding" ) );
		dir.mkdirs();
		ZipInputStream zinput = new ZipInputStream( input );
		ZipEntry ze = null;
		while ( (ze = zinput.getNextEntry()) != null )
		{
			if ( ze.isDirectory() )
			{
				new File( dir.getPath(), ze.getName() ).mkdirs();
			}
			else
			{
				append( zinput ,new File( dir.getPath(), ze.getName() ) ,false );
			}
			zinput.closeEntry();
		}
		zinput.closeEntry();
		zinput.close();
	}

	/**
	 * 解压缩的zip(归档算法)文件
	 * @param src zip文件
	 * @param dir 解压缩的目录
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public final static void unzip(File src, File dir) throws IOException
	{
		unzip( new FileInputStream( src ) ,dir );
	}

	/**
	 * 格式化大小, 单位K 或者M 或者G
	 * @param size
	 * @return
	 */
	public final static String size(long size)
	{
		String answer = null;
		if ( size < 1024 )
		{ //1K
			answer = Long.toString( size ) + "Byte";
		}
		else if ( size < 1048576 )
		{ //1024*1024=1M
			answer = String.format( "%.2f" ,size / 1024.0 ) + "K";
		}
		else if ( size < 1073741824 )
		{ //1024*1024*1024=1G
			answer = String.format( "%.2f" ,size / (1048576.0) ) + "M";
		}
		else if ( size < 1099511627776L )
		{ //1024*1024*1024*11024=1T
			answer = String.format( "%.2f" ,size / (1073741824.0) ) + "G";
		}
		else if ( size > 1099511627776L )
		{
			answer = "";
		}
		return answer;
	}

	/**
	 * 关闭文件通道(nio)
	 * @param channel
	 */
	public final static void close(FileChannel channel)
	{
		try
		{
			if ( channel != null )
			{
				channel.close();
			}
		}
		catch ( IOException e )
		{
			// ignore
		}
	}

	/**
	 * input输入流关闭
	 * @param input
	 */
	public final static void close(InputStream input)
	{
		try
		{
			if ( input != null )
			{
				input.close();
			}
		}
		catch ( IOException e )
		{
			// ignore
		}
	}

	/**
	 * output流关闭
	 * @param output
	 */
	public final static void close(OutputStream output)
	{
		try
		{
			if ( output != null )
			{
				output.flush();
				output.close();
			}
		}
		catch ( IOException e )
		{
			// ignore
		}
	}

	/**
	 * 关闭reader
	 * @param reader
	 */
	public final static void close(Reader reader)
	{
		try
		{
			if ( reader != null )
			{
				reader.close();
			}
		}
		catch ( IOException e )
		{
			// ignore
		}
	}

	/**
	 * writer关闭
	 * @param writer
	 */
	public final static void close(Writer writer)
	{
		try
		{
			if ( writer != null )
			{
				writer.close();
			}
		}
		catch ( IOException e )
		{
			// ignore
		}
	}

	/**
	 * 关闭url连接
	 * @param conn
	 */
	public final static void close(URLConnection conn)
	{
		if ( conn instanceof HttpURLConnection )
		{
			((HttpURLConnection) conn).disconnect();
		}
	}

	/**
	 * 关闭closeable
	 * @param closeable
	 */
	public final static void close(Closeable closeable)
	{
		try
		{
			closeable.close();
		}
		catch ( IOException e )
		{
			// ignore
		}
	}

	/**
	 * 关闭autoCloseable
	 * @param autoCloseable
	 */
	public final static void close(AutoCloseable closeable)
	{
		try
		{
			if ( closeable != null )
			{
				closeable.close();
			}
		}
		catch ( Exception e )
		{
			// ignore
		}
	}
}
