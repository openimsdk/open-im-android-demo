package io.openim.android.ouicalling;

import static com.tencent.smtt.sdk.WebSettings.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.hjq.permissions.Permission;
import com.hjq.window.EasyWindow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.livekit.android.events.RoomEvent;
import io.openim.android.ouicalling.databinding.DialogCallBinding;
import io.openim.android.ouicalling.databinding.LayoutFloatViewBinding;
import io.openim.android.ouicalling.service.AudioVideoService;
import io.openim.android.ouicalling.vm.CallingVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.MediaPlayerUtil;
import io.openim.android.ouicore.utils.NotificationUtil;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;
import timber.log.Timber;

import com.tencent.smtt.export.external.interfaces.PermissionRequest;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import  com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class CallDialog extends BaseDialog {

    private static final String[] RECORD_AV_PERMISSION = new String[]{
        PermissionRequest.RESOURCE_AUDIO_CAPTURE
        , PermissionRequest.RESOURCE_VIDEO_CAPTURE
    };
    //https://www.digai.top:3000/call?callout=0&caller=3933277045&callee=4885942569
    //private static final String BaseURL = "https://www.digai.top/char?tagType=tags&tag=bible&isroot=true";
    private static final String BASEURL = "https://www.digai.top:3000/call";//callout=1&caller=lee&callee=wang";
    private String mWebURL;
    private WebView  mWebView;

    //call
    protected final HasPermissions hasShoot, hasRecord, hasSystemAlert;
    protected Context context;
    private DialogCallBinding view;
    public CallingVM callingVM;

    UserInfo mUserInfo;
    protected SignalingInfo signalingInfo;



    private boolean isSubscribe;


    public CallDialog(@NonNull Context context, CallingService callingService) {
        this(context, callingService, false);
    }

    /*
     * 弹出通话界面
     *
     * @param context        上下文
     * @param callingService 通话服务
     * @param isCallOut      是否呼出
     */
    public CallDialog(@NonNull Context context, CallingService callingService, boolean isCallOut) {
        super(context);
        this.context = context;
        hasShoot = new HasPermissions(context, Permission.CAMERA);
        hasRecord = new HasPermissions(context, Permission.RECORD_AUDIO);
        hasSystemAlert = new HasPermissions(context, Permission.SYSTEM_ALERT_WINDOW);

        callingVM = new CallingVM(callingService, isCallOut);
        callingVM.setDismissListener(v -> {
            dismiss();
        });

        initView();

    }

    private void initView() {

        Window window = getWindow();
        view = DialogCallBinding.inflate(getLayoutInflater());
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(view.getRoot());
        //背景状态栏透明
        window.setDimAmount(1f);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Common.addTypeSystemAlert(params);
        window.setAttributes(params);

        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        //view.zoomOut.setVisibility(Common.isScreenLocked() ? View.GONE : View.VISIBLE);
    }

    public void bindData(SignalingInfo signalingInfo) {
        this.signalingInfo = signalingInfo;
        callingVM.isGroup =
            signalingInfo.getInvitation().getSessionType() != ConversationType.SINGLE_CHAT;
        callingVM.setVideoCalls(Constants.MediaType.VIDEO.equals(signalingInfo.getInvitation().getMediaType()));

        callingVM.callViewModel.setCameraEnabled(callingVM.isVideoCalls);

        bindUserInfo(signalingInfo);
        listener(signalingInfo);

        initWebView();
        if (callingVM.isCallOut) {
            callingVM.signalingInviteByWeb(signalingInfo);//only notify receiver pop my CallDialog
        }
    }

    public void bindUserInfo(SignalingInfo signalingInfo) {
        try {
            ArrayList<String> ids = new ArrayList<>();
            ids.add(callingVM.isCallOut ?
                signalingInfo.getInvitation().getInviteeUserIDList().get(0) :
                signalingInfo.getInvitation().getInviterUserID());

            OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
                @Override
                public void onError(int code, String error) {
                    Toast.makeText(context, error + code, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(List<UserInfo> data) {
                    if (data.isEmpty())
                        return;
                    mUserInfo = data.get(0);
                    L.i(String.format("login user=%s:%s", mUserInfo.getUserID(), mUserInfo.getNickname()));

                    /*
                    view.avatar.load(userInfo.getFaceURL());
                    floatViewBinding.sAvatar.load(userInfo.getFaceURL(), userInfo.getNickname());
                    view.name.setText(userInfo.getNickname());

                    //audio call
                    view.avatar2.load(userInfo.getFaceURL());
                    view.name2.setText(userInfo.getNickname());

                     */
                }
            }, ids);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void listener(SignalingInfo signalingInfo) {


    }

    @Override
    public void show() {
        playRingtone();
        super.show();
    }

    public void playRingtone() {
        try {
            Common.wakeUp(context);
            NotificationUtil.cancelNotify(AudioVideoService.NOTIFY_ID);
//           Ringtone铃声
            if (!MediaPlayerUtil.INSTANCE.isPlaying()) {
                MediaPlayerUtil.INSTANCE.initMedia(BaseApp.inst(), R.raw.incoming_call_ring);
                MediaPlayerUtil.INSTANCE.loopPlay();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        try {
            insertChatHistory();
            MediaPlayerUtil.INSTANCE.pause();
            MediaPlayerUtil.INSTANCE.release();
           // callingVM.setSpeakerphoneOn(true);
            callingVM.callViewModel.setCameraEnabled(false);

            super.dismiss();
            ((CallingServiceImp) callingVM.callingService).callDialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void insertChatHistory() {
        boolean isGroup = callingVM.isGroup;
        if (!isShowing() || isGroup || (null != signalingInfo && TextUtils.isEmpty(callingVM.buildPrimaryKey(signalingInfo))))
            return;
        String id = callingVM.buildPrimaryKey(signalingInfo);
        String senderID = isGroup ? BaseApp.inst().loginCertificate.userID :
            signalingInfo.getInvitation().getInviterUserID();
        String receiver = signalingInfo.getInvitation().getInviteeUserIDList().get(0);

        callingVM.renewalDB(id, (realm, callHistory) -> {
            callHistory = realm.copyFromRealm(callHistory);

            HashMap<String, Object> map = new HashMap<>();
            map.put(Constants.K_CUSTOM_TYPE, Constants.MsgType.LOCAL_CALL_HISTORY);
            map.put(Constants.K_DATA, callHistory);

            String data = GsonHel.toJson(map);
            Message message = OpenIMClient.getInstance().messageManager.createCustomMessage(data,
                "", "");
            message.setRead(true);
            OpenIMClient.getInstance().messageManager.insertSingleMessageToLocalStorage(new IMUtil.IMCallBack<String>() {
                @Override
                public void onSuccess(String data) {
                    Obs.newMessage(Constants.Event.INSERT_MSG);
                }
            }, message, receiver, senderID);
        });
    }

    public void videoViewRelease() {

    }
    public String buildPrimaryKey() {
        return CallingVM.buildPrimaryKey(signalingInfo);
    }

    public void otherSideAccepted() {
        callingVM.isStartCall = true;
        //view.headTips.setVisibility(View.GONE);
        MediaPlayerUtil.INSTANCE.pause();
        MediaPlayerUtil.INSTANCE.release();

        waitingHandle();
    }

    private void waitingHandle() {

    }

    protected void initWebView() {
        mWebView = (WebView) findViewById(R.id.webMain);
        WebSettings settings = mWebView.getSettings();

        settings.setDomStorageEnabled(true);//开启DOM
       // settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        mWebView.addJavascriptInterface(this, "$jsi");

        mWebView.setWebViewClient(new WebViewClient() {
            final String LogTag = "WebVIew";
            private boolean onPageChanging = false;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i("onPageStarted", "start url:" + url);
                //设定加载开始的操作
            }
            //qq webview not support

            @Override
            public void onReceivedSslError(WebView var1, SslErrorHandler handler, com.tencent.smtt.export.external.interfaces.SslError error) {
                Timber.tag("onReceivedSslError").i(error.toString());
                Timber.i("i will handle the error ^^^^");
                //handler.cancel(); 默认的处理方式，WebView变成空白页
                handler.proceed();//接受证书
            }
            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);

                //安卓调用js方法。注意需要在 onPageFinished 回调里调用
                Log.i("onPageFinished", String.format("webView:onPageFinished(),s=%s", s));

                // onPageChanging = false;
            }


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e(LogTag, "onReceivedError:" + Integer.toString(error.getErrorCode()) + ":" + error.getDescription());

                switch (error.getErrorCode()) {
                    case -2:
                        view.loadUrl("file:///android_asset/neterr.html");
                        new Handler().postDelayed(() -> {
                            mWebView.loadUrl(mWebURL);
                        }, 4000);
                        break;
                    case 404:
                        //view.loadUrl("加载一个错误页面提示，优化体验");
                        break;
                }

            }

            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                Log.e(LogTag, "onReceivedHttpError: " + errorResponse.toString());
            }


        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                String[] PERMISSIONS = {
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE
                };
                request.grant(PERMISSIONS);

            }


        });

        //mWebView.loadUrl("file:android_asset/index.html");
        L.e("tip-------------------------------------------------");
        String roomID = signalingInfo.getInvitation().getRoomID();
        String receiver = signalingInfo.getInvitation().getInviteeUserIDList().get(0);
        String sender = signalingInfo.getInvitation().getInviterUserID();
        int callout = callingVM.isCallOut ? 1 : 0;
        String mediaType = callingVM.isVideoCalls?"v":"a";
        String caller = receiver;
        String callee = sender;
        String query = String.format("?callout=%d&mediatype=%s&roomID=%s&caller=%s&callee=%s", callout,mediaType,roomID, caller, callee);
        L.i("weburl:");

        mWebURL = BASEURL + query;
        L.i(mWebURL);
        mWebView.loadUrl(mWebURL);

    }

    @JavascriptInterface
    public void handupClick() {

        callingVM.signalingHungUpByWeb(signalingInfo);
    }

    @JavascriptInterface
    public void rejectClick() {
        callingVM.signalingHungUpByWeb(signalingInfo);
    }

    //answer click
    @JavascriptInterface
    public void answerClick() {
        otherSideAccepted();
        callingVM.isStartCall = true;
    }
    @JavascriptInterface
    public void onConnect() {
        L.i(" webview onConnect ");
    }

    @JavascriptInterface
    public void onConnectClose() {
        L.i(" webview onConnectClose");
    }

    @JavascriptInterface
    public void onIceConnected() {
        L.i(" webview onIceConnected");
    }

    @JavascriptInterface
    public void onIceDisConnected() {
        L.i(" webview onICeDisConnect");
        callingVM.signalingHungUpByWeb(signalingInfo);
    }
    @JavascriptInterface
    public void handleReceiveOffer() {
        L.i(" webview handleReceiveOffer");
    }

    @JavascriptInterface
    public void handleReceiveAnswer() {
        L.i(" webview handleReceiveAnswer");
        otherSideAccepted();
    }

    @JavascriptInterface
    public void handleReceiveCandidate() {
        L.i(" webview handleReciveCandicate");
    }




}

