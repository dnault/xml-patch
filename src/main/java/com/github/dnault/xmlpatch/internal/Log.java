package com.github.dnault.xmlpatch.internal;

public class Log {

    public static void info(String s) {
        System.out.println(s);
    }

    public static void warn(String s) {
        System.err.println("WARN: " + s);
    }

    public static void error(String s) {
        System.err.println("ERROR: " + s);
    }
}
