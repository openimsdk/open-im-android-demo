// Generated by view binder compiler. Do not edit!
package io.openim.android.ouicontact.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import io.openim.android.ouicontact.R;
import io.openim.android.ouicore.widget.AvatarImage;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class LayoutMsgTxtLeftBinding implements ViewBinding {
  @NonNull
  private final FrameLayout rootView;

  @NonNull
  public final AvatarImage avatar;

  @NonNull
  public final TextView content;

  private LayoutMsgTxtLeftBinding(@NonNull FrameLayout rootView, @NonNull AvatarImage avatar,
      @NonNull TextView content) {
    this.rootView = rootView;
    this.avatar = avatar;
    this.content = content;
  }

  @Override
  @NonNull
  public FrameLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static LayoutMsgTxtLeftBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static LayoutMsgTxtLeftBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.layout_msg_txt_left, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static LayoutMsgTxtLeftBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.avatar;
      AvatarImage avatar = ViewBindings.findChildViewById(rootView, id);
      if (avatar == null) {
        break missingId;
      }

      id = R.id.content;
      TextView content = ViewBindings.findChildViewById(rootView, id);
      if (content == null) {
        break missingId;
      }

      return new LayoutMsgTxtLeftBinding((FrameLayout) rootView, avatar, content);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
