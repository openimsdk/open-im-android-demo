package io.openim.android.ouicore.ex;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * 常用扩展字段
 */
@Deprecated
public class CommEx implements Serializable {
    public String key; //Id
    public boolean isSticky = false; //是否是Sticky
    public String sortLetter; //显示数据拼音的首字母

    public boolean isSelect = false;//是否被选中
    public boolean isEnabled = true;//是否可点击

    @Override
    public boolean equals(@Nullable Object obj) {
        if (null != obj) {
            if (hashCode() ==
                obj.hashCode()) return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
