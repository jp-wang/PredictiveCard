package com.telenav.predictivecards;

import com.telenav.api.common.v1.LatLon;
import com.telenav.api.data.user.insight.v1.PersonalizedRoute;
import com.telenav.api.data.user.insight.v1.PredictiveDestination;
import com.telenav.api.data.user.insight.v1.TraversedRoutes;
import com.telenav.dataservice.DataService;
import com.telenav.predictivecards.util.GeoUtility;
import com.telenav.predictivecards.util.PredCardLogger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author jpwang
 * @since 7/21/15
 * <p/>
 * http://spaces.telenav.com:8080/display/auto/ARP+-+Predictive+cards
 */
public final class PredictiveCardManager implements Runnable {
    private List<PredictiveCardListener> listeners = new ArrayList<>();

    private boolean isRunning = false;
    private boolean isInitialised = false;

    private LatLon lastLatLon;
    private LatLon currentLatLon;

    private List<PredictiveCard> predictiveCards = new CopyOnWriteArrayList<>();
    private List<PredictiveCard> allPredictiveCards = new ArrayList<>();
    private Map<Long, List<PredictiveCard>> allPredictiveCardsMap = new HashMap<>();

    private long currentEdgeId = -1, lastEdgeId = -1;

    private PredictiveCardConfig config;
    private int cardsNum;
    private int LOOP_INTERVAL;

    private PredictiveCardDecorator decorator;

    private DataServiceDelegate dataServiceDelegate;

    private final static Object LOOP_MUTEX = new Object();

    private static class PredictiveCardManagerHolder {
        private final static PredictiveCardManager INSTANCE = new PredictiveCardManager();
    }

    private PredictiveCardManager() {
    }

    public static PredictiveCardManager getInstance() {
        return PredictiveCardManagerHolder.INSTANCE;
    }

