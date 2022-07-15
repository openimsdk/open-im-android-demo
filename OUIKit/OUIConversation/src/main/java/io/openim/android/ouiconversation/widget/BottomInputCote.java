package io.openim.android.ouiconversation.widget;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.LayoutInputCoteBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.FixSizeLinkedList;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.AtUserInfo;
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
        this.context = context;
        this.view = view;
        hasMicrophone = AndPermission.hasPermissions(context, Permission.Group.MICROPHONE);

        view.chatSend.setOnClickListener(x -> {
            List<Message> atMessages = vm.atMessages.getValue();
            final Message msg;
            if (atMessages.isEmpty())
                msg = OpenIMClient.getInstance().messageManager.createTextMessage(vm.inputMsg.getValue());
            else {
                List<String> atUserIDList = new ArrayList<>();
                List<AtUserInfo> atUserInfoList = new ArrayList<>();

                Editable msgEdit = view.chatInput.getText();
                final ForegroundColorSpan spans[] = view.chatInput.getText().getSpans(0, view.chatInput.getText().length(), ForegroundColorSpan.class);
                for (Message atMessage : atMessages) {
                    atUserIDList.add(atMessage.getSendID());
                    AtUserInfo atUserInfo = new AtUserInfo();
                    atUserInfo.setAtUserID(atMessage.getSendID());
                    atUserInfo.setGroupNickname(atMessage.getSenderNickname());
                    atUserInfoList.add(atUserInfo);

                    try {
                        for (ForegroundColorSpan span : spans) {
                            if (span == null)
                                continue;
                            MsgExpand msgExpand = (MsgExpand) atMessage.getExt();
                            if (msgExpand.spanHashCode == span.hashCode()) {
                                final int spanStart = view.chatInput.getText().getSpanStart(span);
                                final int spanEnd = view.chatInput.getText().getSpanEnd(span);
                                msgEdit.replace(spanStart, spanEnd, " @" + atMessage.getSendID() + " ");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                msg = OpenIMClient.getInstance().messageManager.createTextAtMessage(msgEdit.toString(), atUserIDList, atUserInfoList, null);
            }
            if (null != msg) {
                vm.sendMsg(msg);
                vm.inputMsg.setValue("");
                view.chatInput.setText("");
                vm.atMessages.getValue().clear();
            }
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

        view.chatInput.setOnKeyListener((v, keyCode, event) -> {
            //监听删除操作，找到最靠近删除的一个Span，然后整体删除
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                final int selectionStart = Selection.getSelectionStart(view.chatInput.getText());
                final int selectionEnd = Selection.getSelectionEnd(view.chatInput.getText());

                final ForegroundColorSpan spans[] = view.chatInput.getText().getSpans(selectionStart - 1, selectionEnd, ForegroundColorSpan.class);
                for (ForegroundColorSpan span : spans) {
                    if (span == null) {
                        continue;
                    }
                    if (view.chatInput.getText().getSpanEnd(span) == selectionStart) {
                        final int spanStart = view.chatInput.getText().getSpanStart(span);
                        final int spanEnd = view.chatInput.getText().getSpanEnd(span);
                        Selection.setSelection(view.chatInput.getText(), spanStart, spanEnd);
                        view.chatInput.getText().delete(spanStart+1, spanEnd);
                    }
                    List<Message> atMessages = vm.atMessages.getValue();
                    Iterator iterator = atMessages.iterator();
                    while (iterator.hasNext()) {
                        Message message = (Message) iterator.next();
                        try {
                            MsgExpand msgExpand = (MsgExpand) message.getExt();
                            if (msgExpand.spanHashCode == span.hashCode()) {
                                iterator.remove();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return false;
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
        vmListener();
    }

    private void vmListener() {
        vm.atMessages.observe((LifecycleOwner) context, messages -> {
            if (messages.isEmpty()) return;
            SpannableString spannableString = new SpannableString("@" + messages.get(messages.size() - 1).getSenderNickname() + "\t");
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
            spannableString.setSpan(colorSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Message lastMsg = messages.get(messages.size() - 1);
            MsgExpand msgExpand = (MsgExpand) lastMsg.getExt();
            if (null != msgExpand)
                msgExpand.spanHashCode = colorSpan.hashCode();
            view.chatInput.append(spannableString);
        });


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
