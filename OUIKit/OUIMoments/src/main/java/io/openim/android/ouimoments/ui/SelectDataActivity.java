package io.openim.android.ouimoments.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.github.promeg.pinyinhelper.Pinyin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.ex.CommEx;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.vm.SocialityVM;
import io.openim.android.ouimoments.databinding.ActivitySelectDataBinding;

public class SelectDataActivity extends BaseActivity<BaseViewModel, ActivitySelectDataBinding> {

    private String title;
    private List<RuleData> ruleDatas;
    private int maxNum;
    //选择的人数
    private int selectMemberNum = 0;
    private RecyclerViewAdapter<RuleData, RecyclerView.ViewHolder> adapter;

    //字母集
    public List<String> letters = new ArrayList<>();
    //排序后的数据
    public List<RuleData> sortRuleData = new ArrayList<>();
    private Boolean isGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySelectDataBinding.inflate(getLayoutInflater()));
        init();
        initView();
        listener();
    }

    private void listener() {
        view.sortView.setOnLetterChangedListener((letter, position) -> {
            for (int i = 0; i < adapter.getItems().size(); i++) {
                RuleData ruleData = adapter.getItems().get(i);
                if (!ruleData.isSticky) continue;
                if (ruleData.sortLetter.equalsIgnoreCase(letter)) {
                    View viewByPosition =
                        view.recyclerView.getLayoutManager().findViewByPosition(i);
                    if (viewByPosition != null) {
                        view.scrollView.smoothScrollTo(0, viewByPosition.getTop());
                    }
                    return;
                }
            }
        });
        view.submit.setOnClickListener(v -> {
            List<RuleData> selectRuleData = new ArrayList<>();
            for (RuleData ruleData : adapter.getItems()) {
                if (ruleData.isSelect) selectRuleData.add(ruleData);
            }
            setResult(RESULT_OK, new Intent().putExtra(Constants.K_RESULT,
                (Serializable) selectRuleData));
            finish();
        });
    }

    private void initView() {
        view.scrollView.fullScroll(View.FOCUS_DOWN);
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter<RuleData, RecyclerView.ViewHolder>() {
            private int STICKY = 1;
            private int ITEM = 2;

            private String lastSticky = "";

            @Override
            public void setItems(List<RuleData> items) {
                if (items.isEmpty()) return;
                lastSticky = items.get(0).sortLetter;
                items.add(0, getExUserInfo());
                for (int i = 0; i < items.size(); i++) {
                    RuleData ruleData = items.get(i);
                    if (!lastSticky.equals(ruleData.sortLetter)) {
                        lastSticky = ruleData.sortLetter;
                        items.add(i, getExUserInfo());
                    }
                }

                super.setItems(items);
            }

            @NonNull
            private RuleData getExUserInfo() {
                RuleData ruleData = new RuleData();
                ruleData.sortLetter = lastSticky;
                ruleData.isSticky = true;
                return ruleData;
            }

            @Override
            public int getItemViewType(int position) {
                return getItems().get(position).isSticky ? STICKY : ITEM;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                              int viewType) {
                if (viewType == ITEM) return new ViewHol.ItemViewHo(parent);

                return new ViewHol.StickyViewHo(parent);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, RuleData data,
                                   int position) {
                if (getItemViewType(position) == ITEM) {
                    ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                    itemViewHo.view.avatar.load(data.icon, isGroup);
                    itemViewHo.view.nickName.setText(data.name);

                    itemViewHo.view.select.setVisibility(View.VISIBLE);
                    itemViewHo.view.select.setChecked(data.isSelect);
                    if (!data.isEnabled) itemViewHo.view.item.setOnClickListener(null);
                    else itemViewHo.view.item.setOnClickListener(v -> {
                        if (selectMemberNum >= maxNum) {
                            toast(String.format(getString(io.openim.android.ouicore.R.string.select_tips), maxNum));
                            return;
                        }
                        data.isSelect = !data.isSelect;
                        notifyItemChanged(position);
                        selectMemberNum = getSelectNum();
                        view.selectNum.setText(String.format(getString(isGroup ?
                            io.openim.android.ouicore.R.string.selected_group_tips :
                            io.openim.android.ouicore.R.string.selected_tips, selectMemberNum)));

                        view.submit.setText(getString(io.openim.android.ouicore.R.string.sure) +
                            "（" + selectMemberNum + "/" + maxNum + "）");
                        view.submit.setEnabled(selectMemberNum > 0);
                    });
                } else {
                    ViewHol.StickyViewHo stickyViewHo = (ViewHol.StickyViewHo) holder;
                    stickyViewHo.view.title.setText(data.sortLetter);
                }
            }
        };
        view.recyclerView.setAdapter(adapter);
        adapter.setItems(sortRuleData);
        StringBuilder lettersAppend = new StringBuilder();
        for (String s : letters) {
            lettersAppend.append(s);
        }
        view.sortView.setLetters(lettersAppend.toString());


        getSelectNum();
        view.title.setText(title);
        view.submit.setText(getString(io.openim.android.ouicore.R.string.sure) + "（" + selectMemberNum + "/" + maxNum + "）");
    }

    private int getSelectNum() {
        int num = 0;
        for (RuleData item : sortRuleData) {
            if (item.isSelect) {
                num++;
            }
        }
        return num;
    }

    private void init() {
        title = getIntent().getStringExtra(Constants.K_NAME);
        isGroup = getIntent().getBooleanExtra(Constants.K_FROM, false);
        ruleDatas = (List<RuleData>) getIntent().getSerializableExtra(Constants.K_RESULT);
        maxNum = getIntent().getIntExtra(Constants.K_SIZE, 999);

        installData();
    }

    private void installData() {
        //# 字开头的数据
        List<RuleData> otRuleData = new ArrayList<>();
        for (RuleData ruleData : ruleDatas) {
            String nickName = ruleData.name;
            if (null == nickName) nickName = "";
            String letter = Pinyin.toPinyin(nickName.charAt(0));
            letter = (letter.charAt(0) + "").trim().toUpperCase();

            if (!Common.isAlpha(letter)) {
                ruleData.sortLetter = "#";
                otRuleData.add(ruleData);
            } else {
                ruleData.sortLetter = letter;
                sortRuleData.add(ruleData);
            }
            if (!letters.contains(ruleData.sortLetter)) letters.add(ruleData.sortLetter);
        }
        sortRuleData.addAll(otRuleData);
        Collections.sort(letters, new SocialityVM.LettersPinyinComparator());
        Collections.sort(sortRuleData, new SocialityVM.PinyinComparator());
    }

    public static class RuleData extends CommEx implements Serializable {
        public String id;
        public String name;
        public String icon;
    }
}
