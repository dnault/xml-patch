package com.github.dnault.xmlpatch.ant;

import com.github.dnault.xmlpatch.BatchPatcher;

public class PatchXmlDir extends AbstractPatchTask {
    @Override
    protected void doPatch() throws Exception {
        BatchPatcher.patch("--patch=" + getPatch(), "--srcdir=" + getSrc(), "--destdir=" + getDest());
    }
}

