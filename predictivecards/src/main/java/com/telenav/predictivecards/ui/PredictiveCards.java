package com.telenav.predictivecards.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.telenav.predictivecards.ExtraInfo;
import com.telenav.predictivecards.PredictiveCard;
import com.telenav.predictivecards.R;
import com.telenav.predictivecards.util.PredCardLogger;

import java.util.ArrayList;
import java.util.List;


/**
 * @author jpwang
 * @since 7/22/15
 */
public class PredictiveCards extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {
    private final static int ANIMATION_TIME = 500;
    public final static float TRAFFIC_THRESHOLD_YELLOW = 25 / 100f; //=20/100 trafficTime/totalTime
    public final static float TRAFFIC_THRESHOLD_RED = 40 / 100f; //=40/100 trafficTime/totalTime

    public enum Mode {
        Expand, Collapse
    }

    private List<PredictiveCard> cards = new ArrayList<>();
    private Mode mode = Mode.Collapse;
    private int predictiveCardHeight, predictiveCardMargin;
    private PredictiveCardActionListener cardActionListener;

    private int backgroundColor;
    private int textColorPrimary;
    private int textColorSecondary;

    public PredictiveCards(Context context) {
        this(context, null);
    }

    public PredictiveCards(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PredictiveCards(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PredictiveCards);
        backgroundColor = typedArray.getColor(R.styleable.PredictiveCards_predictiveCardBackground, getResources().getColor(R.color.predictiveCardDefaultBackgroundColor));
        textColorPrimary = typedArray.getColor(R.styleable.PredictiveCards_predictiveCardTextColorPrimary,
                getResources().getColor(R.color.predictiveCardDefaultPrimaryTextColor));
        textColorSecondary = typedArray.getColor(R.styleable.PredictiveCards_predictiveCardTextColorSecondary,
                getResources().getColor(R.color.predictiveCardDefaultSecondaryTextColor));
        typedArray.recycle();
    }

    public void setCardActionListener(PredictiveCardActionListener listener) {
        this.cardActionListener = listener;
    }

    private void init() {
        predictiveCardHeight = this.getContext().getResources().getDimensionPixelSize(R.dimen.predictiveCardHeight);
        predictiveCardMargin = this.getContext().getResources().getDimensionPixelSize(R.dimen.predictiveCardMargin);
    }

    public void setMode(Mode mode) {
        PredCardLogger.logD(PredictiveCards.class, "setMode = " + mode.name() + ", old mode = " + this.mode.name());
        if (mode != this.mode) {
            this.mode = mode;
            animateCardsByMode(this.mode, new DefaultAnimatorListener());
        }
    }

    public Mode getMode() {
        return this.mode;
    }

    private void animateCardsByMode(Mode mode, Animator.AnimatorListener listener) {
        int childCount = this.getChildCount();
        List<Animator> list = new ArrayList<>();
        switch (mode) {
            case Expand:
                for (int i = 0; i < childCount; i++) {
                    ObjectAnimator anim = ObjectAnimator.ofFloat(this.getChildAt(i),
                            "translationY", 0, -predictiveCardHeight * (childCount - i));
                    list.add(anim);
                }
                break;
            case Collapse:
                for (int i = 0; i < childCount; i++) {
                    ObjectAnimator anim = ObjectAnimator.ofFloat(this.getChildAt(i),
                            "translationY", 0, predictiveCardHeight * (childCount - i));
                    list.add(anim);
                }
                break;
        }

        if (list.size() > 0) {
            AnimatorSet set = new AnimatorSet();
            set.setDuration(ANIMATION_TIME);
            set.playTogether(list);
            if (listener != null) {
                set.addListener(listener);
            }
            set.start();
        } else {
            listener.onAnimationEnd(null);
        }
    }

    public void updateCards(List<PredictiveCard> cards) {
        PredCardLogger.logD(PredictiveCards.class, "Update cards");

        if (cardChanged(cards)) {
            animateCardsByUpdate(cards);
        } else {
            this.cards.clear();
            this.cards.addAll(cards);
            refresh();
        }
    }

