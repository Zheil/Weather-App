package com.example.nikhil.group22_hw08;
/**
 * Assignment - Homework #08
 * File name - CustomAdapter.java
 * Full Name - Naga Manikanta Sri Venkata Jonnalagadda
 *             Karthik Gorijavolu
 * Group #22
 * **/
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ItemHolder> {

    private List<Item> list;
    private LayoutInflater inflater;
    private ItemClickInterface itemClickInterface;
    private Context context;

    public CustomAdapter(Context context, List<Item> list, ItemClickInterface itemClickInterface) {

        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.itemClickInterface = itemClickInterface;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.item_layout,parent,false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {

        final Item item = list.get(position);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yy");
        Date date = null;
        try {
            date = format1.parse(item.getText());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.itemTextView.setText(format2.format(date).replace("-"," "));
        if(item.getBitmap()!=null)
            holder.itemImageView.setImageBitmap(item.getBitmap());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView itemImageView;
        private TextView itemTextView;
        private View itemContainer;

        public ItemHolder(View itemView) {

            super(itemView);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
            itemTextView = (TextView) itemView.findViewById(R.id.itemTextView);

            itemContainer = itemView.findViewById(R.id.item_container);
            itemContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_container:
                    itemClickInterface.onItemClick(getAdapterPosition());
                    break;
                default:
                    break;
            }

        }
    }

    public interface ItemClickInterface extends Serializable {
        void onItemClick(int position);
    }
}
