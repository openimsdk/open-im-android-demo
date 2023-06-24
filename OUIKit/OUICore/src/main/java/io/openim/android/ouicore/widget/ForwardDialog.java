package io.openim.android.ouicore.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import org.jetbrains.annotations.NotNull;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.DialogForwardBinding;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.vm.MultipleChoiceVM;

public class ForwardDialog extends BaseDialog {

    @NotNull("MultipleChoiceVM can't is null")
    private MultipleChoiceVM choiceVM = Easy.find(MultipleChoiceVM.class);


    public ForwardDialog(@NonNull Context context) {
        super(context);
        initView();
    }


    private void initView() {
        DialogForwardBinding view = DialogForwardBinding.inflate(getLayoutInflater(),null, false);
        setContentView(view.getRoot());

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if (choiceVM.metaData.val().size() == 1) {
            //单个
            MultipleChoice data = choiceVM.metaData.val().get(0);
            view.name.setText(data.name);
            view.avatar.load(data.icon, data.isGroup, data.name);
        } else {
            view.tips.setText(R.string.multiple_send);
            view.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
            view.recyclerView.setAdapter(new RecyclerViewAdapter<MultipleChoice, ViewHol.ImageTxtViewHolder>(ViewHol.ImageTxtViewHolder.class) {

                @Override
                public void onBindView(@NonNull ViewHol.ImageTxtViewHolder holder, MultipleChoice data, int position) {
                    holder.view.txt.setText(data.name);
                    holder.view.img.load(data.icon, data.isGroup, data.name);
                }
            });
        }
        view.cancel.setOnClickListener(view1 -> dismiss());
        view.sure.setOnClickListener(view1 -> {
            Obs.newMessage(Constant.Event.FORWARD,choiceVM.metaData.val());
            dismiss();
        });
    }

}
