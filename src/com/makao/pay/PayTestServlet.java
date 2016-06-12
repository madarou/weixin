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
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

@WebServlet(name = "PayServlet", urlPatterns = { "/PayServlet" }, loadOnStartup = 1)
public class PayTestServlet extends HttpServlet {
	//网页授权登录第一步获取code的url
	public static final String AUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=1#wechat_redirect";
	//网页授权登录第二步获取access_token和openid的url
	public static final String AUTH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
	//优格服务号
	public static final String APPID = "wxe41d04b7ffb9ea2a";
	public static final String APPSECRET = "cc515bcc081a76e3b85bbb370c6c7b3c";
	private String openid="oymaht6VhsEzalMfzBaJPGp6JSbA";
	
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
		String page = "";
		
		String code = request.getParameter("code");
		//如果用户不同意授权
		if(code==null || "".equals(code)){
			page = "没有获得您的授权，无法浏览商城";
			out.write(page);
			return;
		}
		System.out.println("code:"+code);
		String get_token_url = AUTH_TOKEN_URL.replace("APPID", APPID)
				.replace("SECRET", APPSECRET).replace("CODE", code);
		net.sf.json.JSONObject jsonObject = doGetObject(get_token_url);
		String access_token = jsonObject.getString("access_token");
		System.out.println("auth access_token: "+access_token);
		String openid = jsonObject.getString("openid");
		this.openid = openid;
		System.out.println("user openid: "+openid);
		out.write("access_token: "+access_token+"   "+"openid: " + openid);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("content-type", "text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		Unifiedorder u = new Unifiedorder();
		u.setAppid(WeixinConstants.APPID);
		u.setMch_id(WeixinConstants.MCHID);
		//u.setSub_mch_id(WeixinConstants.MCHID);
		u.setNonce_str(SignatureUtil.getNonceStr());
		u.setBody("测试支付(商品描述body)");
		u.setOut_trade_no(OrderNumberUtils.generateOrderNumber());
		u.setTotal_fee(1);
		u.setSpbill_create_ip("127.0.0.1");
		u.setNotify_url("http://www.baidu.com/");//接收微信支付异步通知回调地址
		u.setTrade_type("JSAPI");
		u.setOpenid(this.openid);
		u.setDevice_info("WEB");
		
		//生成sign签名
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		parameters.put("appid", u.getAppid());
		parameters.put("body", u.getBody());
		parameters.put("mch_id", u.getMch_id());
		parameters.put("nonce_str", u.getNonce_str());
		parameters.put("notify_url", u.getNotify_url());
		parameters.put("out_trade_no", u.getOut_trade_no());
		parameters.put("total_fee", u.getTotal_fee());
		parameters.put("trade_type", u.getTrade_type());
		parameters.put("spbill_create_ip", u.getSpbill_create_ip());
		parameters.put("openid", u.getOpenid());
		parameters.put("device_info", u.getDevice_info());
		u.setSign(SignatureUtil.createSign(parameters, WeixinConstants.PAY_KEY));
		
		XStream xstream = new XStream(new DomDriver("UTF-8",new XmlFriendlyNameCoder("-_", "_")));
		xstream.alias("xml", Unifiedorder.class);
		String xml = xstream.toXML(u);
		prop.setProperty("发送到unified order的xml", xml);
	    //logger.info("统一下单xml为:\n" + xml);
	    String returnXml = HttpUtil.doPostXml(WeixinConstants.UNIFIEDORDER_URL, xml);
	    prop.setProperty("unified order返回的xml", xml);
	    write("/wxpay.txt");
	    //logger.info("返回结果:" + returnXml);
	    out.write(xml+"<br/>"+returnXml);
	    
	    //为前端页面能够使用JSSDK设置签名
//	    String appId = WeixinConstants.APPID;
//	    Map<String, String> wxConfig = JSSignatureUtil.getSignature("http://madarou1.ngrok.cc/orderOn/unifiedorder");
//	    //将生成的订单需要在提交时使用的信息返回到前端页面
//	    
//		String page = "<!DOCTYPE html>"
//						+ "<html>"
//						+ "<head>"
//							+ "<meta charset=\"utf-8\">"
//							+ "<title>订单支付</title>"
//							+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=0\">"
//							+ "<link rel=\"stylesheet\" href=\"/css/weixin.css\">"
//						+ "</head>"
//						+"<body>"
//							+ "<div class=\"wxapi_container\">"
//								+"<div class=\"lbox_close wxapi_form\">"
//									+ "<h3 id=\"menu-pay\">微信支付接口</h3>"
//									+ "<span class=\"desc\">发起一个微信支付请求</span>"
//									+ "<button class=\"btn btn_primary\" id=\"chooseWXPay\">支付订单</button>"
//								+"</div>"
//							+ "</div>"
//						+ "</body>"
//						+"<script src=\"http://res.wx.qq.com/open/js/jweixin-1.0.0.js\"></script>"
//						+"<script>"
//							+ "wx.config({"
//								+ "debug: true,"
//								+ "appId: '"+appId+"',"
//								+ "timestamp: "+wxConfig.get("timestamp")+","
//								+ "nonceStr: '"+wxConfig.get("nonceStr")+"',"
//								+ "signature: '"+wxConfig.get("signature")+"',"
//								+ "jsApiList: ["
//									+ "'chooseWXPay'"
//								+ "]"
//							+ "});"
//							+ "wx.ready(function () {"
//								+ "var btn = document.getElementById(\"chooseWXPay\");"
//								+ "btn.onclick=function(){"
//									+ "alert('click success');"
//									+ "wx.chooseWXPay({"
//										+ "timestamp: 1414723227,"
//										+ "nonceStr: 'noncestr',"
//										+ "package: 'addition=action_id%3dgaby1234%26limit_pay%3d&bank_type=WX&body=innertest&fee_type=1&input_charset=GBK&notify_url=http%3A%2F%2F120.204.206.246%2Fcgi-bin%2Fmmsupport-bin%2Fnotifypay&out_trade_no=1414723227818375338&partner=1900000109&spbill_create_ip=127.0.0.1&total_fee=1&sign=432B647FE95C7BF73BCD177CEECBEF8D',"
//										+ "signType: 'MD5',"
//										+ "paySign: 'bd5b1933cda6e9548862944836a9b52e8c9a2b69'"
//									+ "});"
//								+ "}"
//							+ "});"
//							+ "wx.error(function (res) {"
//								+ "alert(res.errMsg);"
//							+ "});"
//						+ "</script>"
//					+ "</html>";
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
