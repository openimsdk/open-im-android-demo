package io.openim.android.ouiconversation.widget;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.FragmentInputExpandBinding;
import io.openim.android.ouiconversation.databinding.ItemExpandMenuBinding;
import io.openim.android.ouiconversation.ui.ShootActivity;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.MThreadTool;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.widget.WebViewActivity;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.SignalingInfo;


public class InputExpandFragment extends BaseFragment<ChatVM> {
    public static List<Integer> menuIcons = Arrays.asList(io.openim.android.ouicore.R.mipmap.ic_chat_photo, R.mipmap.ic_chat_shoot, R.mipmap.ic_tools_video_call, R.mipmap.ic_chat_menu_file, R.mipmap.ic_chat_location, R.mipmap.ic_business_card);
    public static List<String> menuTitles = Arrays.asList(BaseApp.inst().getString(io.openim.android.ouicore.R.string.album), BaseApp.inst().getString(io.openim.android.ouicore.R.string.shoot), BaseApp.inst().getString(io.openim.android.ouicore.R.string.video_calls), BaseApp.inst().getString(io.openim.android.ouicore.R.string.file), BaseApp.inst().getString(io.openim.android.ouicore.R.string.location), BaseApp.inst().getString(io.openim.android.ouicore.R.string.business_card));

