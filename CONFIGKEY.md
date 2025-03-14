
# Common Function Configuration Guide

- [Offline push](#offlinepush)
- [Map](#map)

## Offlinepush

In Android Demo, if you wanna to config the offline push, you need to change the demo's **variant** to use different SDK. The information about variants can read it by [offical doc](https://developer.android.com/build/build-variants). 

### Client configuration

#### 1. Use Getui (https://getui.com/) in mainland China

> Change variant to Getui, default configuration like GetuiDebug/GetuiRelease.

##### The Configure in the integration guide of Getui

**The configuration:**
According to [its documentation](https://docs.getui.com/getui/mobile/android/overview/), make corresponding configurations, and pay attention to [multi-vendor](https://docs.getui.com/getui/mobile/vendor/vendor_open/) configurations. Then modify the following file contents:

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

#### 2. Use [FCM (Firebase Cloud Messaging)](https://firebase.google.com/docs/cloud-messaging) in overseas regions

> Change variant to FCM, default configuration like FCMDebug/FCMRelease.

According to the integration guide of [FCM](https://firebase.google.com/docs/cloud-messaging), replace the following files:

- **[google-services.json](./Demo/app/google-services.json)** 

### Offline push banner settings

Currently, the SDK is designed to directly control the display content of the push banner by the client. When sending a message, set the input parameter [offlinePushInfo](https://github.com/openimsdk/open-im-android-demo/blob/main/OUIKit/OUIConversation/src/main/java/io/openim/android/ouiconversation/vm/ChatVM.java#L864). If is the forwarding message, need to modify [this](https://github.com/openimsdk/open-im-android-demo/blob/main/OUIKit/OUIConversation/src/main/java/io/openim/android/ouiconversation/vm/ChatVM.java#L907):

```java
  OfflinePushInfo offlinePushInfo = new OfflinePushInfo();
  offlinePushInfo.setTitle("OfflineMessageTitle");
  offlinePushInfo.setDesc("OfflineMessageDesc");
```

According to actual needs, you can enable the offline push function after completing the corresponding client and server configurations.

---

## Map

### Configuration Guide

The Android Demo send location messages by Tencent Location Services. So you need to config the **mapAppKey** and **mapBackUrl**. You'd better read [offical doc](https://lbs.qq.com/webApi/component/componentGuide/componentPicker) first. 

- **[mapAppKey](https://github.com/openimsdk/open-im-android-demo/blob/main/OUIKit/OUICore/src/main/java/io/openim/android/ouicore/widget/WebViewActivity.java#L50)**
- **[mapBackUrl](https://github.com/openimsdk/open-im-android-demo/blob/main/OUIKit/OUICore/src/main/java/io/openim/android/ouicore/widget/WebViewActivity.java#L51)**

```java
  public static String mapAppKey = "",
        mapBackUrl = "http://callback";
```

Once the configuration is complete, you can enable the map function.