package com.softbankrobotics.qisdktutorials.ui.tutorials.listen;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by tony on 17. 12. 11.
 */

public class MusioSingleton {
    private static MusioSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private MusioSingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }
    public static synchronized MusioSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MusioSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
