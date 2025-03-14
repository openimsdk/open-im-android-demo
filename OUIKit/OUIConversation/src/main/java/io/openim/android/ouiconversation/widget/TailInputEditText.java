package io.openim.android.ouiconversation.widget;

import android.content.Context;
import android.text.Selection;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.vanniktech.emoji.EmojiEditText;

import java.util.Iterator;
import java.util.List;

import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.ex.AtUser;
import io.openim.android.sdk.models.Message;

public class TailInputEditText extends EmojiEditText {
    private ChatVM chatVM;

    public TailInputEditText(@NonNull Context context) {
        super(context);
    }

    public TailInputEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setChatVM(ChatVM chatVM) {
        this.chatVM = chatVM;
    }

}
