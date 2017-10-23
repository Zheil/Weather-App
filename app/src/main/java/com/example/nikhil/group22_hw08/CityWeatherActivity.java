package com.example.nikhil.group22_hw08;
/**
 * Assignment - Homework #08
 * File name - CityWeatherActivity.java
 * Full Name - Naga Manikanta Sri Venkata Jonnalagadda
 *             Karthik Gorijavolu
 * Group #22
 * **/
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CityWeatherActivity extends AppCompatActivity implements View.OnClickListener, CustomAdapter.ItemClickInterface, ImageAsyncTask.ImageInterface {

    ProgressDialog dialog;
    TextView dailyForecastTextView,actualHeadlineTextView,forecastOnDateTextView,
            tempTextView,dayWeatherTextView,nightWeatherTextView,clickMoreDetailsTextView,clickExtendedForecastTextView;
    ImageView dayImageView,nightImageView;
    RecyclerView recyclerView;
    CustomAdapter adapter;
    DatabaseReference databaseReference;
    String cityName,country;
    Long cityKey;
    Double temperature;
    String key,headlineMobileLink,forecastMobileLink;
    int pos = 0;
    String tempUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_weather);
        initializeViews();
        SharedPreferences preferences = getSharedPreferences("myPref",MODE_PRIVATE);
        tempUnit = preferences.getString("tempUnit","");
        Intent intent = getIntent();
        OkHttpClient client = new OkHttpClient();
        cityName = intent.getStringExtra("city");
        country = intent.getStringExtra("country");
        final Request request = new Request.Builder()
                .url("http://dataservice.accuweather.com/locations/v1/"+country+"/search?apikey="+getString(R.string.api_key)+"&q="+intent.getStringExtra("city"))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CityWeatherActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       dialog.dismiss();
                        Toast.makeText(CityWeatherActivity.this,"City not found",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                Log.d("nikhil","onFailure is called message is "+e.getMessage()+" trace is ");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                CityWeatherActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();

                    }
                });
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                String resp = response.body().string();
                if(resp.equals("[]")) {
                    CityWeatherActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CityWeatherActivity.this,"City not found",Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    });
                }
                Log.d("nikhil","resp is "+resp);
                try {
                    JSONArray array = new JSONArray(resp);
                    JSONObject jsonObject = array.getJSONObject(0);
                    key = jsonObject.getString("Key");
                    cityKey = Long.parseLong(key);
                    getFiveDayWeatherDetails(key,pos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void initializeViews() {
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage("Loading Data");
        dialog.show();

        dailyForecastTextView = (TextView) findViewById(R.id.dailyForecastTextView);
        dailyForecastTextView.setText("Daily forecast for "+getIntent().getStringExtra("city")+", "+getIntent().getStringExtra("country"));
        actualHeadlineTextView = (TextView) findViewById(R.id.actualHeadlineTextView);
        forecastOnDateTextView = (TextView) findViewById(R.id.forecastOnDateTextView);
        tempTextView = (TextView) findViewById(R.id.tempTextView);
        dayWeatherTextView = (TextView) findViewById(R.id.dayWeatherTextView);
        nightWeatherTextView = (TextView) findViewById(R.id.nightWeatherTextView);
        clickMoreDetailsTextView = (TextView) findViewById(R.id.clickMoreDetailsTextView);
        clickExtendedForecastTextView = (TextView) findViewById(R.id.clickExtendedForecastTextView);
        clickMoreDetailsTextView.setOnClickListener(this);
        clickExtendedForecastTextView.setOnClickListener(this);

        dayImageView = (ImageView) findViewById(R.id.dayImageView);
        nightImageView = (ImageView) findViewById(R.id.nightImageView);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,1,GridLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);


        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference childReference = databaseReference.child("cities");

        childReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                City city = dataSnapshot.getValue(City.class);
              //  notelist.add(city);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("myPref",MODE_PRIVATE);
        String unit = preferences.getString("tempUnit","");
        if(!unit.equals(tempUnit)) {
            tempUnit = unit;
            getFiveDayWeatherDetails(key,pos);
        }
    }

    private void getFiveDayWeatherDetails(String key, final int day) {
        OkHttpClient client = new OkHttpClient();
        Log.d("nikhil","key is "+key);
        final Request request = new Request.Builder()
                .url("https://dataservice.accuweather.com/forecasts/v1/daily/5day/"+key+"?apikey="+getString(R.string.api_key))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CityWeatherActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Toast.makeText(CityWeatherActivity.this,"City not found",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                Log.d("nikhil","onFailure is called message is "+e.getMessage()+" trace is ");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                final String resp = response.body().string();

                try {
                    final JSONObject jsonObject = new JSONObject(resp);
                    CityWeatherActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                actualHeadlineTextView.setText(jsonObject.getJSONObject("Headline").getString("Text"));
                                headlineMobileLink = jsonObject.getJSONObject("Headline").getString("MobileLink");
                                JSONArray array = jsonObject.getJSONArray("DailyForecasts");
                                JSONObject obj = array.getJSONObject(day);
                                String inputDate = obj.getString("Date").substring(0,10);
                                forecastMobileLink = obj.getString("MobileLink");
                                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                                SimpleDateFormat format2 = new SimpleDateFormat("MMM-dd-yyyy");
                                Date date = null;
                                try {
                                    date = format1.parse(inputDate);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                forecastOnDateTextView.setText("Forecast on "+format2.format(date).replace("-"," "));
                                String maxTemp = obj.getJSONObject("Temperature").getJSONObject("Maximum").getString("Value");
                                String minTemp = obj.getJSONObject("Temperature").getJSONObject("Minimum").getString("Value");
                                temperature = Double.parseDouble(maxTemp);
                                if(getSharedPreferences("myPref",MODE_PRIVATE).getString("tempUnit","").equals("C")) {
                                    Double max = Double.parseDouble(maxTemp);
                                    Double min = Double.parseDouble(minTemp);
                                    max = (max-32)*5/9;
                                    min = (min-32)*5/9;
                                    maxTemp = String.valueOf(max).substring(0,4);
                                    minTemp = String.valueOf(min).substring(0,4);
                                    temperature = max;
                                }
                                tempTextView.setText("Temperature: "+maxTemp+" / "+minTemp);
                                dayWeatherTextView.setText(obj.getJSONObject("Day").getString("IconPhrase"));
                                nightWeatherTextView.setText(obj.getJSONObject("Night").getString("IconPhrase"));
                                int weatherIcon = obj.getJSONObject("Day").getInt("Icon");
                                String icon;
                                if(weatherIcon < 10) {
                                    icon = "0"+String.valueOf(weatherIcon);
                                } else {
                                    icon = String.valueOf(weatherIcon);
                                }
                                String imageUrl = "https://developer.accuweather.com/sites/default/files/"+icon+"-s.png";
                                Log.d("nikhil","image url is "+imageUrl);
                                Picasso.with(CityWeatherActivity.this).load("https://developer.accuweather.com/sites/default/files/"+icon+"-s.png").fit().into(dayImageView);
                                weatherIcon = obj.getJSONObject("Night").getInt("Icon");
                                if(weatherIcon < 10) {
                                    icon = "0"+String.valueOf(weatherIcon);
                                } else {
                                    icon = String.valueOf(weatherIcon);
                                }
                                Picasso.with(CityWeatherActivity.this).load("https://developer.accuweather.com/sites/default/files/"+icon+"-s.png").fit().into(nightImageView);
                                List<Item> items = new ArrayList<Item>();
                                for(int i=0;i<array.length();i++) {
                                    JSONObject object = array.getJSONObject(i);
                                    Item item = new Item();
                                    item.setText(object.getString("Date").substring(0,10));
                                    weatherIcon = object.getJSONObject("Day").getInt("Icon");
                                    if(weatherIcon < 10) {
                                       item.setImageURL("https://developer.accuweather.com/sites/default/files/0"+String.valueOf(weatherIcon)+"-s.png");
                                    } else {
                                        item.setImageURL("https://developer.accuweather.com/sites/default/files/"+String.valueOf(weatherIcon)+"-s.png");
                                    }
                                    new ImageAsyncTask(CityWeatherActivity.this).execute(item);
                                    items.add(item);
                                }
                                adapter = new CustomAdapter(CityWeatherActivity.this,items,CityWeatherActivity.this);
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.city_weather_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.saveCity:
                final City city = new City(cityKey,cityName,country,temperature,false);
                DatabaseReference reference = databaseReference.child("cities");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean entered = false;
                        for(DataSnapshot citySnapshot: dataSnapshot.getChildren()) {
                            City city1 = citySnapshot.getValue(City.class);
                            if(city1.getCityKey().equals(cityKey)) {
                                entered = true;
                                DatabaseReference childRef = databaseReference.child("cities").child(city1.getUid());
                                city.setFavorite(city1.isFavorite());
                                city.setUid(city1.getUid());
                                childRef.setValue(city);
                                Toast.makeText(CityWeatherActivity.this,"City Updated",Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        if(!entered) {
                            City city = new City(cityKey,cityName,country,temperature,false);
                            DatabaseReference childRef = databaseReference.child("cities").push();
                            city.setUid(childRef.getKey());
                            childRef.setValue(city);
                            Toast.makeText(CityWeatherActivity.this,"City Saved",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                break;
            case R.id.currentCity:
                SharedPreferences preferences = getSharedPreferences("myPref",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                String currentCity = preferences.getString("currentCity","");
                editor.putString("currentCity",cityName);
                editor.putString("currentCountry",country);
                editor.apply();
                if(!currentCity.isEmpty()) {
                    Toast.makeText(CityWeatherActivity.this,"Current city Updated",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CityWeatherActivity.this,"Current city Saved",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;

        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clickMoreDetailsTextView:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(forecastMobileLink));
                startActivity(intent);
                break;
            case R.id.clickExtendedForecastTextView:
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setData(Uri.parse(headlineMobileLink));
                startActivity(intent1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        pos = position;
        getFiveDayWeatherDetails(key,position);
    }

    @Override
    public void sendImage(Bitmap bitmap) {
        adapter.notifyDataSetChanged();
    }
}
