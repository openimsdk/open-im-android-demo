<p align="center">
    <a href="https://openim.io">
        <img src="./docs/images/logo.jpg" width="60%" height="30%"/>
    </a>
</p>

# OpenIM Android 💬💻

<p>
  <a href="https://docs.openim.io/">OpenIM Docs</a>
  •
  <a href="https://github.com/openimsdk/open-im-server">OpenIM Server</a>
  •
  <a href="https://github.com/openimsdk/open-im-sdk-android">openim-sdk-android</a>
  •
  <a href="https://github.com/openimsdk/openim-sdk-core">openim-sdk-core</a>
</p>

OpenIM 为开发者提供开源即时通讯 (IM) SDK，作为 Twilio 和 Sendbird 等云服务的替代解决方案。借助 OpenIM，开发者可以构建类似微信的安全可靠的 IM 应用程序。

本仓库基于 OpenIM SDK 的开源版本。您可以将此Demo用作 OpenIM SDK 的参考实现。引用了 `@openim/android-client-sdk` 和 `@openim/core-sdk`来创建原生 Android 应用程序。

<p align="center">
   <img src="./docs/images/preview1.png" alt="Preview" width="40%"/>
   <span style="display: inline-block; width: 16px;"></span>
   <img src="./docs/images/preview2.png" alt="Preview" width="40%"/>
</p>

## 授权许可 📄

仓库采用 GNU Affero 通用公共许可证第 3 版 (AGPL-3.0) 进行许可，并受以下附加条款的约束。 **不允许用于商业用途**。详情请参阅[此处](./LICENSE).

## 开发环境

在开始开发之前，请确保您的系统已安装以下软件：

- **操作系统**: Windows 11 22H2
- **Android Studio**: Android Studio Koala | 2024.1.1
- **Gradle**: 7.5.1
- **AGP**: 7.4.2
- **Java Runtime**: 17.0.10

