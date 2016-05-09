package com.telenav.predictivecards;

import java.util.List;

/**
 * @author jpwang
 * @since 7/21/15
 */
public interface PredictiveCardListener {
    void notifyPredictiveCards(List<PredictiveCard> cards);
}
