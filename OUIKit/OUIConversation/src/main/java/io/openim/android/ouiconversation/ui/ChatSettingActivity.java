package io.openim.android.ouiconversation.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.ActivityChatSettingBinding;
import io.openim.android.ouiconversation.databinding.LayoutBurnAfterReadingBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.vm.ContactListVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.SlideButton;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.Opt;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.UserInfo;

public class ChatSettingActivity extends BaseActivity<ChatVM, ActivityChatSettingBinding> implements ChatVM.ViewAction {

    ContactListVM contactListVM = new ContactListVM();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(ChatVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityChatSettingBinding.inflate(getLayoutInflater()));
        sink();

        initView();
        click();
    }

    private ActivityResultLauncher<Intent> personDetailLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        });

    private void click() {
        view.addChat.setOnClickListener(v -> {
            SelectTargetVM choiceVM = Easy.installVM(SelectTargetVM.class);
            choiceVM.setIntention(SelectTargetVM.Intention.invite);
            if (null != userInfo) {
                MultipleChoice choice=new MultipleChoice();
                choice.key=userInfo.getUserID();
                choice.name=userInfo.getNickname();
                choice.icon=userInfo.getFaceURL();
                choice.isSelect=true;
                choice.isEnabled=false;
                choiceVM.addDate(choice);
            }
            choiceVM.setOnFinishListener(() -> {
                GroupVM groupVM = BaseApp.inst().getVMByCache(GroupVM.class);
                if (null == groupVM) groupVM = new GroupVM();
                groupVM.selectedFriendInfo.getValue().clear();

                for (int i = 0; i < choiceVM.metaData.val().size(); i++) {
                    MultipleChoice us = choiceVM.metaData.val().get(i);
                    FriendInfo friendInfo = new FriendInfo();
                    friendInfo.setUserID(us.key);
                    friendInfo.setNickname(us.name);
                    friendInfo.setFaceURL(us.icon);
                    groupVM.selectedFriendInfo.getValue().add(friendInfo);
                }
                BaseApp.inst().putVM(groupVM);
                ARouter.getInstance().build(Routes.Group.CREATE_GROUP2).navigation();
            });

            ARouter.getInstance().build(Routes.Group.SELECT_TARGET).navigation();
        });
        view.picture.setOnClickListener(v -> {
            startActivity(new Intent(this, MediaHistoryActivity.class).putExtra(Constant.K_RESULT
                , true));
        });
        view.video.setOnClickListener(v -> {
            startActivity(new Intent(this, MediaHistoryActivity.class));
        });
        view.file.setOnClickListener(v -> startActivity(new Intent(this,
            FileHistoryActivity.class)));

        view.readVanish.setOnSlideButtonClickListener(isChecked -> {
            OpenIMClient.getInstance().conversationManager.setOneConversationPrivateChat(new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    toast(error + code);
                    view.readVanish.setCheckedWithAnimation(!isChecked);
                }

                @Override
                public void onSuccess(String data) {
                    view.readVanish.setCheckedWithAnimation(isChecked);
                }
            }, vm.conversationInfo.getValue().getConversationID(), isChecked);
        });
        view.topSlideButton.setOnSlideButtonClickListener(is -> {
            contactListVM.pinConversation(vm.conversationInfo.getValue(), is);
        });
        view.searchChat.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatHistorySearchActivity.class));
        });
        view.chatbg.setOnClickListener(view1 -> {
            startActivity(new Intent(this, SetChatBgActivity.class));
        });

        view.noDisturb.setOnSlideButtonClickListener(is -> {
            vm.setConversationRecvMessageOpt(is ? Opt.ReceiveNotNotifyMessage : Opt.NORMAL,
                vm.conversationInfo.getValue().getConversationID());
        });
        view.user.setOnClickListener(v -> {
            Postcard postcard = ARouter.getInstance().build(Routes.Main.PERSON_DETAIL);
            LogisticsCenter.completion(postcard);
            personDetailLauncher.launch(new Intent(this, postcard.getDestination()).putExtra(Constant.K_ID, vm.userID).putExtra(Constant.K_RESULT, true));
        });
        view.clearRecord.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(this);
            commonDialog.show();
            commonDialog.getMainView().tips.setText(io.openim.android.ouicore.R.string.clear_chat_tips);
            commonDialog.getMainView().cancel.setOnClickListener(view1 -> commonDialog.dismiss());
            commonDialog.getMainView().confirm.setOnClickListener(view1 -> {
                commonDialog.dismiss();
                vm.clearCHistory(vm.conversationID);
            });
        });

        view.readVanishTime.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                LayoutBurnAfterReadingBinding view =
                    LayoutBurnAfterReadingBinding.inflate(getLayoutInflater());
                CommonDialog commonDialog = new CommonDialog(ChatSettingActivity.this);
                commonDialog.setCustomCentral(view.getRoot());
                List<Object> strings = new ArrayList<>();
                strings.add("30" + getString(io.openim.android.ouicore.R.string.seconds));
                strings.add("5" + getString(io.openim.android.ouicore.R.string.minute));
                strings.add("1" + getString(io.openim.android.ouicore.R.string.hour));
                view.roller.setAdapter(new ArrayWheelAdapter(strings));
                int duration = vm.conversationInfo.val().getBurnDuration();
                int currentItem = 0;
                if (duration == 300)
                    currentItem = 1;
                if (duration >= 3600)
                    currentItem = 2;
                view.roller.setCurrentItem(currentItem);
                view.roller.setCyclic(false);
                commonDialog.getMainView().cancel.setOnClickListener(v1 -> commonDialog.dismiss());
                commonDialog.getMainView().confirm.setOnClickListener(v1 -> {
                    commonDialog.dismiss();
                    int burnDuration = 30;
                    int position = view.roller.getCurrentItem();
                    if (position == 1) burnDuration = 5 * 60;
                    if (position == 2) burnDuration = 60 * 60;

                    final int finalBurnDuration = burnDuration;
                    OpenIMClient.getInstance().conversationManager.setConversationBurnDuration(new IMUtil.IMCallBack<String>() {
                        @Override
                        public void onSuccess(String data) {
                            toast(getString(io.openim.android.ouicore.R.string.set_succ));
                            vm.conversationInfo.val().setBurnDuration(finalBurnDuration);
                            vm.conversationInfo.update();
                        }
                    }, vm.conversationID, burnDuration);
                });
                commonDialog.show();
            }
        });
    }

    public String convertToDaysWeeksMonths(long seconds) {
        long days = seconds / (60 * 60 * 24);
        long weeks = days / 7;
        long months = weeks / 4;

        if (days <= 6) {
            return days + getString(io.openim.android.ouicore.R.string.day);
        } else if (weeks <= 6 &&(days % 7==0)) {
            return weeks + getString(io.openim.android.ouicore.R.string.week);
        } else {
            return months + getString(io.openim.android.ouicore.R.string.month);
        }
    }

    private void initView() {
        view.readVanish.setCheckedWithAnimation(vm.conversationInfo.val().isPrivateChat());
        vm.notDisturbStatus.observe(this, integer -> {
            view.noDisturb.post(() -> view.noDisturb.setCheckedWithAnimation(integer == 2));
        });
        vm.conversationInfo.observe(this, conversationInfo -> {
            view.topSlideButton.post(() -> view.topSlideButton.setCheckedWithAnimation(conversationInfo.isPinned()));
            view.readVanishTime.setVisibility(conversationInfo.isPrivateChat() ? View.VISIBLE :
                View.GONE);
            int burnDuration = conversationInfo.getBurnDuration();
            String burnDurationStr = "30" + getString(io.openim.android.ouicore.R.string.seconds);
            if (burnDuration == 300)
                burnDurationStr = "5" + getString(io.openim.android.ouicore.R.string.minute);
            if (burnDuration >= 3600)
                burnDurationStr = "1" + getString(io.openim.android.ouicore.R.string.hour);
            view.readVanishNum.setText(burnDurationStr);
        });

        List<String> uid = new ArrayList<>();
        uid.add(vm.userID);
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                toast(error + code);
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;
                userInfo = data.get(0);
                view.avatar.load(userInfo.getFaceURL());
                view.userName.setText(userInfo.getNickname());

            }
        }, uid);
    }


    @Override
    public void scrollToPosition(int position) {

    }

    @Override
    public void closePage() {

    }
}
