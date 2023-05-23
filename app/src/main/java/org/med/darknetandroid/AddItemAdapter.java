package org.med.darknetandroid;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


// This class' job is to create an adapter for the item to be added. This is made to organize these cards in a ListView
public class AddItemAdapter extends BaseAdapter {
    private Context context;
    List<Items> itemsList;
    ListView listView;
    Activity activity;


    public AddItemAdapter(Context context, List<Items> itemsList, ListView listView, Activity activity){
        this.context = context;
        this.itemsList = itemsList;
        this.listView = listView;
        this.activity = activity;
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


    //get the items and set their listener for the delete button as well as inflate the list view corresponding to every item
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        HolderView holderView;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.add_item_list_view, parent, false);
            holderView = new AddItemAdapter.HolderView(convertView);
            convertView.setTag(holderView);
        }else{
            holderView = (AddItemAdapter.HolderView) convertView.getTag();
        }
        final Items item = itemsList.get(position);
        holderView.imageView.setImageURI(item.getUri());
        ImageButton deleteButton = convertView.findViewById(R.id.add_item_delete_button);
        final View finalConvertView = convertView;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddItemActivity.itemsList.remove(position);
                AddItemActivity.Refresh(finalConvertView.getContext(), (Activity) finalConvertView.getContext());
            }
        });
        return convertView;
    }

    //This class is to hold the imageview and be referenced through the getView
    public static class HolderView{
        private final ImageView imageView;

        public HolderView(View view){
            imageView = view.findViewById(R.id.add_item_list_view_imageview);
        }
    }
}
