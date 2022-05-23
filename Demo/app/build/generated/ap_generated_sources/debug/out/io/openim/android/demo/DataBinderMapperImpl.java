package io.openim.android.demo;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import io.openim.android.demo.databinding.ActivityLoginBindingImpl;
import io.openim.android.demo.databinding.ActivityMainBindingImpl;
import io.openim.android.demo.databinding.ActivityRegisterBindingImpl;
import io.openim.android.demo.databinding.ActivitySearchPersonBindingImpl;
import io.openim.android.demo.databinding.ActivitySendVerifyBindingImpl;
import io.openim.android.demo.databinding.LayoutLoginBindingImpl;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTIVITYLOGIN = 1;

  private static final int LAYOUT_ACTIVITYMAIN = 2;

  private static final int LAYOUT_ACTIVITYREGISTER = 3;

  private static final int LAYOUT_ACTIVITYSEARCHPERSON = 4;

  private static final int LAYOUT_ACTIVITYSENDVERIFY = 5;

  private static final int LAYOUT_LAYOUTLOGIN = 6;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(6);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(io.openim.android.demo.R.layout.activity_login, LAYOUT_ACTIVITYLOGIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(io.openim.android.demo.R.layout.activity_main, LAYOUT_ACTIVITYMAIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(io.openim.android.demo.R.layout.activity_register, LAYOUT_ACTIVITYREGISTER);
    INTERNAL_LAYOUT_ID_LOOKUP.put(io.openim.android.demo.R.layout.activity_search_person, LAYOUT_ACTIVITYSEARCHPERSON);
    INTERNAL_LAYOUT_ID_LOOKUP.put(io.openim.android.demo.R.layout.activity_send_verify, LAYOUT_ACTIVITYSENDVERIFY);
    INTERNAL_LAYOUT_ID_LOOKUP.put(io.openim.android.demo.R.layout.layout_login, LAYOUT_LAYOUTLOGIN);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_ACTIVITYLOGIN: {
          if ("layout/activity_login_0".equals(tag)) {
            return new ActivityLoginBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_login is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYMAIN: {
          if ("layout/activity_main_0".equals(tag)) {
            return new ActivityMainBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_main is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYREGISTER: {
          if ("layout/activity_register_0".equals(tag)) {
            return new ActivityRegisterBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_register is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYSEARCHPERSON: {
          if ("layout/activity_search_person_0".equals(tag)) {
            return new ActivitySearchPersonBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_search_person is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYSENDVERIFY: {
          if ("layout/activity_send_verify_0".equals(tag)) {
            return new ActivitySendVerifyBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_send_verify is invalid. Received: " + tag);
        }
        case  LAYOUT_LAYOUTLOGIN: {
          if ("layout/layout_login_0".equals(tag)) {
            return new LayoutLoginBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for layout_login is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(3);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    result.add(new com.wynsbin.vciv.DataBinderMapperImpl());
    result.add(new io.openim.android.ouicontact.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(6);

    static {
      sKeys.put(1, "LoginVM");
      sKeys.put(2, "MainVM");
      sKeys.put(3, "SearchVM");
      sKeys.put(0, "_all");
      sKeys.put(4, "chatVM");
      sKeys.put(5, "loginVM");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(6);

    static {
      sKeys.put("layout/activity_login_0", io.openim.android.demo.R.layout.activity_login);
      sKeys.put("layout/activity_main_0", io.openim.android.demo.R.layout.activity_main);
      sKeys.put("layout/activity_register_0", io.openim.android.demo.R.layout.activity_register);
      sKeys.put("layout/activity_search_person_0", io.openim.android.demo.R.layout.activity_search_person);
      sKeys.put("layout/activity_send_verify_0", io.openim.android.demo.R.layout.activity_send_verify);
      sKeys.put("layout/layout_login_0", io.openim.android.demo.R.layout.layout_login);
    }
  }
}
