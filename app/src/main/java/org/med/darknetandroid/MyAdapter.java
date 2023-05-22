package org.med.darknetandroid;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.List;

//This adapter's functionality is to turn a listview into a container of cardviews so that the items are represented neatly
public class MyAdapter extends BaseAdapter {

    private Context context;
    List<Items> itemsList;
    ListView listView;
    Activity fragment;


    public MyAdapter(Context context, List<Items> itemsList, ListView listView, Activity f){
        this.context = context;
        this.itemsList = itemsList;
        this.listView = listView;
        this.fragment = f;
    }

    @Override
    public int getCount() {
        return itemsList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemsList.get(position);
    }

    @Override
    public long getItemId(int id) {
        return id;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HolderView holderView;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_view, parent, false);
            holderView = new HolderView(convertView);
            convertView.setTag(holderView);
        }else{
            holderView = (HolderView) convertView.getTag();
        }
        Items item = itemsList.get(position);
        if(item.getImageResource() == -1){
            holderView.imageView.setImageURI(item.getUri());
        }else {
            holderView.imageView.setImageResource(item.getImageResource());
        }
        holderView.name.setText(item.getName());
        if(item.getClassId() != -1)
            holderView.status.setImageResource(R.drawable.check);
        else{
            holderView.status.setImageResource(R.drawable.cancel);
        }
        return convertView;
    }

    public static class HolderView{
        private final ImageView imageView;
        private final TextView name;
        private final ImageView status;

        public HolderView(View view){
            imageView = view.findViewById(R.id.list_view_imageview);
            name = view.findViewById(R.id.list_view_name);
            status = view.findViewById(R.id.item_status);
        }
    }
}
