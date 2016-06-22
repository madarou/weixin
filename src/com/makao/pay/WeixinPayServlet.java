package com.makao.pay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

@WebServlet(name = "WeixinPay", urlPatterns = { "/WeixinPay" }, loadOnStartup = 1)
public class WeixinPayServlet extends HttpServlet {
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxe41d04b7ffb9ea2a&redirect_uri=http%3a%2f%2fyuqq.cc%2fweixin%2fWeixinPay&response_type=code&scope=snsapi_userinfo&state=1#wechat_redirect
	 * 中的回调函数回调，完成网页授权过程，中间获取到要用于下单的用户在该公众号下面的openid, 然后返回用户下单页面,里面显示要下单的订单内容
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("content-type", "text/html;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String page = "";
		
		String openid = request.getParameter("openid");
		if(openid==null || "".equals(openid.trim())){
			page = "订单提交失败，没有openid";
			out.write(page);
			return;
		}
		// 生成微信订单
		Unifiedorder u = new Unifiedorder();
		u.setAppid(WeixinConstants.APPID);
		u.setMch_id(WeixinConstants.MCHID);
		u.setNonce_str(SignatureUtil.getNonceStr());
		u.setBody("测试支付(商品描述body)");
		u.setOut_trade_no(OrderNumberUtils.generateOrderNumber());
		u.setTotal_fee(1);
		u.setSpbill_create_ip("127.0.0.1");
		u.setNotify_url("http://yuqq.cc/weixin/WeixinPostPay");// 接收微信支付异步通知回调地址
		u.setTrade_type("JSAPI");
		u.setOpenid(openid);
		u.setDevice_info("WEB");
		u.setAttach("1");//测试带用户附加数据，因为在商城中需要cityId
		String detail = "{\"goods_detail\":[{\"goods_id\":\"iphone6s_16G\",\"goods_name\":\"iPhone6s 16G\",\"quantity\":1,\"price\":528800,\"goods_category\":\"数码\",\"body\":\"苹果手机\"}]}";
		u.setDetail(detail);
		// 还有签名没有，下面生成sign签名
		// 生成sign签名，这里必须用SortedMap，因为签名算法里key值是要排序的
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
		parameters.put("attach", u.getAttach());
		parameters.put("detail", u.getDetail());
		u.setSign(SignatureUtil.createSign(parameters, WeixinConstants.PAY_KEY));

		// 提交到微信统一订单接口，用xml格式提交和接收
		// 这里XStream是转化-，防止mch_id被转化为了mdc__id
		XStream xstream = new XStream(new DomDriver("UTF-8",
				new XmlFriendlyNameCoder("-_", "_")));
		xstream.alias("xml", Unifiedorder.class);
		String xml = xstream.toXML(u);
		Map<String, String> returnXml = HttpUtil.doPostXmlAndParse(
				WeixinConstants.UNIFIEDORDER_URL, xml);
		if (returnXml == null) {
			page = "下单失败";
			out.write(page);
			return;
		}
		String prepay_id = returnXml.get("prepay_id");
		if (prepay_id == null || "".equals(prepay_id)) {
			page = "参数错误，下单失败";
			out.write(page);
			return;
		}
		System.out.println("prepay_id: " + prepay_id);

		// 为前端页面能够使用JSSDK设置签名
		Map<String, String> wxConfig = JSSignatureUtil
				.getSignature("http://yuqq.cc/weixin/WeixinPay");

		// 生成支付订单需要的参数和签名
		String timeStamp = SignatureUtil.getTimeStamp();
		String nonceStr = SignatureUtil.getNonceStr();
		String packages = "prepay_id=" + prepay_id;
		String signType = "MD5";
		SortedMap<Object, Object> signMap = new TreeMap<Object, Object>();
		signMap.put("appId", WeixinConstants.APPID);
		signMap.put("timeStamp", timeStamp);
		signMap.put("nonceStr", nonceStr);
		signMap.put("package", packages);
		signMap.put("signType", signType);
		// 生成统一订单时的签名算法与支付时使用的签名算法一样，只是用到的key=value不一样
		String paySign = SignatureUtil.createSign(signMap,
				WeixinConstants.PAY_KEY);

		page = "<!DOCTYPE html>"
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
						+ "appId: '"+WeixinConstants.APPID+"',"
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
								+ "paySign:'"+paySign+"',"
								+ "success: function (res) {"
									+ "alert(JSON.stringify(res));"
								+ "}"
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse) 用户提交订单后，生成微信订单，并返回支付页面
	 */
//	@Override
//	protected void doPost(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException {
//		response.setHeader("content-type", "text/html;charset=UTF-8");
//		response.setCharacterEncoding("UTF-8");
//		PrintWriter out = response.getWriter();
//		String page = "";
//		
//		String openid = request.getParameter("openid");
//		if(openid==null || "".equals(openid.trim())){
//			page = "订单提交失败，没有openid";
//			out.write(page);
//			return;
//		}
//		// 生成微信订单
//		Unifiedorder u = new Unifiedorder();
//		u.setAppid(WeixinConstants.APPID);
//		u.setMch_id(WeixinConstants.MCHID);
//		u.setNonce_str(SignatureUtil.getNonceStr());
//		u.setBody("测试支付(商品描述body)");
//		u.setOut_trade_no(OrderNumberUtils.generateOrderNumber());
//		u.setTotal_fee(1);
//		u.setSpbill_create_ip("127.0.0.1");
//		u.setNotify_url("http://yuqq.cc/weixin/WeixinPay");// 接收微信支付异步通知回调地址
//		u.setTrade_type("JSAPI");
//		u.setOpenid(openid);
//		u.setDevice_info("WEB");
//		// 还有签名没有，下面生成sign签名
//		// 生成sign签名，这里必须用SortedMap，因为签名算法里key值是要排序的
//		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
//		parameters.put("appid", u.getAppid());
//		parameters.put("body", u.getBody());
//		parameters.put("mch_id", u.getMch_id());
//		parameters.put("nonce_str", u.getNonce_str());
//		parameters.put("notify_url", u.getNotify_url());
//		parameters.put("out_trade_no", u.getOut_trade_no());
//		parameters.put("total_fee", u.getTotal_fee());
//		parameters.put("trade_type", u.getTrade_type());
//		parameters.put("spbill_create_ip", u.getSpbill_create_ip());
//		parameters.put("openid", u.getOpenid());
//		parameters.put("device_info", u.getDevice_info());
//		u.setSign(SignatureUtil.createSign(parameters, WeixinConstants.PAY_KEY));
//
//		// 提交到微信统一订单接口，用xml格式提交和接收
//		// 这里XStream是转化-，防止mch_id被转化为了mdc__id
//		XStream xstream = new XStream(new DomDriver("UTF-8",
//				new XmlFriendlyNameCoder("-_", "_")));
//		xstream.alias("xml", Unifiedorder.class);
//		String xml = xstream.toXML(u);
//		Map<String, String> returnXml = HttpUtil.doPostXmlAndParse(
//				WeixinConstants.UNIFIEDORDER_URL, xml);
//		if (returnXml == null) {
//			page = "下单失败";
//			out.write(page);
//			return;
//		}
//		String prepay_id = returnXml.get("prepay_id");
//		if (prepay_id == null || "".equals(prepay_id)) {
//			page = "参数错误，下单失败";
//			out.write(page);
//			return;
//		}
//		System.out.println("prepay_id: " + prepay_id);
//
//		// 为前端页面能够使用JSSDK设置签名
//		Map<String, String> wxConfig = JSSignatureUtil
//				.getSignature("http://yuqq.cc/weixin/WeixinPay");
//
//		// 生成支付订单需要的参数和签名
//		String timeStamp = SignatureUtil.getTimeStamp();
//		String nonceStr = SignatureUtil.getNonceStr();
//		String packages = "prepay_id=" + prepay_id;
//		String signType = "MD5";
//		SortedMap<Object, Object> signMap = new TreeMap<Object, Object>();
//		signMap.put("appId", WeixinConstants.APPID);
//		signMap.put("timeStamp", timeStamp);
//		signMap.put("nonceStr", nonceStr);
//		signMap.put("package", packages);
//		signMap.put("signType", signType);
//		// 生成统一订单时的签名算法与支付时使用的签名算法一样，只是用到的key=value不一样
//		String paySign = SignatureUtil.createSign(signMap,
//				WeixinConstants.PAY_KEY);
//
//		page = "<!DOCTYPE html>"
//				+ "<html>"
//				+ "<head>"
//					+ "<meta charset=\"utf-8\">"
//					+ "<title>订单支付</title>"
//					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=0\">"
//					+ "<link rel=\"stylesheet\" href=\"static/css/weixin.css\">"
//				+ "</head>"
//				+"<body>"
//					+ "<div class=\"wxapi_container\">"
//						+"<div class=\"lbox_close wxapi_form\">"
//							+ "<h3 id=\"menu-pay\">微信支付接口</h3>"
//							+ "<span class=\"desc\">发起一个微信支付请求</span>"
//							+ "<button class=\"btn btn_primary\" id=\"chooseWXPay\">支付订单</button>"
//						+"</div>"
//					+ "</div>"
//				+ "</body>"
//				+"<script src=\"http://res.wx.qq.com/open/js/jweixin-1.0.0.js\"></script>"
//				+"<script>"
//					+ "wx.config({"
//						+ "debug: false,"
//						+ "appId: '"+WeixinConstants.APPID+"',"
//						+ "timestamp: "+wxConfig.get("timestamp")+","
//						+ "nonceStr: '"+wxConfig.get("nonceStr")+"',"
//						+ "signature: '"+wxConfig.get("signature")+"',"
//						+ "jsApiList: ["
//							+ "'chooseWXPay'"
//						+ "]"
//					+ "});"
//					+ "wx.ready(function () {"
//						+ "var btn = document.getElementById(\"chooseWXPay\");"
//						+ "btn.onclick=function(){"
//							+ "alert('click success');"
//							+ "wx.chooseWXPay({"
//								+ "timestamp:"+timeStamp+","
//								+ "nonceStr:'"+nonceStr+"',"
//								+ "package:'"+packages+"',"
//								+ "signType:'MD5',"
//								+ "paySign:'"+paySign+"'"
//							+ "});"
//						+ "}"
//					+ "});"
//					+ "wx.error(function (res) {"
//						+ "alert(res.errMsg);"
//					+ "});"
//				+ "</script>"
//			+ "</html>";
//		out.write(page);
//	}

}
