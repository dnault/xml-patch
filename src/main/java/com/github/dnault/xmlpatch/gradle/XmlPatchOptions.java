package com.github.dnault.xmlpatch.gradle;

public class XmlPatchOptions {
    private boolean failOnMissingPatch = true;
    private boolean failOnMissingSourcePath = true;

    public boolean isFailOnMissingPatch() {
        return failOnMissingPatch;
    }

    public void setFailOnMissingPatch(boolean failOnMissingPatch) {
        this.failOnMissingPatch = failOnMissingPatch;
    }

    public boolean isFailOnMissingSourcePath() {
        return failOnMissingSourcePath;
    }

    public void setFailOnMissingSourcePath(boolean failOnMissingSourcePath) {
        this.failOnMissingSourcePath = failOnMissingSourcePath;
    }
}
