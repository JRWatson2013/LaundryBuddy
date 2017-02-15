package cs4518.laundrybuddy;

import android.app.Activity;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.NumberPicker;

/**
 * Created by carolyn on 2/15/17.
 */

public class NumberPickerActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.time_picker);
        numberPicker.setMaxValue(180);
        numberPicker.setMinValue(0);
        numberPicker.setWrapSelectorWheel(true);

    }
}
