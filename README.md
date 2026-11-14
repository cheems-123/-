# 智记 MindNotes · 学习笔记系统

面向课程学习的多格式笔记平台，核心能力：**AI 整理摘要 + 贴吧式共享补充 + 按课程筛选与阅读量/点赞量排序推荐**。
技术栈：**Java（Spring Boot）+ MySQL 8.0 + 静态 HTML / CSS / 原生 JavaScript（fetch AJAX）**。

> 对应需求看板：ZHI-1 系统总项；ZHI-2 Sprint1 基础版本；SB-01 框架落地与数据适配；SB-02 注册登录；SB-03 笔记上传与我的笔记；SB-04 课程浏览、列表与详情；SB-05 AI 笔记整理；SB-06 联调测试与演示。

---

## 一、功能清单

| 模块 | 说明 | 对应需求 |
|---|---|---|
| 注册 / 登录 | 用户名密码注册，BCrypt 哈希；HttpSession 登录态；未登录访问写操作返回 401 | SB-02 |
| 课程分类 | 内置 6 门课程字典，笔记按课程归属，首页可按课程筛选 | SB-04 |
| 多格式笔记发布 | 一篇笔记由若干片段组成，支持 **文字 TEXT / 图片 IMAGE / 音频 AUDIO** 混排，按序展示 | SB-03 |
| 我的笔记 | 查看本人全部笔记、切换公开/私有、删除、手动重新 AI 整理 | SB-03 |
| 笔记列表与详情 | 公开笔记列表；详情阅读；阅读量、点赞量累计；详情防私有越权 | SB-04 |
| 筛选与排序推荐 | 按课程过滤 + 关键词搜索；**排序：最新 / 阅读量高 / 点赞高** | SB-04 |
| AI 笔记整理 | 保存后自动生成**一句话摘要、关键词、结构化要点**；详情侧栏展示，便于快速选择阅读；作者可一键重新整理 | SB-05 |
| 贴吧式共享补充 | 公开笔记下发表补充（文字/图片/音频）、楼中楼回复、补充点赞，在原作基础上共同完善 | 扩展功能 |
| AI 模型预留 | `application.yml` 可配置 OpenAI 兼容大模型接口；未配置/调用失败自动回退内置规则引擎，离线即可演示 | SB-05 |

## 二、技术架构

```
浏览器(静态 HTML + fetch/AJAX JSON)
        │  HTTP / JSON
Spring Boot 2.7.18（内嵌 Tomcat 9，javax.servlet）
 ├─ controller  REST 接口
 ├─ service     业务 / AI 编排 / 文件存储
 ├─ dao          JdbcTemplate（无重型 ORM，便于讲解 SQL）
 ├─ model        实体与 DTO
 └─ interceptor 登录拦截
        │  JDBC
MySQL 8.0（库 zhi_mindnotes，utf8mb4 / InnoDB，8 张表）
```

- 后端：Spring Boot Web、Spring JDBC、spring-security-crypto（仅 BCrypt）、MySQL Connector/J 8.x
- 前端：`src/main/resources/static/` 下 6 个纯 HTML 页面 + `css/style.css` + `js/common.js`
- AI：`service/ai/AiOrganizeService` → `LlmAiAdapter`（预留大模型）+ `RuleAiOrganizer`（离线规则：词频关键词、关键句打分摘要、标记句要点）

## 三、环境准备

| 软件 | 版本要求 | 说明 |
|---|---|---|
| JDK | 17 及以上（本机已检测到 Java 19） | `JAVA_HOME` 配置正确，命令行 `java -version` 正常 |
| IntelliJ IDEA | 2025.3.4（任意支持 Maven 的版本均可） | 使用 IDEA **内置 Maven** 构建，无需单独安装 mvn |
| MySQL | 8.0（MySQL 8.0 Command Line Client / Navicat Premium 16 均可操作） | 服务已启动，记下 root 密码 |
| 浏览器 | Chrome / Edge | 前端页面 |

## 四、数据库初始化（二选一）

### 方式 A：Navicat Premium 16（推荐可视化）
1. 打开 Navicat，连接本机 MySQL（`localhost:3306`，root + 你的密码）。
2. 依次新建查询并运行：先 `sql/schema.sql`（建库建表），再 `sql/data.sql`（课程字典；可选——应用启动也会自动补齐课程与演示数据）。
3. 刷新后应看到 `zhi_mindnotes` 库与 8 张表。

### 方式 B：MySQL 8.0 Command Line Client
```sql
-- 登录后执行（把 <项目根目录> 换成你的实际路径，注意使用正斜杠）
source <项目根目录>/sql/schema.sql;
source <项目根目录>/sql/data.sql;
```

