package io.openim.android.ouimoments.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.JsonParser;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.services.NiService;
import io.openim.android.ouicore.services.OneselfService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.MThreadTool;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.PhotographAlbumDialog;
import io.openim.android.ouicore.widget.SpacesItemDecoration;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.bean.MomentsMeta;
import io.openim.android.ouimoments.databinding.ActivityPushMomentsBinding;
import io.openim.android.ouimoments.databinding.ItemRoundImgBinding;
import io.openim.android.ouimoments.mvp.presenter.PushMomentsVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnFileUploadProgressListener;
import io.openim.android.sdk.models.Message;

public class PushMomentsActivity extends BaseActivity<PushMomentsVM, ActivityPushMomentsBinding> {

    private RecyclerViewAdapter<Object, RoundIMGItem> adapter;
    private PhotographAlbumDialog albumDialog;
    private WaitDialog waitDialog;
    GroupVM groupVM = new GroupVM();
    //上传文件成功数量
    public int successNum = 0;
    private BottomPopDialog bottomPopDialog;

    private boolean hasStorage, hasShoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(PushMomentsVM.class, true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPushMomentsBinding.inflate(getLayoutInflater()));
        sink();
        init();
        vmInit();

        initView();
        listener();
    }

    private void init() {
        MThreadTool.executorService.execute(() -> {
            hasStorage = AndPermission.hasPermissions(this, Permission.Group.STORAGE);
            hasShoot = AndPermission.hasPermissions(this, Permission.CAMERA,
                Permission.RECORD_AUDIO);
        });
    }

    private void vmInit() {
        vm.isPhoto = getIntent().getBooleanExtra(Constant.K_RESULT, true);
        vm.init();
    }

    private List<SelectDataActivity.RuleData> selectedAtList;
    private ActivityResultLauncher<Intent> ruleDataLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK) return;
            selectedAtList =
                (List<SelectDataActivity.RuleData>) result.getData().getSerializableExtra(Constant.K_RESULT);
            view.reminderWho.setText(vm.getRuleDataNames(selectedAtList));

            List<PushMomentsVM.UserOrGroup> userOrGroups = new ArrayList<>();
            for (SelectDataActivity.RuleData ruleData : selectedAtList) {
                PushMomentsVM.UserOrGroup userOrGroup = new PushMomentsVM.UserOrGroup();
                userOrGroup.userID = ruleData.id;
                userOrGroup.userName = ruleData.name;
                userOrGroups.add(userOrGroup);
            }
            vm.param.getValue().atUserList = userOrGroups;
        });
    private ActivityResultLauncher<Intent> resultLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (vm.param.getValue().permission == 0)
                    view.whoSee.setText(io.openim.android.ouicore.R.string.str_public);
                else if (vm.param.getValue().permission == 1)
                    view.whoSee.setText(io.openim.android.ouicore.R.string.str_private);
                else {
                    List<SelectDataActivity.RuleData> ruleDataList = new ArrayList<>();
                    if (null != vm.selectedGroupRuleDataList)
                        ruleDataList.addAll(vm.selectedGroupRuleDataList);
                    if (null != vm.selectedUserRuleDataList)
                        ruleDataList.addAll(vm.selectedUserRuleDataList);
                    if (!ruleDataList.isEmpty())
                        view.whoSee.setText(vm.getRuleDataNames(ruleDataList));
                }
            }
        });
    private OnDedrepClickListener selectUserClick = new OnDedrepClickListener() {
        @Override
        public void click(View v) {
            if (groupVM.exUserInfo.getValue().isEmpty()) {
                waitDialog.show();
                groupVM.getAllFriend();
            } else jumpSelectUser(groupVM.exUserInfo.getValue());
        }
    };

    @Override
    public void onSuccess(Object body) {
        waitDialog.dismiss();
        setResult(RESULT_OK);
        finish();
    }

    private OnDedrepClickListener submitClick = new OnDedrepClickListener() {
        @Override
        public void click(View v) {
            waitDialog.setNotDismiss();
            waitDialog.show();
            final List<String> paths = new ArrayList<>();
            try {
                if (vm.isPhoto) paths.addAll(vm.getMediaPaths());
                else {
                    String original = vm.param.getValue().content.data.metas.get(0).original;
                    paths.add(original);
                }
            } catch (Exception ignored) {
            }
            if (paths.isEmpty()) {
                vm.pushMoments();
                return;
            }
            MThreadTool.executorService.execute(() -> {
                for (String path : paths) {
                    OpenIMClient.getInstance().uploadFile(new OnFileUploadProgressListener() {
                        @Override
                        public void onError(int code, String error) {
                            runOnUiThread(() -> {
                                waitDialog.dismiss();
                                toast(getString(io.openim.android.ouicore.R.string.upload_err));
                            });
                        }

                        @Override
                        public void onProgress(long progress) {

                        }

                        @Override
                        public void onSuccess(String s) {
                            if (!vm.isPhoto) {
                                vm.param.getValue().content.data.metas.get(0).original = s;
                            } else {
                                MomentsMeta momentsMeta = new MomentsMeta();
                                momentsMeta.original = s;
                                momentsMeta.thumb = s;
                                vm.param.getValue().content.data.metas.add(momentsMeta);
                            }
                            successNum++;
                            if (successNum >= paths.size()) {
                                runOnUiThread(() -> {
                                    vm.pushMoments();
                                });
                            }
                        }
                    }, path);
                }
            });

        }
    };

    private void listener() {
        groupVM.exUserInfo.observe(this, exUserInfos -> {
            waitDialog.dismiss();
            jumpSelectUser(exUserInfos);
        });

        view.whoSeeLy.setOnClickListener(v -> {
            resultLauncher.launch(new Intent(this, WhoSeeActivity.class));
        });
        view.reminderWhoLy.setOnClickListener(selectUserClick);

        vm.selectMedia.observe(this, objects -> {
            setFinishEnable();
            adapter.setItems(objects);
        });
        view.content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                vm.param.getValue().content.data.text = s.toString();
                setFinishEnable();
                view.restriction.setText(s.length() + "/500");
            }
        });
    }

    private void setFinishEnable() {
        boolean isEnable;
        if (vm.isPhoto)
            isEnable =
                !vm.getMediaPaths().isEmpty() || !TextUtils.isEmpty(vm.param.getValue().content.data.text);
        else
            isEnable =
                !vm.getMediaPaths().isEmpty() && !TextUtils.isEmpty(vm.param.getValue().content.data.text);
        view.finish.setAlpha(isEnable ? 1 : 0.3f);
        view.finish.setOnClickListener(isEnable ? submitClick : null);
    }

    private void jumpSelectUser(List<ExUserInfo> exUserInfo) {
        if (exUserInfo.isEmpty()) return;
        List<SelectDataActivity.RuleData> ruleDataList = vm.buildUserRuleData(exUserInfo,
            selectedAtList);
        ruleDataLauncher.launch(new Intent(this, SelectDataActivity.class).putExtra(Constant.K_NAME, getString(io.openim.android.ouicore.R.string.select_user)).putExtra(Constant.K_RESULT, (Serializable) ruleDataList).putExtra(Constant.K_FROM, false).putExtra(Constant.K_SIZE, 20));
    }

    private ActivityResultLauncher<Intent> videoLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK) return;
            Intent data = result.getData();
            List<String> files = Matisse.obtainPathResult(data);
            String path = files.get(0);
            Glide.with(this).asBitmap().load(path).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<?
                    super Bitmap> transition) {
                    String firstFame = MediaFileUtil.saveBitmap(resource, Constant.PICTURE_DIR,
                        false);
                    addParamMetas(path, firstFame);
                    vm.addRes(firstFame);
                }
            });
        });

    private final ActivityResultLauncher<Intent> shootLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                String fileUrl = result.getData().getStringExtra("fileUrl");
                if (MediaFileUtil.isVideoType(fileUrl)) {
                    String firstFrameUrl = result.getData().getStringExtra("firstFrameUrl");
                    addParamMetas(fileUrl, firstFrameUrl);
                    vm.addRes(firstFrameUrl);
                }
            }
        });

    private void addParamMetas(String path, String firstFame) {
        MomentsMeta momentsMeta = new MomentsMeta();
        momentsMeta.original = path;
        momentsMeta.thumb = firstFame;
        vm.param.getValue().content.data.metas.clear();
        vm.param.getValue().content.data.metas.add(momentsMeta);
    }

    private void initView() {

        waitDialog = new WaitDialog(this);
        if (vm.isPhoto) albumDialog = new PhotographAlbumDialog(PushMomentsActivity.this);
        else {
            bottomPopDialog = new BottomPopDialog(this);
            bottomPopDialog.getMainView().menu3.setOnClickListener(v1 -> bottomPopDialog.dismiss());

            bottomPopDialog.getMainView().menu1.setOnClickListener(v1 -> {
                Common.permission(this, new Common.OnGrantedListener() {
                    @Override
                    public void onGranted() {
                        hasStorage = true;
                        Matisse.from(PushMomentsActivity.this).choose(MimeType.ofVideo()).countable(true).showSingleMediaType(true).maxSelectable(1).restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED).thumbnailScale(0.85f).imageEngine(new GlideEngine()).forResult(videoLauncher);
                    }
                }, hasStorage, Permission.Group.STORAGE);

            });
            bottomPopDialog.getMainView().menu2.setOnClickListener(v1 -> {
                Common.permission(this, () -> {
                    hasShoot = true;
                    Postcard postcard = ARouter.getInstance().build(Routes.Conversation.SHOOT);
                    LogisticsCenter.completion(postcard);
                    shootLauncher.launch(new Intent(PushMomentsActivity.this,
                        postcard.getDestination()).putExtra(Constant.K_RESULT, 0x102));
                }, hasShoot, Permission.Group.STORAGE);
            });
        }

        view.title.setText(vm.isPhoto ? io.openim.android.ouicore.R.string.push_photo :
            io.openim.android.ouicore.R.string.push_video);
        view.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        view.recyclerView.setAdapter(adapter =
            new RecyclerViewAdapter<Object, RoundIMGItem>(RoundIMGItem.class) {

                @Override
                public void onBindView(@NonNull RoundIMGItem holder, Object data, int position) {
                    if (data instanceof Integer) {
                        holder.view.playBtn.setVisibility(View.GONE);
                        //表示添加按钮
                        holder.view.roundImageView.setOnClickListener(v -> {
                            if (vm.isPhoto) {
                                albumDialog.show();
                                albumDialog.setMaxSelectable(9);
                                albumDialog.setToCrop(false);
                                albumDialog.setOnSelectResultListener(paths -> {
                                    albumDialog.dismiss();
                                    for (String path : paths) {
                                        vm.addRes(path);
                                    }
                                });
                            } else {
                                bottomPopDialog.show();
                            }
                        });
                        holder.view.delete.setVisibility(View.INVISIBLE);
                    } else {
                        holder.view.playBtn.setVisibility(vm.isPhoto ? View.GONE : View.VISIBLE);
                        holder.view.delete.setVisibility(View.VISIBLE);
                        holder.view.delete.setOnClickListener(v -> {
                            vm.removeRes(position);
                        });
                        holder.view.roundImageView.setOnClickListener(v -> {
                            if (vm.isPhoto) {
                                List<String> photoUrls = new ArrayList<>();
                                for (Object o : vm.selectMedia.getValue()) {
                                    if (o instanceof String) photoUrls.add((String) o);
                                }
                                ImagePagerActivity.startImagePagerActivity(PushMomentsActivity.this,
                                    photoUrls, position, null);
                            } else {
                                try {
                                    ARouter.getInstance().build(Routes.Conversation.PREVIEW).withString("media_url", vm.param.getValue().content.data.metas.get(0).original).withString("first_frame", vm.param.getValue().content.data.metas.get(0).thumb).navigation();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    Glide.with(PushMomentsActivity.this).load(data).centerInside().into(holder.view.roundImageView);
                }
            });
        adapter.setItems(vm.selectMedia.getValue());
    }

    public static class RoundIMGItem extends RecyclerView.ViewHolder {

        private final ItemRoundImgBinding view;

        public RoundIMGItem(@NonNull View itemView) {
            super(ItemRoundImgBinding.inflate(LayoutInflater.from(itemView.getContext()),
                (ViewGroup) itemView, false).getRoot());
            view = ItemRoundImgBinding.bind(this.itemView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCacheVM();
    }

}
