package com.telenav.predictivecards.ui;

import com.telenav.predictivecards.PredictiveCard;

/**
 * @author jpwang
 * @since 7/30/15
 */
public interface PredictiveCardActionListener {
    void onCardSwiped(PredictiveCard card);
    void onCardClick(PredictiveCard card);

    void onCardLongClick(PredictiveCard card);
}
