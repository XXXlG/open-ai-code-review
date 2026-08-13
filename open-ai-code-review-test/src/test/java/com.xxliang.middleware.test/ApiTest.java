package com.xxliang.middleware.test;


import com.xxliang.middleware.sdk.infrastructure.GeminiApiClient;

import java.io.IOException;

public class ApiTest {
    public static void main(String[] args) throws IOException {
        String apiKey = "sk-943AOdCND4GGWNHnEc94095a42334f61B9B6BaF5F8454648";
        GeminiApiClient client = new GeminiApiClient(apiKey);
        String prompt = "你好，你叫什么";
        String reviewResult = client.sendMessage(prompt);
        System.out.println(reviewResult);
    }
}
