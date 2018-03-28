package com.softbankrobotics.qisdktutorials.ui.categories;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import com.softbankrobotics.qisdktutorials.R;
import com.softbankrobotics.qisdktutorials.model.data.Tutorial;
import com.softbankrobotics.qisdktutorials.model.data.TutorialCategory;
import com.softbankrobotics.qisdktutorials.model.data.TutorialLevel;
import com.softbankrobotics.qisdktutorials.ui.bilateralswitch.BilateralSwitch;
import com.softbankrobotics.qisdktutorials.ui.bilateralswitch.OnCheckedChangeListener;

import java.util.List;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

/**
 * The activity showing the tutorial categories.
 */
public class CategoriesActivity extends AppCompatActivity implements CategoriesContract.View, OnTutorialClickedListener {

    private CategoriesContract.Presenter presenter;
    private CategoriesContract.Robot robot;
    private CategoriesContract.Router router;

    private TutorialAdapter tutorialAdapter;
    private RadioButton talkButton;
    private RadioButton moveButton;
    private RadioButton smartButton;
    private BilateralSwitch levelSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        setupButtons();
        setupRecyclerView();
        setupSwitch();

        presenter = new CategoriesPresenter();
        robot = new CategoriesRobot(presenter);
        router = new CategoriesRouter();

        presenter.bind(this);
        robot.register(this);

        presenter.loadTutorials(TutorialCategory.TALK);
        Log.d("TonyCategoriesActivity","debug1");
    }

    @Override
    protected void onResume() {
        Log.d("TonyCategoriesActivity","onResume");
        super.onResume();
        tutorialAdapter.unselectTutorials();
        tutorialAdapter.setTutorialsEnabled(true);
    }

    @Override
    protected void onDestroy() {
        robot.unregister(this);
        presenter.unbind();
        super.onDestroy();
    }

    @Override
    public void showTutorials(final List<Tutorial> tutorials) {
        Log.d("TonyCategoriesActivity","showTutorials");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tutorialAdapter.updateTutorials(tutorials);
            }
        });
    }

    @Override
    public void selectTutorial(final Tutorial tutorial) {
        Log.d("TonyCategoriesActivity","selectTutorial");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tutorialAdapter.selectTutorial(tutorial);
                tutorialAdapter.setTutorialsEnabled(false);
            }
        });
    }

    @Override
    public void goToTutorial(final Tutorial tutorial) {
        Log.d("TonyCategoriesActivity","goToTutorial");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                router.goToTutorial(tutorial, CategoriesActivity.this);
            }
        });
    }

    @Override
    public void selectCategory(final TutorialCategory category) {
        Log.d("TonyCategoriesActivity","selectCategory");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (category) {
                    case TALK:
                        talkButton.setChecked(true);
                        break;
                    case MOVE:
                        moveButton.setChecked(true);
                        break;
                    case SMART:
                        smartButton.setChecked(true);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown tutorial category: " + category);
                }
            }
        });
    }

    @Override
    public void selectLevel(final TutorialLevel level) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (level) {
                    case BASICS:
                        levelSwitch.setChecked(false);
                        break;
                    case ADVANCED:
                        levelSwitch.setChecked(true);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown tutorial level: " + level);
                }
            }
        });
    }

    @Override
    public void onTutorialClicked(Tutorial tutorial) {
        Log.d("TonyCategoriesActivity","onTutorialClicked");
        tutorialAdapter.selectTutorial(tutorial);
        tutorialAdapter.setTutorialsEnabled(false);
        robot.stopDiscussion(tutorial);
    }

    /**
     * Configure the buttons.
     */
    private void setupButtons() {
        Log.d("TonyCategoriesActivity","setupButtons");
        talkButton = findViewById(R.id.talk_button);
        moveButton = findViewById(R.id.move_button);
        smartButton = findViewById(R.id.smart_button);

        talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.loadTutorials(TutorialCategory.TALK);
                robot.selectTopic(TutorialCategory.TALK);
            }
        });

        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.loadTutorials(TutorialCategory.MOVE);
                robot.selectTopic(TutorialCategory.MOVE);
            }
        });

        smartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.loadTutorials(TutorialCategory.SMART);
                robot.selectTopic(TutorialCategory.SMART);
            }
        });
    }

    /**
     * Configure the recycler view.
     */
    private void setupRecyclerView() {
        Log.d("TonyCategoriesActivity","setupRecyclerView");
        tutorialAdapter = new TutorialAdapter(this);
        RecyclerView recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setAdapter(tutorialAdapter);

        Drawable drawable = getDrawable(R.drawable.empty_divider);
        if (drawable != null) {
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            dividerItemDecoration.setDrawable(drawable);
            recyclerview.addItemDecoration(dividerItemDecoration);
        }
    }

    /**
     * Configure the level switch.
     */
    private void setupSwitch() {
        levelSwitch = findViewById(R.id.level_switch);

        levelSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isChecked) {
                if (isChecked) {
                    presenter.loadTutorials(TutorialLevel.ADVANCED);
                    robot.selectLevel(TutorialLevel.ADVANCED);
                } else {
                    presenter.loadTutorials(TutorialLevel.BASICS);
                    robot.selectLevel(TutorialLevel.BASICS);
                }
            }
        });
    }
}
