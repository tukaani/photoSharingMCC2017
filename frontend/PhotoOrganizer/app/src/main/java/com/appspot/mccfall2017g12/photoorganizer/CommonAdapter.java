package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class CommonAdapter<T extends Diffable<T>, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements Observer<T[]> {

    private final LifecycleOwner owner;
    private final int itemLayout;
    private final View.OnClickListener onClickListener;
    private LiveData<T[]> liveData;
    private T[] items;

    public CommonAdapter(LifecycleOwner owner, @LayoutRes int itemLayout,
                         @Nullable View.OnClickListener onClickListener) {
        this.owner = owner;
        this.itemLayout = itemLayout;
        this.onClickListener = onClickListener;
    }

    public void setLiveData(LiveData<T[]> liveData) {
        if (this.liveData != null) {
            this.liveData.removeObservers(this.owner);
        }
        this.liveData = liveData;
        this.liveData.observe(this.owner, this);
    }

    public T getItem(int position) {
        return this.items[position];
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(this.itemLayout, parent, false);
        if (onClickListener != null)
            view.setOnClickListener(onClickListener);
        return createViewHolderInternal(view);
    }

    protected abstract VH createViewHolderInternal(View view);

    @Override
    public void onChanged(@Nullable T[] newItems) {
        T[] oldItems = this.items;
        this.items = newItems;
        DiffUtil.calculateDiff(new CommonDiffCallback<>(oldItems, newItems))
                .dispatchUpdatesTo(CommonAdapter.this);
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.length;
    }

    private class CommonDiffCallback<Old extends Diffable<New>, New> extends DiffUtil.Callback {

        private final Old[] oldItems;
        private final New[] newItems;

        public CommonDiffCallback(Old[] oldItems, New[] newItems) {
            this.oldItems = oldItems;
            this.newItems = newItems;
        }

        @Override
        public int getOldListSize() {
            return this.oldItems == null ? 0 : this.oldItems.length;
        }

        @Override
        public int getNewListSize() {
            return this.newItems == null ? 0 : this.newItems.length;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return this.oldItems[oldItemPosition].isTheSameAs(newItems[newItemPosition]);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return this.oldItems[oldItemPosition].hasTheSameContentAs(newItems[newItemPosition]);
        }
    }
}
