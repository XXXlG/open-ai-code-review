package com.xxliang.middleware.sdk;

import com.xxliang.middleware.sdk.infrastructure.GeminiApiClient;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class OpenAiCodeReview {
    public static void main(String[] args) {
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
            GeminiApiClient client = new GeminiApiClient(apiKey);
            String prompt = "你是一个高级编程架构师，精通各类场景方案、架构设计、和编程语言。请对以下 git diff 代码变更进行代码审查，指出潜在问题、改进建议和最佳实践：\n\n" + diffCode;
            String reviewResult = client.sendMessage(prompt);
            
            System.out.println("审查结果：");
            System.out.println(reviewResult);

            System.out.println("持久化日志...");
            String token = System.getenv("MY_GITHUB_TOKEN");
            String saveAdd = writeLog(token,reviewResult);
            System.out.println("持久化日志成功✅,保存地址: "+saveAdd);
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
}
