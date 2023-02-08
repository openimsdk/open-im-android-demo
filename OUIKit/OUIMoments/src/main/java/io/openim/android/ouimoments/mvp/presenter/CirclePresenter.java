package io.openim.android.ouimoments.mvp.presenter;

import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson2.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.UserLabel;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.NiService;
import io.openim.android.ouicore.services.OneselfService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouimoments.bean.CircleItem;
import io.openim.android.ouimoments.bean.Comment;
import io.openim.android.ouimoments.bean.CommentConfig;
import io.openim.android.ouimoments.bean.CommentItem;
import io.openim.android.ouimoments.bean.FavortItem;
import io.openim.android.ouimoments.bean.MomentsBean;
import io.openim.android.ouimoments.bean.MomentsContent;
import io.openim.android.ouimoments.bean.MomentsMeta;
import io.openim.android.ouimoments.bean.MomentsUser;
import io.openim.android.ouimoments.bean.PhotoInfo;
import io.openim.android.ouimoments.bean.User;
import io.openim.android.ouimoments.bean.WorkMoments;
import io.openim.android.ouimoments.listener.IDataRequestListener;
import io.openim.android.ouimoments.mvp.contract.CircleContract;
import io.openim.android.ouimoments.mvp.modle.CircleModel;
import io.openim.android.ouimoments.utils.DatasUtil;
import okhttp3.ResponseBody;

/**
 * @author yiw
 * @ClassName: CirclePresenter
 * @Description: 通知model请求服务器和通知view更新
 * @date 2015-12-28 下午4:06:03
 */
public class CirclePresenter implements CircleContract.Presenter {
    public final static int TYPE_PULLREFRESH = 1;
    public final static int TYPE_UPLOADREFRESH = 2;
    private CircleModel circleModel;
    private CircleContract.View view;
    public static int pageIndex = 1, pageSize = 20;

    public CirclePresenter(CircleContract.View view) {
        circleModel = new CircleModel();
        this.view = view;
    }

    //    String? userID
    public void loadData(int loadType) {
        loadData(loadType, null);
    }

    public void loadData(int loadType, String userID) {
        if (loadType == TYPE_PULLREFRESH) pageIndex = 1; else pageIndex++;

        Parameter parameter = NiService.buildParameter().add("pageNumber", pageIndex).add(
            "showNumber", pageSize).add("userID", userID);
        NetObserver<MomentsBean> netObserver = new NetObserver<MomentsBean>("") {
            @Override
            public void onSuccess(MomentsBean o) {
                packInContent(o);
                List<CircleItem> circleData = packInCircleData(o);

                if (view != null) {
                    view.update2loadData(loadType, circleData);
                }
            }

            @Override
            protected void onFailure(Throwable e) {
                view.showError(e.getMessage());
            }
        };
        if (TextUtils.isEmpty(userID)) {
            N.API(NiService.class).CommNI(Constant.getImApiUrl() + "office/get_user_friend_work_moments", BaseApp.inst().loginCertificate.imToken,
                parameter.buildJsonBody()).compose(N.IOMain()).map(OneselfService.turn(MomentsBean.class)).subscribe(netObserver);
        } else {
            N.API(NiService.class).CommNI(Constant.getImApiUrl() + "office/get_user_work_moments"
                , BaseApp.inst().loginCertificate.imToken, parameter.buildJsonBody()).compose(N.IOMain()).map(OneselfService.turn(MomentsBean.class)).subscribe(netObserver);
        }
    }

    /**
     * 封装CircleData
     *
     * @param o
     */
    private List<CircleItem> packInCircleData(MomentsBean o) {
        List<CircleItem> circleItems = new ArrayList<>();
        for (WorkMoments workMoment : o.workMoments) {
            CircleItem item = new CircleItem();
            item.setType(workMoment.momentsContents.type == 0 ? "2" : "3");
            item.setUser(new User(workMoment.userID, workMoment.userName, workMoment.faceURL));
            item.setId(workMoment.workMomentID);
            item.setContent(workMoment.momentsContents.text);
            item.setCreateTime(TimeUtil.getTime(workMoment.createTime * 1000L,
                TimeUtil.monthTimeFormat));

            List<FavortItem> favortItems = new ArrayList<>();
            for (MomentsUser likeUser : workMoment.likeUsers) {
                FavortItem favortItem = new FavortItem();
                favortItem.setId(likeUser.userID);
                favortItem.setUser(new User(likeUser.userID, likeUser.userName, ""));
                favortItems.add(favortItem);
            }
            item.setFavorters(favortItems);

            List<CommentItem> commentItems = new ArrayList<>();
            for (Comment comment : workMoment.comments) {
                CommentItem commentItem = new CommentItem();
                commentItem.setUser(new User(comment.userID, comment.userName, ""));
                commentItem.setToReplyUser(new User(comment.replyUserID, comment.replyUserName,
                    ""));
                commentItem.setId(comment.contentID);
                commentItem.setContent(comment.content);
                commentItems.add(commentItem);
            }
            item.setComments(commentItems);

            List<PhotoInfo> photos = new ArrayList<>();
            for (MomentsMeta meta : workMoment.momentsContents.metas) {
                if (item.getType().equals(CircleItem.TYPE_VIDEO)) {
                    item.setVideoUrl(meta.original);
                    item.setVideoImgUrl(meta.thumb);
                } else {
                    PhotoInfo photoInfo = new PhotoInfo();
                    photoInfo.url = meta.original;
                    photos.add(photoInfo);
                }
            }
            if (!photos.isEmpty()) item.setPhotos(photos);
            circleItems.add(item);
        }
        return circleItems;
    }

