package io.openim.android.ouiconversation.widget;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.InputType;
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
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;

import com.hjq.permissions.Permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.LayoutInputCoteBinding;
import io.openim.android.ouiconversation.ui.fragment.InputExpandFragment;
import io.openim.android.ouicore.ex.AtUser;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.EmojiUtil;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.GroupRole;
import io.openim.android.sdk.enums.GroupStatus;
import io.openim.android.sdk.models.AtUserInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.Message;

/**
 * 聊天页面底部输入栏
 */
public class BottomInputCote {

    private HasPermissions hasMicrophone;
    private ChatVM vm;
    private Context context;
    private OnAtUserListener onAtUserListener;

    InputExpandFragment inputExpandFragment;
    public LayoutInputCoteBinding view;
    //是否可发送内容
    private boolean isSend;
    //是否已绑定草稿
    boolean isBindDraft = false;

    private OnDedrepClickListener chatMoreOrSendClick;

    public BottomInputCote(Context context, LayoutInputCoteBinding view) {
        this.context = context;
        this.view = view;

        initView(view);

        Common.UIHandler.postDelayed(() -> hasMicrophone = new HasPermissions(context,
            Permission.RECORD_AUDIO), 300);

        view.chatMoreOrSend.setOnClickListener(chatMoreOrSendClick = new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                if (!isSend) {
                    view.voice.setChecked(false);
                    clearFocus();
                    Common.hideKeyboard(BaseApp.inst(), v);
                    view.fragmentContainer.setVisibility(VISIBLE);
                    switchFragment(inputExpandFragment);
                    return;
                }

                List<AtUser> atUsers = vm.atUsers.getValue();
                final Message msg;
                if (!atUsers.isEmpty()) {
                    List<String> atUserIDList = new ArrayList<>();
                    List<AtUserInfo> atUserInfoList = new ArrayList<>();

                    Editable msgEdit = view.chatInput.getText();
                    final ForegroundColorSpan spans[] = view.chatInput.getText().getSpans(0,
                        view.chatInput.getText().length(), ForegroundColorSpan.class);
                    for (AtUser atUser : atUsers) {
                        atUserIDList.add(atUser.key);
                        AtUserInfo atUserInfo = new AtUserInfo();
                        atUserInfo.setAtUserID(atUser.key);
                        atUserInfo.setGroupNickname(atUser.name);
                        atUserInfoList.add(atUserInfo);

                        try {
                            for (ForegroundColorSpan span : spans) {
                                if (span == null) continue;
                                if (atUser.spanHashCode == span.hashCode()) {
                                    final int spanStart =
                                        view.chatInput.getText().getSpanStart(span);
                                    final int spanEnd = view.chatInput.getText().getSpanEnd(span);
                                    msgEdit.replace(spanStart, spanEnd,
                                        IMUtil.atD(atUser.key));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    msg =
                        OpenIMClient.getInstance().messageManager.createTextAtMessage(msgEdit.toString(), atUserIDList, atUserInfoList, vm.replyMessage.val());
                } else if (null != vm.replyMessage.getValue()) {
                    msg =
                        OpenIMClient.getInstance().messageManager.createQuoteMessage(vm.inputMsg.val().toString(), vm.replyMessage.getValue());
                } else {
                    msg =
                        OpenIMClient.getInstance().messageManager.createTextMessage(vm.inputMsg.val().toString());
                }
                if (null != msg) {
                    vm.sendMsg(msg);
                    reset();
                }
            }
        });
        view.voice.setOnCheckedChangeListener((v, isChecked) -> {

        });

        view.chatInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) setExpandHide(false);
        });