> 表清单：`user`、`course`、`note`、`note_segment`、`note_like`、`contribution`、`contribution_like`、`ai_log`。

## 五、IDEA 启动步骤

1. **不要打开 `src` 子目录**（务必选择含 `pom.xml` 的 `zhi-mindnotes` 根目录）：`File → Open` 选择本工程根目录 `zhi-mindnotes`，IDEA 会识别 `pom.xml` 并作为 Maven 项目导入；若已误开 `src`，先 `File → Close Project`，再按上述方式重开，等待右下角 Maven 依赖下载完成（需联网拉取 Spring Boot 依赖）。
2. 配置数据库密码：仓库中不保存明文密码，启动前先设置环境变量 `DB_PASSWORD`（命令行执行 `setx DB_PASSWORD 你本机MySQL的root密码`，重启 IDEA 生效）；账号默认 `root`，可用 `DB_USERNAME` 覆盖。
3. 找到启动类 `src/main/java/com/zhmindnotes/ZhMindNotesApplication.java`，右键 `Run`。
4. 控制台出现 `Started ZhMindNotesApplication` 即启动成功，访问 <http://localhost:8080/index.html>。
5. 首次启动会自动幂等写入：演示账号、6 门课程、3 篇示例笔记（2 公开含文字/图片/音频片段，1 私有），重复启动不会重复插入。

### 演示账号（首次启动自动创建，密码均为 `123456`）
| 用户名 | 密码 | 说明 |
|---|---|---|
| admin | 123456 | 课程管理员，拥有两篇公开示例笔记 |
| student | 123456 | 学习委员，拥有一篇私有示例笔记；也可自行注册新账号体验发布 |

## 六、功能操作指引

1. **浏览推荐**：首页切换「最新发布 / 阅读量高 / 点赞高」，点击课程 chip 按课程过滤，输入关键词搜索标题、摘要、关键词。
2. **发布笔记**：登录后进入「发布笔记」，填写标题、选择课程、选择共享方式；通过「+ 文字 / 图片 / 音频」添加片段；图片、音频选择本地文件上传；保存后自动完成 AI 整理。
3. **阅读与 AI 摘要**：进入详情，右侧侧栏展示 AI 摘要、关键词、要点；作者可见「重新 AI 整理」。
4. **点赞 / 阅读量**：详情页点赞切换；每次进入详情阅读量 +1。
5. **贴吧补充**：公开笔记详情下方可发表文字/图片/音频补充，可对补充点赞、楼中楼回复，形成共同完善的知识楼。
6. **我的笔记**：查看、设为公开/私有、重新整理、删除。

## 七、AI 整理配置（预留大模型接口）

默认 `ai.enabled=false`，使用内置规则引擎离线整理，无需密钥即可完整演示。
如需接入真实大模型（OpenAI 兼容协议，如 OpenAI / DeepSeek / 通义兼容接口等）：

```yaml
ai:
  enabled: true
  base-url: https://api.openai.com/v1   # 换成服务商地址
  api-key: "sk-xxxxxx"
  model: gpt-4o-mini
  timeout-seconds: 15
```

调用成功使用 `LLM` 引擎；若未配置、密钥为空或调用超时/报错，自动回退 `RULE` 规则引擎，保证主流程不中断；每次整理会在 `ai_log` 记录引擎来源与结果快照。

## 八、主要接口（REST，前缀 /api）

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | /api/auth/register | 否 | 注册 |
| POST | /api/auth/login | 否 | 登录，写 Session |
| POST | /api/auth/logout | 是 | 退出 |
| GET  | /api/auth/me | 否 | 当前登录用户 |
| GET  | /api/courses | 否 | 课程列表 |
| GET  | /api/notes?courseId=&sort=views\|likes\|newest&keyword= | 否 | 公开笔记列表与排序 |
| GET  | /api/notes/mine | 是 | 我的笔记 |
| GET  | /api/notes/{id} | 否(私有限作者) | 详情，阅读量+1 |
| POST | /api/notes | 是 | 发布并触发 AI 整理 |
| POST | /api/notes/{id}/organize | 是(作者) | 重新 AI 整理 |
| POST | /api/notes/{id}/like | 是 | 点赞切换 |
| PUT  | /api/notes/{id}/share | 是(作者) | 公开/私有切换 |
| DELETE | /api/notes/{id} | 是(作者) | 删除笔记及关联数据 |
| GET  | /api/notes/{id}/contributions | 否 | 补充楼层树 |
| POST | /api/notes/{id}/contributions | 是 | 发表补充/回复 |
| POST | /api/contributions/{id}/like | 是 | 补充点赞切换 |
| POST | /api/files/upload?kind=image\|audio | 是 | 上传图片/音频 |

