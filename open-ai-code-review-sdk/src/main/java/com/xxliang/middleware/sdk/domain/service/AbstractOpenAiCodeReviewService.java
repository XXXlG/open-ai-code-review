package com.xxliang.middleware.sdk.domain.service;

import com.xxliang.middleware.sdk.infrastructure.feishu.FeishuConfig;
import com.xxliang.middleware.sdk.infrastructure.feishu.FeishuNotifier;
import com.xxliang.middleware.sdk.infrastructure.gemini.IOpenAI;
import com.xxliang.middleware.sdk.infrastructure.git.GitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractOpenAiCodeReviewService implements IOpenAiCodeReviewService{
    private final Logger logger = LoggerFactory.getLogger(AbstractOpenAiCodeReviewService.class);

    protected final GitCommand gitCommand;
    protected final IOpenAI openAI;
    protected final FeishuNotifier feishuNotifier;


    public AbstractOpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, FeishuNotifier feishuNotifier) {
        this.gitCommand = gitCommand;
        this.openAI = openAI;
        this.feishuNotifier = feishuNotifier;
    }




    @Override
    public void exec() {
        try {
            // 1. 获取提交代码
            String diffCode = getDiffCode();
            // 2. 开始评审代码
            String recommend = codeReview(diffCode);
            // 3. 记录评审结果；返回日志地址
            String logUrl = recordCodeReview(recommend);
            // 4. 发送消息通知；日志地址、通知的内容
            pushMessage(recommend,logUrl);
        } catch (Exception e) {
            logger.error("openai-code-review error", e);
        }

    }

    protected abstract String getDiffCode() throws IOException, InterruptedException;

    protected abstract String codeReview(String diffCode) throws Exception;

    protected abstract String recordCodeReview(String recommend) throws Exception;

    protected abstract void pushMessage(String reviewResult , String logUrl) throws Exception;
}
