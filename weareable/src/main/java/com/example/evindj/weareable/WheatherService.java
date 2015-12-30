package com.example.evindj.weareable;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class WheatherService extends WearableListenerService {
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED){
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String path = event.getDataItem().getUri().getPath();
                if(path.equals("/wheater")){
                    String minTemp = dataMap.getString("min_temp");
                    String maxTemp = dataMap.getString("max_temp");
                    Asset asset = dataMap.getAsset("icon");
                    Intent activity =  new Intent(getBaseContext(),MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("min_temp",minTemp);
                    bundle.putString("max_temp", maxTemp);
                    activity.putExtras(bundle);
                    getBaseContext().startActivity(activity);
                }

            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset){
        if(asset == null){
            throw new IllegalArgumentException("Null ASset");
        }
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        ConnectionResult result = mGoogleApiClient.blockingConnect(1000, TimeUnit.MILLISECONDS);
        if(!result.isSuccess())
            return null;
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(mGoogleApiClient,asset).await().getInputStream();
        mGoogleApiClient.disconnect();
        if(assetInputStream == null){
            return  null;
        }
        return BitmapFactory.decodeStream(assetInputStream);
    }

}
