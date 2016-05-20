package im.actor.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import im.actor.maps.google.R;


public class PlacesAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<MapItem> resultList;

    public PlacesAdapter(Context context, ArrayList<MapItem> items) {
        resultList = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public MapItem getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View itemView;
        if (convertView != null) {
            itemView = convertView;
        } else {
            itemView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.picker_item_place, null);
        }

        TextView titleView = (TextView) itemView.findViewById(R.id.title);
        TextView subtitleView = (TextView) itemView.findViewById(R.id.subtitle);
        final ImageView iconView = (ImageView) itemView.findViewById(R.id.icon);

        final MapItem item = getItem(position);

        titleView.setText(item.name);
        subtitleView.setText(item.vicinity);
        iconView.setImageResource(R.drawable.example_user_placeholder);

        return itemView;
    }


}