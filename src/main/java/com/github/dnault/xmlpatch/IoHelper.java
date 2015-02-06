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

import java.io.*;

public class IoHelper {
    public static void move(File from, File to) throws IOException {
        if (!from.renameTo(to)) {
            copy(from, to);
            if (!from.delete()) {
                if (!to.delete()) {
                    throw new IOException("failed to delete " + from + " and " + to);
                }
                throw new IOException("failed to delete " + from);
            }
        }
    }

    public static void copy(File from, File to) throws IOException {
        final OutputStream os = new FileOutputStream(to);
        final InputStream is = new FileInputStream(from);
        final byte[] buffer = new byte[8000];

        int count;
        while ((count = is.read(buffer)) != -1) {
            os.write(buffer, 0, count);
        }

        os.close();
        is.close();
    }

    public static File createTempDir() throws IOException {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = "xmlpatch-" + System.currentTimeMillis() + "-";

        for (int i = 0; i < 5000; i++) {
            File tempDir = new File(baseDir, baseName + i);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IOException("failed to create temp directory in " + baseDir.getAbsolutePath());
    }

    public static void makeParentDirectory(File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("failed to make directory: " + parent.getAbsolutePath());
            }
        } else if (!parent.isDirectory()) {
            throw new IOException("not a directory: " + parent.getAbsolutePath());
        }
    }
}
