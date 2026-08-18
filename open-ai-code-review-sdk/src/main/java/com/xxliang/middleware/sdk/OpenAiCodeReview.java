package com.xxliang.middleware.sdk;

import com.xxliang.middleware.sdk.domain.service.impl.OpenAiCodeReviewService;
import com.xxliang.middleware.sdk.infrastructure.feishu.FeishuConfig;
import com.xxliang.middleware.sdk.infrastructure.feishu.FeishuNotifier;
import com.xxliang.middleware.sdk.infrastructure.gemini.IOpenAI;
import com.xxliang.middleware.sdk.infrastructure.gemini.impl.GeminiApiClient;
import com.xxliang.middleware.sdk.infrastructure.git.GitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class OpenAiCodeReview {
    public static void main(String[] args) {

        GitCommand gitCommand = new GitCommand(
                getEnv("GITHUB_REVIEW_LOG_URI"), // https://github.com/XXXlG/-open-ai-code-review-log.git
                getEnv("MY_GITHUB_USERNAME"),
                getEnv("MY_GITHUB_TOKEN"),  // write_log的日志密令
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );

        FeishuConfig feishuConfig = new FeishuConfig(
                getEnv("FEISHU_APP_ID"),
                getEnv("FEISHU_APP_SECRET"),
                getEnv("FEISHU_RECEIVE_ID"),
                getEnv("FEISHU_RECEIVE_ID_TYPE")
        );

        String aihubmixApiKey = System.getenv("AIHUBMIX_API_KEY");

        IOpenAI openAI = new GeminiApiClient(GeminiApiClient.DEFAULT_API_URL,aihubmixApiKey);

        OpenAiCodeReviewService openAiCodeReviewService = new OpenAiCodeReviewService(gitCommand, openAI, new FeishuNotifier(feishuConfig));

        openAiCodeReviewService.exec();


/**
        try {
            // 从环境变量获取 API Key
            String apiKey = System.getenv("AIHUBMIX_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.err.println("错误：未设置环境变量 AIHUBMIX_API_KEY");
                return;
            }

            // 获取 git diff 差异信息
            String diffCode = getGitDiff();
            System.out.println("代码差异信息：");
            System.out.println(diffCode);
            System.out.println("\n==================== AI 代码审查 ====================\n");
            
            // 调用 Gemini API 进行代码审查
            GeminiApiClient client = new GeminiApiClient(GeminiApiClient.DEFAULT_API_URL,System.getenv("AIHUBMIX_API_KEY"));
            String prompt = "你是一个高级编程架构师，精通各类场景方案、架构设计、和编程语言。请对以下 git diff 代码变更进行代码审查，指出潜在问题、改进建议和最佳实践：\n\n" + diffCode;
            String reviewResult = client.sendMessage(prompt);
            
            System.out.println("审查结果：");
            System.out.println(reviewResult);

            System.out.println("持久化日志...");
            String token = System.getenv("MY_GITHUB_TOKEN");
            String saveAdd = writeLog(token,reviewResult);
            System.out.println("持久化日志成功✅,保存地址: "+saveAdd);
            
            // 发送飞书通知
            sendFeishuNotification(reviewResult, saveAdd);
        } catch (Exception e) {
            System.err.println("代码审查失败：" + e.getMessage());
            e.printStackTrace();
        }

 */
    }


    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException("value is null");
        }
        return value;
    }

    /**
     * 获取 git diff 差异信息
     * @return git diff 输出的差异内容
     * @throws IOException 执行命令失败
     * @throws InterruptedException 进程被中断
     */
    public static String getGitDiff() throws IOException, InterruptedException {
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


    /**
     * 代码审计的地址：
     */
    public static String writeLog(String token , String log) throws GitAPIException, IOException {
        String LOG_URI = "https://github.com/XXXlG/-open-ai-code-review-log.git";
        String username = "XXXlG";
        File repoDir = new File("repo");
        Git git = null;

        if (repoDir.exists() && new File(repoDir, ".git").exists()) {
            // ✅ 仓库已存在，直接打开
            git = Git.open(repoDir);
            System.out.println("打开已有仓库: " + repoDir.getAbsolutePath());
        } else {
            // ❌ 仓库不存在，需要克隆
            if (repoDir.exists()) {
                deleteDirectory(repoDir);  // 如果目录存在但不是 Git 仓库，删除
            }
            git = Git.cloneRepository()
                    .setURI(LOG_URI)
                    .setDirectory(repoDir)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call();
            System.out.println("克隆成功: " + repoDir.getAbsolutePath());
        }


        String dataFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        File dataFolde = new File("repo/" + dataFolderName);

        if(!dataFolde.exists()){
            dataFolde.mkdirs();
        }

        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String fileName = uuid + ".md";
        File newFile = new File(dataFolde, fileName);

        newFile.createNewFile();

        try(FileWriter fileWriter = new FileWriter(newFile)){
            fileWriter.write(log);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        git.add().addFilepattern(dataFolderName+"/"+fileName).call();
        git.commit().setMessage("ADD new file").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token)).call();


//        https://github.com/XXXlG/-open-ai-code-review-log/tree/master/2026-08-13
        return LOG_URI.substring(0, LOG_URI.length() - 4) +"/tree/master/"+dataFolderName+"/"+fileName;
    }
    // 递归删除目录的辅助方法
    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }
    
    /**
     * 发送飞书通知
     * @param reviewResult 审查结果
     * @param logUrl 日志地址
     */
    private static void sendFeishuNotification(String reviewResult, String logUrl) {
        try {
            // 从环境变量获取飞书配置
            String appId = System.getenv("FEISHU_APP_ID");
            String appSecret = System.getenv("FEISHU_APP_SECRET");
            String receiveId = System.getenv("FEISHU_RECEIVE_ID");
            String receiveIdType = System.getenv("FEISHU_RECEIVE_ID_TYPE");

            // 检查配置是否完整
            if (appId == null || appSecret == null || receiveId == null) {
                System.out.println("⚠️ 飞书配置不完整，跳过飞书通知");
                System.out.println("提示：请设置环境变量 FEISHU_APP_ID, FEISHU_APP_SECRET, FEISHU_RECEIVE_ID");
                return;
            }

            // 默认发送到群聊
            if (receiveIdType == null || receiveIdType.trim().isEmpty()) {
                receiveIdType = "chat_id";
            }

            System.out.println("正在发送飞书通知...");
            FeishuConfig config = new FeishuConfig(appId, appSecret, receiveId, receiveIdType);
            FeishuNotifier notifier = new FeishuNotifier(config);

            boolean success = notifier.sendRichTextMessage("代码审查完成", reviewResult, logUrl);

            if (success) {
                System.out.println("飞书通知发送成功✅");
            } else {
                System.out.println("飞书通知发送失败❌");
            }
        } catch (Exception e) {
            System.err.println("发送飞书通知时发生异常：" + e.getMessage());
        }
    }
}
