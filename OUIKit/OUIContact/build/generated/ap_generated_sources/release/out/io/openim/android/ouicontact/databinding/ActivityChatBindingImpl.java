package io.openim.android.ouicontact.databinding;
import io.openim.android.ouicontact.R;
import io.openim.android.ouicontact.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityChatBindingImpl extends ActivityChatBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.card, 3);
        sViewsWithIds.put(R.id.back, 4);
        sViewsWithIds.put(R.id.nickName, 5);
        sViewsWithIds.put(R.id.call, 6);
        sViewsWithIds.put(R.id.more, 7);
        sViewsWithIds.put(R.id.recyclerView, 8);
        sViewsWithIds.put(R.id.inputGroup, 9);
        sViewsWithIds.put(R.id.send, 10);
    }
    // views
    @NonNull
    private final android.widget.RelativeLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers
    private androidx.databinding.InverseBindingListener inputandroidTextAttrChanged = new androidx.databinding.InverseBindingListener() {
        @Override
        public void onChange() {
            // Inverse of chatVM.inputMsg.getValue()
            //         is chatVM.inputMsg.setValue((java.lang.String) callbackArg_0)
            java.lang.String callbackArg_0 = androidx.databinding.adapters.TextViewBindingAdapter.getTextString(input);
            // localize variables for thread safety
            // chatVM.inputMsg.getValue()
            java.lang.String chatVMInputMsgGetValue = null;
            // chatVM != null
            boolean chatVMJavaLangObjectNull = false;
            // chatVM.inputMsg
            androidx.lifecycle.MutableLiveData<java.lang.String> chatVMInputMsg = null;
            // chatVM
            io.openim.android.ouicontact.vm.ChatVM chatVM = mChatVM;
            // chatVM.inputMsg != null
            boolean chatVMInputMsgJavaLangObjectNull = false;



            chatVMJavaLangObjectNull = (chatVM) != (null);
            if (chatVMJavaLangObjectNull) {


                chatVMInputMsg = chatVM.inputMsg;

                chatVMInputMsgJavaLangObjectNull = (chatVMInputMsg) != (null);
                if (chatVMInputMsgJavaLangObjectNull) {




                    chatVMInputMsg.setValue(((java.lang.String) (callbackArg_0)));
                }
            }
        }
    };

    public ActivityChatBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 11, sIncludes, sViewsWithIds));
    }
    private ActivityChatBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 2
            , (android.widget.ImageView) bindings[4]
            , (android.widget.ImageView) bindings[6]
            , (androidx.cardview.widget.CardView) bindings[3]
            , (android.widget.EditText) bindings[2]
            , (android.widget.LinearLayout) bindings[9]
            , (android.widget.TextView) bindings[1]
            , (android.widget.ImageView) bindings[7]
            , (android.widget.TextView) bindings[5]
            , (androidx.recyclerview.widget.RecyclerView) bindings[8]
            , (android.widget.Button) bindings[10]
            );
        this.input.setTag(null);
        this.isTyping.setTag(null);
        this.mboundView0 = (android.widget.RelativeLayout) bindings[0];
        this.mboundView0.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x8L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
        if (BR.chatVM == variableId) {
            setChatVM((io.openim.android.ouicontact.vm.ChatVM) variable);
        }
        else {
            variableSet = false;
        }
            return variableSet;
    }

    public void setChatVM(@Nullable io.openim.android.ouicontact.vm.ChatVM ChatVM) {
        this.mChatVM = ChatVM;
        synchronized(this) {
            mDirtyFlags |= 0x4L;
        }
        notifyPropertyChanged(BR.chatVM);
        super.requestRebind();
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
            case 0 :
                return onChangeChatVMInputMsg((androidx.lifecycle.MutableLiveData<java.lang.String>) object, fieldId);
            case 1 :
                return onChangeChatVMTyping((androidx.databinding.ObservableBoolean) object, fieldId);
        }
        return false;
    }
    private boolean onChangeChatVMInputMsg(androidx.lifecycle.MutableLiveData<java.lang.String> ChatVMInputMsg, int fieldId) {
        if (fieldId == BR._all) {
            synchronized(this) {
                    mDirtyFlags |= 0x1L;
            }
            return true;
        }
        return false;
    }
    private boolean onChangeChatVMTyping(androidx.databinding.ObservableBoolean ChatVMTyping, int fieldId) {
        if (fieldId == BR._all) {
            synchronized(this) {
                    mDirtyFlags |= 0x2L;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        androidx.lifecycle.MutableLiveData<java.lang.String> chatVMInputMsg = null;
        io.openim.android.ouicontact.vm.ChatVM chatVM = mChatVM;
        androidx.databinding.ObservableBoolean chatVMTyping = null;
        java.lang.String chatVMInputMsgGetValue = null;
        boolean chatVMTypingGet = false;
        int chatVMTypingViewVISIBLEViewGONE = 0;

        if ((dirtyFlags & 0xfL) != 0) {


            if ((dirtyFlags & 0xdL) != 0) {

                    if (chatVM != null) {
                        // read chatVM.inputMsg
                        chatVMInputMsg = chatVM.inputMsg;
                    }
                    updateLiveDataRegistration(0, chatVMInputMsg);


                    if (chatVMInputMsg != null) {
                        // read chatVM.inputMsg.getValue()
                        chatVMInputMsgGetValue = chatVMInputMsg.getValue();
                    }
            }
            if ((dirtyFlags & 0xeL) != 0) {

                    if (chatVM != null) {
                        // read chatVM.typing
                        chatVMTyping = chatVM.typing;
                    }
                    updateRegistration(1, chatVMTyping);


                    if (chatVMTyping != null) {
                        // read chatVM.typing.get()
                        chatVMTypingGet = chatVMTyping.get();
                    }
                if((dirtyFlags & 0xeL) != 0) {
                    if(chatVMTypingGet) {
                            dirtyFlags |= 0x20L;
                    }
                    else {
                            dirtyFlags |= 0x10L;
                    }
                }


                    // read chatVM.typing.get() ? View.VISIBLE : View.GONE
                    chatVMTypingViewVISIBLEViewGONE = ((chatVMTypingGet) ? (android.view.View.VISIBLE) : (android.view.View.GONE));
            }
        }
        // batch finished
        if ((dirtyFlags & 0xdL) != 0) {
            // api target 1

            androidx.databinding.adapters.TextViewBindingAdapter.setText(this.input, chatVMInputMsgGetValue);
        }
        if ((dirtyFlags & 0x8L) != 0) {
            // api target 1

            androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(this.input, (androidx.databinding.adapters.TextViewBindingAdapter.BeforeTextChanged)null, (androidx.databinding.adapters.TextViewBindingAdapter.OnTextChanged)null, (androidx.databinding.adapters.TextViewBindingAdapter.AfterTextChanged)null, inputandroidTextAttrChanged);
        }
        if ((dirtyFlags & 0xeL) != 0) {
            // api target 1

            this.isTyping.setVisibility(chatVMTypingViewVISIBLEViewGONE);
        }
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): chatVM.inputMsg
        flag 1 (0x2L): chatVM.typing
        flag 2 (0x3L): chatVM
        flag 3 (0x4L): null
        flag 4 (0x5L): chatVM.typing.get() ? View.VISIBLE : View.GONE
        flag 5 (0x6L): chatVM.typing.get() ? View.VISIBLE : View.GONE
    flag mapping end*/
    //end
}