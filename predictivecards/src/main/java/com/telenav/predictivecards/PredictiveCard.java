package com.telenav.predictivecards;

import android.os.Parcel;
import android.os.Parcelable;

import com.telenav.api.common.v1.LatLon;
import com.telenav.api.data.user.insight.v1.PersonalizedRoute;
import com.telenav.predictivecards.util.GeoUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jpwang
 * @since 7/21/15
 */
public class PredictiveCard implements Parcelable {

    public enum DayOfWeek {
        Sunday(1), Monday(2), Tuesday(3), Wednesday(4), Thursday(5), Friday(6), Saturday(7);

        DayOfWeek(int day) {
            this.dayOfWeek = day;
        }

        public int getValue() {
            return this.dayOfWeek;
        }

        private int dayOfWeek;
    }

    private String label;
    private ExtraInfo extraInfo;
    private LatLon origin;
    private LatLon dest;
    private double predictiveRating;
    private DayOfWeek dayOfWeek;
    private TimeBucket timeBucket;
    private List<PersonalizedRoute> personalizedRoutes;
    private List<Long> preferEdges;
    public static final int NUMBER_OF_ETA = 5;
    private List<Integer> lastSeveralETAs = new ArrayList<>(NUMBER_OF_ETA);
    int etaIndex = 0;
    private float etaPredictiveConfidence = 0f;

    private double probability;

    public PredictiveCard() {

    }

    public void addETA(int eta){
        if(etaIndex >= NUMBER_OF_ETA){
            etaIndex = 0;
        }
        if(lastSeveralETAs.size() < etaIndex + 1){
            lastSeveralETAs.add(etaIndex,eta);
        }else {
            lastSeveralETAs.set(etaIndex, eta);
        }
        etaIndex++;
    }

    public float getEtaPredictiveConfidence() {
        return etaPredictiveConfidence;
    }

    public void setEtaPredictiveConfidence(float etaPredictiveConfidence) {
        if(this.etaPredictiveConfidence >= 10.0f || this.etaPredictiveConfidence <= -10.0f){
            return;
        }
        this.etaPredictiveConfidence = etaPredictiveConfidence;
    }

    public List<Integer> getLastSeveralETAs() {
        return lastSeveralETAs;
    }

