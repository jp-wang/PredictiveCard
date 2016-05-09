package com.telenav.predictivecards;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jpwang
 * @since 7/23/15
 */
public class TimeBucket implements Parcelable {

    enum TimeUnit {
        MONTHS("MONTHS"),
        DAYS("DAYS"),
        HOURS("HOURS"),
        MINUTES("MINUTES"),
        SECONDS("SECONDS");

        private final String value;

        TimeUnit(String value) {
            this.value = value;
        }

        public String toString() {
            return this.name();
        }

        public String getValue() {
            return this.value;
        }

        public static TimeUnit value(String value) {
            for (TimeUnit unit : TimeUnit.values()) {
                if (unit.getValue().equals(value)) {
                    return unit;
                }
            }
            return null;
        }
    }

    private double from;
    private double to;
    private TimeUnit timeUnit = TimeUnit.HOURS;

    public TimeBucket(double from, double to, TimeUnit timeUnit) {
        this.from = from;
        this.to = to;
        this.timeUnit = timeUnit;
    }

    public TimeBucket() {

    }

    protected TimeBucket(Parcel in) {
        this.timeUnit = TimeUnit.valueOf(in.readString());
        this.from = in.readDouble();
        this.to = in.readDouble();
    }

    public double getFrom() {
        return from;
    }

    public void setFrom(double from) {
        this.from = from;
    }

    public double getTo() {
        return to;
    }

    public void setTo(double to) {
        this.to = to;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        if (timeUnit != null) {
            this.timeUnit = timeUnit;
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("from=").append(from).append(" ").append("to=").append(to);
        if (timeUnit != null) {
            s.append(" ").append(timeUnit.name());
        }
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof TimeBucket) {
            TimeBucket compare = (TimeBucket) o;
            if (this.getTimeUnit() != null && compare.getTimeUnit() != null) {
                return this.getTimeUnit().equals(compare.getTimeUnit()) && this.getFrom() == compare.getFrom()
                        && this.getTo() == compare.getTo();
            }
        }
        return false;
    }

    public JSONObject toJsonPacket() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("timeUnit", this.timeUnit);
        jsonObject.put("from", this.from);
        jsonObject.put("to", this.to);
        return jsonObject;
    }

    public void fromJSonPacket(JSONObject jsonObject) throws JSONException {
        this.timeUnit = jsonObject.has("timeUnit") ? TimeBucket.TimeUnit.valueOf(jsonObject.getString("timeUnit")) : null;
        this.from = jsonObject.getDouble("from");
        this.to = jsonObject.getDouble("to");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.timeUnit.toString());
        dest.writeDouble(this.from);
        dest.writeDouble(this.to);
    }

    public static final Parcelable.Creator<TimeBucket> CREATOR = new Parcelable.Creator<TimeBucket>() {
        @Override
        public TimeBucket createFromParcel(Parcel source) {
            return new TimeBucket(source);
        }

        @Override
        public TimeBucket[] newArray(int size) {
            return new TimeBucket[size];
        }
    };
}
