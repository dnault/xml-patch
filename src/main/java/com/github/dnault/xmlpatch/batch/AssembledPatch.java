package com.github.dnault.xmlpatch.batch;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Element;

public class AssembledPatch {
    private LinkedHashSet<File> patchFiles = new LinkedHashSet<>();
    private List<Element> diffs = new ArrayList<>();
    private Set<String> accessedPaths = new HashSet<>();

    public AssembledPatch() {
    }

    public AssembledPatch(File patch) throws Exception {
        new PatchAssembler().assembleRecursive(patch, this);
    }

    /**
     * @return the set of files that comprise the patch
     */
    public LinkedHashSet<File> getPatchFiles() {
        return patchFiles;
    }

    public boolean addPatchFile(File includedFile) {
        return this.patchFiles.add(includedFile);
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
                accessedPaths.add(sourcePath);
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

    public Set<String> getAccessedPaths() {
        return accessedPaths;
    }

    public void addDif(Element diff) {
        this.diffs.add(diff);
    }
}
