package com.xxliang.middleware.sdk.domain.service.impl;

import com.xxliang.middleware.sdk.domain.service.AbstractOpenAiCodeReviewService;
import com.xxliang.middleware.sdk.infrastructure.feishu.FeishuNotifier;
import com.xxliang.middleware.sdk.infrastructure.gemini.IOpenAI;
import com.xxliang.middleware.sdk.infrastructure.git.GitCommand;

import java.io.IOException;

public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, FeishuNotifier feishuNotifier) {
        super(gitCommand, openAI, feishuNotifier);
    }

    /**
     * 获取代码差异
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return this.gitCommand.getGitDiff();
    }

    /**
     * AI评审代码
     * @param diffCode
     * @return
     * @throws Exception
     */
    @Override
    protected String codeReview(String diffCode) throws Exception {
        String prompt = "你是一个高级编程架构师，精通各类场景方案、架构设计、和编程语言。请对以下 git diff 代码变更进行代码审查，指出潜在问题、改进建议和最佳实践：\n\n" + diffCode;
        return this.openAI.sendMessage(prompt);
    }

    /**
     * 代码持久化
     * @param recommend
     * @return
     * @throws Exception
     */
    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        return gitCommand.writeLog(recommend);
    }

    /**
     * 消息通知评审结果
     * @param logUrl
     * @throws Exception
     */
    @Override
    protected void pushMessage(String reviewResult,String logUrl) throws Exception {
        feishuNotifier.sendFeishuNotification(reviewResult,logUrl);
    }
}
