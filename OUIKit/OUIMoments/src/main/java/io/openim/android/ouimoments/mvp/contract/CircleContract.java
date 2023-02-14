package io.openim.android.ouimoments.mvp.contract;

import java.util.List;

import io.openim.android.ouimoments.bean.CircleItem;
import io.openim.android.ouimoments.bean.CommentConfig;
import io.openim.android.ouimoments.bean.CommentItem;
import io.openim.android.ouimoments.bean.FavortItem;

/**
 * Created by suneee on 2016/7/15.
 */
public interface CircleContract {

    interface View extends BaseView{
        void updateAdapterIndex(int index);
        void update2DeleteCircle(String circleId);
        void update2AddFavorite(int circlePosition, FavortItem addItem);
        void update2DeleteFavort(int circlePosition, String favortId);
        void update2AddComment(int circlePosition, CommentItem addItem);
        void update2DeleteComment(int circlePosition, String commentId);
        void updateEditTextBodyVisible(int visibility, CommentConfig commentConfig);
        void update2loadData(int loadType, List<CircleItem> datas);
    }

    interface Presenter extends BasePresenter{
        void loadData(int loadType,String userID);
        void deleteCircle(final String circleId);
        void addFavort(final int circlePosition,final String mFavorId);
        void deleteFavort(final int circlePosition, final String favortId);
        void deleteComment(String momentID,final int circlePosition, final String commentId);
    }

    interface  MsgDetailPresenter extends BasePresenter{

    }
    interface MsgDetailView extends BaseView{

    }

}
