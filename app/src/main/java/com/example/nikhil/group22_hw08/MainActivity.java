package com.example.nikhil.group22_hw08;
/**
 * Assignment - Homework #08
 * File name - MainActivity.java
 * Full Name - Naga Manikanta Sri Venkata Jonnalagadda
 *             Karthik Gorijavolu
 * Group #22
 * **/
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener,ImageAsyncTask.ImageInterface, CityAdapter.ItemClickInterface {

    TextView cityCountryTextView,weatherTextView,temperatureTextView,timeTextView,
            noCitiesToDisplayTextView,searchCitiesTextView;
    Button setCurrentCityButton,searchCityButton;
    EditText cityEditText,countryEditText;
    ImageView weatherImageView;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    DatabaseReference rootReference;
    List<City> cityList;
    String currentCity,currentUnit;
    CityAdapter cityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        SharedPreferences preferences = getSharedPreferences("myPref",MODE_PRIVATE);
        currentCity = preferences.getString("currentCity","");
        String currentCountry = preferences.getString("currentCountry","");
        if(!currentCity.isEmpty()) {
            handleSetCurrentCity(currentCity,currentCountry);
        }

        currentUnit = preferences.getString("tempUnit","");
        if(currentUnit.isEmpty()) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("tempUnit","C");
            editor.apply();
            currentUnit = "C";
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("myPref",MODE_PRIVATE);
        String city = preferences.getString("currentCity","");
        String unit = preferences.getString("tempUnit","");
        String country = preferences.getString("currentCountry","");
        if(!city.equals(currentCity)) {
            progressBar.setVisibility(View.VISIBLE);
            weatherImageView.setVisibility(View.INVISIBLE);
            cityCountryTextView.setVisibility(View.INVISIBLE);
            weatherTextView.setText("");
            temperatureTextView.setVisibility(View.INVISIBLE);
            timeTextView.setVisibility(View.INVISIBLE);
            handleSetCurrentCity(city,country);
        }

        if(!unit.equals(currentUnit)) {
            currentUnit = unit;
            handleSetCurrentCity(city,country);
            for(City city1: cityList) {
                getTime(city1);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
               break;

        }
        return true;
    }

    private void initializeViews() {

        cityList = new ArrayList<>();
        cityCountryTextView = (TextView) findViewById(R.id.cityCountryTextView);
        weatherTextView = (TextView) findViewById(R.id.weatherTextView);
        temperatureTextView = (TextView) findViewById(R.id.temperatureTextView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        noCitiesToDisplayTextView = (TextView) findViewById(R.id.noCitiesToDisplay);
        searchCitiesTextView = (TextView) findViewById(R.id.searchCities);

        setCurrentCityButton = (Button) findViewById(R.id.setCurrentCityButton);
        searchCityButton = (Button) findViewById(R.id. searchCityButton);
        setCurrentCityButton.setOnClickListener(this);
        searchCityButton.setOnClickListener(this);

        cityEditText = (EditText) findViewById(R.id.cityEditText);
        countryEditText = (EditText) findViewById(R.id.countryEditText);
        weatherImageView = (ImageView) findViewById(R.id.weatherImageView);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        FirebaseApp.initializeApp(this);
        rootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference childReference = rootReference.child("cities");
        childReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cityList.clear();
                for(DataSnapshot citySnapshot : dataSnapshot.getChildren()) {
                    City city = citySnapshot.getValue(City.class);
                    Log.d("nikhil","city to string is "+city.toString());
                    cityList.add(city);
                }

                if(cityList.size() > 0) {
                    cityAdapter = new CityAdapter(MainActivity.this,cityList,MainActivity.this);
                    recyclerView.setAdapter(cityAdapter);
                    recyclerView.setVisibility(View.VISIBLE);
                    noCitiesToDisplayTextView.setVisibility(View.INVISIBLE);
                    searchCitiesTextView.setVisibility(View.INVISIBLE);
                    cityAdapter.notifyDataSetChanged();
                } else {
                    recyclerView.setVisibility(View.INVISIBLE);
                    noCitiesToDisplayTextView.setVisibility(View.VISIBLE);
                    searchCitiesTextView.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.setCurrentCityButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.custom_dialog,null,false);
                final EditText city = (EditText) dialogView.findViewById(R.id.cityEditText);
                final EditText country = (EditText) dialogView.findViewById(R.id.countryEditText);
                builder.setTitle("Enter City Details").setView(dialogView)
                        .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressBar.setVisibility(View.VISIBLE);
                                handleSetCurrentCity(city.getText().toString(),country.getText().toString());
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                }).create().show();

                break;
            case R.id.searchCityButton:
                String cityText = cityEditText.getText().toString();
                String countryText = countryEditText.getText().toString();
                if(cityText.isEmpty() || countryText.isEmpty()) {
                    Toast.makeText(MainActivity.this,"Enter both city and country",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this,CityWeatherActivity.class);
                intent.putExtra("city",cityEditText.getText().toString());
                intent.putExtra("country",countryEditText.getText().toString());
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void handleSetCurrentCity(final String city, final String country) {

        OkHttpClient client = new OkHttpClient();
        String url = "https://dataservice.accuweather.com/locations/v1/"+country+"/search?apikey="+getString(R.string.api_key)+"&q="+city;
        Log.d("nikhil","url is "+url);
        final Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
                Log.d("nikhil","onFailure is called message is "+e.getMessage()+" trace is ");
                e.printStackTrace();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"City not Found",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                String resp = response.body().string();
                if(resp.equals("[]")) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            weatherImageView.setVisibility(View.INVISIBLE);
                            cityCountryTextView.setVisibility(View.INVISIBLE);
                            weatherTextView.setText(R.string.city_not_set);
                            temperatureTextView.setVisibility(View.INVISIBLE);
                            timeTextView.setVisibility(View.INVISIBLE);
                            setCurrentCityButton.setVisibility(View.VISIBLE);
                            return;

                        }
                    });
                }
                try {
                    JSONArray array = new JSONArray(resp);
                    JSONObject jsonObject = array.getJSONObject(0);
                    String key = jsonObject.getString("Key");

                    SharedPreferences preferences = getSharedPreferences("myPref",MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    final String currentCity = preferences.getString("currentCity","");
                    editor.putString("currentCity",city);
                    editor.putString("currentCountry",country);
                    editor.apply();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cityCountryTextView.setText(city+", "+country);
                            cityCountryTextView.setVisibility(View.VISIBLE);
                            if(!currentCity.isEmpty()) {
                                Toast.makeText( MainActivity.this,"Current city Updated",Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this,"Current city Saved",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    getWeatherDetailsAndUpdateUI(key);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getWeatherDetailsAndUpdateUI(String key) {
        OkHttpClient client = new OkHttpClient();
        Log.d("nikhil","key is "+key);
        final Request request = new Request.Builder()
                .url("https://dataservice.accuweather.com/currentconditions/v1/"+key+"?apikey="+getString(R.string.api_key))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("nikhil","onFailure is called message is "+e.getMessage()+" trace is ");
                e.printStackTrace();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"City not Found",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                final String resp = response.body().string();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONArray jsonArray = new JSONArray(resp);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            weatherTextView.setText(jsonObject.getString("WeatherText"));
                            int weatherIcon = jsonObject.getInt("WeatherIcon");
                            String icon;
                            if(weatherIcon < 10) {
                                icon = "0"+String.valueOf(weatherIcon);
                            } else {
                                icon = String.valueOf(weatherIcon);
                            }
                            String imageUrl = "https://developer.accuweather.com/sites/default/files/"+icon+"-s.png";
                            Log.d("nikhil","image url is "+imageUrl);
                          //  new ImageAsyncTask(MainActivity.this).execute(imageUrl);
                            Picasso.with(MainActivity.this).load(imageUrl).fit().into(weatherImageView, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    setCurrentCityButton.setVisibility(View.INVISIBLE);
                                    weatherImageView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError() {

                                }
                            });

                            PrettyTime prettyTime = new PrettyTime();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
                            try {
                                Date date = format.parse(jsonObject.getString("LocalObservationDateTime"));
                                timeTextView.setText("Updated "+prettyTime.format(date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            timeTextView.setVisibility(View.VISIBLE);
                            String tempUnit;
                            if(getSharedPreferences("myPref",MODE_PRIVATE).getString("tempUnit","").equals("C")) {
                                tempUnit = "Metric";
                            } else {
                                tempUnit = "Imperial";
                            }
                            String tempValue = jsonObject.getJSONObject("Temperature").getJSONObject(tempUnit).getString("Value");
                            String unit = jsonObject.getJSONObject("Temperature").getJSONObject(tempUnit).getString("Unit");
                            temperatureTextView.setText("Temperature : "+tempValue+" "+unit);
                            temperatureTextView.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void sendImage(Bitmap bitmap) {
        weatherImageView.setImageBitmap(bitmap);
    }

    @Override
    public void updateItem(City city,int pos) {
        DatabaseReference childRef = rootReference.child("cities").child(city.getUid());
        cityList.set(pos,city);
        childRef.setValue(city);
        cityAdapter.notifyDataSetChanged();
    }

    @Override
    public void deleteItem(City city,int pos) {

        DatabaseReference childRef = rootReference.child("cities").child(city.getUid());
        childRef.removeValue();
        cityList.remove(city);
        cityAdapter.notifyDataSetChanged();
    }

    @Override
    public void getTime(final City city) {
        OkHttpClient client = new OkHttpClient();
        Log.d("nikhil","key is "+city.getCityKey());
        final Request request = new Request.Builder()
                .url("https://dataservice.accuweather.com/currentconditions/v1/"+city.getCityKey()+"?apikey="+getString(R.string.api_key))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("nikhil","onFailure is called message is "+e.getMessage()+" trace is ");
                e.printStackTrace();
            }
            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                final String resp = response.body().string();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONArray jsonArray = new JSONArray(resp);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String tempUnit;
                            if(getSharedPreferences("myPref",MODE_PRIVATE).getString("tempUnit","").equals("C")) {
                                tempUnit = "Metric";
                            } else {
                                tempUnit = "Imperial";
                            }
                            String temp = jsonObject.getJSONObject("Temperature").getJSONObject(tempUnit).getString("Value");
                            city.setTemperature(Double.parseDouble(temp));
                            PrettyTime prettyTime = new PrettyTime();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
                            try {
                                Date date = format.parse(jsonObject.getString("LocalObservationDateTime"));
                                city.setLastUpdated(prettyTime.format(date));
                                cityAdapter.notifyDataSetChanged();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onItemClick(int position) {

    }
}
