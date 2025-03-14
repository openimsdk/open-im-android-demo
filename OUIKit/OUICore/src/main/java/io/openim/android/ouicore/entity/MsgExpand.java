package io.openim.android.ouicore.entity;

import android.text.SpannableStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * message 的 ext
 */
public class MsgExpand {
    public MsgExpand() {
    }
    //多选被选中
    public boolean isChoice;
    //此item 应该显示时间
    public boolean isShowTime;
    //富文本
    public transient SpannableStringBuilder sequence;
}
