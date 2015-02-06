package com.github.dnault.xmlpatch.ant;

import com.github.dnault.xmlpatch.CommandLineDriver;

public class PatchXmlFile extends AbstractPatchTask {
    @Override
    protected void doPatch() throws Exception {
        CommandLineDriver.main(getSrc(), getPatch(), getDest());
    }
}

