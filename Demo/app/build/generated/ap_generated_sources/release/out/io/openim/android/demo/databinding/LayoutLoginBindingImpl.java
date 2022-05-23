package io.openim.android.demo.databinding;
import io.openim.android.demo.R;
import io.openim.android.demo.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class LayoutLoginBindingImpl extends LayoutLoginBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.clear, 3);
        sViewsWithIds.put(R.id.eyes, 4);
        sViewsWithIds.put(R.id.forgotPasswordTv, 5);
        sViewsWithIds.put(R.id.registerTv, 6);
    }
    // views
    @NonNull
    private final android.widget.FrameLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers
    private androidx.databinding.InverseBindingListener edt1androidTextAttrChanged = new androidx.databinding.InverseBindingListener() {
        @Override
        public void onChange() {
            // Inverse of loginVM.account.getValue()
            //         is loginVM.account.setValue((java.lang.String) callbackArg_0)
            java.lang.String callbackArg_0 = androidx.databinding.adapters.TextViewBindingAdapter.getTextString(edt1);
            // localize variables for thread safety
            // loginVM != null
            boolean loginVMJavaLangObjectNull = false;
            // loginVM.account
            androidx.lifecycle.MutableLiveData<java.lang.String> loginVMAccount = null;
            // loginVM.account.getValue()
            java.lang.String loginVMAccountGetValue = null;
            // loginVM.account != null
            boolean loginVMAccountJavaLangObjectNull = false;
            // loginVM
            io.openim.android.demo.vm.LoginVM loginVM = mLoginVM;



            loginVMJavaLangObjectNull = (loginVM) != (null);
            if (loginVMJavaLangObjectNull) {


                loginVMAccount = loginVM.account;

                loginVMAccountJavaLangObjectNull = (loginVMAccount) != (null);
                if (loginVMAccountJavaLangObjectNull) {




                    loginVMAccount.setValue(((java.lang.String) (callbackArg_0)));
                }
            }
        }
    };
    private androidx.databinding.InverseBindingListener edt2androidTextAttrChanged = new androidx.databinding.InverseBindingListener() {
        @Override
        public void onChange() {
            // Inverse of loginVM.pwd.getValue()
            //         is loginVM.pwd.setValue((java.lang.String) callbackArg_0)
            java.lang.String callbackArg_0 = androidx.databinding.adapters.TextViewBindingAdapter.getTextString(edt2);
            // localize variables for thread safety
            // loginVM.pwd
            androidx.lifecycle.MutableLiveData<java.lang.String> loginVMPwd = null;
            // loginVM.pwd.getValue()
            java.lang.String loginVMPwdGetValue = null;
            // loginVM != null
            boolean loginVMJavaLangObjectNull = false;
            // loginVM.pwd != null
            boolean loginVMPwdJavaLangObjectNull = false;
            // loginVM
            io.openim.android.demo.vm.LoginVM loginVM = mLoginVM;



            loginVMJavaLangObjectNull = (loginVM) != (null);
            if (loginVMJavaLangObjectNull) {


                loginVMPwd = loginVM.pwd;

                loginVMPwdJavaLangObjectNull = (loginVMPwd) != (null);
                if (loginVMPwdJavaLangObjectNull) {




                    loginVMPwd.setValue(((java.lang.String) (callbackArg_0)));
                }
            }
        }
    };

    public LayoutLoginBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 7, sIncludes, sViewsWithIds));
    }
    private LayoutLoginBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 2
            , (android.widget.ImageView) bindings[3]
            , (android.widget.EditText) bindings[1]
            , (android.widget.EditText) bindings[2]
            , (android.widget.CheckBox) bindings[4]
            , (android.widget.TextView) bindings[5]
            , (android.widget.TextView) bindings[6]
            );
        this.edt1.setTag(null);
        this.edt2.setTag(null);
        this.mboundView0 = (android.widget.FrameLayout) bindings[0];
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
        if (BR.loginVM == variableId) {
            setLoginVM((io.openim.android.demo.vm.LoginVM) variable);
        }
        else {
            variableSet = false;
        }
            return variableSet;
    }

    public void setLoginVM(@Nullable io.openim.android.demo.vm.LoginVM LoginVM) {
        this.mLoginVM = LoginVM;
        synchronized(this) {
            mDirtyFlags |= 0x4L;
        }
        notifyPropertyChanged(BR.loginVM);
        super.requestRebind();
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
            case 0 :
                return onChangeLoginVMAccount((androidx.lifecycle.MutableLiveData<java.lang.String>) object, fieldId);
            case 1 :
                return onChangeLoginVMPwd((androidx.lifecycle.MutableLiveData<java.lang.String>) object, fieldId);
        }
        return false;
    }
    private boolean onChangeLoginVMAccount(androidx.lifecycle.MutableLiveData<java.lang.String> LoginVMAccount, int fieldId) {
        if (fieldId == BR._all) {
            synchronized(this) {
                    mDirtyFlags |= 0x1L;
            }
            return true;
        }
        return false;
    }
    private boolean onChangeLoginVMPwd(androidx.lifecycle.MutableLiveData<java.lang.String> LoginVMPwd, int fieldId) {
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
        androidx.lifecycle.MutableLiveData<java.lang.String> loginVMAccount = null;
        io.openim.android.demo.vm.LoginVM loginVM = mLoginVM;
        androidx.lifecycle.MutableLiveData<java.lang.String> loginVMPwd = null;
        java.lang.String loginVMPwdGetValue = null;
        java.lang.String loginVMAccountGetValue = null;

        if ((dirtyFlags & 0xfL) != 0) {


            if ((dirtyFlags & 0xdL) != 0) {

                    if (loginVM != null) {
                        // read loginVM.account
                        loginVMAccount = loginVM.account;
                    }
                    updateLiveDataRegistration(0, loginVMAccount);


                    if (loginVMAccount != null) {
                        // read loginVM.account.getValue()
                        loginVMAccountGetValue = loginVMAccount.getValue();
                    }
            }
            if ((dirtyFlags & 0xeL) != 0) {

                    if (loginVM != null) {
                        // read loginVM.pwd
                        loginVMPwd = loginVM.pwd;
                    }
                    updateLiveDataRegistration(1, loginVMPwd);


                    if (loginVMPwd != null) {
                        // read loginVM.pwd.getValue()
                        loginVMPwdGetValue = loginVMPwd.getValue();
                    }
            }
        }
        // batch finished
        if ((dirtyFlags & 0xdL) != 0) {
            // api target 1

            androidx.databinding.adapters.TextViewBindingAdapter.setText(this.edt1, loginVMAccountGetValue);
        }
        if ((dirtyFlags & 0x8L) != 0) {
            // api target 1

            androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(this.edt1, (androidx.databinding.adapters.TextViewBindingAdapter.BeforeTextChanged)null, (androidx.databinding.adapters.TextViewBindingAdapter.OnTextChanged)null, (androidx.databinding.adapters.TextViewBindingAdapter.AfterTextChanged)null, edt1androidTextAttrChanged);
            androidx.databinding.adapters.TextViewBindingAdapter.setTextWatcher(this.edt2, (androidx.databinding.adapters.TextViewBindingAdapter.BeforeTextChanged)null, (androidx.databinding.adapters.TextViewBindingAdapter.OnTextChanged)null, (androidx.databinding.adapters.TextViewBindingAdapter.AfterTextChanged)null, edt2androidTextAttrChanged);
        }
        if ((dirtyFlags & 0xeL) != 0) {
            // api target 1

            androidx.databinding.adapters.TextViewBindingAdapter.setText(this.edt2, loginVMPwdGetValue);
        }
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): loginVM.account
        flag 1 (0x2L): loginVM.pwd
        flag 2 (0x3L): loginVM
        flag 3 (0x4L): null
    flag mapping end*/
    //end
}