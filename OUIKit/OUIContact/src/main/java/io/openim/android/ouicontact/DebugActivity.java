package io.openim.android.ouicontact;

import android.os.Bundle;

import io.openim.android.ouicontact.databinding.ActivityDebugBinding;
import io.openim.android.ouicontact.ui.ContactListFragment;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.im.IM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;

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
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, ContactListFragment.newInstance()).commit();
    }
}