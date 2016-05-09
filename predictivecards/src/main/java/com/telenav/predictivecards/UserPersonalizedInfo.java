package com.telenav.predictivecards;

import com.telenav.api.data.user.insight.v1.KeyDestination;
import com.telenav.api.data.user.insight.v1.PersonalizedRoute;
import com.telenav.api.data.user.insight.v1.PredictiveDestination;

import java.util.Collection;

/**
 * @author jpwang
 * @since 7/23/15
 */
public final class UserPersonalizedInfo {
    private Collection<PredictiveDestination> predictiveDestinations;
    private Collection<PersonalizedRoute> personalizedRoutes;
    private Collection<KeyDestination> keyDestinations;

    public Collection<PredictiveDestination> getPredictiveDestinations() {
        return predictiveDestinations;
    }

    public void setPredictiveDestinations(Collection<PredictiveDestination> predictiveDestinations) {
        this.predictiveDestinations = predictiveDestinations;
    }

    public Collection<PersonalizedRoute> getPersonalizedRoutes() {
        return personalizedRoutes;
    }

    public void setPersonalizedRoutes(Collection<PersonalizedRoute> personalizedRoutes) {
        this.personalizedRoutes = personalizedRoutes;
    }

    public Collection<KeyDestination> getKeyDestinations() {
        return keyDestinations;
    }

    public void setKeyDestinations(Collection<KeyDestination> keyDestinations) {
        this.keyDestinations = keyDestinations;
    }
}
