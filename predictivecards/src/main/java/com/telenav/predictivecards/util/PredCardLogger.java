package com.telenav.predictivecards.util;

import android.util.Log;

public final class PredCardLogger {
    private static final String TAG = "PredictiveCard";

    private PredCardLogger() {
    }

    public static void logV(Class<?> loggedClass, String msg) {
        logV((Class)loggedClass, msg, null, new Object[0]);
    }

    public static void logD(Class<?> loggedClass, String msg) {
        logD((Class)loggedClass, msg, null, new Object[0]);
    }

    public static void logI(Class<?> loggedClass, String msg) {
        logI((Class)loggedClass, msg, null, new Object[0]);
    }

    public static void logW(Class<?> loggedClass, String msg) {
        logW((Class)loggedClass, msg, null, new Object[0]);
    }

    public static void logE(Class<?> loggedClass, String msg) {
        logE((Class)loggedClass, msg, null, new Object[0]);
    }

    public static void logV(Class<?> loggedClass, String msg, Throwable throwable) {
        logV(loggedClass, msg, throwable, new Object[0]);
    }

    public static void logD(Class<?> loggedClass, String msg, Throwable throwable) {
        logD(loggedClass, msg, throwable, new Object[0]);
    }

    public static void logI(Class<?> loggedClass, String msg, Throwable throwable) {
        logD(loggedClass, msg, throwable, new Object[0]);
    }

    public static void logW(Class<?> loggedClass, String msg, Throwable throwable) {
        logW(loggedClass, msg, throwable, new Object[0]);
    }

    public static void logE(Class<?> loggedClass, String msg, Throwable throwable) {
        logE(loggedClass, msg, throwable, new Object[0]);
    }

    public static void logV(Class<?> loggedClass, String msg, Object... msgArgs) {
        logV((Class)loggedClass, msg, null, msgArgs);
    }

    public static void logD(Class<?> loggedClass, String msg, Object... msgArgs) {
        logD((Class)loggedClass, msg, null, msgArgs);
    }

    public static void logI(Class<?> loggedClass, String msg, Object... msgArgs) {
        logI((Class)loggedClass, msg, null, msgArgs);
    }
    public static void logW(Class<?> loggedClass, String msg, Object... msgArgs) {
        logW((Class)loggedClass, msg, null, msgArgs);
    }
    public static void logE(Class<?> loggedClass, String msg, Object... msgArgs) {
        logE((Class)loggedClass, msg, null, msgArgs);
    }

    public static void logV(Class<?> loggedClass, String msg, Throwable throwable, Object... msgArgs) {
        log(Log.VERBOSE, loggedClass, msg, throwable, msgArgs);
    }

    public static void logD(Class<?> loggedClass, String msg, Throwable throwable, Object... msgArgs) {
        log(Log.DEBUG, loggedClass, msg, throwable, msgArgs);
    }

    public static void logI(Class<?> loggedClass, String msg, Throwable throwable, Object... msgArgs) {
        log(Log.INFO, loggedClass, msg, throwable, msgArgs);
    }

    public static void logW(Class<?> loggedClass, String msg, Throwable throwable, Object... msgArgs) {
        log(Log.WARN, loggedClass, msg, throwable, msgArgs);
    }

    public static void logE(Class<?> loggedClass, String msg, Throwable throwable, Object... msgArgs) {
        log(Log.ERROR, loggedClass, msg, throwable, msgArgs);
    }

    public static void log(int priority, Class<?> loggedClass, String msg, Throwable throwable, Object... msgArgs) {

        try {
            if(msg != null && msgArgs != null && msgArgs.length > 0) {
                msg = String.format(msg, msgArgs);
            }
        } catch (Throwable var7) {
            var7.printStackTrace();
        }

        String componentName = TAG + ": " + loggedClass.getName();
        Log.println(priority, componentName, msg);
    }


}