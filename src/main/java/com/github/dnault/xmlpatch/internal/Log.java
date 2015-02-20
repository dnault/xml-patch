package com.github.dnault.xmlpatch.internal;

import com.github.dnault.xmlpatch.internal.logging.ConsoleLogger;
import com.github.dnault.xmlpatch.internal.logging.Slf4jLogger;
import com.github.dnault.xmlpatch.internal.logging.XmlPatchLogger;

public class Log {
    private static final XmlPatchLogger logger = isSlf4jPresent() ? new Slf4jLogger() : new ConsoleLogger();

    private static boolean isSlf4jPresent() {
        try {
            Class.forName("org.slf4j.Logger");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void debug(String s) {
        logger.debug(s);
    }

    public static void info(String s) {
        logger.info(s);
    }

    public static void warn(String s) {
        logger.warn(s);
    }

    public static void error(String s) {
        logger.error(s);
    }
}
