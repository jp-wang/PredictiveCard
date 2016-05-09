package com.telenav.predictivecards.util;

import android.location.Location;

/**
 * @author jpwang
 * @since 10/20/15
 */
public class GeoUtility {
    private static final double NAUTICAL_MILE = 1852.0;
    private static final double COEFFICIENT = Math.PI / 180;
    private static final double EPSILON = 1E-6;

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // http://williams.best.vwh.net/avform.htm#Dist

        lat1 = lat1 * COEFFICIENT;
        lon1 = lon1 * COEFFICIENT;
        lat2 = lat2 * COEFFICIENT;
        lon2 = lon2 * COEFFICIENT;

        double distance = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        distance = distance / COEFFICIENT * 60 * NAUTICAL_MILE;
        return distance;
    }

    /**
     * Calculate distance between 2 locations.
     *
     * @param latLon1 1st location.
     * @param latLon2 2nd location.
     * @return Distance in meters.
     */
    public static double calculateDistance(Location latLon1, Location latLon2) {
        return calculateDistance(latLon1.getLatitude(), latLon1.getLongitude(), latLon2.getLatitude(), latLon2.getLongitude());
    }

    /**
     * Calculate new location after displacement.
     *
     * @param latLon           Location.
     * @param distanceInMeters Distance of the displacement, in meters.
     * @param bearingInDegrees Bearing of the displacement, in degrees, reference to north.
     * @return Location after displacement.
     */
    public static Location calculateLocationDisplacement(Location latLon, double distanceInMeters, double bearingInDegrees) {
        // http://williams.best.vwh.net/avform.htm#LL

        final double lat1 = latLon.getLatitude() * COEFFICIENT;
        final double lon1 = latLon.getLongitude() * COEFFICIENT;
        final double distance = distanceInMeters / NAUTICAL_MILE * COEFFICIENT / 60;
        final double bearing = bearingInDegrees * COEFFICIENT;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance) + Math.cos(lat1) * Math.sin(distance) * Math.cos(bearing));
        double lon2;
        if (Math.abs(Math.cos(lat2)) < EPSILON) {
            lon2 = lon1;
        } else {
            lon2 = (lon1 - Math.asin(Math.sin(bearing) * Math.sin(distance) / Math.cos(lat2)) + Math.PI) % (2 * Math.PI) - Math.PI;
        }

        final Location latLon2 = new Location("");
        latLon2.setLatitude(lat2 / COEFFICIENT);
        latLon2.setLongitude(lon2 / COEFFICIENT);
        return latLon2;
    }
}