同时你需要确保已经部署了最新版本的 [OpenIM Server](https://docs.openim.io/guides/gettingStarted/dockerCompose)。 之后，你可以编译项目并连接自己的服务端用于运行及测试。

## 运行环境 

该Demo已通过以下环境的运行测试:

| 系统        | 版本                  | 状态   |
| ----------- | -------------------- | ------ |
| **Android** | 7.0-14.0             | ✅     |

### 说明

- **Gradle&AGP**: 我们建议版本与上述说明中的版本保持一致，如果需要升级则需要自行适配。

## 快速开始

根据下面的步骤来设置并运行此项目:

1. 拉取本仓库

   ```bash
   git clone https://github.com/openimsdk/open-im-android-demo.git
   cd open-im-android-demo
   ```

2. 打开并拉取依赖
   - 用Android Studio打开项目根目录的Demo文件夹作为Android项目的App目录 
   
      > 请注意打开的是Demo文件夹，而不是直接打开项目根目录。 我们建议你先通过 [官方文档](https://developer.android.com/studio/projects) 来了解Android项目的构成及项目中不同文件的作用。

3. 修改配置

   - 修改 `Constants.java` 类:

     > 部署 [OpenIM Server](https://docs.openim.io/guides/gettingStarted/dockerCompose) 后, 你需要修改 [该文件](./OUIKit/OUICore/src/main/java/io/openim/android/ouicore/utils/Constants.java) 来确保项目的正确运行。

     ```java
     public static final String DEFAULT_HOST = "your-server-ip or your-domain"
     ```

4. 如果你在Server部署中修改了端口设置, 你需要修改如下设置。 如果没有修改, 则保持默认即可。
   ```java
   private static final String APP_AUTH = "http://" + DEFAULT_HOST + ":10008/";
   private static final String IM_API = "http://" + DEFAULT_HOST + ":10002";
   private static final String IM_WS = "ws://" + DEFAULT_HOST + ":10001";
   ```

5. 之后便可以运行Demo并针对性的进行测试了 🎉

## 音视频通话

开源版支持一对一音视频通话，并且需要先部署并配置 [音视频服务端](https://github.com/openimsdk/chat/blob/main/HOW_TO_SETUP_LIVEKIT_SERVER.md)。多人音视频通话、视频会议请联系邮箱 [contact@openim.io](mailto:contact@openim.io)。

## Build 🚀

> 该项目允许构建Android应用程序。

1. 执行 assemble 来构建Demo程序

```bash
gradlew assemble
```

2. 生成的程序将位于 `build/output` 目录下。

## 功能列表

### 说明

| 功能模块           | 功能项                                                    | 状态 |
| ------------------| --------------------------------------------------------- | ---- |
| **账号功能**       | 手机号注册\邮箱注册\验证码登录                              | ✅   |
|                   | 个人信息查看\修改                                          | ✅   |
|                   | 多语言设置                                                 | ✅   |
|                   | 修改密码\忘记密码                                          | ✅   |
| **好友功能**       | 查找\申请\搜索\添加\删除好友                               | ✅   |
|                   | 同意\拒绝好友申请                                          | ✅   |
|                   | 好友备注                                                  | ✅   |
|                   | 是否允许添加好友                                           | ✅   |
|                   | 好友列表\好友资料实时同步                                   | ✅   |
| **黑名单功能**     | 限制消息                                                  | ✅   |
|                   | 黑名单列表实时同步                                         | ✅   |
|                   | 添加\移出黑名单                                            | ✅   |
| **群组功能**       | 创建\解散群组                                             | ✅   |
|                   | 申请加群\邀请加群\退出群组\移除群成员                       | ✅   |
|                   | 群名/群头像更改/群资料变更通知和实时同步                  | ✅   |
|                   | 群成员邀请进群                                            | ✅   |
|                   | 群主转让                                                  | ✅   |
|                   | 群主、管理员同意进群申请                                  | ✅   |
|                   | 搜索群成员                                                | ✅   |
| **消息功能**       | 离线消息                                                  | ✅   |
|                   | 漫游消息                                                  | ✅   |
|                   | 多端消息                                                  | ✅   |
|                   | 历史消息                                                  | ✅   |
|                   | 消息删除                                                  | ✅   |
|                   | 消息清空                                                  | ✅   |
|                   | 消息复制                                                  | ✅   |
|                   | 单聊正在输入                                              | ✅   |
|                   | 新消息勿扰                                                | ✅   |
|                   | 清空聊天记录                                              | ✅   |
|                   | 新成员查看群聊历史消息                                    | ✅   |
|                   | 新消息提示                                                | ✅   |
|                   | 文本消息                                                  | ✅   |
|                   | 图片消息                                                  | ✅   |
|                   | 视频消息                                                  | ✅   |
|                   | 表情消息                                                  | ✅   |
|                   | 文件消息                                                  | ✅   |
|                   | 语音消息                                                  | ✅   |
|                   | 名片消息                                                  | ✅   |
|                   | 地理位置消息                                              | ✅   |
|                   | 自定义消息                                                | ✅   |
| **会话功能**       | 置顶会话                                                  | ✅   |
|                   | 会话已读                                                  | ✅   |
|                   | 会话免打扰                                                | ✅   |
| **REST API**      | 认证管理                                                  | ✅   |
|                   | 用户管理                                                  | ✅   |
|                   | 关系链管理                                                | ✅   |
|                   | 群组管理                                                  | ✅   |
|                   | 会话管理                                                  | ✅   |
|                   | 消息管理                                                  | ✅   |
| **Webhook**       | 群组回调                                                  | ✅   |
|                   | 消息回调                                                  | ✅   |
|                   | 推送回调                                                  | ✅   |
|                   | 关系链回调                                                | ✅   |
|                   | 用户回调                                                  | ✅   |
| **容量和性能**     | 1 万好友                                                  | ✅   |
|                   | 10 万人大群                                               | ✅   |
|                   | 秒级同步                                                  | ✅   |
|                   | 集群部署                                                  | ✅   |
|                   | 互踢策略                                                  | ✅   |
| **在线状态**       | 所有平台不互踢                                            | ✅   |
|                   | 每个平台各只能登录一个设备                                | ✅   |
|                   | PC 端、移动端、Pad 端、Web 端、小程序端各只能登录一个设备 | ✅   |
|                   | PC 端不互踢，其他平台总计一个设备                         | ✅   |
| **音视频通话**     | 一对一音视频通话                                          | ✅   |
| **文件类对象存储** | 支持私有化部署 minio                                      | ✅   |
|                   | 支持 COS、OSS、Kodo、S3 公有云                            | ✅   |
| **推送**          | 消息在线实时推送                                          | ✅   |
|                   | 消息离线推送，支持个推，Firebase                          | ✅   |

更多高级功能、音视频通话、视频会议 请联系邮箱 [contact@openim.io](mailto:contact@openim.io)

## 加入社区 👥:

- 🚀 [加入我们的 Slack 社区](https://join.slack.com/t/openimsdk/shared_invite/zt-22720d66b-o_FvKxMTGXtcnnnHiMqe9Q)
- 👀 [加入我们的微信群](https://openim-1253691595.cos.ap-nanjing.myqcloud.com/WechatIMG20.jpeg)

## 常见问题
1. **Demo中如何使用个推及FCM来进行离线消息推送？**

   A: 修改项目的变体来使用不同的推送SDK。FCMDebug/Release变体接入了FCM的推送，其余变体则为个推。

2. **为什么我在切换变体后DataBinding文件标红了？**
   
   A: 切换变体后重新执行sync来使Android Studio来重新索引自动生成的binding文件。

3. **如何解决类似“不支持的类文件”与“类文件具有错误的版本”等类似的问题？**

   A: 请先确保Android Studio项目设置中的JRE版本为17.0.10.
