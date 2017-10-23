package com.example.nikhil.group22_hw08;
/**
 * Assignment - Homework #08
 * File name - ImageAsyncTask.java
 * Full Name - Naga Manikanta Sri Venkata Jonnalagadda
 *             Karthik Gorijavolu
 * Group #22
 * **/

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageAsyncTask extends AsyncTask<Item,Void,Bitmap> {

    private ImageInterface imageInterface;

    public ImageAsyncTask(ImageInterface imageInterface) {
        super();
        this.imageInterface = imageInterface;
    }

    @Override
    protected Bitmap doInBackground(Item... params) {

        HttpURLConnection connection = null;
        try {
            URL url = new URL(params[0].getImageURL());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream is = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if(bitmap!=null) {
                params[0].setBitmap(bitmap);
                return bitmap;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        imageInterface.sendImage(bitmap);
    }

    public interface ImageInterface {
        void sendImage(Bitmap bitmap);
    }
}
