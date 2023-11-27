package io.openim.android.ouiconversation.widget;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelStoreOwner;

import com.lqr.audio.AudioRecordManager;
import com.lqr.audio.IAudioRecordListener;

import java.io.File;
import java.util.Timer;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.LayoutTouchVoiceBinding;
import io.openim.android.ouiconversation.databinding.LayoutTouchVoiceV3Binding;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.NavigationBarUtil;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.vm.CounterVM;

public class TouchVoiceDialogV3 extends BaseDialog {
    //和布局里保持一致
    private static final float VOICE_HEIGHT = 72;
    private final Context context;
    private CounterVM counterVM;

    public TouchVoiceDialogV3(@NonNull Context context) {
        super(context);
        this.context=context;
        initView();
    }

    public TouchVoiceDialogV3(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context=context;
        initView();
    }

    protected TouchVoiceDialogV3(@NonNull Context context, boolean cancelable,
                                 @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context=context;
        initView();
    }

    private int screenWidth, screenHeight;
    private LayoutTouchVoiceV3Binding view;
    private int audioDB = 0;

    private void initView() {
        view = LayoutTouchVoiceV3Binding.inflate(getLayoutInflater());
        setContentView(view.getRoot());
        WindowManager wm = (WindowManager) BaseApp.inst().getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight()+ NavigationBarUtil.getNavigationBarHeightIfRoom(context);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.dimAmount = 0.0f;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        counterVM=Easy.installVM((ViewModelStoreOwner) context, CounterVM.class);
        counterVM.num.observe((LifecycleOwner) context, v-> view.time.setText(TimeUtil.getTime(v*1000,TimeUtil.minuteTimeFormat)));

        //默认时长秒
        AudioRecordManager.getInstance(getContext()).setMaxVoiceDuration(60 * 5);
        //该库内不对文件夹是否存在进行判断，所以请在你的项目中自行判断
        File mAudioDir = new File(Constant.AUDIO_DIR);
        if (!mAudioDir.exists()) {
            mAudioDir.mkdirs();
        }
        AudioRecordManager.getInstance(getContext()).setAudioSavePath(mAudioDir.getAbsolutePath());

        AudioRecordManager.getInstance(getContext()).setAudioRecordListener(new IAudioRecordListener() {
            @Override
            public void initTipView() {

            }

            @Override
            public void setTimeoutTipView(int counter) {

            }

            @Override
            public void setRecordingTipView() {

            }

            @Override
            public void setAudioShortTipView() {
                counterVM.toast(context.getString(io.openim.android.ouicore.R.string.audio_too_short));
            }

            @Override
            public void setCancelTipView() {

            }

            @Override
            public void destroyTipView() {

            }

            @Override
            public void onStartRecord() {
                counterVM.setIncrease();
            }

            @Override
            public void onFinish(Uri audioPath, int duration) {
                counterVM.stopCountdown();

                if (null != onSelectResultListener)
                    onSelectResultListener.result(code, audioPath, counterVM.num.getValue());
            }

            @Override
            public void onAudioDBChanged(int db) {
                audioDB = db;
            }
        });
        setOnAudioDBChanged();
    }

    @Override
    public void show() {
        super.show();
        AudioRecordManager.getInstance(getContext()).startRecord();
        code=0;
        recordHandle();
    }

    @Override
    public void dismiss() {
        AudioRecordManager.getInstance(getContext()).stopRecord();
        AudioRecordManager.getInstance(getContext()).destroyRecord();

        super.dismiss();
    }

    /**
     * 0 录音 1 取消录音
     */
    private int code = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float y = event.getRawY();
        if (event.getAction()==MotionEvent.ACTION_DOWN){
            code=0;
            recordHandle();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            code = y > (screenHeight - Common.dp2px(VOICE_HEIGHT)) ? 0 : 1;
            recordHandle();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            dismiss();
        }
        return super.dispatchTouchEvent(event);
    }

    private void recordHandle() {
        if (code == 0) {
            view.tips.setText(io.openim.android.ouicore.R.string.chat_record_tips1);
            view.root.setBackground(AppCompatResources.getDrawable(context,
                io.openim.android.ouicore.R.drawable.sty_radius_6_black));
        } else {
            view.tips.setText(io.openim.android.ouicore.R.string.chat_record_tips2);
            view.root.setBackground(AppCompatResources.getDrawable(context,
                io.openim.android.ouicore.R.drawable.sty_radius_6_warning));
        }
        if (null != onSelectResultListener)
            onSelectResultListener.onViewChange(code);
    }


    private OnSelectResultListener onSelectResultListener;

    public void setOnSelectResultListener(OnSelectResultListener onSelectResultListener) {
        this.onSelectResultListener = onSelectResultListener;
    }


    public void setOnAudioDBChanged() {
        view.recordWaveView.setAmpListener(() -> audioDB);
    }


    public interface OnSelectResultListener {
        /**
         * @param code      0 录音 1 取消录音
         * @param audioPath 只有code=0时才有效
         * @param duration  只有code=0时才有效
         */
        void result(int code, Uri audioPath, int duration);

        /**
         * @param code 0 录音 1 取消录音
         */
        void onViewChange(int code);
    }
}
