package com.github.dnault.xmlpatch.internal.logging;

public class ConsoleLogger implements XmlPatchLogger {
    @Override
    public void info(String s) {
        System.out.println(s);
    }

    @Override
    public void warn(String s) {
        System.err.println("WARN: " + s);
    }

    @Override
    public void error(String s) {
        System.err.println("ERROR: " + s);
    }

    @Override
    public void debug(String s) {
        System.err.println("DEBUG: " + s);
    }
}
