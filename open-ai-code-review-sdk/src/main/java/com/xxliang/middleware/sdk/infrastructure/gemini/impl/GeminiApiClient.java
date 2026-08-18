package com.xxliang.middleware.sdk.infrastructure.gemini.impl;

import com.alibaba.fastjson2.JSON;
import com.xxliang.middleware.sdk.domain.model.GeminiRequest;
import com.xxliang.middleware.sdk.domain.model.GeminiResponse;
import com.xxliang.middleware.sdk.infrastructure.gemini.IOpenAI;
import org.eclipse.jgit.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Gemini API 调用工具类
 */
public class GeminiApiClient implements IOpenAI {

    public final static String DEFAULT_API_URL = "https://aihubmix.com/gemini/v1beta/models/gemini-3.5-flash-lite-free:generateContent";
    private final String API_URL;
    private final String apiKey;

    public GeminiApiClient(String apiUrl, String apiKey) {
        if(StringUtils.isEmptyOrNull(apiUrl)) {
            API_URL = DEFAULT_API_URL;
        }else {
            API_URL = apiUrl;
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key 不能为空");
        }
        this.apiKey = apiKey;
    }



    /**
     * 发送消息到 Gemini API
     * @param userMessage 用户消息内容
     * @return API 响应内容
     * @throws IOException 网络请求异常
     */
    public String sendMessage(String userMessage) throws IOException {
        GeminiResponse response = sendRequest(userMessage);
        return extractResponseText(response);
    }

    /**
     * 发送请求到 Gemini API
     * @param userMessage 用户消息内容
     * @return 完整的响应对象
     * @throws IOException 网络请求异常
     */
    public GeminiResponse sendRequest(String userMessage) throws IOException {
        // 构建请求体
        GeminiRequest request = buildRequest(userMessage);
        String requestBody = JSON.toJSONString(request);

        // 创建 HTTP 连接
        HttpURLConnection connection = createConnection();

        // 发送请求
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取响应
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String responseBody = readResponse(connection);
            return JSON.parseObject(responseBody, GeminiResponse.class);
        } else {
            String errorBody = readErrorResponse(connection);
            throw new IOException("API 请求失败，状态码：" + responseCode + "，错误信息：" + errorBody);
        }
    }

    /**
     * 构建请求对象
     */
    private GeminiRequest buildRequest(String userMessage) {
        GeminiRequest request = new GeminiRequest();
        
        GeminiRequest.Part part = new GeminiRequest.Part(userMessage);
        List<GeminiRequest.Part> parts = new ArrayList<>();
        parts.add(part);
        
        GeminiRequest.Content content = new GeminiRequest.Content("user", parts);
        List<GeminiRequest.Content> contents = new ArrayList<>();
        contents.add(content);
        
        request.setContents(contents);
        return request;
    }

    /**
     * 创建 HTTP 连接
     */
    private HttpURLConnection createConnection() throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("x-goog-api-key", apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        return connection;
    }

    /**
     * 读取成功响应
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    /**
     * 读取错误响应
     */
    private String readErrorResponse(HttpURLConnection connection) {
        StringBuilder error = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
        } catch (Exception e) {
            return "无法读取错误信息";
        }
        return error.toString();
    }

    /**
     * 从响应中提取文本内容
     */
    private String extractResponseText(GeminiResponse response) {
        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            return "";
        }
        
        GeminiResponse.Candidate candidate = response.getCandidates().get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null 
                || candidate.getContent().getParts().isEmpty()) {
            return "";
        }
        
        return candidate.getContent().getParts().get(0).getText();
    }
}
