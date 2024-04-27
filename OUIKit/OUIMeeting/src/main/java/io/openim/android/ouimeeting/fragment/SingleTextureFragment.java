package io.openim.android.ouimeeting.fragment;

import static io.openim.android.ouimeeting.vm.CallViewModelKt.getIdentity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.util.Objects;

import io.livekit.android.events.ParticipantEvent;
import io.livekit.android.room.participant.Participant;
import io.openim.android.ouicore.base.LazyFragment;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouimeeting.vm.MeetingVM;
import io.openim.android.ouimeeting.widget.SingleTextureView;
import kotlinx.coroutines.CoroutineScope;

public class SingleTextureFragment extends LazyFragment {
    private MeetingVM vm;
    private CoroutineScope scope;
    private SingleTextureView singleTextureView;
    private String lastId;
    private OnAllSeeHeListener onAllSeeHeListener;
    private boolean isAllWatchedUser;
    private CoroutineScope allWatchedUserScope;

    public static SingleTextureFragment newInstance(OnAllSeeHeListener onAllSeeHeListener) {

        Bundle args = new Bundle();

        SingleTextureFragment fragment = new SingleTextureFragment(onAllSeeHeListener);
        fragment.setArguments(args);
        return fragment;
    }

    public SingleTextureFragment(OnAllSeeHeListener onAllSeeHeListener) {
        this.onAllSeeHeListener = onAllSeeHeListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vm = Easy.find(MeetingVM.class);
        scope = vm.callViewModel.buildScope();
    }

    @Override
    public View initViews(LayoutInflater inflater, ViewGroup container) {
        singleTextureView = new SingleTextureView(container.getContext());
        return singleTextureView;
    }

    @Override
    public void loadData() {
        bindLocalParticipant();

        vm.callViewModel.subscribe(vm.callViewModel.getActiveSpeakersFlow(), v -> {
            if (v.isEmpty() || isAllWatchedUser) return null;
            Participant activeSpeaker = v.get(0);
            subscribeParticipant(activeSpeaker);
            return null;
        }, scope);

        vm.allWatchedUser.observe(this, v -> {
            if (v == null) {
                isAllWatchedUser=false;
                cancleAllWatch();
                return;
            }
            isAllWatchedUser = true;
            subscribeParticipant(v);

            cancleAllWatch();
            vm.callViewModel.subscribe(v.getEvents().getEvents(), v1 -> {
                if (v1 instanceof ParticipantEvent.TrackUnpublished) {
                    isAllWatchedUser = false;
                    bindLocalParticipant();
                }
                return null;
            }, allWatchedUserScope = vm.callViewModel.buildScope());
        });

    }

    private void cancleAllWatch() {
        if (null != allWatchedUserScope) {
            vm.callViewModel.scopeCancel(allWatchedUserScope);
        }
    }

    private void bindLocalParticipant() {
        singleTextureView.bindData(vm,
            vm.callViewModel.getRoom().getLocalParticipant());
    }

    private void subscribeParticipant(Participant activeSpeaker) {
        if (Objects.equals(getIdentity(activeSpeaker), lastId)) return;
        lastId =getIdentity(activeSpeaker);
        singleTextureView.bindData(vm, activeSpeaker);
        onAllSeeHeListener.onAllSeeHe();
    }

    @Override
    public void onDestroy() {
        vm.callViewModel.scopeCancel(scope);
        super.onDestroy();
    }

    public interface OnAllSeeHeListener {
        void onAllSeeHe();
    }
}
