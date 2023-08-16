package io.openim.android.ouimeeting.widget;

import static kotlinx.coroutines.CoroutineScopeKt.MainScope;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.participant.ConnectionQuality;
import io.livekit.android.room.participant.Participant;
import io.livekit.android.room.track.TrackPublication;
import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.ParticipantMeta;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouimeeting.databinding.LayoutUserStatusBinding;
import io.openim.android.ouimeeting.databinding.ViewSingleTextureBinding;
import io.openim.android.ouimeeting.vm.CallViewModel;
import io.openim.android.ouimeeting.vm.MeetingVM;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.StateFlow;

public class SingleTextureView extends FrameLayout {
    private static final String TAG = "SingleTextureView";
    private ViewSingleTextureBinding view;
    private final CoroutineScope scope = MainScope();
    private MeetingVM vm;

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
        addView(view.getRoot());
    }

    private void removeRenderer(TextureViewRenderer textureView) {
        Object speakerVideoViewTag = textureView.getTag();
        if (speakerVideoViewTag instanceof VideoTrack) {
            ((VideoTrack) speakerVideoViewTag).removeRenderer(textureView);
        }
    }

    public void subscribeParticipant(MeetingVM vm, Participant participant) {
        this.vm = vm;

        vm.initVideoView(view.textureView);
        removeRenderer(view.textureView);
        bindUserStatus(participant);
        handleCenter(participant);
        Common.UIHandler.postDelayed(() ->
            vm.callViewModel.bindRemoteViewRenderer(view.textureView, participant,
                new Continuation<Unit>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(@NonNull Object o) {
                    }
                }), 200);


        vm.callViewModel.subscribe(participant.getEvents().getEvents(), (v) -> {
            ParticipantMeta meta = GsonHel.fromJson(v.getParticipant().getMetadata(),
                ParticipantMeta.class);
            L.e(TAG,
                "------name-----" + vm.getMetaUserName(meta) + "----Events----" + v.getParticipant().getEvents().getEvents());
            bindUserStatus(v.getParticipant());
            handleCenter(v.getParticipant());

            StateFlow<ConnectionQuality> flow =
                vm.callViewModel.getConnectionFlow(v.getParticipant());
            vm.callViewModel.subscribe(flow, (quality) -> {
                bindConnectionQuality(view.userStatus, quality);
                return null;
            }, scope);

            return null;
        }, scope);
    }

    private void handleCenter(Participant data) {
        boolean textureViewUse = data.isCameraEnabled() || data.isScreenShareEnabled();
        view.textureView.setVisibility(textureViewUse ? View.VISIBLE : View.GONE);
        view.avatar.setVisibility(textureViewUse ? View.GONE : View.VISIBLE);
        ParticipantMeta meta = GsonHel.fromJson(data.getMetadata(), ParticipantMeta.class);
        L.e(TAG,
            "------name-----" + vm.getMetaUserName(meta) + "----isCameraEnabled----"
                + data.isCameraEnabled());
        if (null != meta)
            view.avatar.load(meta.userInfo.getFaceURL(), vm.getMetaUserName(meta));
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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != vm) {
            vm.callViewModel.scopeCancel(scope);
            removeRenderer(view.textureView);
        }
    }
}
