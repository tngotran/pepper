package com.softbankrobotics.qisdktutorials.ui.tutorials.listen;

import android.os.RemoteException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2016. 6. 7..
 */
public interface JsonResponseListener {
  void onSuccess(JSONObject jsonObject) throws JSONException, RemoteException;
  void onError(int code, String message);
  void onTokenRefresh(String jwt) throws JSONException;
}
