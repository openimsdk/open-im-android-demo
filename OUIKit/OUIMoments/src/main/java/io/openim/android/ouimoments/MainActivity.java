package io.openim.android.ouimoments;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.KeyEvent;

import io.openim.android.ouimoments.ui.CircleFragment;

public class MainActivity extends AppCompatActivity {
    CircleFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moments_home);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment = CircleFragment.newInstance(null);
        if (!fragment.isAdded()) {
            transaction.add(R.id.contentFl, fragment);
            transaction.show(fragment).commit();
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (fragment.onKeyDown(keyCode, event)) return true;
        else return super.onKeyDown(keyCode, event);
    }

}
