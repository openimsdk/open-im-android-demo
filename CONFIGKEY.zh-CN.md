
# 常见功能配置指南

- [离线推送功能](#离线推送功能)
- [地图功能](#地图功能)

## 离线推送功能

在开始离线推送配置之前，需要切换不同的变体来使用不同的SDK。对于Android变体的相关信息可以阅读[官方文档](https://developer.android.com/build/build-variants)。

### 客户端配置

#### 1. 中国大陆地区使用个推（Getui）

> 切换项目变体到Getui，项目默认配置为GetuiDebug和GetuiRelease

##### 集成指南

根据[文档](https://docs.getui.com/getui/mobile/android/overview/)做好相应的配置，注意[多厂商](https://docs.getui.com/getui/mobile/vendor/vendor_open/)配置。然后修改以下文件内容：

- **[build.gradle](./Demo/app/build.gradle)**

```gradle
  manifestPlaceholders = [
      GETUI_APPID    : "",
      XIAOMI_APP_ID  : "",
      XIAOMI_APP_KEY : "",
      MEIZU_APP_ID   : "",
      MEIZU_APP_KEY  : "",
      HUAWEI_APP_ID  : "",
      OPPO_APP_KEY   : "",
      OPPO_APP_SECRET: "",
      VIVO_APP_ID    : "",
      VIVO_APP_KEY   : "",
      HONOR_APP_ID   : "",
  ]
```

#### 2. 海外地区使用 [FCM（Firebase Cloud Messaging）](https://firebase.google.com/docs/cloud-messaging)

> 切换项目变体到FCM，项目默认配置为FCMDebug和FCMRelease

根据 [FCM](https://firebase.google.com/docs/cloud-messaging) 的集成指南，替换以下文件：

- **[google-services.json](./Demo/app/google-services.json)** 

### 离线推送横幅设置

目前SDK的设计是直接由客户端控制推送横幅的展示内容。发送消息时，设置入参[offlinePushInfo](https://github.com/openimsdk/open-im-android-demo/blob/main/OUIKit/OUIConversation/src/main/java/io/openim/android/ouiconversation/vm/ChatVM.java#L864)，如果需要推送转发的消息则需要修改这里的[offlinePushInfo](https://github.com/openimsdk/open-im-android-demo/blob/main/OUIKit/OUIConversation/src/main/java/io/openim/android/ouiconversation/vm/ChatVM.java#L907)：

```java
  OfflinePushInfo offlinePushInfo = new OfflinePushInfo();
  offlinePushInfo.setTitle("OfflineMessageTitle");
  offlinePushInfo.setDesc("OfflineMessageDesc");
```

根据实际需求，完成对应的客户端和服务端配置后即可启用离线推送功能。

---

## 地图功能

### 配置指南

Android项目的位置消息是通过腾讯位置服务来实现的，所以需要重新配置项目中的**mapAppKey**和**mapBackUrl**。我们强烈建议您在实际使用之前先去阅读[官方文档](https://lbs.qq.com/webApi/component/componentGuide/componentPicker)

- **[mapAppKey](https://github.com/openimsdk/open-im-android-demo/blob/main/OUIKit/OUICore/src/main/java/io/openim/android/ouicore/widget/WebViewActivity.java#L50)**
- **[mapBackUrl](https://github.com/openimsdk/open-im-android-demo/blob/main/OUIKit/OUICore/src/main/java/io/openim/android/ouicore/widget/WebViewActivity.java#L51)**

```java
  public static String mapAppKey = "",
        mapBackUrl = "http://callback";
```

完成配置后即可启用地图功能。
