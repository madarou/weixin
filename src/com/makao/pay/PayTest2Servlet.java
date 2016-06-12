package com.makao.pay;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;

import com.makao.po.Image;
import com.makao.po.Music;
import com.makao.po.News;
import com.makao.po.TextMessage;
import com.makao.util.CheckUtil;
import com.makao.util.MessageUtil;

@WebServlet(name = "Pay2Servlet", urlPatterns = { "/Pay2Servlet" }, loadOnStartup = 1)
public class PayTest2Servlet extends HttpServlet {
	//网页授权登录第一步获取code的url
	public static final String AUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=1#wechat_redirect";
	//网页授权登录第二步获取access_token和openid的url
	public static final String AUTH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
	//优格服务号
	public static final String APPID = "wxe41d04b7ffb9ea2a";
	public static final String APPSECRET = "cc515bcc081a76e3b85bbb370c6c7b3c";
	
	private Properties prop = new Properties(); 
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}
//https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxe41d04b7ffb9ea2a&redirect_uri=http%3a%2f%2fyuqq.cc%3a8080%2fweixin%2fPayServlet&response_type=code&scope=snsapi_userinfo&state=1#wechat_redirect
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("content-type", "text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		//为前端页面能够使用JSSDK设置签名
	    String appId = WeixinConstants.APPID;
	    Map<String, String> wxConfig = JSSignatureUtil.getSignature("http://yuqq.cc/weixin/Pay2Servlet");
	    //将生成的订单需要在提交时使用的信息返回到前端页面
	    
	    String timeStamp = SignatureUtil.getTimeStamp();
	    String nonceStr = SignatureUtil.getNonceStr();
	    String packages = "prepay_id=wx20160610200351b29b11db200206534278";
	    String signType = "MD5";
	    
	    SortedMap<Object,Object> signMap = new TreeMap<Object,Object>();
        signMap.put("appId", appId); 
        signMap.put("timeStamp", timeStamp);
        signMap.put("nonceStr", nonceStr);
        signMap.put("package", packages);
        signMap.put("signType", signType);
        String paySign = SignatureUtil.createSign(signMap, WeixinConstants.PAY_KEY);
        
		String page = "<!DOCTYPE html>"
						+ "<html>"
						+ "<head>"
							+ "<meta charset=\"utf-8\">"
							+ "<title>订单支付</title>"
							+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=0\">"
							+ "<link rel=\"stylesheet\" href=\"static/css/weixin.css\">"
						+ "</head>"
						+"<body>"
							+ "<div class=\"wxapi_container\">"
								+"<div class=\"lbox_close wxapi_form\">"
									+ "<h3 id=\"menu-pay\">微信支付接口</h3>"
									+ "<span class=\"desc\">发起一个微信支付请求</span>"
									+ "<button class=\"btn btn_primary\" id=\"chooseWXPay\">支付订单</button>"
								+"</div>"
							+ "</div>"
						+ "</body>"
						+"<script src=\"http://res.wx.qq.com/open/js/jweixin-1.0.0.js\"></script>"
						+"<script>"
							+ "wx.config({"
								+ "debug: false,"
								+ "appId: '"+appId+"',"
								+ "timestamp: "+wxConfig.get("timestamp")+","
								+ "nonceStr: '"+wxConfig.get("nonceStr")+"',"
								+ "signature: '"+wxConfig.get("signature")+"',"
								+ "jsApiList: ["
									+ "'chooseWXPay'"
								+ "]"
							+ "});"
							+ "wx.ready(function () {"
								+ "var btn = document.getElementById(\"chooseWXPay\");"
								+ "btn.onclick=function(){"
									+ "alert('click success');"
									+ "wx.chooseWXPay({"
										+ "timestamp:"+timeStamp+","
										+ "nonceStr:'"+nonceStr+"',"
										+ "package:'"+packages+"',"
										+ "signType:'MD5',"
										+ "paySign:'"+paySign+"'"
									+ "});"
								+ "}"
							+ "});"
							+ "wx.error(function (res) {"
								+ "alert(res.errMsg);"
							+ "});"
						+ "</script>"
					+ "</html>";
		out.write(page);
	}

	public static JSONObject doGetObject(String url){
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
	
	private void write(String path)
	{
		OutputStream os;
		try {
			os = new FileOutputStream(path);//再通过文件写方式打开properties文件
			prop.store(os, "comment");//store方法将key=value对重新写到properties文件,comment是在文档首部添加的注释,可设为空串""
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
