package io.openim.android.ouicore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

public class FileDownloadView extends RelativeLayout {
    private ImageView res, bgMask, downBtn;
    private CirclePgBar circlePgBar;
    private String targetUrl;
    private boolean isDownloading = false, isDownLoadCompleted;
    private DownloadTask task;

    public FileDownloadView(Context context) {
        super(context);
        init();
    }

    public FileDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        res = new ImageView(getContext());
        bgMask = new ImageView(getContext());
        bgMask.setImageResource(R.mipmap.ic_file_mask);
        downBtn = new ImageView(getContext());
        downBtn.setImageResource(R.mipmap.ic_file_download);

        RelativeLayout.LayoutParams params =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.width = 80;
        params.height = 80;
        circlePgBar = new CirclePgBar(getContext());
        circlePgBar.setLayoutParams(params);

        RelativeLayout.LayoutParams params2 =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params2.width = 40;
        params2.height = 40;
        downBtn.setLayoutParams(params2);

        addView(res);
        addView(bgMask);
        addView(circlePgBar);
        addView(downBtn);

        setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                String fName =
                    Common.md5(targetUrl) + "." + GetFilePathFromUri.getFileSuffix(targetUrl);
                if (isDownLoadCompleted) {
                    OpenFileUtil.openFile(getContext(), Constant.File_DIR + fName);
                } else {
                    task =
                        new DownloadTask.Builder(targetUrl, new File(Constant.File_DIR))
                            .setFilename(fName)
                            // the minimal interval millisecond for callback progress
                            .setMinIntervalMillisCallbackProcess(30).build();
                    if (isDownloading) {
                        isDownloading = false;
                        task.cancel();
                    } else
                        download();
                }
            }
        });

    }

    private void download() {
        task.enqueue(new DownloadListener1() {
            @Override
            public void taskStart(@NonNull DownloadTask task,
                                  @NonNull Listener1Assist.Listener1Model model) {
                isDownloading = true;
                downBtn.setImageResource(R.mipmap.ic_file_download_pause);
            }

            @Override
            public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {

            }

            @Override
            public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset,
                                  long totalLength) {

            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
                int current =
                    BigDecimal.valueOf(currentOffset).divide(BigDecimal.valueOf(totalLength), 2,
                        BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).intValue();
                circlePgBar.setTargetProgress(current);
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
                                @Nullable Exception realCause,
                                @NonNull Listener1Assist.Listener1Model model) {
                if (cause == EndCause.COMPLETED) {
                    isDownLoadCompleted = true;
                    bgMask.setVisibility(GONE);
                    downBtn.setVisibility(GONE);
                    circlePgBar.setVisibility(GONE);
                } else {
                    isDownloading = false;
                    downBtn.setImageResource(R.mipmap.ic_file_download);
                }
            }
        });
    }

    public void setRes(String url) {
        targetUrl = url;
        if (MediaFileUtil.isZIP(url)) res.setImageResource(R.mipmap.ic_file_zip);
        else if (MediaFileUtil.isWord(url)) res.setImageResource(R.mipmap.ic_file_word);
        else if (MediaFileUtil.isPPT(url)) res.setImageResource(R.mipmap.ic_file_ppt);
        else if (MediaFileUtil.isPDF(url)) res.setImageResource(R.mipmap.ic_file_pdf);
        else res.setImageResource(R.mipmap.ic_file_unknown);

        isDownLoadCompleted = isDownLoadCompleted();
        bgMask.setVisibility(isDownLoadCompleted ? GONE : VISIBLE);
        circlePgBar.setVisibility(isDownLoadCompleted ? GONE : VISIBLE);
        downBtn.setVisibility(isDownLoadCompleted ? GONE : VISIBLE);
    }

    boolean isDownLoadCompleted() {
        return StatusUtil.isCompleted(targetUrl, Constant.File_DIR, Common.md5(targetUrl) +
            "." + GetFilePathFromUri.getFileSuffix(targetUrl));
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != task)
            task.cancel();
    }
}
