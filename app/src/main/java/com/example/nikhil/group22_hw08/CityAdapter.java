package com.example.nikhil.group22_hw08;
/**
 * Assignment - Homework #08
 * File name - CityAdapter.java
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityHolder> {

    private List<City> list;
    private LayoutInflater inflater;
    private ItemClickInterface itemClickInterface;
    private Context context;

    public CityAdapter(Context context, List<City> list, ItemClickInterface itemClickInterface) {

        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.itemClickInterface = itemClickInterface;
    }

    @Override
    public CityHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.saved_city_layout,parent,false);
        return new CityHolder(view);
    }

    @Override
    public void onBindViewHolder(CityHolder holder, int position) {

        City city = list.get(position);
        holder.cityNameCountryName.setText(city.getCityName()+", "+city.getCountry());
        holder.temp.setText("Temperature: "+city.getTemperature()+" "+context.getSharedPreferences("myPref",MODE_PRIVATE).getString("tempUnit",""));
        if(city.isFavorite()) {
            holder.starButton.setImageResource(R.drawable.star_gold);
        }
        if(city.getLastUpdated()!=null)
            holder.updated.setText("Last updated: "+city.getLastUpdated());
        else
            itemClickInterface.getTime(city);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class CityHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        private TextView cityNameCountryName,temp,updated;
        private ImageButton starButton;
        private View itemContainer;

        public CityHolder(View itemView) {

            super(itemView);
            cityNameCountryName = (TextView) itemView.findViewById(R.id.cityNameCountryName);
            temp = (TextView) itemView.findViewById(R.id.temp);
            updated = (TextView) itemView.findViewById(R.id.updated);
            starButton = (ImageButton) itemView.findViewById(R.id.favorite_button);
            starButton.setOnClickListener(this);
            itemContainer = itemView.findViewById(R.id.item_container);
            itemContainer.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.favorite_button:
                    City city = list.get(getAdapterPosition());
                    if(city.isFavorite()) {
                        Toast.makeText(context,"Removed from Favorites",Toast.LENGTH_SHORT).show();
                        city.setFavorite(false);
                    } else {
                        Toast.makeText(context,"Saved to Favorites",Toast.LENGTH_SHORT).show();
                        city.setFavorite(true);
                    }
                    itemClickInterface.updateItem(list.get(getAdapterPosition()),getAdapterPosition());
                    break;
                default:
                    break;
            }

        }

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.item_container:
                    City city = list.get(getAdapterPosition());
                    itemClickInterface.deleteItem(city,getAdapterPosition());
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    public interface ItemClickInterface extends Serializable {
        void updateItem(City city,int pos);
        void deleteItem(City city,int pos);
        void getTime(City city);
        void onItemClick(int position);
    }
}
