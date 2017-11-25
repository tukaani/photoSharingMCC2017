package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LifecycleOwner;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public abstract class HeaderAdapter<T extends Diffable<T> & CategoryItem,
        HeaderVH extends RecyclerView.ViewHolder, ItemVH extends RecyclerView.ViewHolder>
        extends LiveDataAdapter<T, RecyclerView.ViewHolder> {

    public final static int TYPE_ITEM = 0;
    public final static int TYPE_HEADER = 1;

    public HeaderAdapter(LifecycleOwner owner) {
        super(owner);
    }

    @Override
    public int getItemViewType(int position) {
        return  getItem(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return onCreateHeaderViewHolder(parent);
            case TYPE_ITEM:
                return onCreateItemViewHolder(parent);
            default:
                throw new IllegalArgumentException();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case TYPE_HEADER:
                onBindHeaderViewHolder((HeaderVH) holder, position);
                break;
            case TYPE_ITEM:
                onBindItemViewHolder((ItemVH) holder, position);
                break;
        }
    }

    public abstract ItemVH onCreateItemViewHolder(ViewGroup parent);

    public abstract HeaderVH onCreateHeaderViewHolder(ViewGroup parent);

    public abstract void onBindItemViewHolder(ItemVH holder, int position);

    public abstract void onBindHeaderViewHolder(HeaderVH holder, int position);
}
