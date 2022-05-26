package io.openim.android.ouicontact;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.openim.android.ouicontact.ui.ContactFragment;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment_container, new ContactFragment()).commit();
    }
}
