package com.github.dnault.xmlpatch.batch;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

public class AssembledPatch {
    private LinkedHashSet<String> includedFiles = new LinkedHashSet<>();
    private List<Element> diffs = new ArrayList<>();

    public LinkedHashSet<String> getIncludedFiles() {
        return includedFiles;
    }

    public boolean addIncludedFile(String includedFile) {
        return this.includedFiles.add(includedFile);
    }

    public List<Element> getDiffs() {
        return diffs;
    }

    public List<Element> getDiffs(String sourcePath) {
        requireNonNull(sourcePath);

        List<Element> matchingDiffs = new ArrayList<>(0);
        for (Element e : diffs) {
            if (sourcePath.equals(e.getAttributeValue("file"))) {
                matchingDiffs.add(e);
            }
        }
        return matchingDiffs;
    }

    public Set<String> getSourcePaths() {
        Set<String> sourcePaths = new HashSet<>();
        for (Element e : diffs) {
            sourcePaths.add(e.getAttributeValue("file"));
        }
        return sourcePaths;
    }

    public void addDif(Element diff) {
        this.diffs.add(diff);
    }
}
