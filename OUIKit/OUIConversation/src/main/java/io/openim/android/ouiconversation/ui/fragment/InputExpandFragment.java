package io.openim.android.ouiconversation.ui.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hjq.permissions.Permission;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.FragmentInputExpandBinding;
import io.openim.android.ouiconversation.databinding.ItemExpandMenuBinding;
import io.openim.android.ouiconversation.ui.ChatActivity;
import io.openim.android.ouiconversation.ui.ShootActivity;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.LayoutCommonDialogBinding;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.GlideEngine;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.MThreadTool;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.WebViewActivity;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.CardElem;
import io.openim.android.sdk.models.Message;

public class InputExpandFragment extends BaseFragment<ChatVM> {
    public static List<Integer> menuIcons =
        Arrays.asList(io.openim.android.ouicore.R.mipmap.ic_chat_photo, R.mipmap.ic_chat_shoot, R.mipmap.ic_chat_menu_file, R.mipmap.ic_chat_location,
            R.mipmap.ic_business_card);
    public static List<String> menuTitles =
        Arrays.asList(BaseApp.inst().getString(io.openim.android.ouicore.R.string.album),
            BaseApp.inst().getString(io.openim.android.ouicore.R.string.shoot),
            BaseApp.inst().getString(io.openim.android.ouicore.R.string.file),
            BaseApp.inst().getString(io.openim.android.ouicore.R.string.location),
            BaseApp.inst().getString(io.openim.android.ouicore.R.string.business_card));

