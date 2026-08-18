package com.xxliang.middleware.sdk.infrastructure.gemini;

public interface IOpenAI {
    public String sendMessage(String userMessage)  throws Exception;
}
