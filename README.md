## For educational purposes only. Commercial use is strictly prohibited without authorization. Violators will be prosecuted
## Open-IM-Android-Demo
<img src="https://github.com/OpenIMSDK/OpenIM-Docs/blob/main/docs/images/WechatIMG20.jpeg" alt="image" style="width: 350px; " />

#### Demo content
Demo is a set of UI components implemented based on the Open-IM SDK, which includes functionalities such as conversations, chats, relationships, and groups. The project adopts MVVM+modular development, with high decoupling, independent business logic, code separation, making it easy to integrate the functionalities you need quickly and conveniently
#### Download and experience

![Android](https://www.pgyer.com/app/qrcode/OpenIM-Android)

#### Dependency Description

```
 implementation project(':OUICore') required modules
 implementation project(':OUIConversation') Session-related modules
 implementation project(':OUIGroup') Group-related modules
 implementation project(':OUIContact') Contact-related modules
```
#### Development Environment Requirements
```
Android Studio 3.6.1 or above
Gradle-5.1.1 or above
Android Gradle Plugin Version-3.4.0 or above
android x library
```
#### Development Configuration
```
minSdk     : 21
targetSdk  : 32
compileSdk : 32
abiFilters : ['armeabi-v7a', 'arm64-v8a']
```
#### Start
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
            "OUIConversation" : "io.openim.android.ouiconversation",
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
4.Initialization
```
OpenIMClient.getInstance().initSDK(
    2,
    "Your IM server address",
    "Your IM server socket address",
    getStorageDir(),
    1,
    "minio",
    "",
    IMEvent.getInstance().connListener
);
```
5.Login
  
1.Log in to your business server to obtain the userID and token.  
2.Use the userID and token obtained in step 1 to log in to the IM (Instant Messaging) server.
```
public void login() {
        Parameter parameter = getParameter(null);
	//1.Log in to your business server to obtain the userID and token.
        N.API(OpenIMService.class).login(parameter.buildJsonBody())
            .compose(N.IOMain())
            .subscribe(new NetObserver<ResponseBody>(getContext()) {

                @Override
                public void onSuccess(ResponseBody o) {
                    try {
                        String body = o.string();
                        Base<LoginCertificate> loginCertificate = GsonHel.dataObject(body, LoginCertificate.class);
                        if (loginCertificate.errCode != 0) {
                            IView.err(loginCertificate.errMsg);
                            return;
                        }
			//2.Use the userID and token obtained in step 1 to log in to the IM (Instant Messaging) server.
                        OpenIMClient.getInstance().login(new OnBase<String>() {
                            @Override
                            public void onError(int code, String error) {
                                IView.err(error);
                            }

                            @Override
                            public void onSuccess(String data) {
                                //Cache login information and start a delightful chat
                   
                            }
                        }, loginCertificate.data.userID, loginCertificate.data.token);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                protected void onFailure(Throwable e) {
                    IView.err(e.getMessage());
                }
            });
    }
```

#### Common Questions
1.Reminder: If you encounter the "resource loading is not complete" error when calling SDK-related APIs, make sure to call other APIs after the login callback is executed.   
2.After disabling or adding a module, if the app shows an error toast when calling the functionalities of that module, you can resolve the issue by uninstalling and reinstalling the app.  
3.Reminder: Avoid using duplicate names for resources across different modules.  
4.Some ViewModels need to be globally shared. Developers should pay attention to whether ViewModels are cached in viewModels and release them in a timely manner when they are no longer in use to avoid memory leaks.

