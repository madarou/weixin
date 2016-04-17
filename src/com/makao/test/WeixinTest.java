package com.makao.test;

import java.io.IOException;

import net.sf.json.JSONObject;

import com.makao.po.AccessToken;
import com.makao.util.WeixinUtil;

public class WeixinTest {
	public static void main(String[] args) {
		
		//测试获取access_token
		AccessToken token = WeixinUtil.getAccessToken();
		System.out.println("Token: " + token.getToken());
		System.out.println("Expires: " + token.getExpiresIn());
		
		//测试图片消息
		String picpath = "/Users/makao/Desktop/yqq.jpeg";
		try {
//			//上传临时图片，获取其在微信服务器中的media_id
//			String media_id = WeixinUtil.upload(picpath, token.getToken(), "image");
//			System.out.println(media_id);
//			
//			//上传音乐消息中的缩略图，获取其thumb_media_id
//			String thumb_media_id = WeixinUtil.upload(picpath, token.getToken(), "thumb");
//			System.out.println(thumb_media_id);
			
			//测试创建菜单功能
//			int result = WeixinUtil.createMenu(token.getToken(), JSONObject.fromObject(WeixinUtil.initMenu()).toString());
//			if(result==0){
//				System.out.println("创建菜单成功");
//			}else{
//				System.out.println("错误码: " + result);
//			}
			
			//查询自定义菜单
			JSONObject jsonObject = WeixinUtil.queryMenu(token.getToken());
			System.out.println(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
