package cn.com.zach.tools.http;

import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

public class CouchDB {
	
	private String server;
	
	private String auth ;
	
	public CouchDB() {

		this.server="http://139.224.19.74:5984/";
		String user="admin";
		String pwd="admin";
		StringBuilder sb = new StringBuilder();
		sb.append(user).append(":").append(pwd);
		String b64 = Base64.encodeBase64String(sb.toString().getBytes()); 
		sb.setLength(0);
		sb.append("Basic ").append(b64);
		this.auth = sb.toString();
	}
	
	private OkHttpClient connect() {
		return  new OkHttpClient.Builder()
				.connectionPool(new ConnectionPool(1, 1, TimeUnit.MINUTES))
		        .connectTimeout(30, TimeUnit.SECONDS)//链接超时  
		        .readTimeout(30, TimeUnit.SECONDS)//读取超时  
		        .writeTimeout(30, TimeUnit.SECONDS)//写入超时  
				.build();
	}
	
	private JSONObject send (String method, String path, String stringBody) {
		JSONObject answer = null;
		try {
			Request.Builder  builder =  new Request.Builder();
			RequestBody body = null;
			MediaType mediaType = MediaType.parse("application/json".toString());
			if(stringBody!= null ) {
				 body = RequestBody.create(mediaType, stringBody.getBytes("utf-8"));
			}else {
				body= Util.EMPTY_REQUEST;
			}
			builder.header("Authorization", this.auth);
			
			builder.method(method, body);
			builder.url(this.server+path);
			Request request =  builder.build();
			
			OkHttpClient client = connect();
			Response response = client.newCall(request).execute();
			answer = JSON.parseObject(new String(response.body().bytes()));
		} catch (Exception e) {
			e.printStackTrace();
			new RuntimeException(e);
		}
		return answer;
	}
	
	private JSONObject put( String path,  String body ) {
		return send("PUT", path, body);
	}
	
	private JSONObject post( String path,  String body ) {
		return send("POST", path, body);
	}
	/**
	 * 创建一个couch数据库
	 * @param name
	 * @return
	 */
	public JSONObject createDatabase(String name) {
		return put(name, null);
	}
	
	/**
	 * 创建couch用户
	 * @param user
	 * @param pwd
	 * @return
	 */
	public JSONObject createUser(String user, String pwd) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"name\":\"").append(user).append("\", \"password\":\"").append(pwd).append("\", \"roles\":[], \"type\":\"user\"}");
		return put("_users/org.couchdb.user:"+user, sb.toString());
	}
	
	public JSONObject createIndex(String db,String indexName, String fieldArray) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"index\":{\"fields\":[\"")
		.append(fieldArray)
		.append("\"]},\"name\":\"")
		.append(indexName)
		.append("\"")
		.append("}");
		return post(db+"/_index", sb.toString());
	}
	
	
	
	
	public static void main(String []args) {
//		JSONObject res = null;
//		CouchDB couch = new CouchDB();
	}
}

