package cn.com.zach.tools.http;

import java.io.File;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.alibaba.fastjson.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * 发送http/https请求
 */
public class HttpClient {
	
	private String method = "POST";
	
	/**
	 * 单例模式
	 */
	private static final HttpClient client = new HttpClient();

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	public static HttpClient getInstance(){
		return client;
	}

	public OkHttpClient getHttpConnection() {
		OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
		clientBuilder.connectionPool(new ConnectionPool(10, 10, TimeUnit.MINUTES));
		clientBuilder.connectTimeout(15, TimeUnit.MINUTES); // 连接超时
		clientBuilder.readTimeout(15, TimeUnit.MINUTES); // 读取超时
		clientBuilder.writeTimeout(20, TimeUnit.MINUTES); // 写入超时
		clientBuilder.sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts());
		clientBuilder.hostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				Certificate[] localCertificates = new Certificate[0];
                try {
                    //获取证书链中的所有证书
                    localCertificates = session.getPeerCertificates();
                } catch (SSLPeerUnverifiedException e) {
                    e.printStackTrace();
                }
                //打印所有证书内容
                for (Certificate c : localCertificates) {
                		System.out.println("verify: "+c.toString());
                }
                return true;
			}
		});
		return clientBuilder.build();
	}

	public String doGet(String path) {
		this.method = "GET";
		return send(path, null, null);
	}

	public String doPost(String path) {
		this.method = "POST";
		return send(path, null, null, false);
	}

	public String doPost(String path, Map<String, Object> params) {
		this.method = "POST";
		return send(path, params, null, false);
	}
	
	public String doPost(String path, Map<String, Object> params, Map<String, Object> headers) {
		this.method = "POST";
		return send(path, params, headers, false);
	}
	
	public String doPostByJson(String path, Map<String, Object> params) {
		this.method = "POST";
		return send(path, params, null, true);
	}

	public String doPostByJson(String path, Map<String, Object> params, Map<String, Object> headers) {
		this.method = "POST";
		return send(path, params, headers, true);
	}
	
	public void doPostByJsonAsyn(String path, Map<String, Object> params, Callback callback) {
		this.method = "POST";
		sendAsyn(path, params, null, true, callback);
	}
	
	public void doPostByJsonAsyn(String path, Map<String, Object> params, Map<String, Object> headers, Callback callback) {
		this.method = "POST";
		sendAsyn(path, params, headers, true, callback);
	}
	
	/**
	 * 上传文件，带参数
	 * @param url
	 * @param params
	 * @param file
	 */
	public String doPost(String path, File file, Map<String, String> params, Map<String, Object> headers) {
		this.method = "POST";
		return sendFile(path, file, params, headers);
	}
	
	public String send(String path, Object object, Map<String, Object> headers) {
		try {
			Request.Builder builder = new Request.Builder();
			//POST请求
			if (RequestMethod.POST.equals(method.toUpperCase())) {
				RequestBody body = null;
				if(object instanceof Map) {
					String postBodyJsonStr = JSONObject.toJSONString(object);
					body =  RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postBodyJsonStr);
				}else {
					//如果参数为对象
					MediaType mediaType = MediaType.parse("application/json;charset=UTF-8".toString());
					if (object != null) {
						java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
						out(os, object);
						body = RequestBody.create(mediaType, os.toByteArray());
					} else {
						body = Util.EMPTY_REQUEST;
					}
				}
				builder.method(method, body);
			}
			// 设置请求方法及路径
			builder.url(path);
			// 设置请求头
			if (headers != null && headers.size() > 0) {
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					builder.addHeader(entry.getKey(), entry.getValue().toString());
				}
			}

			Request request = builder.build();
			OkHttpClient client = getHttpConnection();
			Response response = client.newCall(request).execute();
			
			return response.body().string();
//			if (response.code() < 400) {
//				return response.body().string();
//			} else {
//				throw new RuntimeException("http code : " + response.code());
//			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}finally {
			if(headers != null) {
				headers.clear();
			}
		}
	}
	
	public String send(String path, Object object, Map<String, Object> headers, boolean isJson) {
		try {
			Request.Builder builder = new Request.Builder();
			if ("POST".equals(method.toUpperCase())) {
				RequestBody body = null;
				
				if (isJson) {
					MediaType mediaType = MediaType.parse("application/json;charset=UTF-8".toString());
					if (object != null) {
						 body = FormBody.create(mediaType, JSONObject.toJSONString(object));
					} else {
						body = Util.EMPTY_REQUEST;
					}
				}else{
					if (object instanceof Map){
						String postBodyJsonStr = JSONObject.toJSONString(object);
						body =  RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postBodyJsonStr);
					}else{
						MediaType mediaType = MediaType.parse("application/json;charset=UTF-8".toString());
						if (object != null) {
							java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
							out(os, object);
							body = RequestBody.create(mediaType, os.toByteArray());
						} else {
							body = Util.EMPTY_REQUEST;
						}
					}
				}
				builder.method(method, body);
			}
			// 设置请求方法及路径
			builder.url(path);

			// 设置请求头
			if (headers != null && headers.size() > 0) {
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					builder.addHeader(entry.getKey(), entry.getValue().toString());
				}
			}
			Request request = builder.build();
			OkHttpClient client = getHttpConnection();
			Response response = client.newCall(request).execute();
			
			return response.body().string();
