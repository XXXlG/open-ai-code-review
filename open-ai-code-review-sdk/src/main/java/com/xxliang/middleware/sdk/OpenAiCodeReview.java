package com.xxliang.middleware.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OpenAiCodeReview {
    public static void main(String[] args) {
        try {
            // 获取 git diff 差异信息
            String diffCode = getGitDiff();
            System.out.println("代码差异信息：");
            System.out.println(diffCode);
            
            // TODO: 将 diffCode 发送给 AI 进行代码审查
            
        } catch (Exception e) {
            System.err.println("获取代码差异失败：" + e.getMessage());
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
