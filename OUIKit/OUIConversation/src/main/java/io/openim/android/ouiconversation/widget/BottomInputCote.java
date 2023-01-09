package io.openim.android.ouiconversation.widget;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouiconversation.databinding.LayoutInputCoteBinding;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.EmojiUtil;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.MThreadTool;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.AtUserInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.Message;

/**
 * 聊天页面底部输入栏
 */
public class BottomInputCote {

    private boolean hasMicrophone=false;
    private ChatVM vm;
    private Context context;

    InputExpandFragment inputExpandFragment;
    EmojiFragment emojiFragment;
    public LayoutInputCoteBinding view;
    TouchVoiceDialog touchVoiceDialog;


    @SuppressLint("WrongConstant")
    public BottomInputCote(Context context, LayoutInputCoteBinding view) {
        this.context = context;
        this.view = view;
        initFragment();

        view.chatSend.setOnClickListener(x -> {
            List<Message> atMessages = vm.atMessages.getValue();
            final Message msg;
            if (null != vm.replyMessage.getValue()) {
                msg = OpenIMClient.getInstance().messageManager.createQuoteMessage(vm.inputMsg.getValue(), vm.replyMessage.getValue());
            } else if (atMessages.isEmpty())
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
                            if (span == null) continue;
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
                reset();
            }
        });
        view.voice.setOnCheckedChangeListener((v, isChecked) -> {
            hasMicrophone=AndPermission.hasPermissions(context,Permission.Group.MICROPHONE);

            clearFocus();
            view.inputLy.setVisibility(isChecked ? GONE : VISIBLE);
            view.touchSay.setVisibility(isChecked ? VISIBLE : GONE);
            setExpandHide();
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
            Common.permission(context, () -> {
                touchVoiceDialog.show();
                hasMicrophone = true;
            }, hasMicrophone, Permission.Group.MICROPHONE);
            return false;
        });

        view.chatInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) setExpandHide();
        });

        view.chatMore.setOnClickListener(v -> {
            view.voice.setChecked(false);
            clearFocus();
            Common.hideKeyboard(BaseApp.inst(), v);
            view.fragmentContainer.setVisibility(VISIBLE);
            switchFragment(inputExpandFragment);
        });
        view.emoji.setOnClickListener(v -> {
            view.voice.setChecked(false);
            clearFocus();
            Common.hideKeyboard(BaseApp.inst(), v);
            view.fragmentContainer.setVisibility(VISIBLE);
            switchFragment(emojiFragment);
        });
        view.cancelReply.setOnClickListener(v -> vm.replyMessage.setValue(null));
        view.chatInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                view.chatSend.setVisibility(s.toString().isEmpty() ? GONE : VISIBLE);
            }
        });
    }


    //消息发出后重置UI
    private void reset() {
        vm.inputMsg.setValue("");
        view.chatInput.setText("");
        vm.atMessages.getValue().clear();
        vm.emojiMessages.getValue().clear();
        vm.replyMessage.setValue(null);
    }

    private void initFragment() {
        inputExpandFragment = new InputExpandFragment();
        inputExpandFragment.setPage(1);

        emojiFragment = new EmojiFragment();
        emojiFragment.setPage(2);
    }

    public void dispatchTouchEvent(MotionEvent event) {
        if (null != touchVoiceDialog) touchVoiceDialog.dispatchTouchEvent(event);
    }

    public void clearFocus() {
        view.chatInput.clearFocus();
    }

    public void setChatVM(ChatVM vm) {
        this.vm = vm;
        inputExpandFragment.setChatVM(vm);
        emojiFragment.setChatVM(vm);

        view.chatInput.setChatVM(vm);
        view.setChatVM(vm);
        vmListener();
    }

    @SuppressLint("SetTextI18n")
    private void vmListener() {
        vm.atMessages.observe((LifecycleOwner) context, messages -> {
            if (messages.isEmpty()) return;
            SpannableString spannableString = new SpannableString("@" + messages.get(messages.size() - 1).getSenderNickname() + "\t");
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
            spannableString.setSpan(colorSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Message lastMsg = messages.get(messages.size() - 1);
            MsgExpand msgExpand = (MsgExpand) lastMsg.getExt();
            if (null != msgExpand) msgExpand.spanHashCode = colorSpan.hashCode();
            view.chatInput.append(spannableString);
        });
        vm.emojiMessages.observe((LifecycleOwner) context, messages -> {
            if (messages.isEmpty()) return;
            String emojiKey = messages.get(messages.size() - 1);
            SpannableStringBuilder spannableString = new SpannableStringBuilder(emojiKey);
            int emojiId = Common.getMipmapId(EmojiUtil.emojiFaces.get(emojiKey));
            Drawable drawable = BaseApp.inst().getResources().getDrawable(emojiId);
            drawable.setBounds(0, 0, Common.dp2px(22), Common.dp2px(22));
            ImageSpan imageSpan = new ImageSpan(drawable);
            spannableString.setSpan(imageSpan, 0, emojiKey.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            view.chatInput.append(spannableString);
        });
        view.chatInput.setOnKeyListener((v, keyCode, event) -> {
            //监听删除操作，找到最靠近删除的一个Span，然后整体删除
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                TailInputEditText.spansDelete((TailInputEditText) v, vm);
            }
            return false;
        });

        if (!vm.isSingleChat) {
            vm.groupInfo.observe((LifecycleOwner) context, groupInfo -> {
                if (null == groupInfo) return;
                if (groupInfo.getStatus() == Constant.GroupStatus.status3 && !groupInfo.getOwnerUserID().equals(BaseApp.inst().loginCertificate.userID)) {
                    view.inputLy.setVisibility(VISIBLE);
                    view.chatSend.setVisibility(VISIBLE);
                    view.touchSay.setVisibility(GONE);

                    view.root.setIntercept(true);
                    view.root.setAlpha(0.5f);
                    view.notice.setVisibility(VISIBLE);
                } else {
                    view.root.setIntercept(false);
                    view.root.setAlpha(1f);
                    view.notice.setVisibility(GONE);
                }
            });
        }
        vm.replyMessage.observe((LifecycleOwner) context, message -> {
            if (null == message) {
                view.replyLy.setVisibility(GONE);
            } else {
                view.replyLy.setVisibility(VISIBLE);
                view.replyContent.setText(message.getSenderNickname() + ":" + IMUtil.getMsgParse(message));
            }
        });

        vm.inputMsg.observe((LifecycleOwner) context, s -> {
            view.chatSend.setEnabled(!TextUtils.isEmpty(s) && !Common.isBlank(s));
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
