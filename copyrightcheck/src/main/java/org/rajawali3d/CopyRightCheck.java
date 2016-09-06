/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author Ian Thomas (toxicbakery@gmail.com)
 */
public class CopyRightCheck {

    private static String LICENSE = "LICENSE.txt";
    private static String BASE_DIR = "./rajawali/src";

    private final File copyrightFile;
    private final File baseDirFile;

    public static void main(String[] args) throws Exception {
        new CopyRightCheck();
    }

    public CopyRightCheck() throws Exception {
        baseDirFile = new File(BASE_DIR);
        copyrightFile = new File(LICENSE);

        if (!baseDirFile.exists())
            throw new RuntimeException("Base directory is not available.");

        if (!copyrightFile.exists())
            throw new RuntimeException("License file not found.");

        // Read the license
        final StringBuilder license = new StringBuilder();
        final BufferedReader br = new BufferedReader(new FileReader(copyrightFile));

        String line;
        while ((line = br.readLine()) != null) {
            license.append(line);
        }

        br.close();

        System.out.println("Starting check.");

        // Validate all source files
        validateDirectory(baseDirFile, license);

        System.out.println("Check finished.");
    }

    private static void validateDirectory(File file, StringBuilder license) throws Exception {
        final File[] files = file.listFiles();
        for (File dirFile : files) {
            if (dirFile.isDirectory()) {
                validateDirectory(dirFile, license);
                continue;
            }

            if (!dirFile.getName().endsWith(".java")) {
                System.out.println("Skipping non Java file: "
                                   + dirFile.getName());
                continue;
            }

            BufferedReader br = new BufferedReader(new FileReader(dirFile));
            try {
                String line;
                for (int i = 0, j = license.length(); i < j;) {
                    line = br.readLine();
                    if (!license.substring(i, i + line.length()).equals(line)) {
                        printFileError(dirFile);
                        break;
                    }
                    i += line.length();
                }
            } catch (Exception e) {
                printFileError(dirFile);
            }
            br.close();
        }
    }

    private static void printFileError(File file) {
        System.out.println("File header did not match copyright: " + file.getName());
    }
}
