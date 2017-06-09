package com.androidworks.nikhil.rapidodemo.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidworks.nikhil.rapidodemo.R;

import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nikhilkumar_S on 6/9/2017.
 */
public class DirectionsAdapter extends BaseAdapter {

    private Activity activity;
    private String[] directions;

    public DirectionsAdapter(Activity activity, String[] directions) {
        this.activity = activity;
        this.directions = directions;
    }

    @Override
    public int getCount() {
        return directions.length;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (activity).getLayoutInflater();
            convertView = inflater.inflate(R.layout.directions_list_item, viewGroup, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String direction = directions[position];
        viewHolder.directionIcon.setBackgroundResource(getIcon(direction));
        viewHolder.directionsText.setText(direction);

        return convertView;
    }

    private int getIcon(String direction) {
        StringTokenizer tokenizer = new StringTokenizer(direction);
        int count = 0;
        while (count < 4 && tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();
            if (word.contains("left")) {
                return R.drawable.arrow_turn_left_128;
            } else if (word.contains("right")) {
                return R.drawable.arrow_turn_right_128;
            } else if (word.contains("turn")) {
                return R.drawable.arrow_uturn_128;
            }
            count++;
        }
        return R.drawable.arrow_up_128;
    }

    public void updateList(String[] newDirections) {
        this.directions = newDirections;
        notifyDataSetChanged();
    }

    static class ViewHolder {

        @BindView(R.id.iv_direction_icon)
        ImageView directionIcon;
        @BindView(R.id.tv_directions)
        TextView directionsText;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }
}
