package com.shakshin.mikrotik.util;

/*
    Utility class to log messages to STDOUT
 */

public class Logger {

    private static boolean enabled = false;

    public static void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }


    public static void trace(String message) {
        if (!enabled) return;
        System.out.println(message);
    }

}