    public synchronized void addListener(PredictiveCardListener listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
            listener.notifyPredictiveCards(this.predictiveCards);
        }
    }

    public synchronized void removeListener(PredictiveCardListener listener) {
        if (listeners.contains(listener)) {
            this.listeners.remove(listener);
        }
    }

    public synchronized void removeAllListeners() {
        this.listeners.clear();
    }

    public synchronized void init(PredictiveCardConfig cardConfig, DataService dataService) {
        if (isInitialised) {
            return;
        }
        this.isInitialised = true;
        this.config = cardConfig;
        this.cardsNum = Integer.parseInt(this.config.getProperty(PredictiveCardConfig.KEY_PREDICTIVE_CARD_NUMBER));
        this.LOOP_INTERVAL = Integer.parseInt(this.config.getProperty(PredictiveCardConfig.KEY_PREDICTIVE_CARD_UPDATE_INTERVAL));
        this.dataServiceDelegate = new DataServiceDelegate(this.config, dataService);
        synchronized (LOOP_MUTEX) {
            LOOP_MUTEX.notifyAll();
        }
    }

    public void setDecorator(PredictiveCardDecorator decorator) {
        this.decorator = decorator;
    }

    public void start() {
        PredCardLogger.logI(PredictiveCardManager.class, "Start predictiveCardManager");
        synchronized (LOOP_MUTEX) {
            if (!isRunning) {
                isRunning = true;
                new Thread(this, "PredCardMgr").start();
            }
        }
    }

    public void stop() {
        PredCardLogger.logI(PredictiveCardManager.class, "Stop predictiveCardManager");
        synchronized (LOOP_MUTEX) {
            isRunning = false;
            LOOP_MUTEX.notifyAll();
        }
    }

    public List<PredictiveCard> getPredictiveCards() {
        return predictiveCards;
    }

    @Override
    public void run() {
        while (isRunning) {
            synchronized (LOOP_MUTEX) {
                if (!isRunning) {
                    return;
                }
                if (!isInitialised) {
                    try {
                        LOOP_MUTEX.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (isInitialised) {
                    buildPredictiveCards();
                }

                try {
                    LOOP_MUTEX.wait(LOOP_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateCondition(long edgeId, LatLon latLon) {
        PredCardLogger.logI(PredictiveCardManager.class, "Update edgeId = " + edgeId + ", and latlon = " + latLon);
        this.currentEdgeId = edgeId;
        this.currentLatLon = latLon;
    }

    private boolean checkDayTimeBasedValidity(PredictiveCard card){
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        // Per PM's new requirement, the start point should not be the necessary condition to filter out things.
        // E.g., if user is driving along high way 101 but running out of gas, he may drive out to fill in gas and back again.
        // In this special case, the user's current start point is becoming the gas station which is not our expectation to filter out the probable destinations.
        if (/**!isStartFromOrigin(card.getOrigin()) ||*/card.getDayOfWeek().getValue() != dayOfWeek
                || (card.getTimeBucket().getTimeUnit() == TimeBucket.TimeUnit.HOURS && (hourOfDay > card.getTimeBucket().getTo() || hourOfDay < card.getTimeBucket().getFrom()))) {
            return false;
        }
        return true;

    }

    private boolean buildETABasedConfidencePredictiveCard(PredictiveCard card){
        if(card.getLastSeveralETAs().size() >= PredictiveCard.NUMBER_OF_ETA) { // we recorded last several ETA and check if that ETA is keep reducing, which provides us another idea that where user is headed
            // towards that direction
            int lastPosition = card.getEtaIndex() - 1;
            int firstPosition = lastPosition + 1;
            if (firstPosition >= PredictiveCard.NUMBER_OF_ETA) {
                firstPosition = 0;
            }
            if(card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) >=45){
                card.setEtaPredictiveConfidence(card.getEtaPredictiveConfidence() + 1.00f);
                return true;
            }else if(card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) < 45
                    && card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) >= 30){
                card.setEtaPredictiveConfidence(card.getEtaPredictiveConfidence() + 0.75f);
            }else if(card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) < 30
                    && card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) >= 15){
                card.setEtaPredictiveConfidence(card.getEtaPredictiveConfidence() + 0.50f);
            }else if(card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) < 15
                    && card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) > 0){
                card.setEtaPredictiveConfidence(card.getEtaPredictiveConfidence() + 0.25f);
            }else if(card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) <= 0
                    && card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) > -15){
                card.setEtaPredictiveConfidence(card.getEtaPredictiveConfidence() - 0.25f);
            }else if(card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) <= -15
                    && card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) > -30){
                card.setEtaPredictiveConfidence(card.getEtaPredictiveConfidence() - 0.50f);
            }else if(card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) <= -30
                    && card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) > -45){
                card.setEtaPredictiveConfidence(card.getEtaPredictiveConfidence() - 0.75f);
            }else if(card.getLastSeveralETAs().get(firstPosition) - card.getLastSeveralETAs().get(lastPosition) <= -45){
                card.setEtaPredictiveConfidence(card.getEtaPredictiveConfidence() - 1.00f);
            }
        }
        return false;
    }

    //Loop all the possible routes that start from the lastLatLon, and find the route whose edge ids contain current location edge id
    private void buildPredictiveCards() {
        if (allPredictiveCards.size() == 0) {
            //send request to server to get all the predictive routes, and sort them by edge ids
            buildInitial(dataServiceDelegate.retrievePersonalized());
        }

        //if it has empty routes or still on the current edge, ignore current loop
//        if (allPredictiveCardsMap.size() == 0) {
//            PredCardLogger.log(LogEnum.LogPriority.info, PredictiveCardManager.class, "ignore current loop since the allPredictiveCardsMap.size = " + allPredictiveCardsMap.size()
//                    + ", and currentEdgeId(" + currentEdgeId + ") == lastEdgeId(" + lastEdgeId + ") is " + (currentEdgeId == lastEdgeId));
//            return;
//        }

        //TODO add possibility to enable/disable this check
        if (currentEdgeId == lastEdgeId && (currentLatLon == null || isOriginInRange(currentLatLon, lastLatLon))) {
            PredCardLogger.logI(PredictiveCardManager.class, "ignore current loop since (currentEdgeid == lastEdgeId) = " + (currentEdgeId == lastEdgeId)
                    + ", and currentLatLon is null or still in range = " + currentLatLon);
            return;
        }

        lastEdgeId = currentEdgeId;
        lastLatLon = currentLatLon;

        List<PredictiveCard> temp = new ArrayList<>();

        //if car engine is just started, and there is no edge id at this time, so we still need to show up the initial cards
        for (PredictiveCard card : allPredictiveCards) {
            if(checkDayTimeBasedValidity(card)){
                decorateCard(card);
                buildETABasedConfidencePredictiveCard(card);

                if (isOriginInRange(card.getOrigin(),lastLatLon)){
                    temp.add(card);
                }else if(isDestinationInRange(card.getDest(), lastLatLon)) {
                    if(card.getExtraInfo() == null){ // no extra info yet, but it is still in range, so add it.
                        if (!temp.contains(card)) {
                            temp.add(card);
                        }

                    } else if(card.getExtraInfo() != null && card.getExtraInfo().getEta() <= Integer.parseInt(this.config.getProperty(PredictiveCardConfig.KEY_PREDICTIVE_CARD_DESTINATION_MAX_ETA))) { // now we know the eta and check that eta is in our range.
                        if (!temp.contains(card)) {
                            temp.add(card);
                        }
                    }else if(card.getEtaPredictiveConfidence() >= 4.0f){
                        if (!temp.contains(card)) {
                            temp.add(card);
                        }
                    }
                }else if(card.getEtaPredictiveConfidence() >= 4.0f){
                    if (!temp.contains(card)) {
                        temp.add(card);
                    }
                }
            }
        }
        PredCardLogger.logI(PredictiveCardManager.class, "Filter out cards by lastLatLon and result = " + temp);

        if (allPredictiveCardsMap.containsKey(currentEdgeId)) {
            List<PredictiveCard> cards = allPredictiveCardsMap.get(currentEdgeId);
            for (PredictiveCard card : cards) {
                if (!temp.contains(card) && checkDayTimeBasedValidity(card)) {
                    decorateCard(card);
                    buildETABasedConfidencePredictiveCard(card);

                    temp.add(card);
                }
            }
            PredCardLogger.logI(PredictiveCardManager.class, "Filter out cards by currentEdgeId(" + currentEdgeId + ") and result = " + temp);
        }

        predictiveCards.clear();
        if (temp.size() > 0) {
//            Iterator<PredictiveCard> iterator = temp.iterator();
//            while (iterator.hasNext()) {
//                PredictiveCard card = iterator.next();
//                int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
//                int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//                // Per PM's new requirement, the start point should not be the necessary condition to filter out things.
//                // E.g., if user is driving along high way 101 but running out of gas, he may drive out to fill in gas and back again.
//                // In this special case, the user's current start point is becoming the gas station which is not our expectation to filter out the probable destinations.
//                if (/**!isStartFromOrigin(card.getOrigin()) ||*/card.getDayOfWeek().getValue() != dayOfWeek
//                        || (card.getTimeBucket().getTimeUnit() == TimeBucket.TimeUnit.HOURS && (hourOfDay > card.getTimeBucket().getTo() || hourOfDay < card.getTimeBucket().getFrom()))) {
//                    iterator.remove();
//                }else{
//                    decorateCard(card);
//                }
//            }
//
//            if(temp.size() <=0){
//                PredCardLogger.log(LogEnum.LogPriority.info, PredictiveCardManager.class, "Sarvesh Test No Card to Show Up");
//            }

            Collections.sort(temp, new Comparator<PredictiveCard>() {
                @Override
                public int compare(PredictiveCard lhs, PredictiveCard rhs) {
                    int result = (int) (rhs.getPredictiveRating() - lhs.getPredictiveRating());
                    if (result == 0) {
                        return (int) (getRouteFrequency(rhs.getPersonalizedRoutes()) - getRouteFrequency(lhs.getPersonalizedRoutes()));
                    } else {
                        return result;
                    }
                }
            });

            if (temp.size() < cardsNum) {
                predictiveCards.addAll(temp);
            } else {
                for (int i = 0; i < cardsNum; i++) {
                    predictiveCards.add(temp.get(i));
                }
            }
            // Set most preferred edgeIds
            for (PredictiveCard card : this.predictiveCards) {
                TraversedRoutes traversedRoutes = getMostFrequencyTraversedRoutes(card.getPersonalizedRoutes());
                if (traversedRoutes != null) {
                    List<Long> preferEdges = new ArrayList<>();
                    for (Double edge : traversedRoutes.getEdgeIds()) {
                        preferEdges.add(edge.longValue());
                    }
                    card.setPreferEdges(preferEdges);
                }
            }

//            // Decorate the predictive card
//            if (decorator != null) {
//                for (PredictiveCard card : predictiveCards) {
//                    decorateCard(card);
//                    if(allPredictiveCards.contains(card)){
//                        allPredictiveCards.remove(card);
//                        allPredictiveCards.add(card);
//                    }
//                }
//            }
        }

        PredCardLogger.logI(PredictiveCardManager.class,
                "the predictiveCards in {currentEdgeId(" + currentEdgeId + "), lastLatLon(" + lastLatLon + ") } = " + this.predictiveCards.size() + ", " + this.predictiveCards);
        synchronized (this) {
            for (PredictiveCardListener listener : listeners) {
                listener.notifyPredictiveCards(predictiveCards);
            }
        }
    }

    private void decorateCard(PredictiveCard card){
        if(decorator == null){
            return;
        }

        decorator.decorateCard(card);

        if(card.getExtraInfo() != null) {
            card.addETA(card.getExtraInfo().getEta() - card.getExtraInfo().getTrafficDelay());
        }
    }

    private double getRouteFrequency(List<PersonalizedRoute> list) {
        TraversedRoutes traversedRoutes = getMostFrequencyTraversedRoutes(list);
        return traversedRoutes != null ? traversedRoutes.getFrequency() : 0;
    }

    private TraversedRoutes getMostFrequencyTraversedRoutes(List<PersonalizedRoute> list) {
        if (list == null)
            return null;
        TraversedRoutes response = null, defaultOne = null;
        for (PersonalizedRoute personalizedRoute : list) {
            for (TraversedRoutes traversedRoutes : personalizedRoute.getTraversedRoutes()) {
                if (response == null || traversedRoutes.getFrequency() > response.getFrequency()) {
                    defaultOne = traversedRoutes;
                    if (currentEdgeId == -1 || traversedRoutes.getEdgeIds().contains((double) currentEdgeId)) {
                        response = traversedRoutes;
                    }
                }
            }
        }
        return response == null ? defaultOne : response;
    }

    private void buildInitial(UserPersonalizedInfo userPersonalizedInfo) {

        for (PredictiveDestination destination : userPersonalizedInfo.getPredictiveDestinations()) {
            if (destination.getProbability() > Double.valueOf(this.config.getProperty(PredictiveCardConfig.KEY_PREDICTIVE_CARD_THRESHOLD))) {
                PredictiveCard card = convertPredictiveCard(destination);

                if(!allPredictiveCards.contains(card)) {
                    allPredictiveCards.add(card);
                }
                int range = Integer.parseInt(this.config.getProperty(PredictiveCardConfig.KEY_PREDICTIVE_CARD_ORIGIN_RANGE));
                for (PersonalizedRoute route : userPersonalizedInfo.getPersonalizedRoutes()) {
                    if (GeoUtility.calculateDistance(destination.getOriginLat(), destination.getOriginLon(), route.getOriginLat(), route.getOriginLon()) <= range
                            && GeoUtility.calculateDistance(destination.getDestinationLat(), destination.getDestinationLon(), route.getDestinationLat(), route.getDestinationLon()) <= range) {
                        card.addPersonalizedRoute(route);
                        for (TraversedRoutes routes : route.getTraversedRoutes()) {
                            for (Double edgeId : routes.getEdgeIds()) {
                                List<PredictiveCard> predictiveCards = allPredictiveCardsMap.get(edgeId.longValue());
                                if (predictiveCards == null) {
                                    predictiveCards = new ArrayList<>();
                                    allPredictiveCardsMap.put(edgeId.longValue(), predictiveCards);
                                }
                                if (!predictiveCards.contains(card)) {
                                    predictiveCards.add(card);
                                }
                            }
                        }
                    }
                }
            }
        }
        PredCardLogger.logI(PredictiveCardManager.class, "AllPredictiveCards = " + allPredictiveCards);
    }

    private boolean isOriginInRange(LatLon origin, LatLon latLon){
        if (origin == null || latLon == null) {
            return false;
        }
        double distance = GeoUtility.calculateDistance(origin.getLat(), origin.getLon(), latLon.getLat(), latLon.getLon());
        return distance <= Integer.parseInt(config.getProperty(PredictiveCardConfig.KEY_PREDICTIVE_CARD_ORIGIN_RANGE));
    }

    private boolean isDestinationInRange(LatLon dest, LatLon latLon2) {
        if (dest == null || latLon2 == null) {
            return false;
        }
        double distance = GeoUtility.calculateDistance(dest.getLat(), dest.getLon(), latLon2.getLat(), latLon2.getLon());
        return distance <= Integer.parseInt(this.config.getProperty(PredictiveCardConfig.KEY_PREDICTIVE_CARD_DESTINATION_RANGE));
    }

    private PredictiveCard convertPredictiveCard(PredictiveDestination destination) {
        PredictiveCard card = new PredictiveCard();
        card.setDayOfWeek(PredictiveCard.DayOfWeek.valueOf(destination.getDayOfWeek()));
        card.setPredictiveRating(destination.getProbability());
        card.setTimeBucket(new TimeBucket(destination.getTimeBucket().getFrom(),
                destination.getTimeBucket().getTo(), TimeBucket.TimeUnit.value(destination.getTimeBucket().getTimeUnit().getValue())));
        card.setProbability(destination.getProbability());

        LatLon latLon = new LatLon();
        latLon.setLat(destination.getOriginLat());
        latLon.setLon(destination.getOriginLon());
        card.setOrigin(latLon);

        latLon = new LatLon();
        latLon.setLat(destination.getDestinationLat());
        latLon.setLon(destination.getDestinationLon());
        card.setDest(latLon);

        return card;
    }

}
