package com.xxliang.middleware.sdk.test;

import com.xxliang.middleware.sdk.domain.model.FeishuConfig;
import com.xxliang.middleware.sdk.infrastructure.FeishuNotifier;
import org.junit.Test;

/**
 * API测试类
 */
public class ApiTest {

    public void test_commit(){
        System.out.println("aaaaa1");
        System.out.println("aaaaa2");
        System.out.println("aaaaa3");
        System.out.println("aaaaa4");
        System.out.println("aaaaa5");
    }

    /**
     * 测试飞书文本消息发送
     * 注意：运行前需要设置环境变量或直接在代码中配置飞书信息
     */
    @Test
    public void test_feishu_send_text_message() {
        // 从环境变量获取配置
        String appId = System.getenv("FEISHU_APP_ID");
        String appSecret = System.getenv("FEISHU_APP_SECRET");
        String receiveId = System.getenv("FEISHU_RECEIVE_ID");
        String receiveIdType = System.getenv("FEISHU_RECEIVE_ID_TYPE");
        
        // 如果环境变量未设置，使用默认值（需要手动替换）
        if (appId == null || appId.isEmpty()) {
            appId = "cli_xxxxxxxxxxxxx";  // 替换为你的App ID
            appSecret = "your_app_secret"; // 替换为你的App Secret
            receiveId = "oc_xxxxxxxxxxxxx"; // 替换为你的群聊ID或用户Open ID
            receiveIdType = "chat_id";      // chat_id 或 open_id
        }
        
        // 创建配置
        FeishuConfig config = new FeishuConfig(appId, appSecret, receiveId, receiveIdType);
        
        // 创建通知器
        FeishuNotifier notifier = new FeishuNotifier(config);
        
        // 发送测试消息
        boolean success = notifier.sendTextMessage("【测试消息】飞书告警功能测试成功！");
        
        System.out.println("发送结果：" + (success ? "成功✅" : "失败❌"));
    }
    
    /**
     * 测试飞书富文本消息发送
     */
    @Test
    public void test_feishu_send_rich_text_message() {
        // 从环境变量获取配置
        String appId = System.getenv("FEISHU_APP_ID");
        String appSecret = System.getenv("FEISHU_APP_SECRET");
        String receiveId = System.getenv("FEISHU_RECEIVE_ID");
        String receiveIdType = System.getenv("FEISHU_RECEIVE_ID_TYPE");
        
        if (appId == null || appId.isEmpty()) {
            System.out.println("⚠️ 请先设置环境变量：FEISHU_APP_ID, FEISHU_APP_SECRET, FEISHU_RECEIVE_ID");
            return;
        }
        
        // 创建配置
        FeishuConfig config = new FeishuConfig(appId, appSecret, receiveId, 
                receiveIdType != null ? receiveIdType : "chat_id");
        
        // 创建通知器
        FeishuNotifier notifier = new FeishuNotifier(config);
        
        // 构造测试数据
        String title = "代码审查完成";
        String content = "测试内容：\n1. 代码质量良好\n2. 无明显bug\n3. 建议优化性能";
        String logUrl = "https://github.com/test/repo/blob/main/log.md";
        
        // 发送富文本消息
        boolean success = notifier.sendRichTextMessage(title, content, logUrl);
        
        System.out.println("富文本消息发送结果：" + (success ? "成功✅" : "失败❌"));
    }
    
    /**
     * 测试代码审查通知发送
     */
    @Test
    public void test_feishu_send_code_review_notification() {
        String appId = System.getenv("FEISHU_APP_ID");
        String appSecret = System.getenv("FEISHU_APP_SECRET");
        String receiveId = System.getenv("FEISHU_RECEIVE_ID");
        String receiveIdType = System.getenv("FEISHU_RECEIVE_ID_TYPE");
        
        if (appId == null || appId.isEmpty()) {
            System.out.println("⚠️ 请先设置环境变量：FEISHU_APP_ID, FEISHU_APP_SECRET, FEISHU_RECEIVE_ID");
            return;
        }
        
        // 创建配置
        FeishuConfig config = new FeishuConfig(appId, appSecret, receiveId, 
                receiveIdType != null ? receiveIdType : "chat_id");
        
        // 创建通知器
        FeishuNotifier notifier = new FeishuNotifier(config);
        
        // 模拟审查结果
        String reviewResult = "## 代码审查结果\n\n"
                + "### 问题列表\n"
                + "1. 变量命名不规范\n"
                + "2. 缺少异常处理\n"
                + "3. 建议添加注释\n\n"
                + "### 总体评价\n"
                + "代码基本功能实现正确，但需要优化代码质量。";
        
        String logUrl = "https://github.com/XXXlG/-open-ai-code-review-log/tree/master/2026-08-14/test123.md";
        
        // 发送代码审查通知
        boolean success = notifier.sendCodeReviewNotification(reviewResult, logUrl);
        
        System.out.println("代码审查通知发送结果：" + (success ? "成功✅" : "失败❌"));
    }
    
    /**
     * 测试配置验证
     */
    @Test
    public void test_feishu_config() {
        // 测试配置对象创建
        FeishuConfig config = new FeishuConfig(
                "cli_test123",
                "secret_test456",
                "oc_test789",
                "chat_id"
        );
        
        System.out.println("配置信息：");
        System.out.println("App ID: " + config.getAppId());
        System.out.println("App Secret: " + (config.getAppSecret() != null ? "已设置" : "未设置"));
        System.out.println("Receive ID: " + config.getReceiveId());
        System.out.println("Receive ID Type: " + config.getReceiveIdType());
        
        // 验证getter和setter
        config.setAppId("new_app_id");
        System.out.println("修改后 App ID: " + config.getAppId());
    }
    
    /**
     * 测试消息内容过长时的截断功能
     */
    @Test
    public void test_feishu_long_message() {
        String appId = System.getenv("FEISHU_APP_ID");
        String appSecret = System.getenv("FEISHU_APP_SECRET");
        String receiveId = System.getenv("FEISHU_RECEIVE_ID");
        String receiveIdType = System.getenv("FEISHU_RECEIVE_ID_TYPE");
        
        if (appId == null || appId.isEmpty()) {
            System.out.println("⚠️ 请先设置环境变量：FEISHU_APP_ID, FEISHU_APP_SECRET, FEISHU_RECEIVE_ID");
            return;
        }
        
        // 创建配置
        FeishuConfig config = new FeishuConfig(appId, appSecret, receiveId, 
                receiveIdType != null ? receiveIdType : "chat_id");
        
        // 创建通知器
        FeishuNotifier notifier = new FeishuNotifier(config);
        
        // 构造超长内容（模拟）
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longContent.append("这是第").append(i + 1).append("行测试内容，用于测试消息截断功能。\n");
        }
        
        String logUrl = "https://github.com/test/repo/blob/main/long_log.md";
        
        // 发送超长消息
        boolean success = notifier.sendRichTextMessage("超长消息测试", longContent.toString(), logUrl);
        
        System.out.println("超长消息发送结果：" + (success ? "成功✅（已自动截断）" : "失败❌"));
    }
}
