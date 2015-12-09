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

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;

public class CommandLineDriver {

    public static void main(String... args) throws Exception {

        if (args.length == 0) {
            usage();
            System.exit(0);
        }

        if (args.length != 3) {
            System.err.println("incorrect number of arguments");
            usage();
            System.exit(1);
        }

        String input = args[0];
        String patch = args[1];
        String output = args[2];

        if (input.equals("-") && patch.equals("-")) {
            System.err.println("input and patch may not both come from standard input");
            System.exit(1);
        }

        boolean patchInPlace = !input.equals("-") && isSameFile(input, output);

        if (patchInPlace) {
            output = File.createTempFile("xmlpatch", ".xml").getAbsolutePath();
        }

        try {
            InputStream inputStream = input.equals("-") ? System.in : new FileInputStream(input);
            InputStream patchStream = patch.equals("-") ? System.in : new FileInputStream(patch);
            OutputStream outputStream = output.equals("-") ? System.out : new FileOutputStream(output);

            Patcher.patch(inputStream, patchStream, outputStream);

            if (patchInPlace) {
                if (!new File(output).renameTo(new File(input))) {
                    throw new IOException("could not rename temp file to " + input);
                }
            }
        }
        catch (FileNotFoundException e) {
            System.err.println("ERROR: Could not access file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static boolean isSameFile(String pathA, String pathB) throws IOException {
        return new File(pathA).getCanonicalPath().equals(new File(pathB).getCanonicalPath());
    }

    private static void usage() {
        System.err.println("USAGE: java -jar xml-patch.jar <input file> <patch file> <output file>");
        System.err.println("  A dash (-) may be used to indicate standard input / output");
        System.err.println("  The patch is an XML diff document as defined by RFC 5261");
        System.err.println("  or an XML patch document as defined by RFC 7351");
        System.exit(1);
    }
}