    /**
     * 解析Content 并赋值给momentsContents
     */
    public void packInContent(MomentsBean momentsBean) {
        try {
            for (WorkMoments workMoment : momentsBean.workMoments) {
                Map map = JSONObject.parseObject(workMoment.content, Map.class);
                JsonElement string = JsonParser.parseString((String) map.get("data"));
                MomentsContent momentsContent = GsonHel.fromJson(string.toString(),
                    MomentsContent.class);
                workMoment.momentsContents = momentsContent.data;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @param circleId
     * @return void    返回类型
     * @throws
     * @Title: deleteCircle
     * @Description: 删除动态
     */
    public void deleteCircle(final String circleId) {
        circleModel.deleteCircle(new IDataRequestListener() {

            @Override
            public void loadSuccess(Object object) {
                if (view != null) {
                    view.update2DeleteCircle(circleId);
                }
            }
        });
    }

    /**
     * @param circlePosition
     * @return void    返回类型
     * @throws
     * @Title: addFavort
     * @Description: 点赞
     */
    public void addFavort(final int circlePosition,final String mFavorId) {
        favortorDeleteFavort(circlePosition,mFavorId,true);
    }
    void favortorDeleteFavort(final int circlePosition,final String mFavorId,Boolean isStar){
        N.API(NiService.class).CommNI(Constant.getImApiUrl()
                    +"/office/like_one_work_moment",BaseApp.inst().loginCertificate.imToken,
                NiService.buildParameter().add("workMomentID",mFavorId).buildJsonBody())
            .map(OneselfService.turn(Object.class))
            .compose(N.IOMain())
            .subscribe(new NetObserver<Object>("") {
                @Override
                public void onSuccess(Object o) {
                    if (isStar){
                        FavortItem favortItem=new FavortItem();
                        favortItem.setId(BaseApp.inst().loginCertificate.userID);
                        favortItem.setUser(new User(BaseApp.inst().loginCertificate.userID,
                            BaseApp.inst().loginCertificate.nickname,
                            BaseApp.inst().loginCertificate.faceURL));
                        view.update2AddFavorite(circlePosition,favortItem);
                    }else {
                        view.update2DeleteFavort(circlePosition,
                            BaseApp.inst().loginCertificate.userID);
                    }
                }

                @Override
                protected void onFailure(Throwable e) {
                    view.showError(e.getMessage());
                }
            });
    }
    /**
     * @param @param circlePosition
     * @param @param favortId
     * @return void    返回类型
     * @throws
     * @Title: deleteFavort
     * @Description: 取消点赞
     */
    public void deleteFavort(final int circlePosition, final String favortId) {
        favortorDeleteFavort(circlePosition,favortId,false);
    }

    /**
     * @param content
     * @param config  CommentConfig
     * @return void    返回类型
     * @throws
     * @Title: addComment
     * @Description: 增加评论
     */
    public void addComment(final String content, final CommentConfig config) {
        if (config == null) {
            return;
        }
        circleModel.addComment(new IDataRequestListener() {

            @Override
            public void loadSuccess(Object object) {
                CommentItem newItem = null;
                if (config.commentType == CommentConfig.Type.PUBLIC) {
                    newItem = DatasUtil.createPublicComment(content);
                } else if (config.commentType == CommentConfig.Type.REPLY) {
                    newItem = DatasUtil.createReplyComment(config.replyUser, content);
                }
                if (view != null) {
                    view.update2AddComment(config.circlePosition, newItem);
                }
            }

        });
    }

    /**
     * @param @param circlePosition
     * @param @param commentId
     * @return void    返回类型
     * @throws
     * @Title: deleteComment
     * @Description: 删除评论
     */
    public void deleteComment(final int circlePosition, final String commentId) {
        circleModel.deleteComment(new IDataRequestListener() {

            @Override
            public void loadSuccess(Object object) {
                if (view != null) {
                    view.update2DeleteComment(circlePosition, commentId);
                }
            }

        });
    }

    /**
     * @param commentConfig
     */
    public void showEditTextBody(CommentConfig commentConfig) {
        if (view != null) {
            view.updateEditTextBodyVisible(View.VISIBLE, commentConfig);
        }
    }


    /**
     * 清除对外部对象的引用，反正内存泄露。
     */
    public void recycle() {
        this.view = null;
    }
}
