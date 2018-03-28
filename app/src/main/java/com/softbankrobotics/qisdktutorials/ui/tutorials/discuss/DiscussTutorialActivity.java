package com.softbankrobotics.qisdktutorials.ui.tutorials.discuss;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.conversation.TopicStatus;
import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationItemType;
import com.softbankrobotics.qisdktutorials.ui.conversation.ConversationView;
import com.softbankrobotics.qisdktutorials.ui.tutorials.TutorialActivity;

import java.util.List;

/**
 * The activity for the Discuss tutorial.
 */
public class DiscussTutorialActivity extends TutorialActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "DiscussTutorialActivity";

    private ConversationView conversationView;

    // Store the Discuss action.
    private Discuss discuss;
    private String Human_input="default text";
    private boolean flag_rece = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationView = findViewById(R.id.conversationView);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
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
        final String textToSay = "I can discuss with humans: say \"Hello\" to start the discussion.";
        displayLine(textToSay, ConversationItemType.ROBOT_OUTPUT);

        Say say = SayBuilder.with(qiContext)
                .withText(textToSay)
                .build();



        say.run();


        // Create a topic.
        Topic topic1 = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                                  .withResource(R.raw.greetings) // Set the topic resource.
                                  .build(); // Build the topic.
//        Topic topic2 = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
//                .withResource(R.raw.greetings2) // Set the topic resource.
//                .build(); // Build the topic.
//
//        List<Topic> twolist = null;
//        twolist.add(topic1);
//        twolist.add(topic2);

        // Create a new discuss action.
        discuss = DiscussBuilder.with(qiContext) // Create the builder using the QiContext.
                                        .withTopic(topic1) // Add the topic.
                                        .build(); // Build the discuss action.
//        TopicStatus temp = discuss.topicStatus(topic1);
//        temp.toString()

        // Set an on started listener to the discuss action.
        discuss.setOnStartedListener(new Discuss.OnStartedListener() {
            @Override
            public void onStarted() {
                String message = "Discussion started.";
                Log.i(TAG, message);
                displayLine(message, ConversationItemType.INFO_LOG);
            }
        });

        discuss.setOnFocusedTopicChangedListener(new Discuss.OnFocusedTopicChangedListener() {
            @Override
            public void onFocusedTopicChanged(Topic topic) {
                Log.d(TAG,"Topic name " + topic.getName());
            }
        });

        discuss.setOnLatestInputUtteranceChangedListener(new Discuss.OnLatestInputUtteranceChangedListener() {
            @Override
            public void onLatestInputUtteranceChanged(Phrase input) {
                displayLine(input.getText(), ConversationItemType.HUMAN_INPUT);
                Human_input = input.getText();
                Log.d(TAG,"message received: "+ Human_input);
            }
        });



        discuss.setOnLatestOutputUtteranceChangedListener(new Discuss.OnLatestOutputUtteranceChangedListener() {
            @Override
            public void onLatestOutputUtteranceChanged(Phrase output) {
                displayLine(output.getText(), ConversationItemType.ROBOT_OUTPUT);
            }
        });

        // Run the discuss action asynchronously.
//        Future<String> discussFuture = discuss.async().run();

        String endReason =  discuss.run();

        if(endReason.equals("discussEnded")){
            Log.d(TAG,"come here");
            say = SayBuilder.with(qiContext)
                    .withText("what up there"+Human_input)
                    .build();
            say.run();

            endReason = discuss.run();
        }


//        // Add a consumer to the action execution.
//        discussFuture.thenConsume(new Consumer<Future<String>>() {
//            @Override
//            public void consume(Future<String> future) throws Throwable {
//                Log.e(TAG, future.toString());
//                if (future.hasError()) {
//                    String message = "Discussion finished with error.";
//                    Log.e(TAG, message, future.getError());
//                    displayLine(message, ConversationItemType.ERROR_LOG);
//                }
//            }
//        });

    }

    @Override
    public void onRobotFocusLost() {
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
}
