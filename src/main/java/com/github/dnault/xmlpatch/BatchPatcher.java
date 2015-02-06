/*
 * Copyright 2015 David Nault and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnault.xmlpatch;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.dnault.xmlpatch.XmlHelper.getChildren;
import static java.util.Arrays.asList;

public class BatchPatcher {

    private static OptionSet parseOptions(OptionParser parser, String... args) throws IOException {
        try {
            return parser.parse(args);
        } catch (OptionException e) {
            System.err.println(e.getMessage());
            System.err.println();
            parser.printHelpOn(System.err);
            System.exit(1);
            throw new Error();
        }
    }

    public static void main(String... args) throws Exception {
        patch(args);


        //patch("--patch", "/tmp/patch.xml", "--srcdir", "/tmp/srcdir", "--destdir", "/tmp/patchdest");
    }

    public static void patch(String... args) throws Exception {
        OptionParser parser = new OptionParser();

        OptionSpec<Void> helpOption = parser.acceptsAll(asList("help", "?"), "display this help message");

        OptionSpec<File> patchOption = parser.accepts("patch", "patch file to apply").withRequiredArg().ofType(File.class).required();

        OptionSpec<File> srcdirOption = parser.accepts("srcdir", "for multi-file patches, specifies the base dir for the files to be patched")
                .withRequiredArg().ofType(File.class).required();

        OptionSpec<File> destdirOption = parser.accepts("destdir", "for multi-file patches, specifies the output directory. (default: apply the patch in-place)")
                .withRequiredArg().ofType(File.class);

        OptionSet options = parseOptions(parser, args);

        if (options.has(helpOption)) {
            parser.printHelpOn(System.out);
            System.exit(0);
        }

        File patchFile = patchOption.value(options);
        assertFileExists(patchFile, "patch");

        File srcdir = srcdirOption.value(options);
        assertDirectoryExists(srcdir, "srcdir");

        File destdir = options.has(destdirOption) ? destdirOption.value(options) : srcdir;
        if (destdir.exists() && !destdir.isDirectory()) {
            throw new IllegalArgumentException("destdir is not a directory: " + destdir.getAbsolutePath());
        }

        File tempdir = IoHelper.createTempDir();

        try {
            Set<String> relativePathsOfPatchedFiles = patch(
                    assemblePatchDocument(patchFile), srcdir, destdir, tempdir);

            info("writing patched files to destdir: " + destdir.getAbsolutePath());
            for (String path : relativePathsOfPatchedFiles) {
                File tempFile = new File(tempdir, path);
                File destFile = new File(destdir, path);
                IoHelper.makeParentDirectory(destFile);
                IoHelper.move(tempFile, destFile);
            }

            IoHelper.deleteDirectory(tempdir);

        } catch (PatchException e) {
            error(e.getMessage());
            System.exit(1);
        }
    }

    private static void info(String s) {
        System.out.println(s);
    }

    private static void warn(String s) {
        System.err.println("WARN: " + s);
    }

    private static void error(String s) {
        System.err.println("ERROR: " + s);
    }

    public static void assertFileExists(File f, String argname) throws FileNotFoundException {
        if (!f.exists()) {
            throw new FileNotFoundException(argname + " file not found: " + f.getAbsolutePath());
        }
        if (!f.isFile()) {
            throw new IllegalArgumentException(argname + " is not a file: " + f.getAbsolutePath());
        }
    }

    public static void assertDirectoryExists(File f, String argname) throws FileNotFoundException {
        if (!f.exists()) {
            throw new FileNotFoundException(argname + " directory not found: " + f.getAbsolutePath());
        }
        if (!f.isDirectory()) {
            throw new IllegalArgumentException(argname + " is not a directory: " + f.getAbsolutePath());
        }
    }

    private static List<Element> assemblePatchDocument(File patchFile) throws Exception {
        List<Element> diffs = new ArrayList<Element>();
        assemblePatchDocument(patchFile, diffs, new HashSet<String>());
        return diffs;
    }

    private static void assemblePatchDocument(File patchFile, List<Element> diffs, Set<String> includedFiles) throws Exception {

        String path = patchFile.getAbsolutePath();
        if (!includedFiles.add(path)) {
            // already included this file.
            return;
        }

        info("including patch file: " + path);

        FileInputStream fis = new FileInputStream(patchFile);
        Document doc = XmlHelper.parse(fis);
        fis.close();

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
                assemblePatchDocument(includeFile, diffs, includedFiles);
                continue;
            }

            if (!diff.getName().equals("diff")) {
                throw new IllegalArgumentException(path + ": unexpected element '" + diff.getName() + "' in patch document, expected 'diff'");
            }

            if (diff.getAttribute("file") == null) {
                throw new IllegalArgumentException(path + ": diff element missing 'file' attribute");
            }

            diffs.add((Element) diff.clone());
        }
    }

    private static Set<String> patch(List<Element> diffs, File srcdir, File destdir, File tempdir) throws Exception {

        Set<String> outputFilePaths = new HashSet<String>();

        for (Element diff : diffs) {

            if (diff.getAttribute("file") == null) {
                throw new IllegalArgumentException("diff element missing 'file' attribute");
            }

            String srcfilePath = diff.getAttributeValue("file");

            File fileToPatch = new File(srcfilePath);
            if (fileToPatch.isAbsolute()) {
                throw new IllegalArgumentException("not a relative path: " + srcfilePath);
            }

            outputFilePaths.add(srcfilePath);

            File alreadyInTempDir = new File(tempdir, srcfilePath);
            fileToPatch = alreadyInTempDir.exists() ? alreadyInTempDir : new File(srcdir, srcfilePath);

            info("patching " + srcfilePath + " [from " + fileToPatch.getAbsolutePath() + "]");

            diff.removeAttribute("file");
            Format format = Format.getRawFormat();
            format.setOmitDeclaration(true);
            XMLOutputter outputter = new XMLOutputter(format);
            String s = outputter.outputString(diff);

            InputStream diffStream = new ByteArrayInputStream(s.getBytes("UTF-8"));
            InputStream inputStream = new FileInputStream(fileToPatch);

            File outputFile = File.createTempFile("xmlpatch", ".xml");
            OutputStream outputStream = new FileOutputStream(outputFile);

            Patcher.patch(inputStream, diffStream, outputStream);

            inputStream.close();
            outputStream.close();

            File outputTemp = new File(tempdir, srcfilePath);
            File tempParentDir = outputTemp.getParentFile();

            if (!tempParentDir.exists() && !tempParentDir.mkdirs()) {
                throw new IOException("failed to create directory: " + tempParentDir.getAbsolutePath());
            }

            IoHelper.move(outputFile, outputTemp);
        }
        return outputFilePaths;
    }
}
