# OpenAI Code Review

<div align="center">

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/XXXlG/open-ai-code-review)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-orange)](https://www.oracle.com/java/)
[![Release](https://img.shields.io/badge/release-v1.0-blue)](https://github.com/XXXlG/open-ai-code-review/releases/tag/1.x)

**基于AI的自动化代码审查工具，通过GitHub Actions实现CI/CD集成**

[快速开始](#-快速开始) · [功能特性](#-功能特性) · [使用文档](#-使用文档) · [接入方案](#-接入方案)

</div>

---

## 📖 项目简介

OpenAI Code Review 是一个基于大语言模型（LLM）的自动化代码审查工具。它通过GitHub Actions在代码提交时自动触发，对代码变更进行智能分析，并将审查结果：
- 📝 持久化存储到指定的GitHub仓库
- 💬 通过飞书机器人实时推送通知
- 🔍 提供详细的代码质量分析和改进建议

### 核心优势

✅ **零侵入**：基于GitHub Actions，无需修改业务代码  
✅ **自动化**：提交代码即触发，无需人工干预  
✅ **智能分析**：基于Gemini大模型，提供专业的代码审查意见  
✅ **多渠道通知**：支持飞书告警，实时获取审查结果  
✅ **日志持久化**：所有审查记录永久保存，可追溯查询

---

## 🚀 快速开始

### 前置条件

- GitHub账号
- Java 17+（如果需要本地开发）
- Gemini API Key（用于AI代码审查）
- 飞书应用（可选，用于消息通知）

### 1️⃣ 创建审查日志仓库

创建一个新的GitHub仓库用于存储代码审查日志，例如：`-open-ai-code-review-log`

### 2️⃣ 配置GitHub Secrets

在你的项目仓库中，进入 `Settings` → `Secrets and variables` → `Actions`，添加以下Secrets：

| Secret名称 | 说明 | 必需 |
|-----------|------|------|
| `MY_GITHUB_TOKEN` | GitHub Personal Access Token | ✅ |
| `AIHUBMIX_API_KEY` | Gemini API密钥 | ✅ |
| `FEISHU_APP_ID` | 飞书应用ID | ❌ |
| `FEISHU_APP_SECRET` | 飞书应用密钥 | ❌ |
| `FEISHU_RECEIVE_ID` | 飞书群聊ID或用户OpenID | ❌ |
| `FEISHU_RECEIVE_ID_TYPE` | 接收者类型（chat_id/open_id） | ❌ |

#### 如何获取这些配置？

**GitHub Token:**
1. 访问 https://github.com/settings/tokens
2. 点击 "Generate new token (classic)"
3. 勾选 `repo` 权限
4. 复制生成的token

**Gemini API Key:**
1. 访问 https://aihubmix.com/
2. 注册并获取API Key

**飞书配置（可选）:**
- 参考文档：[飞书告警快速开始.md](.kiro/outputs/飞书告警快速开始.md)

### 3️⃣ 添加GitHub Actions工作流

在你的项目根目录创建 `.github/workflows/code-review.yml`：

```yaml
name: OpenAI Code Review

on:
  push:
    branches:
      - main        # 根据你的主分支名称修改
      - master
      - develop
  pull_request:
    branches:
      - main
      - master

jobs:
  code-review:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v2
        with:
          fetch-depth: 2  # 获取最近2次提交用于diff

      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          distribution: 'adopt'
          java-version: '17'

      - name: Download OpenAI Code Review JAR
        run: |
          wget https://github.com/XXXlG/open-ai-code-review/releases/download/1.x/open-ai-code-review-sdk-1.0.jar

      - name: Get repository info
        run: |
          echo "REPO_NAME=${GITHUB_REPOSITORY##*/}" >> $GITHUB_ENV
          echo "BRANCH_NAME=${GITHUB_REF#refs/heads/}" >> $GITHUB_ENV
          echo "COMMIT_AUTHOR=$(git log -1 --pretty=format:'%an <%ae>')" >> $GITHUB_ENV
          echo "COMMIT_MESSAGE=$(git log -1 --pretty=format:'%s')" >> $GITHUB_ENV

      - name: Run Code Review
        run: java -jar open-ai-code-review-sdk-1.0.jar
        env:
          # GitHub配置
          MY_GITHUB_USERNAME: YOUR_GITHUB_USERNAME  # 替换为你的GitHub用户名
          MY_GITHUB_TOKEN: ${{ secrets.MY_GITHUB_TOKEN }}
          GITHUB_REVIEW_LOG_URI: https://github.com/YOUR_USERNAME/-open-ai-code-review-log.git  # 替换为你的日志仓库
          COMMIT_PROJECT: ${{ env.REPO_NAME }}
          COMMIT_BRANCH: ${{ env.BRANCH_NAME }}
          COMMIT_AUTHOR: ${{ env.COMMIT_AUTHOR }}
          COMMIT_MESSAGE: ${{ env.COMMIT_MESSAGE }}
          # AI配置
          AIHUBMIX_API_KEY: ${{ secrets.AIHUBMIX_API_KEY }}
          # 飞书配置（可选）
          FEISHU_APP_ID: ${{ secrets.FEISHU_APP_ID }}
          FEISHU_APP_SECRET: ${{ secrets.FEISHU_APP_SECRET }}
          FEISHU_RECEIVE_ID: ${{ secrets.FEISHU_RECEIVE_ID }}
          FEISHU_RECEIVE_ID_TYPE: ${{ secrets.FEISHU_RECEIVE_ID_TYPE }}
```

### 4️⃣ 提交代码测试

```bash
git add .
git commit -m "feat: 添加代码审查功能"
git push
```

提交后，GitHub Actions会自动触发代码审查，你可以在仓库的 `Actions` 标签页查看执行情况。

---

## ✨ 功能特性

### 🤖 AI智能审查

- **代码质量分析**：检测潜在bug、代码异味、性能问题
- **最佳实践建议**：提供符合业界标准的改进建议
- **安全漏洞检测**：识别常见的安全隐患
- **架构设计评估**：评估代码的架构合理性

### 📊 审查结果管理

- **日志持久化**：所有审查记录保存到GitHub仓库
- **结构化存储**：按日期组织，便于查询和追溯
- **Markdown格式**：审查结果以易读的Markdown格式呈现
- **链接可访问**：生成永久访问链接，随时查看历史记录

### 📱 多渠道通知

- **飞书告警**：审查完成后自动推送到飞书群聊
- **富文本消息**：包含审查摘要和详细日志链接
- **实时推送**：秒级响应，及时获取反馈

### 🔧 灵活配置

- **分支控制**：可指定触发代码审查的分支
- **环境隔离**：支持不同环境使用不同配置
- **可选功能**：飞书通知可选，不影响核心功能

---

## 📋 接入方案对比

### 方案1：业务工程引入SDK包 ❌

```java
// 业务代码中调用
OpenAiCodeReview review = new OpenAiCodeReview();
review.execute();
```

**缺点**：
- ❌ 代码侵入性强
- ❌ 所有业务代码都要配置
- ❌ 维护成本高

### 方案2：Java Agent字节码增强 ⚠️

```bash
java -javaagent:code-review-agent.jar -jar your-app.jar
```

**特点**：
- ✅ 降低代码侵入性
- ⚠️ 需要修改启动命令
- ⚠️ 配置相对复杂

### 方案3：GitHub Actions CI/CD ✅ **推荐**

```yaml
# .github/workflows/code-review.yml
on:
  push:
    branches: [main]
```

**优点**：
- ✅ 零代码侵入
- ✅ 自动触发，无需人工干预
- ✅ 配置简单，易于维护
- ✅ 与Git流程深度集成

### 方案4：Git Webhook钩子 ⚠️

```bash
# .git/hooks/pre-commit
./code-review.sh
```

**特点**：
- ✅ 本地即时反馈
- ⚠️ 需要每个开发者配置
- ⚠️ 依赖本地环境

---

## 📚 使用文档

### 环境变量说明

#### GitHub相关配置

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `MY_GITHUB_USERNAME` | GitHub用户名 | `XXXlG` |
| `MY_GITHUB_TOKEN` | GitHub访问令牌 | `ghp_xxxxxxxxxxxx` |
| `GITHUB_REVIEW_LOG_URI` | 日志仓库地址 | `https://github.com/XXXlG/-open-ai-code-review-log.git` |
| `COMMIT_PROJECT` | 项目名称 | `open-ai-code-review` |
| `COMMIT_BRANCH` | 分支名称 | `main` |
| `COMMIT_AUTHOR` | 提交者 | `张三 <zhangsan@example.com>` |
| `COMMIT_MESSAGE` | 提交信息 | `feat: 添加新功能` |

#### AI模型配置

| 变量名 | 说明 | 获取方式 |
|--------|------|----------|
| `AIHUBMIX_API_KEY` | Gemini API密钥 | [AIHubMix官网](https://aihubmix.com/) |

#### 飞书通知配置（可选）

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `FEISHU_APP_ID` | 飞书应用ID | - |
| `FEISHU_APP_SECRET` | 飞书应用密钥 | - |
| `FEISHU_RECEIVE_ID` | 接收者ID（群聊或用户） | - |
| `FEISHU_RECEIVE_ID_TYPE` | 接收者类型 | `chat_id` |

### 审查结果示例

**日志存储路径**：
```
-open-ai-code-review-log/
├── 2026-08-17/
│   ├── abc123def456.md  # UUID命名的审查记录
│   ├── xyz789uvw012.md
│   └── ...
└── 2026-08-18/
    └── ...
```

**审查报告内容**：
```markdown
## 代码审查结果

### 📌 提交信息
- **项目**：open-ai-code-review
- **分支**：main
- **作者**：张三 <zhangsan@example.com>
- **提交**：feat: 添加飞书告警功能

### 🔍 审查意见

#### 1. 代码质量 ⭐⭐⭐⭐
代码整体质量较好，符合Java编码规范。

#### 2. 潜在问题
- **异常处理**：建议在FeishuNotifier中添加重试机制
- **日志记录**：部分关键操作缺少日志

#### 3. 改进建议
- 考虑添加单元测试覆盖
- 优化错误提示信息

### 📊 代码变更统计
- 新增文件：3个
- 修改文件：2个
- 新增代码：+450行
- 删除代码：-10行
```

### 飞书通知示例

<img src="docs/images/feishu-notification.png" alt="飞书通知示例" width="400"/>

**消息内容**：
```
【代码审查完成】

审查结果：
代码整体质量较好，建议添加异常处理和单元测试...

查看详细日志 →
```

---

## 🛠️ 本地开发

### 克隆项目

```bash
git clone https://github.com/XXXlG/open-ai-code-review.git
cd open-ai-code-review
```

### 构建项目

```bash
mvn clean install
```

### 本地运行

```bash
# 设置环境变量
export AIHUBMIX_API_KEY="your_api_key"
export MY_GITHUB_TOKEN="your_github_token"
export GITHUB_REVIEW_LOG_URI="https://github.com/YOUR_USERNAME/-open-ai-code-review-log.git"
export MY_GITHUB_USERNAME="your_username"

# 运行
java -jar open-ai-code-review-sdk/target/open-ai-code-review-sdk-1.0.jar
```

### 运行测试

```bash
mvn test
```

---

## 📦 模块说明

### open-ai-code-review-sdk

核心SDK模块，包含：
- **OpenAiCodeReview**：主程序入口
- **GeminiApiClient**：Gemini API客户端
- **FeishuNotifier**：飞书消息通知工具
- **FeishuConfig**：飞书配置模型

### open-ai-code-review-test

测试模块，用于SDK功能验证。

---

## 🔧 故障排查

### 常见问题

#### 1. GitHub Actions执行失败

**问题**：`git diff` 返回空内容

**解决**：
```yaml
- name: Checkout repository
  uses: actions/checkout@v2
  with:
    fetch-depth: 2  # 确保获取足够的提交历史
```

#### 2. 飞书通知发送失败

**问题**：`NoClassDefFoundError: com/google/gson/annotations/SerializedName`

**解决**：确保pom.xml中包含了所有依赖，参考：[NoClassDefFoundError问题解决](.kiro/outputs/NoClassDefFoundError问题解决.md)

#### 3. API调用失败

**问题**：`401 Unauthorized`

**解决**：
- 检查`AIHUBMIX_API_KEY`是否正确
- 确认API Key是否过期

#### 4. 日志仓库推送失败

**问题**：`Permission denied`

**解决**：
- 检查`MY_GITHUB_TOKEN`权限是否包含`repo`
- 确认日志仓库是否存在

---

## 📖 更多文档

- [飞书告警快速开始](.kiro/outputs/飞书告警快速开始.md)
- [飞书告警功能开发总结](.kiro/outputs/飞书告警功能开发总结.md)
- [Gemini API调用工具类开发总结](.kiro/outputs/Gemini-API调用工具类开发总结.md)
- [Git敏感信息清除完整教程](.kiro/outputs/Git敏感信息清除完整教程.md)
- [NoClassDefFoundError问题解决](.kiro/outputs/NoClassDefFoundError问题解决.md)

---

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

### 提交代码

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

### 提交规范

遵循[约定式提交](https://www.conventionalcommits.org/zh-hans/)规范：

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链相关

---

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 🙏 致谢

- [Gemini API](https://aihubmix.com/) - 提供AI代码审查能力
- [飞书开放平台](https://open.feishu.cn/) - 提供消息通知能力
- [GitHub Actions](https://github.com/features/actions) - 提供CI/CD能力

---

## 📞 联系方式

- **作者**：XXXlG
- **邮箱**：18561144539@163.com
- **GitHub**：[@XXXlG](https://github.com/XXXlG)

---

<div align="center">

**如果这个项目对你有帮助，请给个⭐Star支持一下！**

Made with ❤️ by XXXlG

</div>
