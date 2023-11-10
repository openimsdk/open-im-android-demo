package io.openim.android.ouicore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import java.io.File;
import java.lang.ref.SoftReference;
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
    private String targetUrl, fileName;
    private DownloadTask task;
    private DownloadListener1 downloadListener;
    public boolean completed=false;

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
                if (isCompleted()) {
                    OpenFileUtil.openFile(getContext(), Constant.File_DIR + fileName);
                } else {
                    if (getStatus() == StatusUtil.Status.RUNNING) {
                        task.cancel();
                    } else
                        download();
                }
            }
        });

    }

    private void buildTask() {
        task =
            new DownloadTask.Builder(targetUrl, new File(Constant.File_DIR))
                .setFilename(fileName)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(30).build();
        DownloadTask lastTask = OkDownload.with().downloadDispatcher().findSameTask(task);
        if (null != lastTask) {
            task = lastTask;
        }
    }

    private void buildDownloadListener() {
        downloadListener = new DownloadListener(this);
    }

    public void completed() {
        completed=true;
        bgMask.setVisibility(GONE);
        downBtn.setVisibility(GONE);
        circlePgBar.setVisibility(GONE);
    }

    private void download() {
        if (task.getListener() != null)
            downloadListener = (DownloadListener1) task.getListener();
        else
            buildDownloadListener();

        task.enqueue(downloadListener);
    }

    public void setDownBtn(boolean isDown) {
        downBtn.setImageResource(isDown ?
            R.mipmap.ic_file_download_pause2 : R.mipmap.ic_file_download);
    }

    public void setRes(String url) {
        targetUrl = url;
        fileName = Common.md5(targetUrl) +
            "." + GetFilePathFromUri.getFileSuffix(targetUrl);
        if (MediaFileUtil.isZIP(url)) res.setImageResource(R.mipmap.ic_file_zip);
        else if (MediaFileUtil.isWord(url)) res.setImageResource(R.mipmap.ic_file_word);
        else if (MediaFileUtil.isPPT(url)) res.setImageResource(R.mipmap.ic_file_ppt);
        else if (MediaFileUtil.isPDF(url)) res.setImageResource(R.mipmap.ic_file_pdf);
        else res.setImageResource(R.mipmap.ic_file_unknown);

        buildTask();

        bgMask.setVisibility(isCompleted() ? GONE : VISIBLE);
        circlePgBar.setVisibility(isCompleted() ? GONE : VISIBLE);
        downBtn.setVisibility(isCompleted() ? GONE : VISIBLE);

        if (getStatus() == StatusUtil.Status.RUNNING
            || getStatus() == StatusUtil.Status.PENDING) {
            setDownBtn(true);
            ((DownloadListener) task.getListener()).replaceUpdateView(this);
        }
    }

   public boolean isCompleted() {
        return completed=getStatus() == StatusUtil.Status.COMPLETED;
    }

    StatusUtil.Status getStatus() {
        return StatusUtil.getStatus(targetUrl, Constant.File_DIR, fileName);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        if (null != task)
//            task.cancel();
    }

    private static class DownloadListener extends DownloadListener1 {
        SoftReference<FileDownloadView> softReference;

        public DownloadListener(FileDownloadView fileDownloadView) {
            this.softReference = new SoftReference<>(fileDownloadView);
        }

        public void replaceUpdateView(FileDownloadView fileDownloadView) {
            this.softReference.clear();
            this.softReference = new SoftReference<>(fileDownloadView);
        }

        @Override
        public void taskStart(@NonNull DownloadTask task,
                              @NonNull Listener1Assist.Listener1Model model) {
            if (null != softReference.get())
                softReference.get().setDownBtn(true);
        }

        @Override
        public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {

        }

        @Override
        public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset,
                              long totalLength) {
            if (null != softReference.get())
                softReference.get().setDownBtn(true);
        }

        @Override
        public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
            int current =
                BigDecimal.valueOf(currentOffset).divide(BigDecimal.valueOf(totalLength), 2,
                    BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).intValue();
            if (null != softReference.get())
                softReference.get().circlePgBar.setTargetProgress(current);

            if (current == 100) {
                if (null != softReference.get())
                    softReference.get().completed();
            }
        }

        @Override
        public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
                            @Nullable Exception realCause,
                            @NonNull Listener1Assist.Listener1Model model) {
            if (cause == EndCause.COMPLETED) {
                if (null != softReference.get())
                    softReference.get().completed();
            } else {
                if (null != softReference.get())
                    softReference.get().setDownBtn(false);
            }
        }
    }
}
