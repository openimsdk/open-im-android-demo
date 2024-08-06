<p align="center">
    <a href="https://www.openim.online">
        <img src="./openim-logo.gif" width="60%" height="30%"/>
    </a>
</p>

# OpenIM Android Demo for Jetpack Compose 💬💻

# 注意：这个项目更新缓慢
# MVI, Jetpack Compose, Kotlin, Coroutines, Navigation, Kotlinx-serialization
# 以下是原README.md内容

<p>
  <a href="https://doc.rentsoft.cn/">OpenIM Docs</a>
  •
  <a href="https://github.com/openimsdk/open-im-server">OpenIM Server</a>
  •
  <a href="https://github.com/openimsdk/openim-sdk-core">openim-sdk-core</a>
  •
  <a href="https://github.com/openimsdk/open-im-sdk-android">open-im-sdk-android</a>

</p>

<br>
Demo is a set of UI components implemented based on the Open-IM SDK, which includes functionalities such as conversations, chats, relationships, and groups. The project adopts MVVM+modular development, with high decoupling, independent business logic, code separation, making it easy to integrate the functionalities you need quickly and conveniently

## Tech Stack 🛠️
- This is a Android project
- Using Java/kotlin
- App is built with [open-im-sdk-android](https://github.com/openimsdk/open-im-sdk-android) library.

## Official demo use

- Download the experience app

  ![Android](https://www.pgyer.com/app/qrcode/OpenIM-Enterprise)

## Dev Setup 🛠️
- Android Studio 3.6.1 or above
- Gradle-5.1.1 or above
- Android Gradle Plugin Version-3.4.0 or above
- android x library
### Development Configuration
```
minSdk     : 21
targetSdk  : 32
compileSdk : 32
abiFilters : ['armeabi-v7a','arm64-v8a']
```
## Build 🚀
1.git clone：
```
git clone https://github.com/OpenIMSDK/Open-IM-Android-Demo.git
```
2.Importing the project

Place the Demo and UIkit folders in the same directory, and then import demo/app to get started

3.Configuration of modules

In the app/config.gradle file
```
ext {
    //Module standalone running is set to true
    isModule=false

    //android config
    androidConfig = [
            minSdk     : 21,
            targetSdk  : 32,
            compileSdk : 32,
            versionCode: 2,
            versionName: "1.0.2",
            abiFilters : ['armeabi-v7a', 'arm64-v8a']
    ]
    //The ID of the module
    applicationId = [
            "app" : "io.openim.android.demo",
            "OUIConversation" :"io.openim.android.ouiconversation",
            "OUIGroup" : "io.openim.android.ouigroup",
            "OUIContact" : "io.openim.android.ouicontact",
            "OUICalling" : "io.openim.android.ouicalling",
    ]

}
```
In the app/build.gradle file
```
    api project(':OUICore')
    if (!isModule) {
        implementation project(':OUIConversation')
        implementation project(':OUIGroup')
        implementation project(':OUIContact')
	//Disabling a module will remove all functionalities associated with that module
       //implementation project(':OUICalling')
    }
```
### Issues :bookmark_tabs:
1. Reminder: If you encounter the "resource loading is not complete" error when calling SDK-related APIs, make sure to call other APIs after the login callback is executed.   
2. After disabling or adding a module, if the app shows an error toast when calling the functionalities of that module, you can resolve the issue by uninstalling and reinstalling the app.  
3. Reminder: Avoid using duplicate names for resources across different modules.  
4. Some ViewModels need to be globally shared. Developers should pay attention to whether ViewModels are cached in viewModels and release them in a timely manner when they are no longer in use to avoid memory leaks.


## Community :busts_in_silhouette:

- 📚 [OpenIM Community](https://github.com/OpenIMSDK/community)
- 💕 [OpenIM Interest Group](https://github.com/Openim-sigs)
- 🚀 [Join our Slack community](https://join.slack.com/t/openimsdk/shared_invite/zt-22720d66b-o_FvKxMTGXtcnnnHiMqe9Q)
- :eyes: [Join our wechat (微信群)](https://openim-1253691595.cos.ap-nanjing.myqcloud.com/WechatIMG20.jpeg)

## Community Meetings :calendar:

We want anyone to get involved in our community and contributing code, we offer gifts and rewards, and we welcome you to join us every Thursday night.

Our conference is in the [OpenIM Slack](https://join.slack.com/t/openimsdk/shared_invite/zt-22720d66b-o_FvKxMTGXtcnnnHiMqe9Q) 🎯, then you can search the Open-IM-Server pipeline to join

We take notes of each [biweekly meeting](https://github.com/orgs/OpenIMSDK/discussions/categories/meeting) in [GitHub discussions](https://github.com/openimsdk/open-im-server/discussions/categories/meeting), Our historical meeting notes, as well as replays of the meetings are available at [Google Docs :bookmark_tabs:](https://docs.google.com/document/d/1nx8MDpuG74NASx081JcCpxPgDITNTpIIos0DS6Vr9GU/edit?usp=sharing).

## Who are using OpenIM :eyes:

Check out our [user case studies](https://github.com/OpenIMSDK/community/blob/main/ADOPTERS.md) page for a list of the project users. Don't hesitate to leave a [📝comment](https://github.com/openimsdk/open-im-server/issues/379) and share your use case.

## License :page_facing_up:

OpenIM is licensed under the Apache 2.0 license. See [LICENSE](https://github.com/openimsdk/open-im-server/tree/main/LICENSE) for the full license text.
