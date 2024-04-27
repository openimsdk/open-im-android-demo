package io.openim.android.ouimeeting;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.Toast;


import com.alibaba.android.arouter.facade.annotation.Route;

import io.livekit.android.room.track.VideoTrack;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.services.IMeetingBridge;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimeeting.vm.MeetingVM;

@Route(path = Routes.Service.MEETING)
public class IBridgeImpl implements IMeetingBridge, MeetingVM.Interaction {

    private WaitDialog waitDialog;
    private Context context;

    @Override
    public void init(Context context) {
        this.context = context;
        waitDialog = new WaitDialog(context);
        waitDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
    }


    @Override
    public void joinMeeting(String roomID) {
        waitDialog.show();
        MeetingVM meetingVM = new MeetingVM();
        meetingVM.setContext(context);
        meetingVM.setIView(this);
        BaseApp.inst().putVM(meetingVM);
        meetingVM.joinMeeting(roomID);
    }

    @Override
    public void connectRoomSuccess(VideoTrack localVideoTrack) {

    }

    @Override
    public void onError(String error) {
        BaseApp.inst().removeCacheVM(MeetingVM.class);
        waitDialog.dismiss();
        Toast.makeText(context,
            context.getString(io.openim.android.ouicore.R.string.meeting_has_ended),
            Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess(Object body) {
        waitDialog.dismiss();
        context.startActivity(new Intent(context, MeetingHomeActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK));
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }

    }

    @Override
    public void toast(String tips) {
        Toast.makeText(context, tips, Toast.LENGTH_LONG).show();
    }

    @Override
    public void close() {

    }
}
