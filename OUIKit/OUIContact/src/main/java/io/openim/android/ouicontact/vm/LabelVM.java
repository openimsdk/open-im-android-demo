package io.openim.android.ouicontact.vm;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.api.NiService;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.entity.UserLabel;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.api.OneselfService;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.widget.WaitDialog;

public class LabelVM extends BaseViewModel {

    private static final String TAG = "LabelVM";
    public MutableLiveData<List<UserLabel>> userLabels = new MutableLiveData<>(new ArrayList<>());

    public void getUserTags() {
        Parameter parameter = getParameter();
        N.API(NiService.class).CommNI(Constant.getImApiUrl() + "office/get_user_tags",
            BaseApp.inst().loginCertificate.imToken, parameter.buildJsonBody()).compose(N.IOMain()).map(OneselfService.turn(UserLabel.class)).subscribe(new NetObserver<UserLabel>(TAG) {
            @Override
            public void onSuccess(UserLabel o) {
                if (null == o.getTags() || o.getTags().isEmpty()) return;
                userLabels.setValue(o.getTags());
            }

            @Override
            protected void onFailure(Throwable e) {
                getIView().toast(e.getMessage());
            }
        });
    }

    public void createTag(String tagName, List<String> ids,
                          IMUtil.OnSuccessListener successListener) {
        Parameter parameter = getParameter();
//        'tagName': tagName,
//         'userIDList': userIDList,

        N.API(NiService.class).CommNI(Constant.getImApiUrl() + "office/create_tag",
            BaseApp.inst().loginCertificate.imToken,
            parameter.add("tagName", tagName).add(
                "userIDList", ids).buildJsonBody()).compose(N.IOMain()).map(OneselfService.turn(Object.class)).subscribe(new NetObserver<Object>(TAG) {
            @Override
            public void onSuccess(Object o) {
                successListener.onSuccess(null);
            }

            @Override
            protected void onFailure(Throwable e) {
                getIView().toast(e.getMessage());
            }
        });
    }

    public void removeTag(UserLabel userLabel, boolean isShowWaiting) {
        WaitDialog waitDialog = null;
        if (isShowWaiting) {
            waitDialog = new WaitDialog(getContext());
            waitDialog.show();
        }
        WaitDialog finalWaitDialog = waitDialog;
        N.API(NiService.class).CommNI(Constant.getImApiUrl() + "office/delete_tag",
            BaseApp.inst().loginCertificate.imToken, getParameter().add("tagID",
                userLabel.getTagID()).buildJsonBody()).compose(N.IOMain()).map(OneselfService.turn(Object.class)).subscribe(new NetObserver<Object>(TAG) {
            @Override
            public void onSuccess(Object o) {
                userLabels.getValue().remove(userLabel);
                userLabels.setValue(userLabels.getValue());
            }

            @Override
            protected void onFailure(Throwable e) {
                getIView().toast(e.getMessage());
            }

            @Override
            public void onComplete() {
                if (null != finalWaitDialog) finalWaitDialog.dismiss();
            }
        });
    }

    private Parameter getParameter() {
        Parameter parameter = new Parameter().add("operationID", System.currentTimeMillis() + "");
        return parameter;
    }
}
