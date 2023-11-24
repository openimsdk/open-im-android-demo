package io.openim.android.ouimeeting.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.participant.ConnectionQuality;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.ParticipantMeta;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouimeeting.databinding.LayoutUserStatusBinding;
import io.openim.android.ouimeeting.databinding.ViewSingleTextureBinding;
import io.openim.android.ouimeeting.vm.MeetingVM;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.flow.StateFlow;

public class SingleTextureView extends FrameLayout {
    private static final String TAG = "SingleTextureView";
    private ViewSingleTextureBinding view;
    public CoroutineScope scope;
    private MeetingVM vm;
    private Participant participant;

    public SingleTextureView(@NonNull Context context) {
        super(context);
        initView();
    }

    public SingleTextureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        view = ViewSingleTextureBinding.inflate(LayoutInflater.from(getContext()));
        view.switchCamera.setOnClickListener(v -> vm.callViewModel.flipCamera());
        addView(view.getRoot());
    }

    private void removeRenderer(TextureViewRenderer textureView) {
        Object speakerVideoViewTag = textureView.getTag();
        if (speakerVideoViewTag instanceof VideoTrack) {
            ((VideoTrack) speakerVideoViewTag).removeRenderer(textureView);
            textureView.setTag(null);
        }
    }

    public void bindData(MeetingVM vm, Participant participant) {
        this.vm = vm;
        this.participant = participant;
        subscribeParticipant();
    }

    private void subscribeParticipant() {
        initScope();
        vm.initVideoView(view.textureView);
//      removeRenderer(view.textureView);
        bindUserStatus(participant);
        handleCenter(participant);

        vm.callViewModel.subscribe(participant.getEvents().getEvents(), (v) -> {
            Context context = getContext();
            if (context instanceof Activity) {
                if (((Activity) context).isFinishing() || ((Activity) context).isDestroyed())
                    return null;
            }
            ParticipantMeta meta = GsonHel.fromJson(v.getParticipant().getMetadata(),
                ParticipantMeta.class);

            L.e(TAG, "------name-----" + vm.getMetaUserName(meta) + "----Events----" + v);
            bindUserStatus(v.getParticipant());
            handleCenter(v.getParticipant());
            return null;
        }, scope);

        StateFlow<ConnectionQuality> flow = vm.callViewModel.getConnectionFlow(participant);
        vm.callViewModel.subscribe(flow, (quality) -> {
            bindConnectionQuality(view.userStatus, quality);
            return null;
        }, scope);
    }

    private void bindRemoteViewRenderer(Participant participant) {
        vm.callViewModel.bindRemoteViewRenderer(view.textureView, participant, scope,
            new Continuation<Unit>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object o) {
            }
        });
    }

    private void initScope() {
        if (null == scope) {
            scope = vm.callViewModel.buildScope();
        }
    }

    private void handleCenter(Participant data) {
        boolean textureViewUse = data.isCameraEnabled() || data.isScreenShareEnabled();
        boolean isShowSwitchCamera = data.isCameraEnabled() && Objects.equals(data.getIdentity(),
            BaseApp.inst().loginCertificate.userID);
        view.switchCamera.setVisibility(isShowSwitchCamera ? VISIBLE : GONE);
        view.textureView.setVisibility(textureViewUse ? View.VISIBLE : View.INVISIBLE);
        view.avatar.setVisibility(textureViewUse ? View.GONE : View.VISIBLE);
        ParticipantMeta meta = GsonHel.fromJson(data.getMetadata(), ParticipantMeta.class);
        L.e(TAG,
            "------name-----" + vm.getMetaUserName(meta) + "----isCameraEnabled----" + textureViewUse);
        if (null != meta) view.avatar.load(meta.userInfo.getFaceURL(), vm.getMetaUserName(meta));
    }

    private void bindUserStatus(Participant participant) {
        view.userStatus.mc.setVisibility(vm.isHostUser(participant) ? View.VISIBLE : View.GONE);
        view.userStatus.mic.setImageResource(participant.isMicrophoneEnabled() ?
            io.openim.android.ouicore.R.mipmap.ic__mic_on :
            io.openim.android.ouicore.R.mipmap.ic__mic_off);
        ParticipantMeta meta = GsonHel.fromJson(participant.getMetadata(), ParticipantMeta.class);
        view.userStatus.name.setText(vm.getMetaUserName(meta));
        bindConnectionQuality(view.userStatus, participant.getConnectionQuality());
    }

    private void bindConnectionQuality(LayoutUserStatusBinding holder, ConnectionQuality quality) {
        switch (quality) {
            case UNKNOWN:
            case EXCELLENT:
                holder.net.setImageResource(io.openim.android.ouicore.R.mipmap.ic_net_excellent);
                break;
            case GOOD:
                holder.net.setImageResource(io.openim.android.ouicore.R.mipmap.ic_net_good);
                break;
            default:
                holder.net.setImageResource(io.openim.android.ouicore.R.mipmap.ic_net_poor);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        bindRemoteViewRenderer(participant);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recycle();
    }

    public void recycle() {
        if (null != scope) {
            vm.callViewModel.scopeCancel(scope);
            scope = null;
        }
        removeRenderer(view.textureView);
    }

}
