package com.makao.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.makao.po.Image;
import com.makao.po.ImageMessage;
import com.makao.po.News;
import com.makao.po.NewsMessage;
import com.makao.po.TextMessage;
import com.thoughtworks.xstream.XStream;

/**
 * @author makao
 * @date 2016年4月16日
 */
/**
 * @author makao
 * @date 2016年4月17日
 */
public class MessageUtil {
	public static final String MESSAGE_TEXT = "text";
	public static final String MESSAGE_NEWS = "news";//图文消息
	public static final String MESSAGE_IMAGE = "image";
	public static final String MESSAGE_VOICE = "voice";
	public static final String MESSAGE_VIDEO = "video";
	public static final String MESSAGE_LOCATION = "location";
	public static final String MESSAGE_LINK = "link";
	public static final String MESSAGE_EVENT = "event";
	public static final String MESSAGE_SUBSCRIBE = "subscribe";
	public static final String MESSAGE_SCAN = "SCAN";
	public static final String MESSAGE_CLICK = "CLICK";
	public static final String MESSAGE_VIEW = "VIEW";
	
	/**
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 * 微信传来的xml格式转换成map
	 */
	public static Map<String, String> xmlToMap(HttpServletRequest request) throws IOException, DocumentException{
		Map<String, String> map = new HashMap<String, String>();
		SAXReader reader = new SAXReader();
		
		InputStream ins = request.getInputStream();
		Document doc = reader.read(ins);
		Element root = doc.getRootElement();
		
		List<Element> list = root.elements();
		for(Element e : list){
			map.put(e.getName(), e.getText());
		}
		ins.close();
		return map;
	}
	
	/**
	 * @return
	 * 用户关注时的自动回复
	 */
	public static String onSubscriptionAutoReply(){
		return "才来？！等你很久了";
	}
	
	/**
	 * @param textMessage
	 * @return
	 * 将文本消息对象转换为xml
	 */
	public static String textMessageToXml(TextMessage textMessage){
		XStream xstream = new XStream();
		//原始组装的根节点是com.makao.po.TextMessage，将它转换成'xml'，与微信文档中定义的一致
		xstream.alias("xml", textMessage.getClass());
		return xstream.toXML(textMessage);
	}
	
	/**
	 * @param toUserName
	 * @param fromUserName
	 * @param content
	 * @return
	 * 重载的文本消息转换为xml
	 */
	public static String textMessageToXml(String toUserName, String fromUserName, String content){
		TextMessage text = new TextMessage();
		text.setFromUserName(toUserName);
		text.setToUserName(fromUserName);
		text.setMsgType(MessageUtil.MESSAGE_TEXT);
		text.setContent(content);
		text.setCreateTime(new Date().toLocaleString());

		return textMessageToXml(text);
	}
	
	/**
	 * <xml>
		<ToUserName><![CDATA[toUser]]></ToUserName>
		<FromUserName><![CDATA[fromUser]]></FromUserName>
		<CreateTime>12345678</CreateTime>
		<MsgType><![CDATA[news]]></MsgType>
		<ArticleCount>2</ArticleCount>
		<Articles>
		<item>
		<Title><![CDATA[title1]]></Title> 
		<Description><![CDATA[description1]]></Description>
		<PicUrl><![CDATA[picurl]]></PicUrl>
		<Url><![CDATA[url]]></Url>
		</item>
		<item>
		<Title><![CDATA[title]]></Title>
		<Description><![CDATA[description]]></Description>
		<PicUrl><![CDATA[picurl]]></PicUrl>
		<Url><![CDATA[url]]></Url>
		</item>
		</Articles>
		</xml>
	 * @param newsMessage
	 * @return
	 * 将图文消息转换为xml
	 */
	public static String newsMessageToXml(NewsMessage newsMessage){
		XStream xstream = new XStream();
		//原始组装的根节点是com.makao.po.TextMessage，将它转换成'xml'，与微信文档中定义的一致
		xstream.alias("xml", newsMessage.getClass());
		//里面的item也要手动修改
		xstream.alias("item", new News().getClass());
		return xstream.toXML(newsMessage);
	}
	

	/**
	 * @param toUserName
	 * @param fromUserName
	 * @return
	 * 重载的将图文消息转换为xml
	 */
	public static String newsMessageToXml(String toUserName, String fromUserName, List<News> items){
		NewsMessage newsMessage = new NewsMessage();
		
		newsMessage.setFromUserName(toUserName);
		newsMessage.setToUserName(fromUserName);
		newsMessage.setCreateTime(new Date().toGMTString());
		newsMessage.setMsgType(MESSAGE_NEWS);
		newsMessage.setArticleCount(items.size());
		newsMessage.setArticles(items);

		return newsMessageToXml(newsMessage);
	}
	
	/**
	 * <xml>
		<ToUserName><![CDATA[toUser]]></ToUserName>
		<FromUserName><![CDATA[fromUser]]></FromUserName>
		<CreateTime>12345678</CreateTime>
		<MsgType><![CDATA[image]]></MsgType>
		<Image>
		<MediaId><![CDATA[media_id]]></MediaId>
		</Image>
		</xml>
	 * @param imageMessage
	 * @return
	 * 将图片消息转换成xml
	 */
	public static String imageMessageToXml(ImageMessage imageMessage){
		XStream xstream = new XStream();
		//原始组装的根节点是com.makao.po.ImageMessage，将它转换成'xml'，与微信文档中定义的一致
		xstream.alias("xml", imageMessage.getClass());
		//里面的com.makao.po.Image也要手动修改为Image
		xstream.alias("Image", new Image().getClass());
		return xstream.toXML(imageMessage);
	}
	
	/**
	 * @param toUserName
	 * @param fromUserName
	 * @param image
	 * @return
	 * 重载的将图片消息转换为xml
	 */
	public static String imageMessageToXml(String toUserName, String fromUserName, Image image){
		ImageMessage imageMessage = new ImageMessage();
		imageMessage.setFromUserName(toUserName);
		imageMessage.setToUserName(fromUserName);
		imageMessage.setCreateTime(new Date().toGMTString());
		imageMessage.setMsgType(MESSAGE_IMAGE);
		imageMessage.setImage(image);
		
		return imageMessageToXml(imageMessage);
	}
}
