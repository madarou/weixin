package com.makao.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.makao.menu.Button;
import com.makao.menu.ClickButton;
import com.makao.menu.Menu;
import com.makao.menu.ViewButton;
import com.makao.po.AccessToken;

import net.sf.json.JSONObject;

public class WeixinUtil {
	private static final String APPID = "to_be_filled";
	private static final String APPSECRET = "to_be_filled";
	
	//测试用的订阅号，因为个人订阅号有权限限制，如不能请求图片上传的URL等，只能用测试号代替
	private static final String APPID_TEST = "wx454e62d4ba842c70";
	private static final String APPSECRET_TEST = "ea2fa885539a667e302a92bb86b54e55";
	
	private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	private static final String UPLOAD_URL = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
	//post，自定义菜单创建
	private static final String CREATEMENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
	//get，自定义菜单查询
	private static final String QUERYMENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=ACCESS_TOKEN";
	//get，自定义菜单删除
	private static final String DELETEMENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=ACCESS_TOKEN";
	/**
	 * @param url
	 * @return
	 * 通过http get请求，去url为
	 * "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET"
	 * 获取access_token
	 */
	public static JSONObject doGetStr(String url){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		JSONObject jsonObject = null;
		try {
			//执行http get请求，并获得response
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if(entity != null){
				String result = EntityUtils.toString(entity,"UTF-8");
				jsonObject = JSONObject.fromObject(result);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * @param url
	 * @param outStr
	 * @return
	 * 通过http post请求获取access_token
	 */
	public static JSONObject doPostStr(String url, String outStr){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		JSONObject jsonObject = null;
		//设置post参数
		httpPost.setEntity(new StringEntity(outStr,"UTF-8"));
		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if(entity != null){
				String result = EntityUtils.toString(entity,"UTF-8");
				jsonObject = JSONObject.fromObject(result); 
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * @return
	 * 获取access_token
	 */
	public static AccessToken getAccessToken(){
		AccessToken token = new AccessToken();
		//修改成实际的URL
		String url = ACCESS_TOKEN_URL.replace("APPID", APPID_TEST).replace("APPSECRET", APPSECRET_TEST);
		JSONObject jsonObject = doGetStr(url);
		if(jsonObject != null){
			token.setToken(jsonObject.getString("access_token"));
			token.setExpiresIn(jsonObject.getInt("expires_in"));
		}
		return token;
	}
	
	/**
	 * @param filePath
	 * @param accessToken
	 * @param type
	 * @return
	 * @throws IOException
	 * 上传media，返回media_id，音乐文件中的缩略图ThumbMediaId也是同样的方式，只是返回结果中json字段名
	 * 为thumb_media_id
	 */
	public static String upload(String filePath, String accessToken, String type) throws IOException{
		File file = new File(filePath);
		if(!file.exists() || !file.isFile()){
			throw new IOException("文件不存在");
		}
		String url = UPLOAD_URL.replace("ACCESS_TOKEN", accessToken).replace("TYPE", type);
		//开始请求url
		URL urlObj = new URL(url);
		HttpURLConnection con = (HttpURLConnection)urlObj.openConnection();
		
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		
		//************读取本地图片到url connection***************
		//设置请求头
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Charset", "UTF-8");
		//设置边界
		String BOUNDARY = "------------" + System.currentTimeMillis();
		con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
		StringBuilder sb = new StringBuilder();
		sb.append("--");
		sb.append(BOUNDARY);
		sb.append("\r\n");
		sb.append("Content-Disposition:form-data=\"file\";filename=\"" + file.getName() + "\"\r\n");
		sb.append("Content-Type:application/octet-stream\r\n\r\n");
		
		byte[] head = sb.toString().getBytes("utf-8");
		
		//获得输出流
		OutputStream out = new DataOutputStream(con.getOutputStream());
		//输出头信息
		out.write(head);
		
		//正文部分
		//读入文件流
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while((bytes = in.read(bufferOut)) != -1){
			//输出正文
			out.write(bufferOut);
		}
		in.close();
		
		//组装消息尾部
		byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");
		
		out.write(foot);
		out.flush();
		out.close();
		
		//************从前面的url connection中读上传的文件之后的响应，从而取得media_id***************
		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		String result = null;
		try{
			//定义BufferedReader输入流来读取URL的响应
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = null;
			while((line = reader.readLine()) != null){
				buffer.append(line);
			}
			if(result == null){
				result = buffer.toString();
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(reader != null){
				reader.close();
			}
		}
		
		JSONObject jsonObj = JSONObject.fromObject(result);
		System.out.println(jsonObj);
		//针对音乐的thumb_media_id
		String typeName = "media_id";
		if(!"image".equals(type)){
			typeName = type + "_" +typeName;
		}
		String mediaId = jsonObj.getString(typeName);
		return mediaId;
	}
	
	/**
	 * @return
	 * 创建一个菜单，
	 * 里面包含三个一级菜单clickButton, viewButton和compoundButton
	 * 其中compoundButton还是包含两个子菜单
	 */
	public static Menu initMenu(){
		Menu menu = new Menu();
		ClickButton clickButton = new ClickButton();
		clickButton.setName("click菜单");
		clickButton.setType("click");
		clickButton.setKey("1");//用来区分多个click类型的button，如果有的话
		
		ViewButton viewButton = new ViewButton();
		viewButton.setName("view菜单");
		viewButton.setType("view");
		viewButton.setUrl("http://www.yuqq.cc");//注意必须加http，否则会报40055错误
		
		//创建一个扫码的菜单，注意扫码的也要key的，所以也是ClickButton类型
		ClickButton scancodePushButton = new ClickButton();
		scancodePushButton.setName("扫码菜单");
		scancodePushButton.setType("scancode_push");
		scancodePushButton.setKey("2");
		
		//创建一个地理位置选择器的菜单，注意地理位置选择器的也要key的，所以也是ClickButton类型
		ClickButton locationSelectButton = new ClickButton();
		locationSelectButton.setName("地理位置选择菜单");
		locationSelectButton.setType("location_select");
		locationSelectButton.setKey("3");
		
		//创建一个一级菜单，来包含scancodePushButton和locationSelectButton
		//注意，如果是包含子菜单的一级菜单，就不用设置Type
		Button compoundButton = new Button();
		compoundButton.setName("其他菜单");
		compoundButton.setSub_button(new Button[]{scancodePushButton,locationSelectButton});
		
		menu.setButton(new Button[]{clickButton,viewButton,compoundButton});
		return menu;
	}
	
	/**
	 * @param token
	 * @param menu
	 * @return
	 * 向微信服务器发送POST请求，创建菜单，返回创建后的错误码，
	 * 为0表示创建成功
	 */
	public static int createMenu(String token, String menu){
		int result = 0;
		String url = CREATEMENU_URL.replace("ACCESS_TOKEN", token);
		//根据文档，创建菜单以POST方式提交请求，参数为json格式的菜单对象,string型
		JSONObject jsonObject = doPostStr(url, menu);
		if(jsonObject != null){
			result = jsonObject.getInt("errcode");
		}
		return result;
	}
	
	/**
	 * @param token
	 * @return
	 * 自定义菜单的查询
	 */
	public static JSONObject queryMenu(String token){
		String url = QUERYMENU_URL.replace("ACCESS_TOKEN", token);
		JSONObject jsonObject = doGetStr(url);
		return jsonObject;
	}
	
	/**
	 * @param token
	 * @param menu
	 * @return
	 * 自定义菜单删除
	 */
	public static int deleteMenu(String token){
		int result = 0;
		String url = DELETEMENU_URL.replace("ACCESS_TOKEN", token);
		//根据文档，创建菜单以POST方式提交请求，参数为json格式的菜单对象,string型
		JSONObject jsonObject = doGetStr(url);
		if(jsonObject != null){
			result = jsonObject.getInt("errcode");
		}
		return result;
	}
}
