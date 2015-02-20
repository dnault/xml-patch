package com.github.dnault.xmlpatch.internal.logging;

public interface XmlPatchLogger {
    void info(String s);

    void warn(String s);

    void error(String s);

    void debug(String s);
}
