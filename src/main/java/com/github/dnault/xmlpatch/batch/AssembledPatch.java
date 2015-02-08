package com.github.dnault.xmlpatch.batch;

import java.util.LinkedHashSet;
import java.util.List;

import org.jdom.Element;

public class AssembledPatch {
    private LinkedHashSet<String> includedFiles;
    private List<Element> diffs;

    public LinkedHashSet<String> getIncludedFiles() {
        return includedFiles;
    }

    public boolean addIncludedFile(String includedFile) {
        return this.includedFiles.add(includedFile);
    }

    public List<Element> getDiffs() {
        return diffs;
    }

    public void addDif(Element diff) {
        this.diffs.add(diff);
    }
}
