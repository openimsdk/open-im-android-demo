package io.openim.android.ouiconversation.widget;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ReplacementSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.hjq.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.LayoutInputCoteBinding;
import io.openim.android.ouiconversation.ui.fragment.InputExpandFragment;
import io.openim.android.ouicore.ex.AtUser;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.EmojiUtil;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
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
    private ChatVM vm;
    private Context context;

    InputExpandFragment inputExpandFragment;
    public LayoutInputCoteBinding view;
    //是否可发送内容
    private boolean isSend;

    private OnDedrepClickListener chatMoreOrSendClick;

    public BottomInputCote(Context context, LayoutInputCoteBinding view) {
        this.context = context;
        this.view = view;

        initView(view);

        view.chatMoreOrSend.setOnClickListener(chatMoreOrSendClick = new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                if (!isSend) {
                    vm.mTypingState.postValue(false);
                    clearFocus();
                    Common.hideKeyboard(BaseApp.inst(), v);
                    view.fragmentContainer.setVisibility(VISIBLE);
                    switchFragment(inputExpandFragment);
                    return;
                }

                Message msg = OpenIMClient.getInstance().messageManager.createTextMessage(vm.inputMsg.val().toString());
                if (null != msg) {
                    vm.sendMsg(msg);
                    reset();
                }
            }
        });

        view.chatInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) setExpandHide(false);
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
                    vm.mTypingState.setValue(true);
                    Common.UIHandler.removeCallbacks(vm.finishInputting);
                    Common.UIHandler.postDelayed(vm.finishInputting, 3000L);
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
        view.fragmentContainer.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) setExpandHide(true);
        });
//        view.chatInput.getViewTreeObserver().addOnDrawListener(() -> {
//            Editable inputContent = view.chatInput.getEditableText();
//            Object[] atSpan = inputContent.getSpans(0, view.chatInput.getSelectionEnd(), Object.class);
////            if (atSpan.length > 0) {
////                view.chatInput.setSelection(atSpan[atSpan.length - 1].);
////            }
//        });
    }

    private void initView(LayoutInputCoteBinding view) {
        view.root.setIntercept(false);
        initFragment();

        view.chatInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        view.chatInput.setSingleLine(false);
        view.chatInput.setMaxLines(4);
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
                String tips = !isJoin ? ((Context) context).getString(io.openim.android.ouicore.R.string.quited_tips) : null;
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

    public int getCurrentInputPosition() {
        return view.chatInput.getSelectionStart();
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
