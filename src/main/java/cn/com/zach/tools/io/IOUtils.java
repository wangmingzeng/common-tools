package cn.com.zach.tools.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * io操作工具类
 * @author zengwangming
 *
 */
public class IOUtils
{

	/**
	 * 流结束EOF标记
	 */
	private static final int	EOF						= -1;

	/**
	 * 1KB大小
	 */
	public static final long	ONE_KB					= 1024;

	/**
	 * 1MB大小
	 */
	public static final long	ONE_MB					= ONE_KB * ONE_KB;

	/**
	 * 默认nio缓冲区大小
	 */
	private static final long	FILE_COPY_BUFFER_SIZE	= ONE_MB * 30;

	/**
	 * 默认bio缓冲区大小
	 */
	private static final int	DEFAULT_BUFFER_SIZE		= 1024;

	/**
	 * URL读取内容
	 * @param input url地址
	 * @param encoding URL内容字符集,默认utf-8
	 * @return 返回URL中字符串
	 * @throws IOException
	 */
	public static String toString(URL input, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		StringWriter writer = new StringWriter();
		copy( new InputStreamReader( input.openStream(), encoding ) ,writer );
		return writer.toString();
	}

	/**
	 * 读取文件
	 * @param input 文件
	 * @param encoding 文件字符集,默认utf-8
	 * @return 返回读取字符串
	 * @throws IOException
	 */
	public static String toString(File input, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		StringWriter writer = new StringWriter();
		copy( new InputStreamReader( new FileInputStream( input ), encoding ) ,writer );
		return writer.toString();
	}

	/**
	 * 从reader读取字符串内容
	 * @param input 输入reader
	 * @return 返回读取字符串
	 * @throws IOException
	 */
	public static String toString(Reader input) throws IOException
	{
		StringWriter writer = new StringWriter();
		copy( input ,writer );
		return writer.toString();
	}

	/**
	 * 从输入流中读取字符窜内容
	 * @param input 输入流
	 * @param encoding 输入流字符集,默认utf-8
	 * @return 返回读取字符串
	 * @throws IOException
	 */
	public static String toString(InputStream input, String encoding) throws IOException
	{
		StringWriter writer = new StringWriter();
		copy( input ,writer ,encoding );
		return writer.toString();
	}

	//写入
	/**
	 * 字符串追加到文件中
	 * @param data 字符串
	 * @param output 写入文件
	 * @param encoding 字符串字符集,默认使用utf-8
	 * @throws IOException
	 */
	public static void append(String data, File output, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		OutputStream out = null;
		try
		{
			out = new FileOutputStream( output, true );
			out.write( data.getBytes( encoding ) );
		}
		finally
		{
			close( out );
		}
	}

