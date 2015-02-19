package com.github.dnault.xmlpatch.gradle;

import static com.google.common.base.Throwables.propagate;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.Set;

import com.github.dnault.xmlpatch.batch.AssembledPatch;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.FileCollectionAdapter;
import org.gradle.api.internal.file.collections.MinimalFileSet;

public class XmlMultiPatch {

    private final File file;
    private AssembledPatch assembledPatch;

    public XmlMultiPatch(File file) {
        this.file = requireNonNull(file);
    }

    FileCollection getPatchFragments() {
        return new FileCollectionAdapter(new MinimalFileSet() {
            @Override
            public Set<File> getFiles() {
                try {
                    return getAssembledPatch().getPatchFiles();
                } catch (Exception e) {
                    throw propagate(e);
                }
            }

            @Override
            public String getDisplayName() {
                return "Patch fragments referenced by " + file.getAbsolutePath();
            }
        });
    }

    AssembledPatch getAssembledPatch() throws Exception {
        if (assembledPatch == null) {
            assembledPatch = new AssembledPatch(file);
        }
        return assembledPatch;
    }


}
