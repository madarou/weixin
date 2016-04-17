package com.makao.weixin;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import org.dom4j.DocumentException;

import com.makao.po.Image;
import com.makao.po.News;
import com.makao.po.TextMessage;
import com.makao.util.CheckUtil;
import com.makao.util.MessageUtil;

@WebServlet(name = "WeixinServlet", urlPatterns = { "/WeixinServlet" }, loadOnStartup = 1)
public class WeixinServlet extends HttpServlet {
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echostr = request.getParameter("echostr");
		PrintWriter out = response.getWriter();
		if (CheckUtil.checkSignature(signature, timestamp, nonce)) {
			out.print(echostr);
		}
		out.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		try {
			Map<String, String> map = MessageUtil.xmlToMap(request);
			String fromUserName = map.get("FromUserName");
			String toUserName = map.get("ToUserName");
			String createTime = map.get("CreateTime");
			String msgType = map.get("MsgType");
			String content = map.get("Content");
			System.out.println(content);
			String msgId = map.get("MsgId");

			String message = null;
			//处理文本消息
			if (MessageUtil.MESSAGE_TEXT.equals(msgType)) {
				//创建关键字回复
				if("你好".equals(content) || "hello".equals(content)){
					message = MessageUtil.textMessageToXml(toUserName, fromUserName, "你好");
					
				}else if("图文".equals(content)){//创建图文消息回复
					News news = new News();
					news.setTitle("鱼圈圈水族");
					news.setDescription("成都鱼圈圈水族馆");
					news.setPicUrl("http://madarou.ngrok.cc/weixin/img/yqq.jpeg");
					news.setUrl("www.yuqq.cc");
					List<News> news_items = new ArrayList<News>();//可以创建多个图文消息，都放到list中就行
					news_items.add(news);
					
					message = MessageUtil.newsMessageToXml(toUserName, fromUserName, news_items);
				}else if("图片".equals(content)){//创建图片消息回复
					Image image = new Image();
					//这里的media_id是从com.makao.weixin.test.WeixinTest中请求上传url后返回获取到的临时图片media_id，
					//只能保留三天有效，这里是测试用
					image.setMediaId("M85cAWmhyB-yy2OhMs6gAmmD3-yg7Cckx9cOgDVhkyE9-WY5ukNuaEbc5u0tDO22");
					message = MessageUtil.imageMessageToXml(toUserName, fromUserName, image);
				}else{//创建默认回复
					TextMessage text = new TextMessage();
					text.setFromUserName(toUserName);
					text.setToUserName(fromUserName);
					text.setMsgType(MessageUtil.MESSAGE_TEXT);
					text.setContent(content);
					text.setCreateTime(new Date().toLocaleString());
	
					message = MessageUtil.textMessageToXml(text);
				}
			}else if(MessageUtil.MESSAGE_EVENT.equals(msgType)){
				String eventType = map.get("Event");
				if(MessageUtil.MESSAGE_SUBSCRIBE.equals(eventType)){
					message = MessageUtil.textMessageToXml(toUserName, fromUserName, MessageUtil.onSubscriptionAutoReply());
				}
			}
			System.out.println(message);
			out.print(message);
		} catch (DocumentException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}

	}
}
