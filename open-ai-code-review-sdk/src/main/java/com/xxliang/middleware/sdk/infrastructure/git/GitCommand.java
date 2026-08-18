package com.xxliang.middleware.sdk.infrastructure.git;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class GitCommand {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String githubReviewLogUri;

    private final String githubUsername;

    private final String githubToken;

    private final String project;

    private final String branch;

    private final String author;

    private final String message;

    public GitCommand(String githubReviewLogUri, String githubUsername, String githubToken, String project, String branch, String author, String message) {
        this.githubReviewLogUri = githubReviewLogUri;
        this.githubUsername = githubUsername;
        this.githubToken = githubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
    }

    public String getGitDiff() throws IOException, InterruptedException {
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
    public String writeLog(String log) throws GitAPIException, IOException {
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
                    .setURI(githubReviewLogUri)
                    .setDirectory(repoDir)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubUsername, githubToken))
                    .call();
            System.out.println("克隆成功: " + repoDir.getAbsolutePath());
        }


        String dataFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        File dataFolde = new File( "repo/"+ project + "/" + dataFolderName);

        if(!dataFolde.exists()){
            dataFolde.mkdirs();
        }

        String nickName = author;
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 4);
        String fileName =  project + "-" + branch + "-" + nickName.trim().replace("-", "") + "-" +System.currentTimeMillis()+ "-" + uuid + ".md";
        File newFile = new File(dataFolde, fileName);

        newFile.createNewFile();

        try(FileWriter fileWriter = new FileWriter(newFile)){
            fileWriter.write(log);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        git.add().addFilepattern(project+"/"+dataFolderName+"/"+fileName).call();
        git.commit().setMessage("ADD new file").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubUsername, githubToken)).call();


//        https://github.com/XXXlG/-open-ai-code-review-log/tree/master/2026-08-13
        return githubReviewLogUri.substring(0, githubReviewLogUri.length() - 4) +"/tree/master/"+project+"/"+dataFolderName+"/"+fileName;
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


    public Logger getLogger() {
        return logger;
    }

    public String getGithubReviewLogUri() {
        return githubReviewLogUri;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public String getProject() {
        return project;
    }

    public String getBranch() {
        return branch;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public String getGithubUsername() {
        return githubUsername;
    }
}

