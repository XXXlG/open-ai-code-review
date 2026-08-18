package com.xxliang.middleware.sdk.infrastructure.feishu;

/**
 * 飞书配置类
 * 用于存储飞书应用的认证信息
 */
public class FeishuConfig {
    
    /**
     * 飞书应用 App ID
     */
    private String appId;
    
    /**
     * 飞书应用 App Secret
     */
    private String appSecret;
    
    /**
     * 接收消息的飞书群聊 ID 或用户 Open ID
     */
    private String receiveId;
    
    /**
     * 接收者ID类型：chat_id(群聊) 或 open_id(用户)
     */
    private String receiveIdType;
    
    public FeishuConfig() {
    }
    
    public FeishuConfig(String appId, String appSecret, String receiveId, String receiveIdType) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.receiveId = receiveId;
        this.receiveIdType = receiveIdType;
    }
    
    public String getAppId() {
        return appId;
    }
    
    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    public String getAppSecret() {
        return appSecret;
    }
    
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
    
    public String getReceiveId() {
        return receiveId;
    }
    
    public void setReceiveId(String receiveId) {
        this.receiveId = receiveId;
    }
    
    public String getReceiveIdType() {
        return receiveIdType;
    }
    
    public void setReceiveIdType(String receiveIdType) {
        this.receiveIdType = receiveIdType;
    }
}
