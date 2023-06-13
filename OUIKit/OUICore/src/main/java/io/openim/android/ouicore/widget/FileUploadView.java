package io.openim.android.ouicore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import java.io.File;
import java.math.BigDecimal;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.OpenFileUtil;

public class FileUploadView extends RelativeLayout {
    private ImageView res, bgMask;
    private CirclePgBar circlePgBar;
    private String localUrl = "";

    public FileUploadView(Context context) {
        super(context);
        init();
    }

    public FileUploadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        res = new ImageView(getContext());
        bgMask = new ImageView(getContext());
        bgMask.setImageResource(R.mipmap.ic_file_mask);

        LayoutParams params =
            new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.width = 80;
        params.height = 80;
        circlePgBar = new CirclePgBar(getContext());
        circlePgBar.setLayoutParams(params);

        addView(res);
        addView(bgMask);
        addView(circlePgBar);

        setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                GetFilePathFromUri.openFile(getContext(), localUrl);
            }
        });

    }

    public void setRes(String localUrl) {
        this.localUrl = localUrl;
        if (MediaFileUtil.isZIP(localUrl)) res.setImageResource(R.mipmap.ic_file_zip);
        else if (MediaFileUtil.isWord(localUrl)) res.setImageResource(R.mipmap.ic_file_word);
        else if (MediaFileUtil.isPPT(localUrl)) res.setImageResource(R.mipmap.ic_file_ppt);
        else if (MediaFileUtil.isPDF(localUrl)) res.setImageResource(R.mipmap.ic_file_pdf);
        else res.setImageResource(R.mipmap.ic_file_unknown);
    }

    public void setForegroundVisibility(boolean visibility) {
        bgMask.setVisibility(visibility ? GONE : VISIBLE);
        circlePgBar.setVisibility(visibility ? GONE : VISIBLE);
        if (visibility)
            circlePgBar.reset();
    }

    public void setProgress(int progress) {
        circlePgBar.setTargetProgress(progress);
    }

}