    private boolean cardChanged(List<PredictiveCard> cards) {
        boolean isCardChanged = (this.cards.size() != cards.size());
        if (!isCardChanged) {
            for (PredictiveCard card : this.cards) {
                if (!cards.contains(card)) {
                    isCardChanged = true;
                    break;
                }
            }
        }
        if (!isCardChanged) {
            for (PredictiveCard card : cards) {
                if (!this.cards.contains(card)) {
                    isCardChanged = true;
                    break;
                }
            }
        }
        return isCardChanged;
    }

    private void animateCardsByUpdate(final List<PredictiveCard> newCards) {
        switch (mode) {
            case Expand:
                animateCardsByMode(Mode.Collapse, new DefaultAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        PredictiveCards.this.cards.clear();
                        PredictiveCards.this.cards.addAll(newCards);
                        PredictiveCards.this.removeAllViews();
                        reLayout(Mode.Collapse);
                        animateCardsByMode(Mode.Expand, new DefaultAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                refresh();
                            }
                        });
                    }
                });
                break;
            case Collapse:
                this.cards.clear();
                this.cards.addAll(newCards);
                refresh();
                break;
        }
    }

    public void refresh() {
        this.removeAllViews();
        if (cards.size() > 0) {
            reLayout(this.mode);
        }
    }

    private void reLayout(Mode mode) {
        PredCardLogger.logD(PredictiveCards.class, "relayout and mode = " + mode.name() + ", child size = " + this.cards.size());

        switch (mode) {
            case Expand:
                for (int i = 0; i < cards.size(); i++) {
                    LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    View cardView = LayoutInflater.from(this.getContext()).inflate(R.layout.predictive_card, null);
                    PredictiveCard card = cards.get(i);
                    updateCardUiInfo(cardView, i);

                    cardView.setOnClickListener(this);
                    cardView.setOnLongClickListener(this);
                    cardView.setTag(card);
                    cardView.setOnTouchListener(new SwipeDismissTouchListener(
                            cardView, cards.get(i), new SwipeDismissTouchListener.OnDismissCallback() {

                        @Override
                        public void onDismiss(View view, Object token) {
                            PredictiveCard c = (PredictiveCard) token;
                            cards.remove(c);
                            // call onCardSwiped() listener
                            PredCardLogger.logD(PredictiveCards.class, "Swiping away the predictive card = " + c);
                            if (cardActionListener != null) {
                                cardActionListener.onCardSwiped(c);
                            }
                            refresh();
                        }
                    }));

                    int bottomPx = (predictiveCardHeight + predictiveCardMargin) * (cards.size() - i - 1) + predictiveCardMargin;

                    lp.setMargins(predictiveCardMargin, predictiveCardMargin, predictiveCardMargin, bottomPx);
                    lp.addRule(ALIGN_PARENT_BOTTOM);

                    cardView.setLayoutParams(lp);

                    StringBuilder builder = new StringBuilder();
                    builder.append("{leftMargin=").append(predictiveCardMargin).append(", topMargin=")
                            .append(predictiveCardMargin).append(", rightMargin=")
                            .append(predictiveCardMargin).append(", bottomMargin=")
                            .append(bottomPx).append("}");
                    PredCardLogger.logD(PredictiveCards.class, "The layout params of child(" + i + ") = " + builder.toString());

                    this.addView(cardView);
                }
                break;
            case Collapse:
                boolean isShownRedCard = false;
                for (int i = 0; i < cards.size(); i++) {
                    LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    View cardView = LayoutInflater.from(this.getContext()).inflate(R.layout.predictive_card, null);
                    PredictiveCard card = cards.get(i);
                    updateCardUiInfo(cardView, i);

                    this.setOnClickListener(this);
                    this.setOnTouchListener(new SwipeDismissTouchListener(
                            cardView, null, new SwipeDismissTouchListener.OnDismissCallback() {

                        @Override
                        public void onDismiss(View view, Object token) {
                            cards.clear();
                            refresh();
                        }
                    }));

                    int bottomPx = -predictiveCardHeight + predictiveCardMargin * (cards.size() - i);

                    ExtraInfo route = card.getExtraInfo();
                    if (!isShownRedCard && route != null && route.getTrafficDelay() > 0 && route.getTrafficDelay() >= TRAFFIC_THRESHOLD_RED * route.getEta()) {
                        isShownRedCard = true;
                        bottomPx = predictiveCardMargin;
                    }

                    lp.setMargins(predictiveCardMargin, predictiveCardMargin, predictiveCardMargin, bottomPx);
                    lp.addRule(ALIGN_PARENT_BOTTOM);

                    cardView.setLayoutParams(lp);

                    this.addView(cardView);
                }
                break;
        }
    }

    private void updateCardUiInfo(View cardView, int index) {
        PredictiveCard card = cards.get(index);
        cardView.setBackgroundColor(backgroundColor);
        TextView cardName = ((TextView) cardView.findViewById(R.id.cardName));
        cardName.setTextColor(textColorPrimary);
        TextView etaView = ((TextView) cardView.findViewById(R.id.cardEta));
        etaView.setTextColor(this.getResources().getColor(R.color.predictiveCardLightTrafficColor));
        TextView addressView = ((TextView) cardView.findViewById(R.id.cardAddress));
        addressView.setTextColor(textColorSecondary);

        cardName.setText(card.getLabel());
        cardName.setSelected(true);
        ExtraInfo info = card.getExtraInfo();
        if (info != null) {
            int trafficDelay = card.getExtraInfo().getTrafficDelay();
            if (info.getSummary() != null && !info.getSummary().isEmpty()) {
                addressView.setText("via " + info.getSummary());
            }
            if (info.getEta() > 0) {
                etaView.setText(getFormattedString((info.getEta() + trafficDelay) * 1000));
            }
            if (trafficDelay >= TRAFFIC_THRESHOLD_RED * info.getEta()) {
                etaView.setTextColor(this.getResources().getColor(R.color.predictiveCardHeavyTrafficColor));
            } else if (trafficDelay >= TRAFFIC_THRESHOLD_YELLOW * info.getEta()) {
                etaView.setTextColor(this.getResources().getColor(R.color.predictiveCardMediumTrafficColor));
            }
        }
        cardView.findViewById(R.id.cardAlert).setVisibility(GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            PredictiveCard card = (PredictiveCard) v.getTag();
            PredCardLogger.logD(PredictiveCards.class, "Clicking the predictive card = " + card);
            if (cardActionListener != null) {
                cardActionListener.onCardClick(card);
            }
        } else if (this.mode == Mode.Collapse) {
            setMode(Mode.Expand);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getTag() != null) {
            PredictiveCard card = (PredictiveCard) v.getTag();
            PredCardLogger.logD(PredictiveCards.class, "Long Clicking the predictive card = " + card);
            if (cardActionListener != null) {
                cardActionListener.onCardLongClick(card);
            }
            return true;
        }
        return false;
    }

    private String getFormattedString(long timeInMilliSeconds) {
        int timeUnit = 60;
        long timeInSeconds = timeInMilliSeconds / 1000;
        int hours = (int) (timeInSeconds / timeUnit / timeUnit);
        int minutes = (int) (timeInSeconds / timeUnit % timeUnit);
        int seconds = (int) timeInSeconds % timeUnit;

        String unitHour = "hr";
        String unitMinute = "min";
        String unitHourAbbreviation = "h";
        String unitMinuteAbbreviation = "m";
        String timeStr = "";
        if (hours > 0 && minutes > 0) {
            timeStr += hours + unitHourAbbreviation + " " + minutes + unitMinuteAbbreviation; // format is '{hour}h {minute}m'
        } else if (hours > 0) {
            timeStr += hours + " " + unitHour;  // format is '{hour} hr'
        } else if (minutes > 0) {
            timeStr += minutes + " " + unitMinute;  // format is '{minute} min'
        } else if (seconds > 0) {
            timeStr += "1 " + unitMinute; // format is '1 min'
        }

        return timeStr;
    }

    class DefaultAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            refresh();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

}
