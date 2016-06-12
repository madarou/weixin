package com.makao.pay;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @description: TODO
 * @author makao
 * @date 2016年5月8日
 */
public class HttpUtil {
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
	
	public static JSONArray doGetArray(String url){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		JSONArray jsonArray = null;
		try {
			//执行http get请求，并获得response
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if(entity != null){
				String result = EntityUtils.toString(entity,"UTF-8");
				jsonArray = JSONArray.fromObject(result);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonArray;
	}
	
	public static JSONObject doPostStr(String url, String outStr){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		JSONObject jsonObject = null;
		//设置post参数
		StringEntity en = new StringEntity(outStr,"UTF-8");
		en.setContentType("application/json");
		httpPost.setEntity(en);
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
	
	public static String doPostXml(String url, String outStr){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		String res = null;
		//设置post参数
		StringEntity en = new StringEntity(outStr,"UTF-8");
		en.setContentType("text/xml");
		httpPost.setEntity(en);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if(entity != null){
				res = EntityUtils.toString(entity,"UTF-8");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public static Map<String, String> doPostXmlAndParse(String url, String outStr) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		Map<String, String> res = new HashMap<String, String>();
		SAXReader reader = new SAXReader();
		//设置post参数
		StringEntity en = new StringEntity(outStr,"UTF-8");
		en.setContentType("text/xml");
		httpPost.setEntity(en);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if(entity != null){
				InputStream ins = entity.getContent();
				Document doc;
				try {
					doc = reader.read(ins);
				} catch (DocumentException e1) {
					e1.printStackTrace();
					return null;
				}
				Element root = doc.getRootElement();
				List<Element> list = root.elements();
				for(Element e : list){
					res.put(e.getName(), e.getText());
				}
				ins.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
}