        view.emoji.setOnClickListener(v -> {

        });
        view.cancelReply.setOnClickListener(v -> vm.replyMessage.setValue(null));
        view.chatInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (vm.isSingleChat) return;
                    if (count == 0) return;
                    String content = s.toString().substring(s.length() - 1);
                    if (!TextUtils.isEmpty(content) && null != onAtUserListener) {
                        if (content.equals("@")) {
                            onAtUserListener.onAtUser();
                        }
                    }
                } catch (Exception ignore) {
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                boolean isSend = !TextUtils.isEmpty(content) && !Common.isBlank(content);
                setSendButton(isSend);
            }
        });
        view.chatInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND && BottomInputCote.this.isSend) {
                if (null != chatMoreOrSendClick) {
                    chatMoreOrSendClick.click(view.chatMoreOrSend);
                }
            }
            return true;
        });
    }

    private void initView(LayoutInputCoteBinding view) {
        view.root.setIntercept(false);
        initFragment();

        view.chatInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        view.chatInput.setSingleLine(false);
        view.chatInput.setMaxLines(4);
    }

    private void bindDraft() {
        if (isBindDraft) return;
        Object[] draft = IMUtil.getDraft(vm.conversationID);
        CharSequence sequence = (CharSequence) draft[0];
        List<AtUser> atUsers = (List<AtUser>) draft[1];
        if (!TextUtils.isEmpty(sequence)) {
            vm.inputMsg.setValue(sequence);
        }
        vm.atUsers.val().addAll(atUsers);
        isBindDraft = true;
    }

    public void setOnAtUserListener(OnAtUserListener onAtUserListener) {
        this.onAtUserListener = onAtUserListener;
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
        vm.atUsers.getValue().clear();
        vm.emojiMessages.getValue().clear();
        vm.replyMessage.setValue(null);
    }

    private void initFragment() {
        inputExpandFragment = new InputExpandFragment();
        inputExpandFragment.setPage(1);

    }

    public void clearFocus() {
        view.chatInput.clearFocus();
    }

    public void setChatVM(ChatVM vm) {
        this.vm = vm;
        inputExpandFragment.setChatVM(vm);

        view.chatInput.setChatVM(vm);
        view.setChatVM(vm);
        vmListener();
    }

    @SuppressLint("SetTextI18n")
    private void vmListener() {
        vm.conversationInfo.observe((LifecycleOwner) context, conversationInfo -> bindDraft());
        vm.atUsers.observe((LifecycleOwner) context, atUsers -> {
            if (atUsers.isEmpty()) return;
            AtUser atUser;
            SpannableString spannableString = new SpannableString(
                IMUtil.atD((atUser =
                    atUsers.get(atUsers.size() - 1)).name));
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
            spannableString.setSpan(colorSpan, 0, spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            atUser.spanHashCode = colorSpan.hashCode();
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
            vm.memberInfo.observe((LifecycleOwner) context, mem -> {
                if (null == mem) return;
                setMute();
            });
            vm.groupInfo.observe((LifecycleOwner) context, groupInfo -> {
                if (null == groupInfo) return;
                setMute();
            });
            vm.isJoinGroup.observe((LifecycleOwner) context, isJoin -> {
                String tips=!isJoin?((Context) context)
                    .getString(io.openim.android.ouicore.R.string.quited_tips):null;
                setHighTips(tips);
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

    private void setHighTips(String tips) {
        if (TextUtils.isEmpty(tips)) {
            view.main.setVisibility(VISIBLE);
            view.highTips.setVisibility(GONE);
        } else {
            view.main.setVisibility(GONE);
            view.highTips.setVisibility(VISIBLE);
            view.tips.setText(tips);
        }

    }

    private void setMute() {
        GroupInfo groupInfo = vm.groupInfo.val();
        GroupMembersInfo mem = vm.memberInfo.val();
        if (null == groupInfo || null == mem) return;
        if (groupInfo.getStatus() == GroupStatus.GROUP_DISSOLVE) {
            editMute(true);
            view.notice.setText(BaseApp.inst().getString(io.openim.android.ouicore.R.string.dissolve_tips2));
        } else if (groupInfo.getStatus() == GroupStatus.GROUP_BANNED) {
            editMute(true);
            view.notice.setText(BaseApp.inst().getString(io.openim.android.ouicore.R.string.group_ban));
        } else {
            if (groupInfo.getStatus() == GroupStatus.GROUP_MUTED && mem.getRoleLevel() == GroupRole.MEMBER) {
                editMute(true);
                view.notice.setText(BaseApp.inst().getString(io.openim.android.ouicore.R.string.start_group_mute));
                return;
            }
            long endTime = vm.getMuteEndTime(mem) - System.currentTimeMillis();
            if (endTime > 0) {
                editMute(true);
                view.notice.setText(io.openim.android.ouicore.R.string.you_mute);
                return;
            }
            editMute(false);
        }
    }

    private void editMute(boolean isMute) {
        if (isMute) {
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
    }

    //设置扩展菜单隐藏
    public void setExpandHide(boolean isGone) {
        view.fragmentContainer.setVisibility(isGone ? View.GONE : View.INVISIBLE);
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

    public interface OnAtUserListener {
        void onAtUser();
    }

}
