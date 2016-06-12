package com.makao.pay;
/**
 * @description: TODO
 * @author makao
 * @date 2016年6月6日
 */
public class WeixinConstants {
	//诸葛王朗订阅号
	//public static final String APPID = "wx454e62d4ba842c70";
	//public static final String APPSECRET = "ea2fa885539a667e302a92bb86b54e55";
	//优格服务号
	public static final String APPID = "wxe41d04b7ffb9ea2a";
	public static final String APPSECRET = "cc515bcc081a76e3b85bbb370c6c7b3c";
	
	public static final String MCHID = "1239292402";//商户号，支付用
	public static final String PAY_KEY = "shygxx1234567890shygxx1234567890";//生成支付签名时所用的商户key
	//统一下单地址，POST
	public static final String UNIFIEDORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	
	//测试用的订阅号，因为个人订阅号有权限限制，如不能请求图片上传的URL等，只能用测试号代替
	//public static final String APPID_TEST = "wx454e62d4ba842c70";
	//public static final String APPSECRET_TEST = "ea2fa885539a667e302a92bb86b54e55";
	//获取access_token，get
	public static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	//JSSDK获取jsapi_ticket的url，get
	public static final String JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";
	//自定义菜单创建，post
	public static final String CREATEMENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
	
	//网页授权登录第一步获取code的url
	public static final String AUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=1#wechat_redirect";
	//网页授权登录第二步获取access_token和openid的url
	public static final String AUTH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
	//网页授权登录第三步获取用户基本信息
	public static final String AUTH_USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
}
