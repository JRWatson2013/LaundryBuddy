package cs4518.laundrybuddy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LaundromatActivity extends FragmentActivity {

    private LaundryLocation mLocation;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundromat);

        Intent intent = getIntent();
        mLocation = intent.getParcelableExtra("location");
        TextView name = (TextView) findViewById(R.id.laundromat_name);
        name.setText(mLocation.getName());
        TextView address = (TextView) findViewById(R.id.laundromat_address);
        address.setText(mLocation.getPlace());

        TextView nWasher = (TextView) findViewById(R.id.washerCountTextView);
        nWasher.setText("~" + ((Integer)(mLocation.estimateAvailableWashers())).toString());
        TextView nDryer = (TextView) findViewById(R.id.dryerCountTextView);
        nDryer.setText("~" + ((Integer)(mLocation.estimateAvailableDryers())).toString());

        final GridView gridview = (GridView) findViewById(R.id.GridView);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                LaundryMachine clickedMachine = mLocation.machineList.get(position);
                if(clickedMachine.getState().equals("free")) {
                    clickedMachine.setState("inUse");
                    mLocation.machineList.set(position, clickedMachine);
                    mLocation.postMachineUpdate(position,"inUse",LaundryMapFragment.queue);
                }
                else if (clickedMachine.getState().equals("inUse")) {
                    clickedMachine.setState("free");
                    mLocation.machineList.set(position, clickedMachine);
                    mLocation.postMachineUpdate(position,"free",LaundryMapFragment.queue);
                }

                // Update gridview with changes
                gridview.setAdapter(new ImageAdapter(getBaseContext()));
//                Toast.makeText(LaundromatActivity.this, "Clicked Item:" + position,
//                        Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton closeButton = (FloatingActionButton) findViewById(R.id.close_action_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra("location",mLocation);
                setResult(RESULT_OK,data);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.laundromat_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.editLaundromat:
                launchEditor();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mLocation.machineList.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LaundryMachineView machineView;
            LaundryMachine machine = mLocation.machineList.get(position);
            if (convertView == null) {
                machineView = new LaundryMachineView(mContext,machine.getMachNum(),machine.getState(),machine.getType());
                machineView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
                machineView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                machineView.setPadding(0,0,0,25);
            } else {
                machineView = (LaundryMachineView) convertView;
            }

            machineView.setMinimumHeight(400);
            return machineView;
        }
    }

    public void launchEditor(){
        // launch utility/gui to edit and revise information
    }
}