    FragmentInputExpandBinding v;
    private int currentAvailableCameraNum = 0;
    // permissions
    private HasPermissions hasStorage, hasLocation, hasShoot;
    private CameraManager cameraManager;
    private final CameraManager.AvailabilityCallback availableCameraListener = new CameraManager.AvailabilityCallback() {
        @Override
        public void onCameraAvailable(@NonNull String cameraId) {
            ++ currentAvailableCameraNum;
        }

        @Override
        public void onCameraUnavailable(@NonNull String cameraId) {
            -- currentAvailableCameraNum;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MThreadTool.executorService.execute(() -> {
            hasStorage = new HasPermissions(getActivity(), Permission.MANAGE_EXTERNAL_STORAGE);
            hasLocation = new HasPermissions(getActivity(), Permission.ACCESS_FINE_LOCATION);
            hasShoot = new HasPermissions(getActivity(), Permission.CAMERA, Permission.RECORD_AUDIO);
        });

        if (getActivity() != null) {
            cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            cameraManager.registerAvailabilityCallback(availableCameraListener, null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraManager != null)
            cameraManager.unregisterAvailabilityCallback(availableCameraListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v = FragmentInputExpandBinding.inflate(inflater);
        init();
        return v.getRoot();
    }

    private void init() {
        v.getRoot().setLayoutManager(new GridLayoutManager(getContext(), 4));
        RecyclerViewAdapter adapter =
            new RecyclerViewAdapter<Object, ExpandHolder>(ExpandHolder.class) {

                @Override
                public void onBindView(@NonNull ExpandHolder holder, Object data, int position) {
                    holder.v.menu.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        getContext().getDrawable(menuIcons.get(position)), null, null);
                    holder.v.menu.setText(menuTitles.get(position));
                    holder.v.menu.setOnClickListener(v -> {
                        switch (position) {
                            case 0:
                                showMediaPicker();
                                break;
                            case 1:
                                goToShoot();
                                break;
                            case 2:
                                gotoSelectFile();
                                break;
                            case 3:
                                gotoShareLocation();
                                break;
                            case 4:
                                SelectTargetVM selectTargetVM =
                                    Easy.installVM(SelectTargetVM.class);
                                selectTargetVM.setIntention(SelectTargetVM.Intention.singleSelect);
                                selectTargetVM.setOnFinishListener(() -> {
                                    Common.finishRoute(Routes.Group.SELECT_TARGET,
                                        Routes.Contact.ALL_FRIEND);
                                    Activity activity = ActivityManager.isExist(ChatActivity.class);
                                    if (null == activity) return;
                                    CommonDialog commonDialog = new CommonDialog(activity);
                                    LayoutCommonDialogBinding mainView = commonDialog.getMainView();
                                    mainView.tips.setText(BaseApp.inst().getString(io.openim.android.ouicore.R.string.send_card_confirm));
                                    mainView.cancel.setOnClickListener(v1 -> commonDialog.dismiss());
                                    mainView.confirm.setOnClickListener(v1 -> {
                                        commonDialog.dismiss();

                                        sendCardMessage(selectTargetVM.metaData.val().get(0));
                                    });
                                    commonDialog.show();
                                });
                                ARouter.getInstance().build(Routes.Group.SELECT_TARGET).navigation();
                                break;
                        }
                    });
                }
            };
        v.getRoot().setAdapter(adapter);
        adapter.setItems(menuIcons);
    }

    private void sendCardMessage(MultipleChoice multipleChoice) {
        CardElem cardElem = new CardElem();
        cardElem.setUserID(multipleChoice.key);
        cardElem.setNickname(multipleChoice.name);
        cardElem.setFaceURL(multipleChoice.icon);
        Message message = OpenIMClient.getInstance().messageManager.createCardMessage(cardElem);
        vm.sendMsg(message);
    }

    private final ActivityResultLauncher<Intent> shareLocationLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK) return;
            Bundle resultBundle = result.getData().getBundleExtra("result");
            if (null == resultBundle) return;

            Double latitude = resultBundle.getDouble("latitude");
            Double longitude = resultBundle.getDouble("longitude");
            String description = resultBundle.getString("description");
            Message message =
                OpenIMClient.getInstance().messageManager.createLocationMessage(latitude, longitude,
                    description);
            vm.sendMsg(message);
        });

    //分享位置
    @SuppressLint("WrongConstant")
    private void gotoShareLocation() {
        if (TextUtils.isEmpty(WebViewActivity.mapAppKey)) {
            toast(BaseApp.inst().getString(io.openim.android.ouicore.R.string.no_map_key));
            return;
        }
        hasLocation.safeGo(() -> {
            shareLocationLauncher.launch(new Intent(getActivity(), WebViewActivity.class).putExtra(WebViewActivity.ACTION, WebViewActivity.LOCATION));
        });
    }

    private void gotoSelectFile() {
        hasStorage.safeGo(() -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            fileLauncher.launch(intent);
        });
    }

    // open camera
    private void goToShoot() {
        hasShoot.safeGo(() -> {
            try {
                int totalAvailableCameraNum = cameraManager.getCameraIdList().length;
                if (totalAvailableCameraNum == currentAvailableCameraNum)
                    shootLauncher.launch(new Intent(getActivity(), ShootActivity.class));
                else
                    toast(BaseApp.inst().getString(io.openim.android.ouicore.R.string.camera_is_occupied));
            } catch (Exception e) {
                L.e(e.getMessage());
            }
        });
    }

    private final ActivityResultLauncher<Intent> fileLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (null != data) {
                    Uri uri = data.getData();
                    if (null != uri) {
                        String filePath = GetFilePathFromUri.getFileAbsolutePath(getContext(), uri);
                        if (TextUtils.isEmpty(filePath)) return;
                        if (MediaFileUtil.isImageType(filePath)) {
                            Message msg =
                                OpenIMClient.getInstance().messageManager.createImageMessageFromFullPath(filePath);
                            vm.sendMsg(msg);
                            return;
                        }
                        if (MediaFileUtil.isVideoType(filePath)) {
                            Glide.with(this).asBitmap().load(filePath).into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource,
                                                            @Nullable Transition<? super Bitmap> transition) {
                                    String firstFame = MediaFileUtil.saveBitmap(resource,
                                        Constants.PICTURE_DIR, false);
                                    long duration = MediaFileUtil.getDuration(filePath) / 1000;
                                    Message msg =
                                        OpenIMClient.getInstance().messageManager.createVideoMessageFromFullPath(filePath, MediaFileUtil.getFileType(filePath).mimeType, duration, firstFame);
                                    vm.sendMsg(msg);
                                }
                            });
                            return;
                        }
                        Message msg =
                            OpenIMClient.getInstance().messageManager.createFileMessageFromFullPath(filePath, new File(filePath).getName());
                        if (null != msg)
                            vm.sendMsg(msg);
                    }
                }
            }
        });

    private final ActivityResultLauncher<Intent> captureLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            try {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    ArrayList<LocalMedia> files = PictureSelector.obtainSelectorList(data);

                    for (LocalMedia file : files) {
                        String path = GetFilePathFromUri.getFileAbsolutePath(InputExpandFragment.this.getActivity() ,Uri.parse(file.getAvailablePath()));
                        Message msg = null;
                        if (MediaFileUtil.isImageType(path)) {
                            msg =
                                OpenIMClient.getInstance().messageManager.createImageMessageFromFullPath(path);
                        }
                        if (MediaFileUtil.isVideoType(path)) {
                            Glide.with(this).asBitmap().load(path).into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource,
                                                            @Nullable Transition<? super Bitmap> transition) {
                                    String firstFame = MediaFileUtil.saveBitmap(resource,
                                        Constants.PICTURE_DIR, false);
                                    long duration = MediaFileUtil.getDuration(path) / 1000;
                                    Message msg =
                                        OpenIMClient.getInstance().messageManager.createVideoMessageFromFullPath(path, MediaFileUtil.getFileType(path).mimeType, duration, firstFame);
                                    vm.sendMsg(msg);
                                }
                            });
                            continue;
                        }
                        if (null == msg)
                            msg =
                                OpenIMClient.getInstance().messageManager.createTextMessage("[" + getString(io.openim.android.ouicore.R.string.unsupported_type) + "]");
                        vm.sendMsg(msg);
                    }
                }
            } catch (Exception e) {
                L.e(e.getMessage());
            }
        });

    private final ActivityResultLauncher<Intent> shootLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                String fileUrl = result.getData().getStringExtra("fileUrl");
                if (MediaFileUtil.isImageType(fileUrl)) {
                    Message msg =
                        OpenIMClient.getInstance().messageManager.createImageMessageFromFullPath(fileUrl);
                    vm.sendMsg(msg);
                }
                if (MediaFileUtil.isVideoType(fileUrl)) {
                    String firstFrameUrl = result.getData().getStringExtra("firstFrameUrl");
                    MediaFileUtil.MediaFileType mediaFileType = MediaFileUtil.getFileType(fileUrl);
                    long duration = (long) Math.ceil((double) MediaFileUtil.getDuration(fileUrl) / 1000);
                    Message msg =
                        OpenIMClient.getInstance().messageManager.createVideoMessageFromFullPath(fileUrl, mediaFileType.mimeType, duration, firstFrameUrl);
                    vm.sendMsg(msg);
                }
            }
        });

    @SuppressLint("unchecked")
    private void showMediaPicker() {
        hasStorage.safeGo(() -> {
            try {
                PictureSelector.create(this)
                    .openGallery(SelectMimeType.ofAll())
                    .setImageEngine(GlideEngine.createGlideEngine())
                    .setMaxVideoSelectNum(9)
                    .setMaxSelectNum(9)
                    .forResult(captureLauncher);
            } catch (Exception e) {
                L.e(e.getMessage());
            }
        });
    }


    public void setChatVM(ChatVM vm) {
        this.vm = vm;
    }

    public static class ExpandHolder extends RecyclerView.ViewHolder {
        public ItemExpandMenuBinding v;

        public ExpandHolder(@NonNull View itemView) {
            super(ItemExpandMenuBinding.inflate(LayoutInflater.from(itemView.getContext())).getRoot());
            v = ItemExpandMenuBinding.bind(this.itemView);
        }
    }
}
