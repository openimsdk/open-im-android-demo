package io.openim.android.ouiconversation.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;

import java.lang.reflect.Type;
import java.util.List;

import io.openim.android.ouiconversation.databinding.ActivityChatHistoryDetailsBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.vm.PreviewMediaVM;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.models.Message;

public class ChatHistoryDetailsActivity extends BaseActivity<BaseViewModel, ActivityChatHistoryDetailsBinding> {


    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityChatHistoryDetailsBinding.inflate(getLayoutInflater()));
        sink();
        initView();
        init();
    }

    private void initView() {
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerview.setAdapter(adapter = new RecyclerViewAdapter<Message,
            ViewHol.ChatHistoryItemHolder>(ViewHol.ChatHistoryItemHolder.class) {

            @Override
            public void onBindView(@NonNull ViewHol.ChatHistoryItemHolder holder, Message data, int position) {
                holder.viewBinding.avatar.load(data.getSenderFaceUrl());
                holder.viewBinding.nickName.setText(data.getSenderNickname());
                holder.viewBinding.time.setText(TimeUtil.getTimeString(data.getSendTime()));

                holder.viewBinding.lastMsg.setText(IMUtil.getMsgParse(data));

                holder.viewBinding.getRoot().setOnClickListener(v -> {
                    String url;
                    PreviewMediaVM previewMediaVM;
                    PreviewMediaVM.MediaData mediaData;
                    switch (data.getContentType()) {
                        case MessageType.MERGER:
                            startActivity(new Intent(ChatHistoryDetailsActivity.this,
                                ChatHistoryDetailsActivity.class).putExtra(Constant.K_RESULT,
                                GsonHel.toJson(data.getMergeElem().getMultiMessage())));
                            break;
                        case MessageType.PICTURE:
                            url = data.getPictureElem().getSourcePicture().getUrl();
                            previewMediaVM = Easy.installVM(PreviewMediaVM.class);
                            mediaData =new PreviewMediaVM.MediaData(data.getClientMsgID());
                            mediaData.mediaUrl=url;
                            mediaData.thumbnail= data.getPictureElem().getSnapshotPicture().getUrl();
                            previewMediaVM.previewSingle(mediaData);
                            startActivity(
                                new Intent(v.getContext(),
                                    PreviewMediaActivity.class));
                            break;
                        case MessageType.VIDEO:
                            String snapshotUrl = data.getVideoElem().getSnapshotUrl();
                            url = data.getVideoElem().getVideoUrl();
                            previewMediaVM = Easy.installVM(PreviewMediaVM.class);
                            mediaData =new PreviewMediaVM.MediaData(data.getClientMsgID());
                            mediaData.mediaUrl=url;
                            mediaData.thumbnail= snapshotUrl;
                            previewMediaVM.previewSingle(mediaData);
                            v.getContext().startActivity(
                                new Intent(v.getContext(), PreviewMediaActivity.class));
                            break;
                        case MessageType.CARD:
                            ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                                .withString(Constant.K_ID, data.getCardElem().getUserID())
                                .navigation();
                            break;
                        case MessageType.LOCATION:
                            Common.toMap(data, v);
                            break;
                        case MessageType.FILE:
                            GetFilePathFromUri.openFile(v.getContext(), data);
                            break;
                    }

                });
            }
        });
    }

    void init() {
        String extra = getIntent().getStringExtra(Constant.K_RESULT);
        try {
            Type listType = new GsonHel.ParameterizedTypeImpl(List.class, new Class[]{Message.class});
            List<Message> messages = GsonHel.getGson().fromJson(extra, listType);
            for (Message message : messages) {
                IMUtil.buildExpandInfo(message);
            }
            adapter.setItems(messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
