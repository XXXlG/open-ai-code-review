package com.xxliang.middleware.sdk;

import com.xxliang.middleware.sdk.infrastructure.GeminiApiClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OpenAiCodeReview {
    public static void main(String[] args) {
        try {
            // 从环境变量获取 API Key
            final String apiKey = "sk-943AOdCND4GGWNHnEc94095a42334f61B9B6BaF5F8454648";

            // 获取 git diff 差异信息
            String diffCode = getGitDiff();
            System.out.println("代码差异信息：");
            System.out.println(diffCode);
            System.out.println("\n==================== AI 代码审查 ====================\n");
            
            // 调用 Gemini API 进行代码审查
            GeminiApiClient client = new GeminiApiClient(apiKey);
            String prompt = "你是一个高级编程架构师，精通各类场景方案、架构设计、和编程语言。请对以下 git diff 代码变更进行代码审查，指出潜在问题、改进建议和最佳实践：\n\n" + diffCode;
            String reviewResult = client.sendMessage(prompt);
            
            System.out.println("审查结果：");
            System.out.println(reviewResult);
            
        } catch (Exception e) {
            System.err.println("代码审查失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取 git diff 差异信息
     * @return git diff 输出的差异内容
     * @throws IOException 执行命令失败
     * @throws InterruptedException 进程被中断
     */
    private static String getGitDiff() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(null); // 使用当前目录
        
        Process process = processBuilder.start();
        
        // 读取命令输出
        StringBuilder diffCode = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                diffCode.append(line).append("\n");
            }
        }
        
        // 等待进程执行完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // 读取错误信息
            StringBuilder errorMsg = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorMsg.append(line).append("\n");
                }
            }
            throw new RuntimeException("git diff 执行失败，退出码：" + exitCode + "，错误信息：" + errorMsg);
        }
        
        return diffCode.toString();
    }
}
