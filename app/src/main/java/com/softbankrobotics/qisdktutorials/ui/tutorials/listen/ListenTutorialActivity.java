package com.softbankrobotics.qisdktutorials.ui.tutorials.listen;

import android.app.DownloadManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;
import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import static java.net.Proxy.Type.HTTP;

/**
 * The activity for the Listen tutorial.
 */
public class ListenTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "ListenTutorialActivity";
    private ConversationView conversationView;
    public static RequestQueue queue;
    private final String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE0ODg1MjM1NDEsImNsaWVudF9rZXkiOiIybVp3cUFQYUh5TnRSR2l4NFpVZ3FMME8wR1BOWUw0OXZUZ2RwMG1SdHRFaG5DblQ2UkVJRVJ6Yjd4Y24zaUlpR3lWVFVvVHA2a1hid0Rzb0dhZHpsUlJONXpVODczdHk2Z0JPMWIzdFRGNmhvR2U5SEt4M3laZjdOOXJEcWxnYSIsImVtYWlsIjoiakBha2EuY29tIiwiZXhwIjoxNDkzNzA3NTQxLCJpZCI6NTAxfQ.t7PeLgAnSrUXeBFg8vnU0sxamf5ZbIYZqGfcIga3Kxs";
    private final String user_id = "0";
    private String Musio_url = "https://ai-test.themusio.com/chat/send/";
    private String MusioResponse = "default text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        queue_setup();
        Log.d("tony","come to onCreate Listentutorial");
    }

    @Override
    protected void onDestroy() {
        // Unregister all the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this);
        super.onDestroy();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.conversation_layout;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.d("tony","onRobotFocusGained");
        String textToSay = "I can listen to you: say something to start chating.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();
//
        say.run();
//
//        // Create a new listen action.
//        Listen listen = ListenBuilder.with(qiContext) // Create the builder with the QiContext.
//                .withPhraseSet(phraseSetYes, phraseSetNo) // Set the PhraseSets to listen to.
//                .build(); // Build the listen action.
//
//        // Run the listen action 0and get the result.
//        ListenResult listenResult = listen.run();
//        final String humanText = listenResult.getHeardPhrase().getText();
//        String message = "Heard phrase: " + humanText;
//        Log.i(TAG, message);
//        displayLine(humanText, ConversationItemType.HUMAN_INPUT);

        final String humanText = "are you a big cat person?";
//        post_message(humanText);
          post_message_json(humanText);
    }


    private void post_message_json(final String mes){
        JsonResponseRequest request = new JsonResponseRequest();
        request.setUrl(Musio_url);

        request.setMethod(Request.Method.POST);
        request.putHeader("Authorization", "Bearer " + jwt);
        request.putParam("candidates", mes);
        request.putParam("member_id", "0");
        request.setListener(new SuccessListener() {
            @Override
            public void onSuccess(JSONObject jsonObject) throws JSONException, RemoteException {
                Log.d(TAG, TAG + " @ Chat onSuccess Called");
                Log.d(TAG, TAG + " @ Chat onSuccess Json Object : " + jsonObject.toString());
                JSONObject data = jsonObject.optJSONObject("data");
                JSONObject MusioText = data.optJSONObject("musio_text");
                MusioResponse = MusioText.getString("message");
                Log.d("tony", MusioResponse);
                displayLine(MusioResponse, ConversationItemType.HUMAN_INPUT);
            }

            @Override
            public void onCustomError(int code, String message) {
//                return message;
//                returnAsyncMessage(errorMessage(jsonErrorResult(code, message)));
            }
        });
        queue.add(request.getRequest());
    }



    private void post_message(final String mes){
        StringRequest myReq = new StringRequest(Request.Method.POST,
                Musio_url,
                createMyReqSuccessListener(),
                createMyReqErrorListener()) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Basic Authentication
                headers.put("Authorization", "Bearer " + jwt);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
//                params.put("candidates", "is the sky blue");
                params.put("candidates", mes);
                params.put("member_id", user_id);
                return params;
            }
        };
        queue.add(myReq);
    }
    private Response.Listener<String> createMyReqSuccessListener() {
        return new Response.Listener<String>() {

            @Override

            public void onResponse(String response) {
                Log.d("Tony-response",response);
            }
        };
    }
    private Response.ErrorListener createMyReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Tony-error", error.getMessage());
                //mTvResult.setText(error.getMessage());
            }
        };
    }
    private void queue_setup(){
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        queue = new RequestQueue(cache, network);
        // Start the queue
        queue.start();
    }

    private abstract class SuccessListener implements JsonResponseListener {
        abstract public void onSuccess(JSONObject jsonObject) throws JSONException, RemoteException;

        abstract public void onCustomError(int code, String message);

        @Override
        public void onError(int code, String message) {
            if (code == 1002) {
                //JWT Auth failed
//                if (message == Authenticator.MUSE_CLIENT_KEY) {
//                    signOut();
//                } else {
//                    user.removeClient(message);
//                }
            }
            onCustomError(code, message);
        }

        @Override
        public void onTokenRefresh(String jwt) throws JSONException {
//            user.putClient(jwt);
        }
    }

    @Override
    public void onRobotFocusLost() {
        Log.d("Tony","On Robot Focus Lost");
        // Nothing here.
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Nothing here.
    }

    private void displayLine(final String text, final ConversationItemType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                conversationView.addLine(text, type);
            }
        });
    }
}
