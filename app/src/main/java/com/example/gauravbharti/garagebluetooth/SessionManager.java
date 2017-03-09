package com.example.gauravbharti.garagebluetooth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gauravbharti on 09/03/17.
 */

public class SessionManager
{   SharedPreferences.Editor editor;
    SharedPreferences pref;
    Context _context;

    int PRIVATE_MODE=0;

    private static final String PREF_NAME = "Garage";
    public static final String name="name";
    public static final String address="address";
    public SessionManager(Context context)
    {
        this._context=context;
        pref=_context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor=pref.edit();
    }





}
