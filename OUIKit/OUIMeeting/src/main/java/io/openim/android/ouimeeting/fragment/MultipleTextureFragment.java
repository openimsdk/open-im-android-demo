package io.openim.android.ouimeeting.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

import io.livekit.android.room.participant.Participant;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.LazyFragment;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.ViewRecyclerViewBinding;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.widget.GridSpaceItemDecoration;
import io.openim.android.ouimeeting.MeetingHomeActivity;
import io.openim.android.ouimeeting.vm.MeetingVM;
import io.openim.android.ouimeeting.widget.SingleTextureView;
import kotlinx.coroutines.CoroutineScope;

public class MultipleTextureFragment extends LazyFragment {
    private MeetingVM vm;
    private CoroutineScope scope;
    private List<Participant> participants;
    private ViewRecyclerViewBinding view;
    RecyclerViewAdapter<Participant, UserStreamViewHolder> adapter;

    public static MultipleTextureFragment newInstance(List<Participant> participants) {

        Bundle args = new Bundle();
        args.putSerializable(Constant.K_RESULT, (Serializable) participants);
        MultipleTextureFragment fragment = new MultipleTextureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        participants = (List<Participant>) getArguments().getSerializable(Constant.K_RESULT);
        vm = Easy.find(MeetingVM.class);
        scope = vm.callViewModel.buildScope();
    }

    @Override
    public View initViews(LayoutInflater inflater, ViewGroup container) {
        view = ViewRecyclerViewBinding.inflate(inflater);
        return view.getRoot();
    }

    @Override
    public void loadData() {
        view.recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        GridSpaceItemDecoration divItemDecoration = new GridSpaceItemDecoration(Common.dp2px(1));
        view.recyclerView.addItemDecoration(divItemDecoration);
        view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<Participant, UserStreamViewHolder>(UserStreamViewHolder.class) {

            @Override
            public void onBindView(@NonNull UserStreamViewHolder holder, Participant data, int position) {
                holder.setItemHeight(view.recyclerView.getHeight() / 2);
                holder.view.subscribeParticipant(vm, data);
            }
        });
        adapter.setItems(participants);
    }

    public static class UserStreamViewHolder extends RecyclerView.ViewHolder {
        public final SingleTextureView view;

        public UserStreamViewHolder(@NonNull View itemView) {
            super(new SingleTextureView(itemView.getContext()));
            view = (SingleTextureView) this.itemView;
        }

        public void setItemHeight(int height) {
            View childAt = view.getChildAt(0);
            if (null != childAt) {
                ViewGroup.LayoutParams params = childAt.getLayoutParams();
                params.height = height;
            }
        }
    }

    public void update(List<Participant> participants) {
        if (null != adapter)
            adapter.setItems(participants);
    }

    @Override
    public void onDestroy() {
        vm.callViewModel.scopeCancel(scope);
        super.onDestroy();
    }

}
