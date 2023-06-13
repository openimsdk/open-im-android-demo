package io.openim.android.ouiconversation;

import android.os.Bundle;

import io.openim.android.ouiconversation.databinding.ActivityDebugBinding;

public class DebugActivity extends io.openim.android.ouicore.widget.DebugActivity {
    ActivityDebugBinding view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view=ActivityDebugBinding.inflate(getLayoutInflater());
        setContentView(view.getRoot());
    }

    @Override
    public void onSuccess(String data) {
        super.onSuccess(data);
    }
}