//			if (response.code() < 400) {
//				return response.body().string();
//			} else {
//				throw new RuntimeException("http code : " + response.code());
//			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}finally {
			if(headers != null) {
				headers.clear();
			}
		}
	}
	
	public void sendAsyn(String path, Object object, Map<String, Object> headers, boolean isJson, Callback callback) {
		try {
			Request.Builder builder = new Request.Builder();
			if ("POST".equals(method.toUpperCase())) {
				RequestBody body = null;
				
				if (isJson) {
					MediaType mediaType = MediaType.parse("application/json;charset=UTF-8".toString());
					if (object != null) {
						 body = FormBody.create(mediaType, JSONObject.toJSONString(object));
					} else {
						body = Util.EMPTY_REQUEST;
					}
				}else{
					if (object instanceof Map){
						String postBodyJsonStr = JSONObject.toJSONString(object);
						body =  RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postBodyJsonStr);
					}else{
						MediaType mediaType = MediaType.parse("application/json;charset=UTF-8".toString());
						if (object != null) {
							java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
							out(os, object);
							body = RequestBody.create(mediaType, os.toByteArray());
						} else {
							body = Util.EMPTY_REQUEST;
						}
					}
				}
				builder.method(method, body);
			}
			// 设置请求方法及路径
			builder.url(path);

			// 设置请求头
			if(headers != null && headers.size() > 0) {
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					builder.addHeader(entry.getKey(), entry.getValue().toString());
				}
			}

			Request request = builder.build();
			OkHttpClient client = getHttpConnection();
			Call newCall = client.newCall(request);
			
			// 异步调用
			newCall.enqueue(callback);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}finally {
			if(headers != null) {
				headers.clear();
			}
		}
	}

	private void out(OutputStream out, Object obj) throws Exception {
		ObjectOutputStream ser = new ObjectOutputStream(out);
		ser.writeObject(obj);
	}

	public String sendFile(String path, File file, Map<String, String> params, Map<String, Object> headers) {
		try {
			Request.Builder builder = new Request.Builder();
			//POST请求
			if (RequestMethod.POST.equals(method.toUpperCase())) {
				MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
				if(file != null) {
					//MediaType.parse()里面是上传的类型
					RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
					String filename = file.getName();
					//设置参数分别为  key, 文件名称， RequestBody
					requestBody.addFormDataPart(filename, filename, body);
				}
				if(params != null) {
					Set<Map.Entry<String, String>> entries = params.entrySet();
					for(Map.Entry<String, String> entry: entries) {
						String key = entry.getKey();
						String value = entry.getValue();
						requestBody.addFormDataPart(key, value);
					}
				}
				builder.method(method, requestBody.build());
			}
			// 设置请求方法及路径
			builder.url(path);
			// 设置请求头
			if (headers != null && headers.size() > 0) {
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					builder.addHeader(entry.getKey(), entry.getValue().toString());
				}
			}

			Request request = builder.build();
			OkHttpClient client = getHttpConnection();
			Response response = client.newCall(request).execute();
			return response.body().string();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}finally {
			if(headers != null) {
				headers.clear();
			}
		}
	}
	
	/**
	 * 获取图片字节
	 * @param path
	 * @return
	 */
	public byte[] getImage(String path) {
		try {
			Request.Builder builder = new Request.Builder();
			// 设置请求方法及路径
			builder.url(path);
			Request request = builder.build();
			OkHttpClient client = getHttpConnection();
			Response response = client.newCall(request).execute();
			
			return response.body().bytes();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 生成安全套接字工厂，用于https请求的证书跳过
	 * @return
	 */
	private SSLSocketFactory createSSLSocketFactory() {
		SSLSocketFactory ssfFactory = null;
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			ssfFactory = sc.getSocketFactory();
		} catch (Exception e) {
		}
		return ssfFactory;
	}

	/**
	 * 用于信任所有证书
	 */
	final TrustManager[] trustAllCerts = new TrustManager[]{
			new TrustAllCerts()
	};
	
	class TrustAllCerts implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
		}
		@Override
		public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

		}
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}
	
	public static class RequestMethod {
        public static String POST = "POST";
        public static String GET = "GET";
    }
	
	public static void main(String[] args) {
		HttpClient client = HttpClient.getInstance();
		String res = client.doPost("https://bizapi-t1.umss.cn/oppty/home");
		System.out.println("****************************");
		System.out.println("****************************");
		System.out.println(res);
	}
}
