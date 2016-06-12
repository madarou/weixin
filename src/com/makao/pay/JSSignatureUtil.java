package com.makao.pay;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;


import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * @description: TODO
 * @author makao
 * @date 2016年6月9日
 * JSSDK的签名生成
 */
public class JSSignatureUtil {
	private static final Logger logger = Logger.getLogger(JSSignatureUtil.class);
	private static String jsapi_ticket = getJsApiTicket(AccessTokenUtil.getToken().getToken());
	//private static String jsapi_ticket = "kgt8ON7yVITDhtdwci0qeX6wTpn15xQ_HJUqdWfRW-9-EjYL1HCmjYvn9joC_B5XJpePVLgFl5oFUlTy_YSTkg";
	public static String getJsApiTicket(String access_token) {
        String requestUrl = WeixinConstants.JSAPI_TICKET_URL.replace("ACCESS_TOKEN", access_token);
        // 发起GET请求获取凭证
        JSONObject jsonObject = HttpUtil.doGetObject(requestUrl);
        String ticket = null;
        if (null != jsonObject) {
            try {
                ticket = jsonObject.getString("ticket");
                System.out.println("jsapi_ticket: "+ticket);
            } catch (JSONException e) {
                // 获取token失败
            	logger.error("获取token失败 errcode:{"+jsonObject.getInt("errcode")+"} errmsg:{"+jsonObject.getString("errmsg")+"}");
            }
        }
        return ticket;
    }
    public static Map<String, String> getSignature(String url) {
        Map<String, String> ret = new HashMap<String, String>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String str;
        String signature = "";
 
        //注意这里参数名必须全部小写，且必须有序
        str = "jsapi_ticket=" + jsapi_ticket +
                  "&noncestr=" + nonce_str +
                  "&timestamp=" + timestamp +
                  "&url=" + url;
 
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
 
        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);
 
        return ret;
    }
 
    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
 
    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }
 
    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