    public int getEtaIndex() {
        return etaIndex;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ExtraInfo getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(ExtraInfo extraInfo) {
        this.extraInfo = extraInfo;
    }

    public List<Long> getPreferEdges() {
        return preferEdges;
    }

    public void setPreferEdges(List<Long> preferEdges) {
        this.preferEdges = preferEdges;
    }

    public LatLon getOrigin() {
        return origin;
    }

    public void setOrigin(LatLon origin) {
        this.origin = origin;
    }

    public LatLon getDest() {
        return dest;
    }

    public void setDest(LatLon dest) {
        this.dest = dest;
    }

    public double getPredictiveRating() {
        return predictiveRating;
    }

    public void setPredictiveRating(double predictiveRating) {
        this.predictiveRating = predictiveRating;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public TimeBucket getTimeBucket() {
        return timeBucket;
    }

    public void setTimeBucket(TimeBucket timeBucket) {
        this.timeBucket = timeBucket;
    }

    public List<PersonalizedRoute> getPersonalizedRoutes() {
        return personalizedRoutes;
    }

    public void setPersonalizedRoutes(List<PersonalizedRoute> personalizedRoutes) {
        this.personalizedRoutes = personalizedRoutes;
    }

    public void addPersonalizedRoute(PersonalizedRoute personalizedRoute) {
        if (personalizedRoute == null) {
            return;
        }
        if (this.personalizedRoutes == null) {
            this.personalizedRoutes = new ArrayList<>();
        }
        this.personalizedRoutes.add(personalizedRoute);
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof PredictiveCard) {
            PredictiveCard compare = (PredictiveCard) o;
            if (compare.getOrigin() != null && this.getOrigin() != null
                    && compare.getDest() != null && this.getDest() != null
                    && compare.getDayOfWeek() != null && this.getDayOfWeek() != null
                    && compare.getTimeBucket() != null && this.getTimeBucket() != null) {
                return GeoUtility.calculateDistance(compare.getOrigin().getLat(), compare.getOrigin().getLon(),
                        this.getOrigin().getLat(), this.getOrigin().getLon()) <= 50
                        &&
                        GeoUtility.calculateDistance(compare.getDest().getLat(), compare.getDest().getLon(),
                                this.getDest().getLat(), this.getDest().getLon()) <= 50
                        &&
                        compare.getDayOfWeek().equals(this.getDayOfWeek())
                        &&
                        compare.getTimeBucket().equals(this.getTimeBucket());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Origin=").append(origin)
                .append(" ").append("Dest=").append(dest)
                .append(" ").append("PredictiveRating=").append(predictiveRating);
        if (label != null) {
            s.append(" ").append("label=").append(label);
        }
        if (preferEdges != null) {
            s.append(" ").append("preferEdgesSize=").append(preferEdges.size());
        }
        if (dayOfWeek != null) {
            s.append(" ").append("DayOfWeek=").append(dayOfWeek.name());
        }
        if (timeBucket != null) {
            s.append(" ").append("TimeBucket=").append(timeBucket);
        }
        if (extraInfo != null) {
            s.append("summary=").append(extraInfo.getSummary())
                    .append(" ").append("eta=").append(extraInfo.getEta())
                    .append(" ").append("trafficDelay=").append(extraInfo.getTrafficDelay());
        }
        s.append(" etaPredictiveConfidence=").append(etaPredictiveConfidence);
        return s.toString();
    }

    public JSONObject toJsonPacket() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("label", this.label);
        jsonObject.put("origin", this.origin.toJsonString());
        jsonObject.put("dest", this.dest.toJsonString());
        jsonObject.put("summary", this.extraInfo.getSummary());
        jsonObject.put("eta", this.extraInfo.getEta());
        jsonObject.put("trafficDelay", this.extraInfo.getTrafficDelay());
        jsonObject.put("dayOfWeek", this.dayOfWeek.toString());
        jsonObject.put("timeBucket", this.timeBucket.toJsonPacket());

        return jsonObject;
    }

    public void fromJSonPacket(JSONObject jsonObject) throws JSONException {
        this.label = jsonObject.has("label") ? jsonObject.getString("label") : null;
        if (jsonObject.has("origin")) {
            this.origin = LatLon.buildFromJson(jsonObject.getString("origin"));
        }
        if (jsonObject.has("dest")) {
            this.dest = new LatLon();
            this.dest = LatLon.buildFromJson(jsonObject.getString("dest"));
        }
        this.extraInfo = new ExtraInfo();
        this.extraInfo.setSummary(jsonObject.has("summary") ? jsonObject.getString("summary") : null);
        this.extraInfo.setEta(jsonObject.has("eta") ? jsonObject.getInt("eta") : -1);
        this.extraInfo.setTrafficDelay(jsonObject.has("trafficDelay") ? jsonObject.getInt("trafficDelay") : -1);
        this.dayOfWeek = jsonObject.has("dayOfWeek") ? DayOfWeek.valueOf(jsonObject.getString("dayOfWeek")) : null;

        if (jsonObject.has("timeBucket")) {
            this.timeBucket = new TimeBucket();
            this.timeBucket.fromJSonPacket(jsonObject.getJSONObject("timeBucket"));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.label);
        dest.writeString(this.origin.toJsonString());
        dest.writeString(this.dest.toJsonString());
        dest.writeString(this.extraInfo.getSummary());
        dest.writeInt(this.extraInfo.getEta());
        dest.writeInt(this.extraInfo.getTrafficDelay());
        dest.writeString(this.dayOfWeek.toString());
        dest.writeParcelable(this.timeBucket, flags);
    }

    protected PredictiveCard(Parcel in) {
        this.label = in.readString();
        this.origin = in.readParcelable(LatLon.class.getClassLoader());
        this.dest = in.readParcelable(LatLon.class.getClassLoader());
        this.extraInfo = new ExtraInfo();
        this.extraInfo.setSummary(in.readString());
        this.extraInfo.setEta(in.readInt());
        this.extraInfo.setTrafficDelay(in.readInt());
        this.dayOfWeek = DayOfWeek.valueOf(in.readString());
        this.timeBucket = in.readParcelable(TimeBucket.class.getClassLoader());
    }

    public final static Parcelable.Creator<PredictiveCard> CREATOR = new Parcelable.Creator<PredictiveCard>() {
        @Override
        public PredictiveCard createFromParcel(Parcel source) {
            return new PredictiveCard(source);
        }

        @Override
        public PredictiveCard[] newArray(int size) {
            return new PredictiveCard[size];
        }
    };
}
