package io.openim.android.ouicore.entity;

import java.io.Serializable;

/**
 * 常用扩展字段
 */
public class CommEx implements Serializable {
    public boolean isSticky = false; //是否是Sticky
    public String sortLetter; //显示数据拼音的首字母

    public boolean isSelect = false;//是否被选中
    public boolean isEnabled = true;//是否可点击
}