	/**
	 * 把URL中内容追加到文件
	 * @param url url地址
	 * @param output 写入文件
	 * @throws IOException
	 */
	public static void append(URL url, File output) throws IOException
	{
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = url.openStream();
			out = new FileOutputStream( output, true );
			copy( in ,out );
		}
		finally
		{
			close( in );
			close( out );
		}
	}

	/**
	 * 把输入流追加到文件 
	 * @param input 输入流
	 * @param output 写入文件
	 * @throws IOException
	 */
	public static void append(InputStream input, File output) throws IOException
	{
		OutputStream out = null;
		try
		{
			out = new FileOutputStream( output, true );
			copy( input ,out );
		}
		finally
		{
			close( out );
		}
	}

	/**
	 * 把Reader写入到文件
	 * @param input reader
	 * @param output 写入文件
	 * @throws IOException
	 */
	public static void append(Reader input, File output) throws IOException
	{
		Writer out = null;
		try
		{
			out = new FileWriter( output, true );
			copy( input ,out );
		}
		finally
		{
			close( out );
		}
	}

	/**
	 * 使用nio追加两个文件
	 * @param srcFile 源文件
	 * @param destFile 目标文件
	 * @throws IOException
	 */
	public static void appendFile(File srcFile, File destFile) throws IOException
	{
		appendFile( srcFile ,destFile ,true );
	}

	/**
	 * 使用nio追加两个文件
	 * @param srcFile 源文件
	 * @param destFile 目标文件
	 * @param preserveFileDate 是或否修改文件最后一次修改时间
	 * @throws IOException
	 */
	public static void appendFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel input = null;
		FileChannel output = null;
		try
		{
			destFile.getParentFile().mkdirs();
			fis = new FileInputStream( srcFile );
			fos = new FileOutputStream( destFile, true );
			input = fis.getChannel();
			output = fos.getChannel();
			long bytesTransferred = 0;
			while ( bytesTransferred < input.size() )
			{
				bytesTransferred += input.transferTo( 0 ,input.size() ,output );
			}
			if ( preserveFileDate )
			{
				destFile.setLastModified( srcFile.lastModified() );
			}
		}
		finally
		{
			close( output );
			close( fos );
			close( input );
			close( fis );
		}
	}

	/**
	 * 字符串写入文件
	 * @param data 字符串
	 * @param output 文件
	 * @param encoding 字符串字符集,默认utf-8
	 * @throws IOException
	 */
	public static void write(String data, File output, String encoding) throws IOException
	{
		OutputStream out = null;
		try
		{
			out = new FileOutputStream( output );
			write( data ,out ,encoding );
		}
		finally
		{
			close( out );
		}
	}

	/**
	 * 把byte数组写入Writer中
	 * @param data 输入byte数组
	 * @param output 输出Writer
	 * @param encoding 字符串字符集,默认utf-8
	 * @throws IOException
	 */
	public static void write(byte[] data, Writer output, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		if ( data != null )
		{
			output.write( new String( data, encoding ) );
		}
	}

	/**
	 * 把字符串写入输出流中
	 * @param data 字符串
	 * @param output 输出流
	 * @param encoding 字符串字符集,默认utf-8
	 * @throws IOException
	 */
	public static void write(String data, OutputStream output, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		if ( data != null )
		{
			output.write( data.getBytes( encoding ) );
		}
	}

	//复制方法
	/**
	 * 使用nio复制文件
	 * @param srcFile 源文件
	 * @param destFile 目标文件
	 * @throws IOException
	 */
	public static void copyFile(File srcFile, File destFile) throws IOException
	{
		copyFile( srcFile ,destFile ,true );
	}

	/**
	 * 使用nio复制文件到文件夹
	 * @param srcFile 源文件
	 * @param destDir 目标文件夹
	 * @throws IOException
	 */
	public static void copyFileToDirectory(File srcFile, File destDir) throws IOException
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
	public static void copyDirectory(File srcDir, File destDir) throws IOException
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
	public static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate) throws IOException
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
	private static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate, List<String> exclusionList) throws IOException
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
	public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException
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

	/**
	 * 把URL中内容复制到文件
	 * @param input 源url
	 * @param output 目标文件
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public static int copy(URL input, File output) throws IOException
	{
		int count = 0;
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = input.openStream();
			out = new FileOutputStream( output );
			count = copy( in ,out );
		}
		finally
		{
			close( in );
			close( out );
		}
		return count;
	}

	/**
	 * 把URL中内容复制到输出流中
	 * @param input 源RUL
	 * @param output 目标输出流
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public static int copy(URL input, OutputStream output) throws IOException
	{
		int count = 0;
		InputStream in = null;
		try
		{
			in = input.openStream();
			count = copy( in ,output );
		}
		finally
		{
			close( in );
		}
		return count;
	}

	/**
	 * 把URL中内容复制到Writer中
	 * @param input 源URL
	 * @param output 目标writer
	 * @param encoding URL中内容字符集,默认utf-8
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public static int copy(URL input, Writer output, String encoding) throws IOException
	{
		int count = 0;
		Reader in = null;
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		try
		{
			in = new InputStreamReader( input.openStream(), encoding );
			count = copy( in ,output );
		}
		finally
		{
			close( in );
		}
		return count;
	}

	/**
	 * 使用bio文件复制
	 * @param input 源文件
	 * @param output 目标文件
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public static int copy(File input, File output) throws IOException
	{
		int count = 0;
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = new FileInputStream( input );
			out = new FileOutputStream( output );
			count = copy( in ,out );
		}
		finally
		{
			close( in );
			close( out );
		}
		return count;
	}

	/**
	 * Reader复制到OutputStream
	 * @param input 源reader
	 * @param output 目标OutputStream
	 * @param encoding reader字符集字符集,默认utf-8
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public static int copy(Reader input, OutputStream output, String encoding) throws IOException
	{
		int count = 0;
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		OutputStreamWriter out = new OutputStreamWriter( output, encoding );
		count = copy( input ,out );
		out.flush();
		return count;
	}

	/**
	 * Reader复制到Writer
	 * @param input 源reader
	 * @param output 目标write
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public static int copy(Reader input, Writer output) throws IOException
	{
		long count = copy( input ,output ,new char[DEFAULT_BUFFER_SIZE] );
		if ( count > Integer.MAX_VALUE )
		{
			return -1;
		}
		return (int) count;
	}

	/**
	 * 输入流复制到Writer
	 * @param input 源输入流
	 * @param output 目标writer
	 * @param encoding 输入流字符集字符集,默认utf-8
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public static int copy(InputStream input, Writer output, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		InputStreamReader in = new InputStreamReader( input, encoding );
		return copy( in ,output );
	}

	/**
	 * 把输入流复制到输出流
	 * @param input 源输入流
	 * @param output 目标输出流
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public static int copy(InputStream input, OutputStream output) throws IOException
	{
		long count = copy( input ,output ,new byte[DEFAULT_BUFFER_SIZE] );
		if ( count > Integer.MAX_VALUE )
		{
			return -1;
		}
		return (int) count;
	}

	/**
	 * Reader复制到Writer
	 * @param input 输入
	 * @param output 输出
	 * @param buffer 缓冲区
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public static long copy(Reader input, Writer output, char[] buffer) throws IOException
	{
		long count = 0;
		int n = 0;
		while ( EOF != (n = input.read( buffer )) )
		{
			output.write( buffer ,0 ,n );
			count += n;
		}
		return count;
	}

	/**
	 * 复制输入流到输出流
	 * @param input 输入
	 * @param output 输出
	 * @param buffer 缓冲区
	 * @return 返回复制长度
	 * @throws IOException
	 */
	public static long copy(InputStream input, OutputStream output, byte[] buffer) throws IOException
	{
		long count = 0;
		int n = 0;
		while ( EOF != (n = input.read( buffer )) )
		{
			output.write( buffer ,0 ,n );
			count += n;
		}
		return count;
	}

	/**
	 * 把reader转换成BufferedReader
	 * @param reader
	 * @return
	 */
	public static BufferedReader toBufferedReader(Reader reader)
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
	public static InputStream toInputStream(String input, String encoding) throws IOException
	{
		if ( encoding == null || encoding.length() == 0 )
		{
			encoding = "utf-8";
		}
		byte[] bytes = input.getBytes( encoding );
		return new ByteArrayInputStream( bytes );
	}

	/**
	 * 递归遍历目录下所有的文件,并且执行事件.
	 * @param aNode 指定目录
	 * @param filter 文件名过滤, 过滤文件名中是否包含指定的关键字.如果为null遍历所有文件子文件夹
	 * @param event 触发遍历事件
	 */
	public static final boolean traverse(File aNode, String[] filters, TraverseEvent event)
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
	 * 关闭文件通道(nio)
	 * @param channel 
	 */
	public static void close(FileChannel channel)
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
	public static void close(InputStream input)
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
	public static void close(OutputStream output)
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
	public static void close(Reader reader)
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
	public static void close(Writer writer)
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
	public static void close(URLConnection conn)
	{
		if ( conn instanceof HttpURLConnection )
		{
			((HttpURLConnection) conn).disconnect();
		}
	}
	
	public static boolean deleteFile(File file) {
		if(file.isDirectory()) {
			String[] childs = file.list();
			//递归删除子目录
			for(int i=0; i<childs.length; i++) {
				boolean result = deleteFile(new File(file, childs[i]));
				if(!result) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return file.delete();
	}
}
