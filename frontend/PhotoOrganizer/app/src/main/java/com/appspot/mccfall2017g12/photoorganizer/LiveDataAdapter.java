package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class LiveDataAdapter<T extends Diffable<T>, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private final LifecycleOwner owner;
    private LiveData<T[]> liveData;
    private T[] items;
    private final Observer<T[]> itemsObserver;

    public LiveDataAdapter(LifecycleOwner owner) {

        this.owner = owner;
        this.itemsObserver = new ItemsObserver();
    }

    public void setLiveData(LiveData<T[]> liveData) {
        if (this.liveData != null) {
            this.liveData.removeObservers(this.owner);
        }
        this.liveData = liveData;
        this.liveData.observe(this.owner, this.itemsObserver);
    }

    public T getItem(int position) {
        return this.items[position];
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.length;
    }

    private class ItemsObserver implements Observer<T[]> {

        @Override
        public void onChanged(@Nullable T[] newItems) {
            T[] oldItems = LiveDataAdapter.this.items;
            LiveDataAdapter.this.items = newItems;
            DiffUtil.calculateDiff(new DiffCallback<>(oldItems, newItems))
                    .dispatchUpdatesTo(LiveDataAdapter.this);
        }
    }

    private static class DiffCallback<Old extends Diffable<New>, New> extends DiffUtil.Callback {

        private final Old[] oldItems;
        private final New[] newItems;

        public DiffCallback(Old[] oldItems, New[] newItems) {
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
