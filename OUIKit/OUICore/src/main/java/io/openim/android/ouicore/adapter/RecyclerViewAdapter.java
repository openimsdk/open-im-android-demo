package io.openim.android.ouicore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public abstract class RecyclerViewAdapter<T, V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {

    private List<T> items;
    private Class<V> viewHolder;

    public RecyclerViewAdapter(Class<V> viewHolder) {
        this.viewHolder = viewHolder;
    }

    public void setItems(List<T> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public V onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            return viewHolder.getConstructor(View.class).newInstance(parent);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
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

}
