package com.example.android.sunshine.app;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import com.example.android.sunshine.app.data.WeatherContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

public class WearService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
    private static final int INDEX_MIN_TEMP = 3;
    private int weatherId;
    private String formattedMinTemperature;
    private int weatherArtResourceId;
    private double maxTemp;
    private String description;
    private double minTemp;
    private String formattedMaxTemperature;


    public WearService() {
        super("TodayWidgetIntentService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get today's data from the ContentProvider
        String location = Utility.getPreferredLocation(this);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location, System.currentTimeMillis());
        Cursor data = getContentResolver().query(weatherForLocationUri, FORECAST_COLUMNS, null,
                null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the weather data from the Cursor
         weatherId = data.getInt(INDEX_WEATHER_ID);
         weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
         description = data.getString(INDEX_SHORT_DESC);
         maxTemp = data.getDouble(INDEX_MAX_TEMP);
         minTemp = data.getDouble(INDEX_MIN_TEMP);
         formattedMaxTemperature = Utility.formatTemperature(this, maxTemp);
         formattedMinTemperature = Utility.formatTemperature(this, minTemp);
        data.close();

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        // send data.
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),weatherArtResourceId);
        Asset asset = createAssetFromBitmap(bitmap);
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/wheater");
        putDataMapRequest.getDataMap().putString("min_temp",formattedMinTemperature);
        putDataMapRequest.getDataMap().putString("max_temp", formattedMaxTemperature);
        putDataMapRequest.getDataMap().putAsset("icon", asset);
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient,request).setResultCallback(
                new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {

                    }
                }
        );
        //send image.
    }
    private static Asset createAssetFromBitmap(Bitmap bitmap){
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteStream);
        return  Asset.createFromBytes(byteStream.toByteArray());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Intent service = new Intent(getBaseContext(),WearService.class);
        getBaseContext().startService(service);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
