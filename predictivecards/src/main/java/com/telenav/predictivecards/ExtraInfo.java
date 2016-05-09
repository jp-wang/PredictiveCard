package com.telenav.predictivecards;

/**
 * /**
 * @author dmitri
 * @since 10/21/15
 */
public class ExtraInfo {
    private String summary;
    private int eta;
    private int trafficDelay;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getEta() {
        return eta;
    }

    public void setEta(int eta) {
        this.eta = eta;
    }

    public int getTrafficDelay() {
        return trafficDelay;
    }

    public void setTrafficDelay(int trafficDelay) {
        this.trafficDelay = trafficDelay;
    }


}
