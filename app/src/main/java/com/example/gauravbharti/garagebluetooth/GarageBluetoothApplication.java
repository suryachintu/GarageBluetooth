package com.example.gauravbharti.garagebluetooth;

import android.app.Activity;
import android.app.Application;

/**
 * Created by gauravbharti on 11/03/17.
 */

public class GarageBluetoothApplication extends Application
{   public static GarageBluetoothApplication mInstance;
    private Activity mCurrentActivity=null;
    public static final String TAG=GarageBluetoothApplication.class.getSimpleName();
    @Override
    public void onCreate()
    {   super.onCreate();
        mInstance=this;
    }
    public Activity getCurrentActivity()
    {
        return mCurrentActivity;
    }
    public static synchronized GarageBluetoothApplication getInstance()
    {
        return mInstance;
    }
    public void setCurrentActivity(Activity mCurrentActivity)
    {
        this.mCurrentActivity=mCurrentActivity;
    }
}
