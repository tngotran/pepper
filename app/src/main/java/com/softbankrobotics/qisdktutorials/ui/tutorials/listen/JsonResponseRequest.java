package com.softbankrobotics.qisdktutorials.ui.tutorials.listen;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

//import com.akaintelligence.musio.library.muse.MuseAttribute;
//import com.akaintelligence.musio.library.utils.LOG;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by james on 2016. 6. 3..
 */
public class JsonResponseRequest {
  private static final String TAG = JsonResponseRequest.class.getSimpleName();
  String url;
  int method;
  Map<String, String> headers;
  Map<String, String> params;
  JsonResponseListener jsonResponseListener;
  Response.Listener responseListener;
  Response.ErrorListener errorListener;
  StringRequest request;
  Request rq;
  Messenger messenger;
  int what;

  public JsonResponseRequest() {
    headers = new HashMap<>();
    params = new HashMap<>();
  }

  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @param method : int value,   DEPRECATED_GET_OR_POST = -1; GET = 0; POST = 1; PUT = 2; DELETE =
   *               3; HEAD = 4; OPTIONS = 5; TRACE = 6; PATCH = 7;
   */
  public void setMethod(int method) {
    this.method = method;
  }

  public void setReplyTo(Messenger messenger, int what){
    this.what = what;
    this.messenger = messenger;
  }

  public void putHeader(String key, String value) {
    if (value != null) {
      headers.put(key, value);
    }
  }

  public void putParam(String key, String value) {
    if (value != null) {
      params.put(key, value);
    }
  }

  public Request getRequest() {
    request = new StringRequest(method, url, responseListener, errorListener) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
      }

      @Override
      protected Map<String, String> getParams() {
        return params;
      }
    };
    request.setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    return request;
  }

  public void setListener(final JsonResponseListener jsonResponseListener) {
    Log.d(TAG, "setListener @ jsonResponseListener");
    this.jsonResponseListener = jsonResponseListener;
    responseListener = new Response.Listener() {
      @Override
      public void onResponse(Object o) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("status",false);
        try {
          JSONObject jsonObject = new JSONObject(o.toString());

          String jwt = jsonObject.optString("token");
          Log.d(TAG, "jwt : " + jwt);
          if(jwt!=""){
            jsonResponseListener.onTokenRefresh(jwt);
          }
          if(jsonObject.optBoolean("status")){
            jsonResponseListener.onSuccess(jsonObject);
            reply(jsonObject);
          }else {
            int code = jsonObject.optInt("code");
            String message = jsonObject.optString("message");
            Log.d(TAG, "code : " + code + " , message : " + message);
            jsonResponseListener.onError(code, message);
            replyError(code, message);
          }
        } catch (JSONException e) {
          e.printStackTrace();
        } catch (RemoteException e) {
          e.printStackTrace();
        } catch (NullPointerException e){
          e.printStackTrace();
        }
      }
    };
    errorListener = new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError volleyError) {
        volleyError.printStackTrace();
        Log.d("VolleyError","Volley error: "+ volleyError.getMessage());
        try {
          replyError(volleyError);
        } catch (RemoteException e) {
          e.printStackTrace();
        } catch (NullPointerException e){
          e.printStackTrace();
        }finally {
          if(volleyError!=null){
            jsonResponseListener.onError(0,volleyError.getMessage());
          }else {
            jsonResponseListener.onError(0,"Network error");
          }
        }
      }
    };
  }
  protected void reply(JSONObject jsonObject) throws RemoteException {
    Message message = Message.obtain(null, what);
    Bundle bundle = new Bundle();
//    bundle.putString(MuseAttribute.KEY_FULL_RESPONSE, jsonObject.toString());
    Iterator<String> keys = jsonObject.keys();
    while (keys.hasNext()){
      String key = keys.next();
      Object object = jsonObject.opt(key);
      if(object instanceof Integer){
        bundle.putInt(key, (Integer) object);
      }else if(object instanceof Boolean){
        bundle.putBoolean(key, (Boolean) object);
      }else {
        bundle.putString(key, object.toString());
      }
    }
    message.setData(bundle);
    if (this.messenger!=null){
      this.messenger.send(message);
    }
  }
  protected void replyError(VolleyError volleyError) throws RemoteException {
    Message message = Message.obtain(null, what);
    Bundle bundle = new Bundle();
    bundle.putBoolean("status", false);
    bundle.putInt("code", 0);
    bundle.putString("message", volleyError.getMessage());

    message.setData(bundle);
    this.messenger.send(message);
  }
  protected void replyError(int code, String msg) throws RemoteException {
    Message message = Message.obtain(null, what);
    Bundle bundle = new Bundle();
    bundle.putBoolean("status", false);
    bundle.putInt("code", 0);
    bundle.putString("message", msg);
    message.setData(bundle);
    this.messenger.send(message);
  }
}