    FragmentInputExpandBinding v;
    //权限
    boolean hasStorage, hasShoot, hasLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MThreadTool.executorService.execute(() -> {
            hasStorage = AndPermission.hasPermissions(getActivity(), Permission.Group.STORAGE);
            hasShoot = AndPermission.hasPermissions(getActivity(), Permission.CAMERA, Permission.RECORD_AUDIO);
            hasLocation = AndPermission.hasPermissions(getActivity(), Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION);
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = FragmentInputExpandBinding.inflate(inflater);
        init();
        return v.getRoot();
    }

    private void init() {
        v.getRoot().setLayoutManager(new GridLayoutManager(getContext(), 4));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter<Object, ExpandHolder>(ExpandHolder.class) {

            @Override
            public void onBindView(@NonNull ExpandHolder holder, Object data, int position) {
                holder.v.menu.setCompoundDrawablesRelativeWithIntrinsicBounds(null, getContext().getDrawable(menuIcons.get(position)), null, null);
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
                            goToCall();
                            break;
                        case 3:
                            gotoSelectFile();
                            break;
                        case 4:
                            gotoShareLocation();
                            break;
                        case 5:
                            Postcard postcard = ARouter.getInstance().build(Routes.Contact.ALL_FRIEND);
                            LogisticsCenter.completion(postcard);
                            businessCardLauncher.launch(new Intent(getContext(), postcard.getDestination()).putExtra("formChat", true));
                            break;
                    }
                });
            }
        };
        v.getRoot().setAdapter(adapter);
        adapter.setItems(menuIcons);
    }

    @SuppressLint("WrongConstant")
    private void goToCall() {
        Common.permission(getContext(), () -> {
            hasStorage = true;
            CallingService callingService = (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
            if (null == callingService) return;
            IMUtil.showBottomPopMenu(getContext(), (v1, keyCode, event) -> {
                vm.isVideoCall = keyCode != 1;
                if (vm.isSingleChat) {
                    List<String> ids = new ArrayList<>();
                    ids.add(vm.userID);
                    SignalingInfo signalingInfo = IMUtil.buildSignalingInfo(vm.isVideoCall, vm.isSingleChat, ids, null);
                    callingService.call(signalingInfo);
                } else {
                    toSelectMember();
                }
                return false;
            });
        }, hasStorage, Permission.Group.STORAGE);
    }

    public void toSelectMember() {
            GroupVM groupVM = new GroupVM();
            groupVM.groupId = vm.groupID;
            BaseApp.inst().putVM(groupVM);
            ARouter.getInstance().build(Routes.Group.SUPER_GROUP_MEMBER)
                .withBoolean(Constant.IS_SELECT_MEMBER, true)
                .withInt(Constant.K_SIZE, 9).navigation(getActivity(), Constant.Event.CALLING_REQUEST_CODE);
    }

    private final ActivityResultLauncher<Intent> businessCardLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK) return;
        String friendInfo = result.getData().getStringExtra(Constant.K_RESULT);

        FriendInfo friendInfoBean = GsonHel.fromJson(friendInfo, FriendInfo.class);
        Map<String, String> bean = new HashMap();
        bean.put("userID", friendInfoBean.getUserID());
        bean.put("nickname", friendInfoBean.getNickname());
        bean.put("faceURL", friendInfoBean.getFaceURL());
        Message message = OpenIMClient.getInstance().messageManager.createCardMessage(GsonHel.toJson(bean));
        vm.sendMsg(message);
    });

    private final ActivityResultLauncher<Intent> shareLocationLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK) return;
        Bundle resultBundle = result.getData().getBundleExtra("result");
        if (null == resultBundle) return;

        Double latitude = resultBundle.getDouble("latitude");
        Double longitude = resultBundle.getDouble("longitude");
        String description = resultBundle.getString("description");
        Message message = OpenIMClient.getInstance().messageManager.createLocationMessage(latitude, longitude, description);
        vm.sendMsg(message);
    });

    //分享位置
    @SuppressLint("WrongConstant")
    private void gotoShareLocation() {
        if (hasLocation) {
            shareLocationLauncher.launch(new Intent(getActivity(), WebViewActivity.class).putExtra(WebViewActivity.ACTION, WebViewActivity.LOCATION));
        } else {
            AndPermission.with(this).runtime().permission(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION).onDenied(data -> {
            }).onGranted(data -> {
                hasLocation = true;
                shareLocationLauncher.launch(new Intent(getActivity(), WebViewActivity.class).putExtra(WebViewActivity.ACTION, WebViewActivity.LOCATION));
            }).start();
        }

    }

    private void gotoSelectFile() {
        Common.permission(getContext(), () -> {
            hasStorage = true;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            fileLauncher.launch(intent);
        }, hasStorage, Permission.Group.STORAGE);
    }

    //去拍摄
    private void goToShoot() {
        if (hasShoot)
            shootLauncher.launch(new Intent(getActivity(), ShootActivity.class));
        else {
            AndPermission.with(this).runtime().permission(Permission.CAMERA, Permission.RECORD_AUDIO).onGranted(permissions -> {
                // Storage permission are allowed.
                hasShoot = true;
                shootLauncher.launch(new Intent(getActivity(), ShootActivity.class));
            }).onDenied(permissions -> {
                // Storage permission are not allowed.
            }).start();
        }
    }

    private final ActivityResultLauncher<Intent> fileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (null != data) {
                Uri uri = data.getData();
                if (null != uri) {
                    String filePath = GetFilePathFromUri.getFileAbsolutePath(getContext(), uri);
                    if (MediaFileUtil.isImageType(filePath)) {
                        Message msg = OpenIMClient.getInstance().messageManager.createImageMessageFromFullPath(filePath);
                        vm.sendMsg(msg);
                        return;
                    }
                    if (MediaFileUtil.isVideoType(filePath)) {
                        Glide.with(this).asBitmap().load(filePath).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                String firstFame = MediaFileUtil.saveBitmap(resource, Constant.PICTURE_DIR);
                                long duration = MediaFileUtil.getDuration(filePath);
                                Message msg = OpenIMClient.getInstance().messageManager.createVideoMessageFromFullPath(filePath, MediaFileUtil.getFileType(filePath).mimeType, duration, firstFame);
                                vm.sendMsg(msg);
                            }
                        });
                        return;
                    }
                    if (null != filePath) {
                        Message msg = OpenIMClient.getInstance().messageManager.createFileMessageFromFullPath(filePath, new File(filePath).getName());
                        vm.sendMsg(msg);
                    }
                }
            }
        }
    });
    private final ActivityResultLauncher<Intent> captureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            List<String> files = Matisse.obtainPathResult(data);

            for (String file : files) {
                Message msg = null;
                if (MediaFileUtil.isImageType(file)) {
                    msg = OpenIMClient.getInstance().messageManager.createImageMessageFromFullPath(file);
                }
                if (MediaFileUtil.isVideoType(file)) {
                    Glide.with(this).asBitmap().load(file).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            String firstFame = MediaFileUtil.saveBitmap(resource, Constant.PICTURE_DIR);
                            long duration = MediaFileUtil.getDuration(file);
                            Message msg = OpenIMClient.getInstance().messageManager.createVideoMessageFromFullPath(file, MediaFileUtil.getFileType(file).mimeType, duration, firstFame);
                            vm.sendMsg(msg);
                        }
                    });
                    continue;
                }
                if (null == msg)
                    msg = OpenIMClient.getInstance().messageManager.createTextMessage("[" + getString(R.string.unsupported_type) + "]");
                vm.sendMsg(msg);
            }
        }
    });

    private final ActivityResultLauncher<Intent> shootLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            String fileUrl = result.getData().getStringExtra("fileUrl");
            if (MediaFileUtil.isImageType(fileUrl)) {
                Message msg = OpenIMClient.getInstance().messageManager.createImageMessageFromFullPath(fileUrl);
                vm.sendMsg(msg);
            }
            if (MediaFileUtil.isVideoType(fileUrl)) {
                String firstFrameUrl = result.getData().getStringExtra("firstFrameUrl");
                MediaFileUtil.MediaFileType mediaFileType = MediaFileUtil.getFileType(fileUrl);
                long duration = MediaFileUtil.getDuration(fileUrl);
                Message msg = OpenIMClient.getInstance().messageManager.createVideoMessageFromFullPath(fileUrl, mediaFileType.mimeType, duration, firstFrameUrl);
                vm.sendMsg(msg);
            }
        }
    });

    @SuppressLint("WrongConstant")
    private void showMediaPicker() {
        if (hasStorage) goMediaPicker();
        else
            AndPermission.with(this).runtime().permission(Permission.Group.STORAGE).onGranted(permissions -> {
                // Storage permission are allowed.
                hasStorage = true;
                goMediaPicker();
            }).onDenied(permissions -> {
                // Storage permission are not allowed.
            }).start();
    }


    private void goMediaPicker() {
        Matisse.from(getActivity()).choose(MimeType.ofAll()).countable(true).maxSelectable(9).restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED).thumbnailScale(0.85f).imageEngine(new GlideEngine()).forResult(captureLauncher);
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
