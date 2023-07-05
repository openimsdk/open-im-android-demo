package io.openim.android.ouiconversation.widget;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.LayoutInputCoteBinding;
import io.openim.android.ouiconversation.ui.fragment.EmojiFragment;
import io.openim.android.ouiconversation.ui.fragment.InputExpandFragment;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.EmojiUtil;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.GroupStatus;
import io.openim.android.sdk.models.AtUserInfo;
import io.openim.android.sdk.models.Message;

/**
 * 聊天页面底部输入栏
 */
public class BottomInputCote {

    private boolean hasMicrophone = false;
    private ChatVM vm;
    private Context context;

    InputExpandFragment inputExpandFragment;
    EmojiFragment emojiFragment;
    public LayoutInputCoteBinding view;
    TouchVoiceDialogV3 touchVoiceDialog;
    //是否可发送内容
    private boolean isSend;


    @SuppressLint("WrongConstant")
    public BottomInputCote(Context context, LayoutInputCoteBinding view) {
        this.context = context;
        this.view = view;
        initFragment();
        Common.UIHandler.postDelayed(() -> hasMicrophone = AndPermission.hasPermissions(context,
            Permission.Group.MICROPHONE), 300);

        view.chatMoreOrSend.setOnClickListener(v -> {
            if (!isSend) {
                view.voice.setChecked(false);
                clearFocus();
                Common.hideKeyboard(BaseApp.inst(), v);
                view.fragmentContainer.setVisibility(VISIBLE);
                switchFragment(inputExpandFragment);
                return;
            }

            List<Message> atMessages = vm.atMessages.getValue();
            final Message msg;
            if (null != vm.replyMessage.getValue()) {
                msg =
                    OpenIMClient.getInstance().messageManager
                        .createQuoteMessage(vm.inputMsg.getValue(), vm.replyMessage.getValue());
            } else if (atMessages.isEmpty())
                msg =
                    OpenIMClient.getInstance().messageManager.createTextMessage(vm.inputMsg.getValue());
            else {
                List<String> atUserIDList = new ArrayList<>();
                List<AtUserInfo> atUserInfoList = new ArrayList<>();

                Editable msgEdit = view.chatInput.getText();
                final ForegroundColorSpan spans[] = view.chatInput.getText().getSpans(0,
                    view.chatInput.getText().length(), ForegroundColorSpan.class);
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
                                msgEdit.replace(spanStart, spanEnd,
                                    " @" + atMessage.getSendID() + " ");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                msg =
                    OpenIMClient.getInstance().messageManager.createTextAtMessage(msgEdit.toString(), atUserIDList, atUserInfoList, null);
            }
            if (null != msg) {
                vm.sendMsg(msg);
                reset();
            }
        });
        view.voice.setOnCheckedChangeListener((v, isChecked) -> {
            clearFocus();
            view.inputLy.setVisibility(isChecked ? GONE : VISIBLE);
            view.touchSay.setVisibility(isChecked ? VISIBLE : GONE);
            setExpandHide();
        });
        view.touchSay.setOnLongClickListener(v -> {
            if (null == touchVoiceDialog) {
                touchVoiceDialog = new TouchVoiceDialogV3(context);
                touchVoiceDialog.setOnSelectResultListener(new TouchVoiceDialogV3.OnSelectResultListener() {
                    @Override
                    public void result(int code, Uri audioPath, int duration) {
                        if (code == 0) {
                            //录音结束
                            Message message =
                                OpenIMClient.getInstance().messageManager.createSoundMessageFromFullPath(audioPath.getPath(), duration);
                            vm.sendMsg(message);
                        }
                    }

                    @Override
                    public void onViewChange(int code) {
                        view.touchSay.setText(code == 0 ?
                            io.openim.android.ouicore.R.string.chat_record_tips3 :
                            io.openim.android.ouicore.R.string.chat_record_tips2);
                    }
                });
                touchVoiceDialog.setOnShowListener(dialog -> showingViewChange());
                touchVoiceDialog.setOnDismissListener(dialog -> showingViewChange());
            }
            Common.permission(context, () -> {
                touchVoiceDialog.show();
                hasMicrophone = true;
            }, hasMicrophone, Permission.Group.MICROPHONE);
            return false;
        });

        view.chatInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) setExpandHide();
//            InputMethodManager inputMethodManager =
//                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
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
                boolean isSend = !TextUtils.isEmpty(s) && !Common.isBlank(s);
                setSendButton(isSend);
            }
        });
    }

    private void showingViewChange() {
        boolean showing = touchVoiceDialog.isShowing();
        if (showing) {
            view.touchSay.setBackground(AppCompatResources.getDrawable(context,
                io.openim.android.ouicore.R.drawable.sty_radius_4_33shallow));
            view.touchSay.setTextColor(context.getResources().getColor(io.openim.android.ouicore.R.color.white));
        } else {
            view.touchSay.setBackground(AppCompatResources.getDrawable(context,
                io.openim.android.ouicore.R.drawable.sty_radius_4_white));
            view.touchSay.setTextColor(context.getResources().getColor(io.openim.android.ouicore.R.color.txt_black));
            view.touchSay.setText(R.string.touch_say);
        }
    }

    private void setSendButton(boolean isSend) {
        if (BottomInputCote.this.isSend == isSend) return;
        view.chatMoreOrSend.setImageResource(isSend ? R.mipmap.ic_c_send : R.mipmap.ic_chat_add);
        BottomInputCote.this.isSend = isSend;
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
        if (null != touchVoiceDialog && touchVoiceDialog.isShowing())
            touchVoiceDialog.dispatchTouchEvent(event);
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
            SpannableString spannableString =
                new SpannableString("@" + messages.get(messages.size() - 1).getSenderNickname() + "\t");
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
            spannableString.setSpan(colorSpan, 0, spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            spannableString.setSpan(imageSpan, 0, emojiKey.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                if (groupInfo.getStatus() == GroupStatus.GROUP_MUTED
                    && !groupInfo.getOwnerUserID().equals(BaseApp.inst().loginCertificate.userID)) {
                    view.inputLy.setVisibility(VISIBLE);
                    setSendButton(true);
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
                FragmentTransaction transaction =
                    ((BaseActivity) context).getSupportFragmentManager().beginTransaction();
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
