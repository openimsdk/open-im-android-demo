package io.openim.android.ouicore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import io.openim.android.ouicore.utils.L;

public abstract class RecyclerViewAdapter<T, V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {

    private List<T> items;
    private Class<V> viewHolder;


    public RecyclerViewAdapter() {
    }


    public RecyclerViewAdapter(Class<V> viewHolder) {
        this.viewHolder = viewHolder;
    }

    public void setItems(List<T> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public List<T> getItems() {
        return items;
    }

    @NonNull
    @Override
    public V onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            return viewHolder.getConstructor(View.class).newInstance(parent);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 InstantiationException e) {
            e.printStackTrace();
            throw new NullPointerException();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull V holder, int position) {
        onBindView(holder, items.get(position), position);
    }

    public abstract void onBindView(@NonNull V holder, T data, int position);

    @Override
    public int getItemCount() {
        return null == items ? 0 : items.size();
    }


    public void setOnLoadMoreListener(RecyclerView view, int pageSize,
                                      OnLoadMoreListener listener) {
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager =
                    (LinearLayoutManager) recyclerView.getLayoutManager();
                if (null!=linearLayoutManager){
                    int lastVisiblePosition =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (getItems().size()>=pageSize
                        && lastVisiblePosition >= getItems().size() - 3) {
                        listener.onLoad();
                    }
                }
            }
        });
    }
    public interface OnLoadMoreListener {
        public void onLoad();
    }
}


