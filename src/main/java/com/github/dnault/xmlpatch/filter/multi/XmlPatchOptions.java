package com.github.dnault.xmlpatch.filter.multi;

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

    @Override
    public String toString() {
        return "XmlPatchOptions{" +
                "failOnMissingPatch=" + failOnMissingPatch +
                ", failOnMissingSourcePath=" + failOnMissingSourcePath +
                '}';
    }
}
