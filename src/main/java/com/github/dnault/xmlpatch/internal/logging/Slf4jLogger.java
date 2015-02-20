package com.github.dnault.xmlpatch.internal.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogger implements XmlPatchLogger {
    private static Logger logger = LoggerFactory.getLogger("com.github.dnault.xmlpatch");

    public void info(String s) {
        logger.info(s);
    }

    public void warn(String s) {
        logger.warn(s);
    }

    public void error(String s) {
        logger.error(s);
    }

    @Override
    public void debug(String s) {
        logger.debug(s);
    }
}
