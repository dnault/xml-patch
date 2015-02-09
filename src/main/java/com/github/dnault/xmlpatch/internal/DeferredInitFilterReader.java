package com.github.dnault.xmlpatch.internal;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public abstract class DeferredInitFilterReader extends FilterReader {
    private boolean initialized;

    protected DeferredInitFilterReader(Reader in) {
        super(in);
    }

    abstract protected void initialize();

    private void initializeIfNecessary() {
        if (!initialized) {
            initialize();
            initialized = true;
        }
    }

    @Override
    public int read() throws IOException {
        initializeIfNecessary();
        return super.read();
    }

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        initializeIfNecessary();
        return super.read(cbuf, off, len);
    }
}
