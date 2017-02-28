package cs4518.laundrybuddy;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

public class LaundryMachineView extends ImageView {

    Integer mMachNum;
    String mState;
    String mType;
    private Paint mTextPaint;
    private int textXPos;
    private int textYPos;

    public LaundryMachineView(Context c, Integer machNum, String state, String type) {
        super(c);

        mMachNum = machNum;
        mState = state;
        mType = type;

        init();
    }

    private void initBitmap(){
        if(mType.equals("washer")) {
            if (mState.equals("free"))
                this.setImageResource(R.drawable.good_washer);
            else if (mState.equals("inUse"))
                this.setImageResource(R.drawable.broken_washer);
        }
        else if(mType.equals("dryer")){
            if (mState.equals("free"))
                this.setImageResource(R.drawable.good_dryer);
            else if (mState.equals("inUse"))
                this.setImageResource(R.drawable.broken_dryer);
        }
    }

    public void setState(String newState){
        mState = newState;
        initBitmap();
        invalidate();
        requestLayout();
    }

    private void init(){
        initBitmap();
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(50.0f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        TypedValue a = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            this.setBackgroundColor(a.data);
        } else {
            this.setBackgroundColor(Color.WHITE);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        textXPos = (canvas.getWidth() / 2);
        textYPos = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2))  + 5 ;
        canvas.drawText(mMachNum.toString(),textXPos,textYPos,mTextPaint);
    }
}
