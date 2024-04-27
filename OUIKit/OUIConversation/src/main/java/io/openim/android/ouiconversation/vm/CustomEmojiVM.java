package io.openim.android.ouiconversation.vm;

import static io.openim.android.ouicore.utils.Common.UIHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.entity.CustomEmoji;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.PictureInfo;
import io.realm.RealmResults;

public class CustomEmojiVM extends BaseVM {
    public static final int addID = io.openim.android.ouicore.R.mipmap.ic_add3;
    public final State<List<CustomEmoji>> customEmojis = new State<>(new ArrayList<>());
    public final State<List<String>> deletedEmojiUrl =new  State<>(new ArrayList<>());

    public void addDeleted(String url) {
        if (!isDeletedList(url)) {
            deletedEmojiUrl.val().add(url);
            deletedEmojiUrl.update();
        }
    }
    public void removeDeleted(String url) {
        if (isDeletedList(url)) {
            deletedEmojiUrl.val().remove(url);
            deletedEmojiUrl.update();
        }
    }

    public boolean isDeletedList(String url) {
        return deletedEmojiUrl.getValue().contains(url);
    }

    public void toDelete() {
        BaseApp.inst().realm.executeTransactionAsync(realm -> {
            realm.where(CustomEmoji.class).equalTo("userID",
                BaseApp.inst().loginCertificate.userID).in("sourceUrl",
                deletedEmojiUrl.val().toArray(new String[]{})).findAll().deleteAllFromRealm();
            deletedEmojiUrl.val().clear();
            deletedEmojiUrl.postValue(deletedEmojiUrl.val());
            UIHandler.post(this::loadCustomEmoji);
        });
    }
    public void insertEmojiDb(Message message){
        PictureInfo pictureInfo = message.getPictureElem().getSourcePicture();
        PictureInfo snapshotPicture = message.getPictureElem().getSnapshotPicture();
        if (null == pictureInfo) return;
        CustomEmoji  customEmoji = new CustomEmoji();
        customEmoji.setUserID(BaseApp.inst().loginCertificate.userID);
        customEmoji.setSourceUrl(pictureInfo.getUrl());
        customEmoji.setSourceW(pictureInfo.getWidth());
        customEmoji.setSourceH(pictureInfo.getHeight());
        if (null != snapshotPicture) {
            customEmoji.setThumbnailUrl(snapshotPicture.getUrl());
            customEmoji.setThumbnailW(snapshotPicture.getWidth());
            customEmoji.setThumbnailH(snapshotPicture.getHeight());
        }
        insertEmojiDb(new ArrayList<>(Collections.singleton(customEmoji)));
    }
    public void insertEmojiDb(List<CustomEmoji> customEmojis) {
        BaseApp.inst().realm.executeTransactionAsync(realm -> {
            realm.insertOrUpdate(customEmojis);
            UIHandler.post(() -> {
                loadCustomEmoji();
                toast(BaseApp.inst().getString(io.openim.android
                    .ouicore.R.string.add_success));
            });
        });
    }

    public void loadCustomEmoji() {
        BaseApp.inst().realm.executeTransactionAsync(realm -> {
            RealmResults<CustomEmoji> results = realm.where(CustomEmoji.class).findAll();
            customEmojis.postValue(realm.copyFromRealm(results));
        });
    }
}
