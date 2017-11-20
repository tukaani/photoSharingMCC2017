package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class MenuItemAdapter extends BaseAdapter {
    private MenuItem[] menuItems;
    private Context context;

    MenuItemAdapter(Context context, MenuItem[] menuItems)
    {
        this.context = context;
        this.menuItems = menuItems;
    }

    @Override
    public int getCount() {
        return menuItems.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MenuItem menuItem = menuItems[position];

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(this.context);
            convertView = layoutInflater.inflate(R.layout.layout_menu_item, null);
        }

        TextView captionTextView = convertView.findViewById(R.id.captionTextView);
        ImageView thumbImageView = convertView.findViewById(R.id.thumbImageView);

        captionTextView.setText(menuItem.getCaptionId());
        thumbImageView.setImageResource(menuItem.getThumbId());

        return convertView;
    }
}
