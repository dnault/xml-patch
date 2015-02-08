package com.github.dnault.xmlpatch.batch;

import static com.github.dnault.xmlpatch.internal.XmlHelper.getChildren;

import java.io.File;

import com.github.dnault.xmlpatch.internal.Log;
import com.github.dnault.xmlpatch.internal.XmlHelper;
import org.jdom.Document;
import org.jdom.Element;

public class PatchAssembler {

    public AssembledPatch assemble(File patchFile) throws Exception {
        AssembledPatch assembled = new AssembledPatch();
        assembleRecursive(patchFile, assembled);
        return assembled;
    }

    protected void assembleRecursive(File patchFile, AssembledPatch assembled) throws Exception {

        String path = patchFile.getAbsolutePath();
        if (!assembled.addIncludedFile(path)) {
            // already included this file.
            return;
        }

        Log.info("including patch file: " + path);

        Document doc = XmlHelper.parse(patchFile);
        Element batchElement = doc.getRootElement();

        if (!batchElement.getName().equals("diffs")) {
            throw new IllegalArgumentException(path + ": expected root element of patch document to be 'diffs' but found '" + batchElement.getName() + "'");
        }

        for (Element diff : getChildren(batchElement)) {
            if (diff.getName().equals("include")) {
                String includeFilename = diff.getAttributeValue("file");
                if (includeFilename == null) {
                    throw new IllegalArgumentException(path + ": element 'include' missing 'file' attribute");
                }

                File includeFile = new File(patchFile.getParent(), includeFilename);
                assembleRecursive(includeFile, assembled);
                continue;
            }

            if (!diff.getName().equals("diff")) {
                throw new IllegalArgumentException(path + ": unexpected element '" + diff.getName() + "' in patch document, expected 'diff' or 'include'");
            }

            if (diff.getAttribute("file") == null) {
                throw new IllegalArgumentException(path + ": 'diff' element missing 'file' attribute");
            }

            assembled.addDif((Element) diff.clone());
        }
    }
}
