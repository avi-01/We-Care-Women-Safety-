package com.avnish.wecare;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendsAdpater extends ArrayAdapter<User> {

    private LayoutInflater mInflater;
    private ArrayList<User> list;
    private Context mContext;

    public FriendsAdpater(Context context, int resource, ArrayList<User> list) {
        super(context, resource, list);
        this.list=list;

        mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        view=mInflater.inflate(R.layout.friend_layout,null);
        User listset=list.get(position);
        TextView nameV=(TextView)view.findViewById(R.id.list_content8);
        TextView g_idV=(TextView)view.findViewById(R.id.list_content7);
        nameV.setText(listset.getName());
        g_idV.setText(listset.getEmail());

        return view;
    }


}
