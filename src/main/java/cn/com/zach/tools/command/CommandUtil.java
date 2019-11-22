package cn.com.zach.tools.command;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * 执行命令行工具类
 * 
 * @author huangchao
 */
public class CommandUtil
{

	/**
	 * 是否是window系统
	 */
	private static final boolean	isWindow				= System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) >= 0;

	/**
	 * 控制台默认使用的字符集
	 */
	private static final String		DEFAULT_CONSOL_CHARSET	= "utf-8";

	/**
	 * 获取执行结果
	 * @param process	执行process对象
	 * @param isPrintConsole	是否打印到终端
	 * @param charset 从终端获取获取执行结果使用的字符集
	 * @return	执行结果, 如果使用输出重定向, 这里获取不到结果
	 */
	private String execResult(Process process, String charset)
	{
		if ( charset == null )
		{
			charset = DEFAULT_CONSOL_CHARSET;
		}
		StringBuilder answer = new StringBuilder();
		BufferedInputStream buffer = null;
		BufferedReader reader = null;
		String line = null;
		try
		{
			//读取正常输出流
			buffer = new BufferedInputStream( process.getInputStream() );
			reader = new BufferedReader( new InputStreamReader( buffer, charset ) );
			while ( (line = reader.readLine()) != null )
			{
				answer.append( line ).append( "\n" );
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			if ( reader != null ) try
			{
				reader.close();
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
			if ( buffer != null ) try
			{
				buffer.close();
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
		}
		//读取异常输出流
		try
		{
			buffer = new BufferedInputStream( process.getErrorStream() );
			reader = new BufferedReader( new InputStreamReader( buffer ) );
			while ( (line = reader.readLine()) != null )
			{
				answer.append( line ).append( "\n" );
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			if ( reader != null ) try
			{
				reader.close();
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
			if ( buffer != null ) try
			{
				buffer.close();
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
		}
		return answer.toString();
	}

	/**
	 * 构建和系统相关的命令行数组
	 * @param command 命令行字符串
	 * @return
	 */
	private final String[] platformCommand(String command)
	{
		String[] answer = null;
		if ( isWindow )
		{
			answer = new String[]
			{ "cmd.exe ", "/c", command };
		}
		else
		{
			//执行Linux系统命令, 系统使用/bin/sh执行, 并且导入/etc/profile下的环境变量
			answer = new String[]
			{ "/bin/sh", "-c", "source /etc/profile; " + command };
		}
		return answer;
	}

	/**
	 * 构建Process对象, 用于执行本地命令的对象
	 * @param command 命令行数组
	 * @param envp	运行的上下文环境变量，每项都应该写成name=value的格式；null表示直接继承当前Java进程的全部环境变量
	 * @param dir 命令执行的工作目录；null表示继承当前Java进程的工作目录
	 * @param redirectOutput 输出重定向到指定文件, 即: 重定向stdout.
	 * @param redirectError 错误输出重定向到指定文件, 即: 重定向stderr.
	 * @param redirectErrorStream 是否把stderr输出合并到stdout中, 默认false.
	 * @return
	 */
	private Process build(String[] command, String[] envp, File dir, File redirectOutput, File redirectError, Boolean redirectErrorStream)
	{
		Process answer = null;
		try
		{
			ProcessBuilder pb = new ProcessBuilder( command );
			if ( envp != null )
			{
				Map<String, String> environment = pb.environment();
				for ( String envstring : envp )
				{
					int pos = envstring.indexOf( '=' ,0 );
					if ( pos != -1 )
					{
						environment.put( envstring.substring( 0 ,pos ) ,envstring.substring( pos + 1 ) );
					}
				}
			}
			if ( dir != null )
			{
				pb.directory( dir );
			}
			if ( redirectOutput != null )
			{
				pb.redirectOutput( redirectOutput );
			}
			if ( redirectError != null )
			{
				pb.redirectError( redirectError );
			}
			if ( redirectErrorStream != null )
			{
				pb.redirectErrorStream( redirectErrorStream.booleanValue() );
			}
			answer = pb.start();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		return answer;
	}

	/**
	 * 执行系统命令, 事例:
	 * <pre>
	 * 事例1(默认):
	 * 		//使用/bin/sh执行命令,
	 * 		String []cmd = { "/bin/sh", "-c",  "source /etc/profile; java -version ; pwd" };
	 * 		String res = exec(cmd, null, null, null, null, null)
	 *  		System.out.println(res);
	 *  	
	 * 事例2(设置环境变量):
	 * 		//设置系统系统环境变量
	 * 		String []evnpath={"TEST_PATH_1=test1", "TEST_PATH_2=test2"};
	 * 		//执行命令
	 * 		String []cmd = { "/bin/sh", "-c",  "export; echo ${TEST_PATH_2}" };
	 * 		String res = exec(cmd, evnpath, null, null, null, null);
	 * 		System.out.println(res);
	 *  
	 *  事例3(重定向输出流和错误流):
	 *  		//stdout(输出流)重定向
	 *  		File out = new File("/tmp/test.out");
	 *  		//stderr(错误输出流)重定向
	 *  		File err = new File("/tmp/err.out");
	 *  		String []cmd = { "/bin/sh", "-c",  "export" }; 
	 *  		
	 *  		//输出流重定向后, 程序无法获取输出流和错误输出流
	 *  		exec(cmd, null, null, out, err, null);
	 *  	
	 *  事例4(改变命令执行的工作目录):
	 *  		String []cmd = { "/bin/sh", "-c",  "pwd" }; 
	 *  		//设置命令执行的工作目录
	 *  		File dir = new File("/tmp");
	 *  		String res = exec(cmd, null, dir, null, null, null);
	 *  		System.out.println(res);
	 *  
	 * 事例5(执行长时间命令,主线程被阻塞):
	 * 		//执行ping命令, 命令永不结束, 主线程被阻塞直到令结束.
	 * 		//建议使用execAsync命令执行
	 *  		String []cmd = { "/bin/sh", "-c",  "ping 127.0.0.1" }; 
	 *  		String res = exec(cmd, null, null, null, null, "utf-8");
	 *  		System.out.println(res);
	 *  		
	 *  事例6(设置控制台字符集):
	 * 		//执行ping命令, 命令永不结束, 主线程被阻塞直到令结束.
	 * 		//建议使用execAsync命令执行
	 *  		String []cmd = { "/bin/sh", "-c",  "ls -l" }; 
	 *  		String res = exec(cmd, null, null, null, null, "utf-8");
	 *  		System.out.println(res);
	 * </pre>
	 * @param command 命令行数组, 必须
	 * @param envp 运行的上下文环境变量, 每项都应该写成name=value的格式. 非必须
	 * @param dir 命令执行的工作目录；null表示继承当前Java进程的工作目录. 非必须
	 * @param redirectOutput 输出重定向到指定文件, 即: 重定向stdout. 非必须
	 * @param redirectError 错误输出重定向到指定文件, 即: 重定向stderr. 非必须
	 * @param charset 控制台字符集. 非必须
	 */
	public String exec(String[] command, String[] envp, File dir, File redirectOutput, File redirectError, String charset)
	{
		String answer = null;
		Process process = this.build( command ,envp ,dir ,redirectOutput ,redirectError ,true );
		answer = execResult( process ,charset );
		try
		{
			process.waitFor();
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
		if ( process.exitValue() != 0 )
		{
			System.out.println( "error!" );
		}
		process.destroy();
		return answer;
	}

	/**
	 * 同步执行命令
	 * 	</pre>
	 * 	事例1:
	 * 		exec("java -version", null, null);
	 * 
	 * 	事例2:
	 * 		exec("java -version ; pwd ; ps -ef | grep java", null, null);
	 * 	
	 * 事例3:
	 * 		//主线程被阻塞
	 * 		exec("ping 127.0.0.1", null, null);
	 * 	</pre>
	 * @param command	命令字符串
	 * @param envp  运行的上下文环境变量, 每项都应该写成name=value的格式. 非必须
	 * @param dir 命令执行的工作目录；null表示继承当前Java进程的工作目录. 非必须
	 */
	public String exec(String command, String[] envp, File dir)
	{
		return exec( platformCommand( command ) ,envp ,dir ,null ,null ,null );
	}

	/**
	 * 同步执行命令
	 * 	</pre>
	 * 	事例1:
	 * 		exec("java -version");
	 * 
	 * 	事例2:
	 * 		exec("java -version ; pwd ; ps -ef | grep java");
	 * 	
	 * 事例3:
	 * 		//主线程被阻塞
	 * 		exec("ping 127.0.0.1");
	 * 	</pre>
	 * @param command	命令字符串
	 */
	public String exec(String command)
	{
		return exec( platformCommand( command ) ,null ,null ,null ,null ,null );
	}

	/**
	 * 异步获取命令执行的结果, 主要是用于长时间的命令执行
	 * 	注意: 	
	 * 		1. 执行依然是同步方法, 只是获取命令执行结果是异步的;
	 * 		2. 方法不阻塞主线程
	 * <pre>
	 * 	事例1:
	 * 		//测试ping命令, ping永不结束,使用异步方式接收输出流,主线程不被阻塞. 
	 * 		String []cmd = { "/bin/sh", "-c",  "ping 127.0.0.1" };
	 * 		execAsync(cmd, null, null,null, new DefaultProcessEvent() );
	 *		System.out.println("主线程没有被阻塞");
	 *
	 *	事例2:
	 * 		//测试ping命令, ping永不结束,使用异步方式接收输出流,主线程不被阻塞.
	 * 		String []cmd = { "/bin/sh", "-c",  "ping 127.0.0.1" };
	 * 		execAsync(cmd, null, null, null,new AbstractProcessEvent() {
	 *			@Override
	 *			public void readLine(Integer pid, String line) {
	 *				System.out.println("[pid="+ pid +"] "+ line);
	 *			}
	 *		});
	 *		System.out.println("主线程没有被阻塞");
	 *		
	 * 	事例3:
	 * 		//测试ping命令, ping永不结束,使用异步方式接收输出流,主线程不被阻塞.
	 * 		String []cmd = { "/bin/sh", "-c",  "ping 127.0.0.1" };
	 * 		execAsync(cmd, null, null, null, new ProcessEvent() {
	 *			public void start(Process process) {}
	 *			public void readNewLine(String line) {
	 *				System.out.println(line);
	 *			}
	 *			public void stop() {}
	 *		});
	 *		System.out.println("主线程没有被阻塞");
	 * </pre>
	 * @param command 命令行数组, 必须
	 * @param envp 运行的上下文环境变量, 每项都应该写成name=value的格式. 非必须
	 * @param dir  命令执行的工作目录；null表示继承当前Java进程的工作目录. 非必须
	 * @param charset 控制台字符集. 非必须
	 * @param perocessEvent 异步事件, 非必须
	 * @return
	 */
	public void execAsync(String[] command, String[] envp, File dir, final String charset, final ProcessEvent perocessEvent)
	{
		final Process process = this.build( command ,envp ,dir ,null ,null ,true );
		Thread stdoutThread = new Thread()
		{

			@Override
			public void run()
			{
				perocessEvent.start( process );
				InputStream is = process.getInputStream();
				InputStreamReader ir = null;
				BufferedReader br = null;
				try
				{
					ir = new InputStreamReader( is, charset == null ? DEFAULT_CONSOL_CHARSET : charset );
					br = new BufferedReader( ir );
					String line = null;
					while ( null != (line = br.readLine()) )
					{
						perocessEvent.readNewLine( line );
					}
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						if ( null != br ) br.close();
						if ( null != ir ) ir.close();
						if ( null != is ) is.close();
					}
					catch ( IOException e )
					{
						e.printStackTrace();
					}
					perocessEvent.stop();
				}
			}
		};
		stdoutThread.setDaemon( true );
		stdoutThread.setName( "Process-Command-Stdout-Thead" );
		stdoutThread.start();
	}

	/**
	 * 异步获取命令执行的结果, 主要是用于长时间的命令执行
	 * 	注意: 	
	 * 		1. 执行依然是同步方法, 只是获取命令执行结果是异步的;
	 * 		2. 方法不阻塞主线程
	 * 		3. 加载/etc/profile文件到当前命令行中
	 *<pre>
	 * 	事例1:
	 * 		//测试ping命令, ping永不结束,使用异步方式接收输出流,主线程不被阻塞. 
	 * 		execAsync("ping 127.0.0.1 ",new  DefaultProcessEvent());
	 * 
	 * 事例2:
	 * 		//多命令组合
	 * 		execAsync("java -version ; ping -c 5 127.0.0.1 ; pwd ",new  DefaultProcessEvent());
	 * 
	 * 事例3:
	 * 		//多命令组合+管道命令
	 * 		execAsync("java -version ; ping -c 5 127.0.0.1 ; ps -ef | grep java ",new  DefaultProcessEvent());
	 * </pre>
	 * @param command
	 * @param perocessEvent
	 */
	public void execAsync(String command, ProcessEvent perocessEvent)
	{
		execAsync( platformCommand( command ) ,null ,null ,null ,perocessEvent );
	}

	/**
	 * 针对异步执行命令行,异步事件
	 * @author huangchao
	 */
	public static interface ProcessEvent
	{

		/**
		 * 开始执行命令行
		 */
		public void start(Process process);

		/**
		 * 读取一行控制台结果
		 * @param line
		 */
		public void readNewLine(String line);

		/**
		 * 执行结束
		 */
		public void stop();
	}

	public static abstract class AbstractProcessEvent implements ProcessEvent
	{

		/**
		 * 进程
		 */
		protected Process	process	= null;

		/**
		 * 只有类Unix系统(Linux unix)才能获取pid, window系统暂时不处理
		 */
		private Integer		pid		= null;

		/**
		 * 开始执行命令行
		 */
		public void start(Process process)
		{
			this.process = process;
			try
			{
				String cName = process.getClass().getName();
				//java在类Unix系统(Linux unix)使用UNIXProcess实现Process对象
				if ( "java.lang.UNIXProcess".equals( cName ) )
				{
					Class<?> clazz = Class.forName( "java.lang.UNIXProcess" );
					Field field = clazz.getDeclaredField( "pid" );
					field.setAccessible( true );
					pid = (Integer) field.get( process );
				}
			}
			catch ( Throwable e )
			{
				e.printStackTrace();
			}
		}

		public void readNewLine(String line)
		{
			readLine( pid ,line );
		}

		/**
		 * 读取新一行数据
		 * @param pid	进程ID, 只有类Unix系统(Linux unix)才能获取pid, window系统暂时null
		 * @param line	读取一行控制台结果
		 */
		public abstract void readLine(Integer pid, String line);

		/**
		 * 执行结束
		 */
		public void stop()
		{
			try
			{
				process.waitFor();
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
			process.destroy();
			process = null;
		};
	}

	public static class DefaultProcessEvent extends AbstractProcessEvent
	{

		@Override
		public void readLine(Integer pid, String line)
		{
			System.out.println( "[pid=" + pid + "] " + line );
		}
	}
}
