package com.makao.test;

import java.io.IOException;

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
			String media_id = WeixinUtil.upload(picpath, token.getToken(), "image");
			System.out.println(media_id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
