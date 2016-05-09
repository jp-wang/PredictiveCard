package com.telenav.predictivecards;

import com.telenav.api.common.v1.LatLon;
import com.telenav.api.services.v1.Context;

import java.util.Map;

/**
 * @author jpwang
 * @since 7/23/15
 */
public interface DataServiceContext {
    LatLon getHomeLocation();

    LatLon getWorkLocation();

    Context getContext();

    Map<String, String> getHeader();

    //device id to get home area
    String getDeviceId();
}
