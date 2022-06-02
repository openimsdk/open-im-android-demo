package io.openim.android.ouiconversation.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.LayoutInputCoteBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.Message;

/**
 * 聊天页面底部输入栏
 */
public class BottomInputCote extends LinearLayout {
    private InputExpandFragment inputExpandFragment;
    private ChatVM vm;

    public BottomInputCote(Context context) {
        super(context);
        initView();
    }

    public BottomInputCote(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BottomInputCote(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    LayoutInputCoteBinding view;
    TouchVoiceDialog touchVoiceDialog;
    boolean hasMicrophone;

    @SuppressLint("WrongConstant")
    private void initView() {
        hasMicrophone = AndPermission.hasPermissions(getContext(), Permission.Group.MICROPHONE);
        view = LayoutInputCoteBinding.inflate(LayoutInflater.from(getContext()), this, false);
        addView(view.getRoot());
        view.send.setOnClickListener(x -> {
            final Message msg = OpenIMClient.getInstance().messageManager.createTextMessage(vm.inputMsg.getValue());
            vm.sendMsg(msg);
            vm.inputMsg.setValue("");
            view.input.setText("");
        });

        view.voice.setOnCheckedChangeListener((v, isChecked) -> {
            view.input.setVisibility(isChecked ? GONE : VISIBLE);
            view.send.setVisibility(isChecked ? GONE : VISIBLE);
            view.touchSay.setVisibility(isChecked ? VISIBLE : GONE);
        });
        view.touchSay.setOnLongClickListener(v -> {
            if (null == touchVoiceDialog) {
                touchVoiceDialog = new TouchVoiceDialog(getContext());
                touchVoiceDialog.setOnSelectResultListener((code, audioPath, duration) -> {
                    if (code == 0) {
                        //录音结束
                        Message message=OpenIMClient.getInstance().messageManager.createSoundMessageFromFullPath(audioPath.getPath(),duration);
                        vm.sendMsg(message);
                    }
                });
            }

            if (hasMicrophone)
                touchVoiceDialog.show();
            else
                AndPermission.with(getContext())
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

        view.input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                setExpandHide();
        });
        view.more.setOnClickListener(v -> {
            clearFocus();
            Common.hideKeyboard(getContext(), v);
            view.fragmentContainer.setVisibility(VISIBLE);
            if (null == inputExpandFragment) {
                inputExpandFragment = new InputExpandFragment();
                inputExpandFragment.setPage(1);
                inputExpandFragment.setChatVM(vm);
            }
            switchFragment(inputExpandFragment);
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (null != touchVoiceDialog)
            touchVoiceDialog.dispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void clearFocus() {
        view.input.clearFocus();
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
                FragmentTransaction transaction = ((BaseActivity) getContext()).getSupportFragmentManager().beginTransaction();
                if (!fragment.isAdded()) {
                    transaction.add(R.id.fragment_container, fragment);
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
