package io.openim.android.ouiconversation.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.alibaba.android.arouter.launcher.ARouter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.adapter.MessageAdapter;
import io.openim.android.ouiconversation.adapter.MessageViewHolder;
import io.openim.android.ouiconversation.databinding.ActivityChatHistoryDetailsBinding;
import io.openim.android.ouiconversation.databinding.LayoutMsgTxtLeftBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.vm.PreviewMediaVM;
import io.openim.android.ouicore.widget.AvatarImage;
import io.openim.android.ouicore.widget.SpacesItemDecoration;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.models.Message;

public class ChatHistoryDetailsActivity extends BaseActivity<BaseViewModel,
    ActivityChatHistoryDetailsBinding> {


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
        view.recyclerview.setBackgroundColor(getResources().getColor(io.openim.android.ouicore.R.color.theme_bg));
        SpacesItemDecoration divItemDecoration = new SpacesItemDecoration();
        divItemDecoration.setColor(getResources()
            .getColor(io.openim.android.ouicore.R.color.txt_grey));
        divItemDecoration.setMargin(7,50,0);
        divItemDecoration.setDividerHeight(2);
        divItemDecoration.addNotDrawIndex(1);
        view.recyclerview.addItemDecoration(divItemDecoration);
        view.recyclerview.setAdapter(adapter = new RecyclerViewAdapter<Message,
            MessageViewHolder.MsgViewHolder>() {

            @Override
            public int getItemViewType(int position) {
                Message message = getItems().get(position);
                return message.getContentType();
            }

            public void process(MessageViewHolder.MsgViewHolder holder, int position) {
                holder.hFirstItem(position);
                holder.hName();
            }

            @NonNull
            @Override
            public MessageViewHolder.MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                      int viewType) {
                if (viewType == Constant.MsgType.CUSTOMIZE_MEETING)
                    return new MessageViewHolder.MeetingInviteView(parent){
                        @Override
                        protected boolean getSendWay(Message message) {
                            return false;
                        }

                        @Override
                        protected void unifiedProcess(int position) {
                            process(this, position);
                        }

                    };
                if (viewType == MessageType.FILE) return new MessageViewHolder.FileView(parent) {
                    @Override
                    protected boolean getSendWay(Message message) {
                        return false;
                    }

                    @Override
                    protected void unifiedProcess(int position) {
                        process(this, position);
                    }

                };
                if (viewType == MessageType.VOICE) return new MessageViewHolder.AudioView(parent) {
                    @Override
                    protected boolean getSendWay(Message message) {
                        return false;
                    }

                    @Override
                    protected void unifiedProcess(int position) {
                        process(this, position);
                    }

                    @Override
                    protected void bindLeft(View itemView, Message message) {
                        TextView badge =
                            itemView.findViewById(io.openim.android.ouicore.R.id.badge);
                        badge.setVisibility(View.GONE);
                    }

                    @Override
                    public void clickPlay(Message message, LottieAnimationView lottieView) {

                    }
                };
                if (viewType == MessageType.MERGER) return new MessageViewHolder.MergeView(parent) {
                    @Override
                    protected boolean getSendWay(Message message) {
                        return false;
                    }

                    @Override
                    protected void unifiedProcess(int position) {
                        process(this, position);
                    }
                };
                if (viewType == MessageType.LOCATION)
                    return new MessageViewHolder.LocationView(parent) {
                        @Override
                        protected boolean getSendWay(Message message) {
                            return false;
                        }

                        @Override
                        protected void unifiedProcess(int position) {
                            process(this, position);
                        }
                    };
                if (viewType == MessageType.CARD)
                    return new MessageViewHolder.BusinessCardView(parent) {
                        @Override
                        protected boolean getSendWay(Message message) {
                            return false;
                        }

                        @Override
                        protected void unifiedProcess(int position) {
                            process(this, position);
                        }
                    };
                if (viewType == MessageType.VIDEO) return new MessageViewHolder.VideoView(parent) {
                    @Override
                    protected boolean getSendWay(Message message) {
                        return false;
                    }

                    @Override
                    protected void unifiedProcess(int position) {
                        process(this, position);
                    }

                    @Override
                    public void toPreview(View view, String url, String firstFrameUrl) {
                        super.toPreview(view, url, firstFrameUrl, true);
                    }
                };
                if (viewType == MessageType.PICTURE || viewType == MessageType.CUSTOM_FACE)
                    return new MessageViewHolder.IMGView(parent) {
                        @Override
                        protected boolean getSendWay(Message message) {
                            return false;
                        }

                        @Override
                        protected void unifiedProcess(int position) {
                            process(this, position);
                        }

                        @Override
                        public void toPreview(View view, String url, String firstFrameUrl) {
                            super.toPreview(view, url, firstFrameUrl, true);
                        }
                    };
                return new MessageViewHolder.TXTView(parent) {
                    protected boolean getSendWay(Message message) {
                        return false;
                    }

                    @Override
                    protected void unifiedProcess(int position) {
                        process(this, position);
                    }

                    @Override
                    protected void bindLeft(View itemView, Message message) {
                        super.bindLeft(itemView, message);
                        LayoutMsgTxtLeftBinding v = LayoutMsgTxtLeftBinding.bind(itemView);
                        v.content.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        v.content.setTextIsSelectable(true);
                    }
                };
            }

            @Override
            public void onBindView(@NonNull MessageViewHolder.MsgViewHolder holder,
                                   Message message, int position) {
                holder.bindData(message, position);
                boolean isSame =
                    position != 0 && Objects.equals(getItems().get(position - 1).getSenderFaceUrl(), message.getSenderFaceUrl());
                AvatarImage avatarImage = holder.itemView.findViewById(R.id.avatar);
                if (isSame) {
                    avatarImage.setVisibility(View.INVISIBLE);
                } else {
                    avatarImage.setVisibility(View.VISIBLE);
                    avatarImage.load(message.getSenderFaceUrl(), message.getSenderNickname());
                }
            }
        });
    }

    void init() {
        String extra = getIntent().getStringExtra(Constant.K_RESULT);
        try {
            Type listType = new GsonHel.ParameterizedTypeImpl(List.class,
                new Class[]{Message.class});
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
