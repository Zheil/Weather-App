package com.example.nikhil.group22_hw08;
/**
 * Assignment - Homework #08
 * File name - SettingsFragment.java
 * Full Name - Naga Manikanta Sri Venkata Jonnalagadda
 *             Karthik Gorijavolu
 * Group #22
 * **/
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    EditText cityEditText, countryEditText;
    String finalPosButtonText;

    public SettingsFragment() {

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        String key = preference.getKey();
        if(key.equals(getString(R.string.pref_key_temp_unit))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final SharedPreferences pref = getActivity().getSharedPreferences("myPref",Context.MODE_PRIVATE);
            String unit = pref.getString("tempUnit","");
            final int[] checkedItem = {(unit.equals("C") ? 0 : 1)};
            builder.setTitle(R.string.choose_temp_unit).setSingleChoiceItems(R.array.listArray, checkedItem[0], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkedItem[0] = which;
                }
            }).setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = pref.edit();
                    if(checkedItem[0] == 0) {
                        editor.putString("tempUnit","C");
                        Toast.makeText(getActivity().getApplicationContext(), R.string.tempcelcius,Toast.LENGTH_SHORT).show();
                    } else {
                        editor.putString("tempUnit","F");
                        Toast.makeText(getActivity().getApplicationContext(), R.string.temp_fahren,Toast.LENGTH_SHORT).show();
                    }
                    editor.apply();
                }
            }).create().show();
        } else if (key.equals("pref_key_current_city")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            SharedPreferences preferences = getActivity().getSharedPreferences("myPref",Context.MODE_PRIVATE);
            String city = preferences.getString("currentCity","");
            String country = preferences.getString("currentCountry","");
            String title = "Enter city details";
            String posButtonText = "Set";
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.custom_dialog,null,false);
            cityEditText = (EditText) view.findViewById(R.id.cityEditText);
            countryEditText = (EditText) view.findViewById(R.id.countryEditText);
            if(!city.isEmpty()) {
                title = "Update city details";
                posButtonText = "Change";
                cityEditText.setText(city);
                countryEditText.setText(country);
            }
            finalPosButtonText = posButtonText;
            builder.setTitle(title).setView(view).setPositiveButton(posButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("nikhil"," setPositiveButton which is "+which);
                    handleSetCurrentCity(cityEditText.getText().toString(),countryEditText.getText().toString());

                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("nikhil","setNegativeButton which is  "+which);
                }
            }).create().show();
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(),"City not found",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                String resp = response.body().string();
                if(resp.equals("[]")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(),"City not found",Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    SharedPreferences pref = getActivity().getSharedPreferences("myPref",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("currentCity",cityEditText.getText().toString());
                    editor.putString("currentCountry",countryEditText.getText().toString());
                    editor.apply();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(finalPosButtonText.equals("Change")) {
                                Toast.makeText(getActivity().getApplicationContext(),"Current City updated",Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(),"Current City Set",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }


}
