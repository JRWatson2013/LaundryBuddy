package cs4518.laundrybuddy;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class LaundromatActivity extends AppCompatActivity {

    private Integer[] mMachineIds;
    private int totalMachines;
    private int washersOpen;
    private int dryersOpen;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundromat);

        populateMachineList();

        GridView gridview = (GridView) findViewById(R.id.GridView);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(LaundromatActivity.this, "Clicked Item:" + position,
                        Toast.LENGTH_SHORT).show();
                /*if(machine at position in db status is open){
                    change status to in use;
                    }
                    else if(status is in use){
                        change status to ooo;
                        }
                    else if(status is ooo){
                        change status to open;
                        }
                 */
                populateMachineList();
                onCreate(savedInstanceState);
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


    public void populateMachineList(){
        mMachineIds = new Integer[totalMachines];
        for(int i = 0; i < totalMachines; i++){
            mMachineIds[i] = getDrawableInt(i);
        }
//      mMachineIds = new Integer[]{R.drawable.washer1open, R.drawable.washer2open, R.drawable.washer3inuse, R.drawable.washer4ooo};
    }

    public int getDrawableInt(int i){
        // get the correct drawable resource from position i in database
        //if(i == washer){
        if(true){
            if(true){
                //if(i == open){
                if(i == 1){
                    return R.drawable.washer1open;
                }
                if(i == 2){
                    return R.drawable.washer2open;
                }
                if(i == 3){
                    return R.drawable.washer3open;
                }
                if(i == 4){
                    return R.drawable.washer4open;
                }
            }
            //if(i == inUse){
            if(true){
                if(i == 1){
                    return R.drawable.washer1inuse;
                }
                if(i == 2){
                    return R.drawable.washer2inuse;
                }
                if(i == 3){
                    return R.drawable.washer3inuse;
                }
                if(i == 4){
                    return R.drawable.washer4inuse;
                }
            }
            //if(i == ooo){
            if(true){
                if(i == 1){
                    return R.drawable.washer1ooo;
                }
                if(i == 2){
                    return R.drawable.washer2ooo;
                }
                if(i == 3){
                    return R.drawable.washer3ooo;
                }
                if(i == 4){
                    return R.drawable.washer4ooo;
                }
            }
        }
        /*if(i == dryer){
            if(i == open){
                if(i == 1){
                    return R.drawable.dryer1open;
                }
                if(i == 2){
                    return R.drawable.dryer2open;
                }
                if(i == 3){
                    return R.drawable.dryer3open;
                }
                if(i == 4){
                    return R.drawable.dryer4open;
                }
            }
            if(i == inUse){
                if(i == 1){
                    return R.drawable.dryer1inUse;
                }
                if(i == 2){
                    return R.drawable.dryer2inUse;
                }
                if(i == 3){
                    return R.drawable.dryer3inUse;
                }
                if(i == 4){
                    return R.drawable.dryer4inUse;
                }
            }
            if(i == ooo){
                if(i == 1){
                    return R.drawable.dryer1ooo;
                }
                if(i == 2){
                    return R.drawable.dryer2ooo;
                }
                if(i == 3){
                    return R.drawable.dryer3ooo;
                }
                if(i == 4){
                    return R.drawable.dryer4ooo;
                }
            }
        }*/
        return R.drawable.washer1open;
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mMachineIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mMachineIds[position]);
            return imageView;
        }
    }

    public void launchEditor(){
        // launch utility/gui to edit and revise information
    }
}
