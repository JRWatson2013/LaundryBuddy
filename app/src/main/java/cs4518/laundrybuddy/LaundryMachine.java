package cs4518.laundrybuddy;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class LaundryMachine implements Parcelable {
    private int mMachNum;
    private String mState;
    private String mType;

    public LaundryMachine(int machNum, String state, String type){
        mMachNum = machNum;
        mState = state;
        mType = type;
    }

    public int getMachNum() {
        return mMachNum;
    }

    public void setMachNum(int machNum) {
        this.mMachNum = machNum;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        this.mState = state;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public static final Parcelable.Creator<LaundryMachine> CREATOR = new Parcelable.Creator<LaundryMachine>() {
        public LaundryMachine createFromParcel(Parcel in) {
            return new LaundryMachine(in);
        }

        public LaundryMachine[] newArray(int size) {
            return new LaundryMachine[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mMachNum);
        parcel.writeString(mState);
        parcel.writeString(mType);
    }

    public LaundryMachine(Parcel in){
        mMachNum = in.readInt();
        mState = in.readString();
        mType = in.readString();
    }
}
