package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.RawRes;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


/**
 * The activity for the QiChatVariables tutorial.
 */
public class QiChatVariablesTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private ConversationView conversationView;
    private static final String TAG = "QiChat";

    private Animate animate;

    // Store the variable.
    private QiChatVariable variable;
    private Discuss discuss;
    private Bookmark update_Musio_response;
    private String Human_input="default text";

    public static RequestQueue queue;
    private final String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE0ODg1MjM1NDEsImNsaWVudF9rZXkiOiIybVp3cUFQYUh5TnRSR2l4NFpVZ3FMME8wR1BOWUw0OXZUZ2RwMG1SdHRFaG5DblQ2UkVJRVJ6Yjd4Y24zaUlpR3lWVFVvVHA2a1hid0Rzb0dhZHpsUlJONXpVODczdHk2Z0JPMWIzdFRGNmhvR2U5SEt4M3laZjdOOXJEcWxnYSIsImVtYWlsIjoiakBha2EuY29tIiwiZXhwIjoxNDkzNzA3NTQxLCJpZCI6NTAxfQ.t7PeLgAnSrUXeBFg8vnU0sxamf5ZbIYZqGfcIga3Kxs";
    private final String user_id = "0";
    private String Musio_url = "https://ai.themusio.com/chat/send/";
    private String MusioResponse = "default text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conversationView = findViewById(R.id.conversationView);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        queue_setup();
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
    public void onRobotFocusGained(final QiContext qiContext) {
        final String textToSay = "I can discuss with humans: say \"Hello\" to start the discussion.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);
        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();
        say.run();

        // Create a topic.
        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.variable)
                .build();

        // Create a new discuss action.
        discuss = DiscussBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Get the variable.
        variable = discuss.variable("var");

        Map<String, Bookmark> bookmarks = topic.getBookmarks();
        // Get the proposal bookmark.
        update_Musio_response = bookmarks.get("say_response");


        discuss.setOnStartedListener(new Discuss.OnStartedListener() {
            @Override
            public void onStarted() {
                String message = "Discussion started.";
                Log.i(TAG, message);
                displayLine(message, ConversationItemType.INFO_LOG);
            }
        });

        discuss.setOnLatestInputUtteranceChangedListener(new Discuss.OnLatestInputUtteranceChangedListener() {
            @Override
            public void onLatestInputUtteranceChanged(Phrase input) {
                displayLine(input.getText(), ConversationItemType.HUMAN_INPUT);
                Human_input = input.getText();
                Log.d(TAG,"human text: " + Human_input);
                post_message_json(Human_input);//send human input to Muse server
                if(Human_input.equals("hi"))//this is for animation
                    mimic(R.raw.hello_a002, qiContext);
            }
        });

        discuss.setOnLatestOutputUtteranceChangedListener(new Discuss.OnLatestOutputUtteranceChangedListener() {
            @Override
            public void onLatestOutputUtteranceChanged(Phrase output) {
                displayLine(output.getText(), ConversationItemType.ROBOT_OUTPUT);
            }
        });

        // Run the discuss action asynchronously.
        discuss.run();
    }


    private void post_message_json(String mes){
        JsonResponseRequest request = new JsonResponseRequest();
        request.setUrl(Musio_url);

        request.setMethod(Request.Method.POST);
        request.putHeader("Authorization", "Bearer " + jwt);
        request.putParam("candidates", mes);
        request.putParam("member_id", "0");
        request.setListener(new SuccessListener() {
            @Override
            public void onSuccess(JSONObject jsonObject) throws JSONException, RemoteException {
//                Log.d(TAG, TAG + " @ Chat onSuccess Called");
//                Log.d(TAG, TAG + " @ Chat onSuccess Json Object : " + jsonObject.toString());
                JSONObject data = jsonObject.optJSONObject("data");
                JSONObject MusioText = data.optJSONObject("musio_text");
                MusioResponse = MusioText.getString("message");
                Log.d(TAG,"Musio response: "+MusioResponse);
                variable.async().setValue(MusioResponse);
                discuss.async().goToBookmarkedOutputUtterance(update_Musio_response);//say the response
            }

            @Override
            public void onCustomError(int code, String message) {
//                return message;
//                returnAsyncMessage(errorMessage(jsonErrorResult(code, message)));
            }
        });
        queue.add(request.getRequest());
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
        // Nothing here.
        Log.e(TAG,"robot lost focus");
        // Remove the listeners from the discuss action.
        if (discuss != null) {
            discuss.setOnStartedListener(null);
            discuss.setOnLatestInputUtteranceChangedListener(null);
            discuss.setOnLatestOutputUtteranceChangedListener(null);
        }
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

    private void mimic(@RawRes Integer mimicResource, QiContext qiContext) {
        // Create an animation from the mimic resource.
        Animation animation = AnimationBuilder.with(qiContext)
                .withResources(mimicResource)
                .build();

        // Create an animate action.
        Animate animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build();

        // Run the animate action asynchronously.
        animate.async().run();
    }
}
