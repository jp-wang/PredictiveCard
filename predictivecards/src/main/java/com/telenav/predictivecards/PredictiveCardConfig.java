package com.telenav.predictivecards;

import java.util.Properties;

/**
 * @author jpwang
 * @since 7/23/15
 */
public class PredictiveCardConfig extends Properties {
    public static final String KEY_PREDICTIVE_CARD_UPDATE_INTERVAL = "predictivecard.update.interval";
    public static final String KEY_PREDICTIVE_CARD_NUMBER = "predictivecard.num";
    public static final String KEY_PREDICTIVE_CARD_THRESHOLD = "predictivecard.threshold";
    public static final String KEY_PREDICTIVE_CARD_ORIGIN_RANGE = "predictivecard.origin.range";
    public static final String KEY_PREDICTIVE_CARD_DESTINATION_RANGE = "predictivecard.destination.range";
    public static final String KEY_PREDICTIVE_CARD_DESTINATION_MAX_ETA = "predictivecard.destination.max.eta";
    public static final String KEY_PREDICTIVE_CARD_USER_ID = "predictivecard.origin.userid";

    public PredictiveCardConfig() {
        init();
    }

    public PredictiveCardConfig(PredictiveCardConfig config) {
        super(config);
    }

    private void init() {
        setProperty(KEY_PREDICTIVE_CARD_UPDATE_INTERVAL, Integer.valueOf(30 * 1000).toString());
        setProperty(KEY_PREDICTIVE_CARD_NUMBER, Integer.valueOf(3).toString());
        setProperty(KEY_PREDICTIVE_CARD_THRESHOLD, Integer.valueOf(50).toString());
        setProperty(KEY_PREDICTIVE_CARD_ORIGIN_RANGE, Integer.valueOf(50).toString());
        setProperty(KEY_PREDICTIVE_CARD_DESTINATION_RANGE,Integer.valueOf(9656).toString());
        setProperty(KEY_PREDICTIVE_CARD_DESTINATION_MAX_ETA,Integer.valueOf(400).toString());
    }
}