## 九、目录结构

```
zhi-mindnotes/
├─ pom.xml
├─ README.md
├─ sql/
│  ├─ schema.sql          # 建库建表
│  └─ data.sql            # 课程字典（可选，应用启动自动补齐）
└─ src/main/
   ├─ resources/
   │  ├─ application.yml
   │  └─ static/          # 前端页面与样式脚本
   │     ├─ index.html / login.html / register.html
   │     ├─ noteDetail.html / publish.html / myNotes.html
   │     ├─ css/style.css
   │     └─ js/common.js
   └─ java/com/zhmindnotes/
      ├─ ZhMindNotesApplication.java
      ├─ common/  config/  controller/  controller/dto/
      ├─ dao/  model/  interceptor/
      └─ service/  service/ai/
```

## 十、常见问题

1. **启动报 Access denied for user**：环境变量 `DB_PASSWORD` 未设置或与本机 MySQL 不符，按第五、十章说明重新设置后重启。
2. **Unknown database 'zhi_mindnotes'**：先执行 `schema.sql` 建库。
3. **图片/音频不显示**：文件保存在运行目录 `./uploads/`，由 `/uploads/**` 静态映射提供；请勿删除运行目录下该文件夹。
4. **Maven 依赖下载慢/失败**：可在 IDEA `Settings → Build → Maven` 配置阿里云镜像后刷新；本工程不使用本机命令行 mvn。
5. **端口占用**：修改 `application.yml` 的 `server.port`。
6. **隐私与边界**：密码仅存 BCrypt 哈希，接口不返回密码；私有笔记他人直接访问 ID 返回 403。
7. **报「程序包 javax.servlet / com.fasterxml.jackson.databind / org.springframework.web.servlet 不存在」**：这不是代码问题，而是 IDEA 没有把工程当作 Maven 项目（模块里没有任何依赖库）。
   处理：`File → Close Project` → `File → Open` 选择根目录 `zhi-mindnotes`（含 `pom.xml`）→ 若弹出 `Maven project detected, load it?` 点 `Load`（或右键 `pom.xml` → `Add as Maven Project`）→ 等 `Maven` 面板的依赖下载完成后 `Build → Rebuild Project`。
8. **不用改 yml 也能换数据库密码**：`application.yml` 只读取环境变量：`${DB_USERNAME:root}`、`${DB_PASSWORD:}`（默认空值，因此启动前必须设置密码）。命令行执行 `setx DB_PASSWORD 你的密码` 后重启 IDEA 即可；密码不写入仓库，避免公开泄露。
9. **只想用命令行构建**：本机未安装独立 Maven，可用 IDEA 自带 Maven（无需下载安装）：
   ```powershell
   & "<IDEA 安装目录>\plugins\maven\lib\maven3\bin\mvn.cmd" -f .\pom.xml clean package -DskipTests
   ```
   本工程已验证该命令可成功编译 39 个源文件。
10. **打开首页就被弹到登录页（游客无法浏览）**：已修复。原因是 `GET /api/auth/me` 对未登录访客返回 401，前端公共请求把 401 统一跳转登录页，导致首页"打不开"。现 `/api/auth/me` 对游客返回 `code=0, data=null`，并在拦截器放行，游客可正常浏览首页、详情；只有写操作（发布、点赞、补充）才要求登录。
11. **页面顶部出现红色横幅「无法连接后端服务」**：说明当前页面不是由 Spring Boot 提供。常见于双击 `index.html`（`file://` 协议）或用 IDEA 内置预览（`63342` 端口）。请运行启动类后，在浏览器访问 `http://localhost:8080/index.html`；页面已内置该提示，无需再猜。
12. **改了代码要重启**：IDEA 中 `Run` 运行的是编译后的 `target/classes`，改完 Java/HTML/JS 后请点 `Ctrl+F5`（或停止后重新 `Run`）；只改 HTML/CSS/JS 可先在 IDEA 中 `Build → Rebuild Project`，再重启即可。
13. **启动后自动打开浏览器**：已内置。应用启动完成（种子数据写完后）会自动用默认浏览器打开 `http://localhost:8080/index.html`，控制台会打印 `已在默认浏览器中打开首页` / `已通过系统命令打开首页`。不想自动打开时，把 `application.yml` 里的 `app.auto-open-browser` 改成 `false`（或启动参数加 `--app.auto-open-browser=false`）。
14. **不想用 IDEA、想双击就启动**：工程根目录有 `启动.bat`，双击即可运行 `target\zhi-mindnotes-1.0.0.jar` 并自动开网页；前提是先 `package` 出 jar（IDEA 里 `Maven → package` 或执行第 9 条命令）。
