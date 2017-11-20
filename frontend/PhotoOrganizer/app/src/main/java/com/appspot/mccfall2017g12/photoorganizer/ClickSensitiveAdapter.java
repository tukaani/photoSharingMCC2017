package com.appspot.mccfall2017g12.photoorganizer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class ClickSensitiveAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private final int itemLayout;
    private final View.OnClickListener onClickListener;

    public ClickSensitiveAdapter(int itemLayout, View.OnClickListener onClickListener) {
        this.itemLayout = itemLayout;
        this.onClickListener = onClickListener;
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
}
