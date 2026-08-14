package com.xxliang.middleware.sdk.infrastructure;

import com.lark.oapi.Client;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import com.xxliang.middleware.sdk.domain.model.FeishuConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 飞书消息通知工具类
 * 用于发送代码审查结果到飞书
 */
public class FeishuNotifier {
    
    private static final Logger logger = LoggerFactory.getLogger(FeishuNotifier.class);
    
    private final Client client;
    private final FeishuConfig config;
    
    /**
     * 构造函数
     * @param config 飞书配置信息
     */
    public FeishuNotifier(FeishuConfig config) {
        this.config = config;
        this.client = Client.newBuilder(config.getAppId(), config.getAppSecret()).build();
    }
    
    /**
     * 发送文本消息
     * @param content 消息内容
     * @return 是否发送成功
     */
    public boolean sendTextMessage(String content) {
        try {
            String jsonContent = String.format("{\"text\":\"%s\"}", escapeJson(content));
            return sendMessage("text", jsonContent);
        } catch (Exception e) {
            logger.error("发送飞书文本消息失败", e);
            return false;
        }
    }
    
    /**
     * 发送代码审查通知
     * @param reviewResult 审查结果内容
     * @param logUrl 日志地址
     * @return 是否发送成功
     */
    public boolean sendCodeReviewNotification(String reviewResult, String logUrl) {
        try {
            StringBuilder message = new StringBuilder();
            message.append("【代码审查通知】\\n\\n");
            message.append("审查结果：\\n");
            message.append(truncateMessage(reviewResult, 2000));
            message.append("\\n\\n");
            message.append("详细日志：").append(logUrl);
            
            return sendTextMessage(message.toString());
        } catch (Exception e) {
            logger.error("发送代码审查通知失败", e);
            return false;
        }
    }
    
    /**
     * 发送富文本消息（使用Post格式）
     * @param title 标题
     * @param content 内容
     * @param logUrl 日志链接
     * @return 是否发送成功
     */
    public boolean sendRichTextMessage(String title, String content, String logUrl) {
        try {
            String postContent = buildPostContent(title, content, logUrl);
            return sendMessage("post", postContent);
        } catch (Exception e) {
            logger.error("发送飞书富文本消息失败", e);
            return false;
        }
    }
    
    /**
     * 核心发送消息方法
     * @param msgType 消息类型：text, post, interactive等
     * @param content 消息内容JSON
     * @return 是否发送成功
     */
    private boolean sendMessage(String msgType, String content) {
        try {
            CreateMessageReq req = CreateMessageReq.newBuilder()
                    .receiveIdType(config.getReceiveIdType())
                    .createMessageReqBody(CreateMessageReqBody.newBuilder()
                            .receiveId(config.getReceiveId())
                            .msgType(msgType)
                            .content(content)
                            .build())
                    .build();
            
            CreateMessageResp resp = client.im().message().create(req);
            
            if (!resp.success()) {
                logger.error("飞书消息发送失败, code: {}, msg: {}, logId: {}", 
                        resp.getCode(), resp.getMsg(), resp.getRequestId());
                return false;
            }
            
            logger.info("飞书消息发送成功, messageId: {}", resp.getData().getMessageId());
            return true;
        } catch (Exception e) {
            logger.error("飞书消息发送异常", e);
            return false;
        }
    }
    
    /**
     * 构建Post格式的富文本内容
     */
    private String buildPostContent(String title, String content, String logUrl) {
        StringBuilder postJson = new StringBuilder();
        postJson.append("{\"zh_cn\":{");
        postJson.append("\"title\":\"").append(escapeJson(title)).append("\",");
        postJson.append("\"content\":[[");
        postJson.append("{\"tag\":\"text\",\"text\":\"").append(escapeJson(truncateMessage(content, 1500))).append("\"}");
        postJson.append("],[");
        postJson.append("{\"tag\":\"a\",\"text\":\"查看详细日志\",\"href\":\"").append(logUrl).append("\"}");
        postJson.append("]]}}");
        return postJson.toString();
    }
    
    /**
     * JSON字符串转义
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * 截断过长的消息
     */
    private String truncateMessage(String message, int maxLength) {
        if (message == null) {
            return "";
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength) + "...（内容过长已截断）";
    }
}
