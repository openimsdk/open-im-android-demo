package io.openim.android.ouiconversation.widget;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.LayoutInputCoteBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.Message;

/**
 * 聊天页面底部输入栏
 */
public class BottomInputCote {

    private ChatVM vm;
    private Context context;

    InputExpandFragment inputExpandFragment;
   public LayoutInputCoteBinding view;
    TouchVoiceDialog touchVoiceDialog;
    boolean hasMicrophone;



    @SuppressLint("WrongConstant")
    public BottomInputCote(Context context, LayoutInputCoteBinding view) {
        this.context=context;
        this.view=view;
        hasMicrophone = AndPermission.hasPermissions(context, Permission.Group.MICROPHONE);

        view.chatSend.setOnClickListener(x -> {
            final Message msg = OpenIMClient.getInstance().messageManager.createTextMessage(vm.inputMsg.getValue());
            vm.sendMsg(msg);
            vm.inputMsg.setValue("");
            view.chatInput.setText("");
        });

        view.voice.setOnCheckedChangeListener((v, isChecked) -> {
            view.chatInput.setVisibility(isChecked ? GONE : VISIBLE);
            view.chatSend.setVisibility(isChecked ? GONE : VISIBLE);
            view.touchSay.setVisibility(isChecked ? VISIBLE : GONE);
        });
        view.touchSay.setOnLongClickListener(v -> {
            if (null == touchVoiceDialog) {
                touchVoiceDialog = new TouchVoiceDialog(context);
                touchVoiceDialog.setOnSelectResultListener((code, audioPath, duration) -> {
                    if (code == 0) {
                        //录音结束
                        Message message = OpenIMClient.getInstance().messageManager.createSoundMessageFromFullPath(audioPath.getPath(), duration);
                        vm.sendMsg(message);
                    }
                });
            }

            if (hasMicrophone)
                touchVoiceDialog.show();
            else
                AndPermission.with(context)
                    .runtime()
                    .permission(Permission.Group.MICROPHONE)
                    .onGranted(permissions -> {
                        // Storage permission are allowed.
                        hasMicrophone = true;
                    })
                    .onDenied(permissions -> {
                        // Storage permission are not allowed.
                    })
                    .start();
            return false;
        });

        view.chatInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                setExpandHide();
        });

        view.chatMore.setOnClickListener(v -> {
            clearFocus();
            Common.hideKeyboard(BaseApp.instance(), v);
            view.fragmentContainer.setVisibility(VISIBLE);
            if (null == inputExpandFragment) {
                inputExpandFragment = new InputExpandFragment();
                inputExpandFragment.setPage(1);
                inputExpandFragment.setChatVM(vm);
            }
            switchFragment(inputExpandFragment);
        });


    }

    public void dispatchTouchEvent(MotionEvent event) {
        if (null != touchVoiceDialog)
            touchVoiceDialog.dispatchTouchEvent(event);
    }

    public void clearFocus() {
        view.chatInput.clearFocus();
    }

    public void setChatVM(ChatVM vm) {
        this.vm = vm;
        view.setChatVM(vm);
    }

    //设置扩展菜单隐藏
    public void setExpandHide() {
        view.fragmentContainer.setVisibility(GONE);
    }

    private int mCurrentTabIndex;
    private BaseFragment lastFragment;


    private void switchFragment(BaseFragment fragment) {
        try {
            if (fragment != null && !fragment.isVisible() && mCurrentTabIndex != fragment.getPage()) {
                FragmentTransaction transaction = ((BaseActivity) context).getSupportFragmentManager().beginTransaction();
                if (!fragment.isAdded()) {
                    transaction.add(view.fragmentContainer.getId(), fragment);
                }
                if (lastFragment != null) {
                    transaction.hide(lastFragment);
                }
                transaction.show(fragment).commit();
                lastFragment = fragment;
                mCurrentTabIndex = lastFragment.getPage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
