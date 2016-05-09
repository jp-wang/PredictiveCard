package com.telenav.predictivecards;

import com.telenav.api.data.api.v2.DataDomain;
import com.telenav.api.data.api.v2.DataFilter;
import com.telenav.api.data.api.v2.DataFilterType;
import com.telenav.api.data.api.v2.DataObject;
import com.telenav.api.data.api.v2.DataRetrieveRequest;
import com.telenav.api.data.api.v2.DataRetrieveResponse;
import com.telenav.api.data.user.insight.v1.KeyDestination;
import com.telenav.api.data.user.insight.v1.PersonalizedRoute;
import com.telenav.api.data.user.insight.v1.PredictiveDestination;
import com.telenav.dataservice.DataService;
import com.telenav.predictivecards.util.PredCardLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jpwang
 * @since 7/23/15
 */
class DataServiceDelegate {
    private DataService dataService;
    private PredictiveCardConfig config;

    DataServiceDelegate(PredictiveCardConfig cardConfig, DataService dataService) {
        this.dataService = dataService;
        this.config = cardConfig;
    }

    UserPersonalizedInfo retrievePersonalized() {
        UserPersonalizedInfo info = new UserPersonalizedInfo();
        Collection<PredictiveDestination> destinations = new ArrayList<>();
        Collection<PersonalizedRoute> routes = new ArrayList<>();
        Collection<KeyDestination> keyDestinations = new ArrayList<>();
        info.setPredictiveDestinations(destinations);
        info.setKeyDestinations(keyDestinations);
        info.setPersonalizedRoutes(routes);
        try {
            List<String> params = new ArrayList<>();
            params.add(this.config.getProperty(PredictiveCardConfig.KEY_PREDICTIVE_CARD_USER_ID));

            List<DataFilter> filters = new ArrayList<>();
            DataFilter filter = new DataFilter();
            filters.add(filter);
            filter.setType(DataFilterType.USER_ID);
            filter.setParams(params);

            DataRetrieveRequest retrieveRequest = new DataRetrieveRequest();
            retrieveRequest.setDomain(DataDomain.USER_INSIGHT);
            retrieveRequest.setFilters(filters);


            PredCardLogger.logI(DataServiceDelegate.class, "PredictiveRequest:" + retrieveRequest.toJsonString());
            DataRetrieveResponse response = dataService.retrieve(retrieveRequest);
            if (response != null) {
                PredCardLogger.logI(DataServiceDelegate.class, "PredictiveResponse:" + response.toJsonString());
                Collection<DataObject> collection = response.getData();
                for (DataObject dataObject : collection) {
                    destinations.addAll(dataObject.getUserInsight().getPredictiveDestinations());
                    routes.addAll(dataObject.getUserInsight().getPersonalizedRoute());
                    keyDestinations.addAll(dataObject.getUserInsight().getKeyDestinations());
                }
            } else {
                PredCardLogger.logW(DataServiceDelegate.class, "retrieve key destination response is null.");
            }
        } catch (Exception e) {
            PredCardLogger.logE(DataServiceDelegate.class, "key destination retrieve exception: ", e);
        }

        return info;
    }

}
