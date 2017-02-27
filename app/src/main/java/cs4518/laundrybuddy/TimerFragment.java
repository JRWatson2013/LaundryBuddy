package cs4518.laundrybuddy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.os.CountDownTimer;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Toast;
import android.view.animation.AnimationUtils;


public class TimerFragment extends Fragment implements OnClickListener {

    private Button buttonStartTime, buttonStopTime;
    private TextView textViewShowTime;
    private CountDownTimer countdownTimer;
    private long totalTimeCountInMilliseconds;
    private long timeBlinkInMilliseconds;
    private boolean blink;
    private NumberPicker numberPicker;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_timer, container, false);
        numberPicker = (NumberPicker) v.findViewById(R.id.time_picker);
        numberPicker.setMaxValue(180);
        numberPicker.setMinValue(1);
        numberPicker.setWrapSelectorWheel(true);


        buttonStartTime = (Button) v.findViewById(R.id.button_timer_start);
        buttonStopTime = (Button) v.findViewById(R.id.button_timer_stop);
        textViewShowTime = (TextView) v.findViewById(R.id.textView_time_remaining);

        buttonStartTime.setOnClickListener(this);
        buttonStopTime.setOnClickListener(this);
        return v;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_timer_start) {
            setTimer();
            buttonStopTime.setVisibility(View.VISIBLE);
            buttonStartTime.setVisibility(View.GONE);
            startTimer();
        } else if (v.getId() == R.id.button_timer_stop) {
            countdownTimer.cancel();
            buttonStartTime.setVisibility(View.VISIBLE);
            buttonStopTime.setVisibility(View.GONE);
        }

    }

    private void setTimer() {
        int time = 0;
        if (numberPicker.getValue() > 0) {
            time = numberPicker.getValue();
        } else {
            Toast.makeText(getActivity(), "Please Select Minutes.", Toast.LENGTH_LONG).show();
        }

        totalTimeCountInMilliseconds = 60 * time * 1000;
        timeBlinkInMilliseconds = 30 * 1000;
    }

    private void startTimer() {
        countdownTimer = new CountDownTimer(totalTimeCountInMilliseconds, 500) {

            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;

                if (millisUntilFinished < timeBlinkInMilliseconds) {
                    Animation startBlink = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.blinking_animation);
                    textViewShowTime.startAnimation(startBlink);
                    if (blink) {
                        textViewShowTime.setVisibility(View.VISIBLE);
                    } else {
                        textViewShowTime.setVisibility(View.INVISIBLE);
                    }
                    blink = !blink;
                }

                textViewShowTime.setText(String.format("%02d", seconds / 60)
                        + ":" + String.format("%02d", seconds % 60));
            }



            @Override
            public void onFinish() {
                Toast.makeText(getActivity(), "Laundry Time up!", Toast.LENGTH_LONG).show();
                textViewShowTime.setText("Time Up!");
                textViewShowTime.setVisibility(View.VISIBLE);
                buttonStartTime.setVisibility(View.VISIBLE);
                buttonStopTime.setVisibility(View.GONE);
            }
        }.start();
    }
}
