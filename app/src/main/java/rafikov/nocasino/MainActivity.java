package rafikov.nocasino;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.motion.widget.MotionLayout;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import rafikov.nocasino.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mainBinding;

    private final BooleanSwitcher buttonPulse = new BooleanSwitcher();
    private Casino casino;
    private boolean isLoosed = false;

    private Drawable[] drawables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDrawables();
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        TextView scores = mainBinding.scorePoints;
        int scoresInt = Integer.parseInt(scores.getText().toString());
        casino = new Casino(scoresInt);
        prepareSpinButton();
        prepareChangeBetButtons();
    }

    private void initDrawables() {
        drawables = new Drawable[] {
                AppCompatResources.getDrawable(this, R.drawable.bar1),
                AppCompatResources.getDrawable(this, R.drawable.bell),
                AppCompatResources.getDrawable(this, R.drawable.cherries),
                AppCompatResources.getDrawable(this, R.drawable.clover),
                AppCompatResources.getDrawable(this, R.drawable.heart),
                AppCompatResources.getDrawable(this, R.drawable.horseshoe),
                AppCompatResources.getDrawable(this, R.drawable.lemon),
                AppCompatResources.getDrawable(this, R.drawable.lucky7_rainbow),
                AppCompatResources.getDrawable(this, R.drawable.melon),
                AppCompatResources.getDrawable(this, R.drawable.heart_rainbow)
        };
    }

    private void prepareChangeBetButtons() {
        Button downBetButton = mainBinding.downBetButton;
        Button upBetButton = mainBinding.upBetButton;
        Consumer<Integer> clickAction = (deltaCost) -> {
            int newCost = casino.setCost(casino.cost + deltaCost);
            TextView bet = mainBinding.betTextView;
            bet.setText(String.valueOf(newCost));
            checkScore();
        };

        downBetButton.setOnClickListener((v) -> {
            clickAction.accept(-10);
        });
        upBetButton.setOnClickListener((v) -> {
            clickAction.accept(10);
        });

        BiConsumer<Integer, View> longClickAction = (deltaCost, view) -> {
            new Thread(() -> {

                while (view.isPressed()) {
                    if (casino.getMoney() < casino.cost + deltaCost) { // up limiter
                        break;
                    }
                    boolean costChanged = changeCost(casino.cost + deltaCost);
                    if (!costChanged) { // down limiter
                        break;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (Exception ignored) {
                        runOnUiThread(() -> Toast.makeText(
                                getApplicationContext(),
                                "1",
                                Toast.LENGTH_SHORT));
                    }
                }
                checkScore();
            }).start();
        };

        downBetButton.setOnLongClickListener(v -> {
            longClickAction.accept(-10, v);
            return true;
        });
        upBetButton.setOnLongClickListener(v -> {
            longClickAction.accept(10, v);
            return true;
        });
    }

    private void prepareSpinButton() {
        ImageView slot1 = mainBinding.slot1;
        ImageView slot2 = mainBinding.slot2;
        ImageView slot3 = mainBinding.slot3;
        ImageButton spinButton = mainBinding.include.spinButton;

        spinButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                spinButton.setEnabled(false);
                new Thread(() -> {
                    // calculating results
                    int[] slots;
                    try {
                        slots = casino.doSpin();
                        addPoints(-casino.cost);
                    } catch (NoMoneyException ex) {
                        runOnUiThread(() -> Toast.makeText(
                                getApplicationContext(),
                                "You have not money",
                                Toast.LENGTH_SHORT));
                        return;
                    }
                    // animation config
                    int fps = 60;
                    double seconds = 0.5;
                    // do invisible
                    animate(fps, seconds, 1, (delta) -> {
                        slot1.setAlpha((float)(slot1.getAlpha() - delta));
                        slot2.setAlpha((float)(slot2.getAlpha() - delta));
                        slot3.setAlpha((float)(slot3.getAlpha() - delta));
                    });
                    // change images
                    runOnUiThread(() -> {
                        slot1.setImageDrawable(drawables[slots[0]]);
                        slot2.setImageDrawable(drawables[slots[1]]);
                        slot3.setImageDrawable(drawables[slots[2]]);
                    });
                    // do visible
                    animate(fps, seconds, 1, (delta) -> {
                        slot1.setAlpha((float)(slot1.getAlpha() + delta));
                        slot2.setAlpha((float)(slot2.getAlpha() + delta));
                        slot3.setAlpha((float)(slot3.getAlpha() + delta));
                    });
                    addPoints(casino.getLastWin());
                    runOnUiThread(() -> mainBinding.deltaPoints.setText(
                            getString(
                                    R.string.win_points_placeholder,
                                    String.valueOf(casino.getLastWin()))
                    ));
                    checkScore();
                }).start();
            }
        });
        doPulse(mainBinding.include.motionContainer, buttonPulse);
    }

    private void addPoints(int points) {
        TextView scores = mainBinding.scorePoints;
        int scoresInt = Integer.parseInt(scores.getText().toString());
        scoresInt += points;
        final int finalScoresInt = scoresInt;
        runOnUiThread(() -> scores.setText(String.valueOf(finalScoresInt)));
    }

    private void checkScore() {
        int scoresInt = casino.getMoney();
        ImageButton spinButton = mainBinding.include.spinButton;
        if (scoresInt == 0) {
            if (!isLoosed) {
                isLoosed = true;
                runOnUiThread(() -> {
                    mainBinding.loseText.setVisibility(View.VISIBLE);
                });
                doPulse(mainBinding.loseText);
                buttonPulse.setFalse();
            }
        } else if (casino.cost > casino.getMoney()) {
            boolean costChanged = changeCost(casino.getMoney());
            if (!costChanged) { // down limiter
                runOnUiThread(() -> spinButton.setEnabled(false));
            } else {
                runOnUiThread(() -> spinButton.setEnabled(true));
            }
        } else {
            runOnUiThread(() -> spinButton.setEnabled(true));
        }
    }

    private boolean changeCost(int needCost) {
        TextView bet = mainBinding.betTextView;
        int oldCost = casino.cost;
        int newCost = casino.setCost(needCost);
        if (oldCost == newCost) {
            return false;
        } else {
            runOnUiThread(() -> bet.setText(String.valueOf(newCost)));
            return true;
        }
    }

    /**
     * Анимация с помощью MotionLayout. Сложно, долго, неудобно, но позволяет делать большие
     * анимационные сцены, в теории
     */
    private void doPulse(MotionLayout layout, BooleanSwitcher switcher) {
        new Thread(() -> {
            switcher.setTrue();
            while (switcher.getValue()) {
                try {
                    layout.transitionToEnd();
                    Thread.sleep(layout.getTransitionTimeMs());
                    layout.transitionToStart();
                    Thread.sleep(layout.getTransitionTimeMs());
                } catch (Exception ignored) {
                    runOnUiThread(() -> Toast.makeText(
                            getApplicationContext(),
                            "2",
                            Toast.LENGTH_SHORT));
                }
            }
        }).start();
    }

    /**
     * Анимация с помощью AnimationUtils. Быстро, просто, проблемно настраивать большие сцены,
     * в теории
     */
    private void doPulse(View view) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.pulser);
        view.startAnimation(animation);
    }

    /**
     * Программная анимация. Возможно, не эффективно. Работает не со всеми параметрами.
     * Лучше не использовать
     */
    private void animate(int fps, double seconds, double dataMaxValue, Consumer<Double> work) {
        int frames = (int) (fps * seconds);
        double deltaData = dataMaxValue / frames;
        for (int i = 0; i < frames; i++) {
            work.accept(deltaData);
            try {
                Thread.sleep((long) (seconds / frames * 1000));
            } catch (Exception ex) {
                runOnUiThread(() -> Toast.makeText(
                        getApplicationContext(),
                        "3",
                        Toast.LENGTH_SHORT));
            }
        }
    }
}