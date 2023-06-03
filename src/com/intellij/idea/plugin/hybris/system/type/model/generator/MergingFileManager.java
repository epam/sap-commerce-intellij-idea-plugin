/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * XSD/DTD Model generator tool
 *
 * By Gregory Shrago
 * 2002 - 2006
 */
package com.intellij.idea.plugin.hybris.system.type.model.generator;

import com.intellij.util.ArrayUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class MergingFileManager implements FileManager {

    public File getOutputFile(File target) {
        File outFile = target;
        if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
            Util.logerr("parent mkdirs failed: " + outFile);
            return null;
        }
        for (int i = 0; outFile.exists(); i++) {
            outFile = new File(target.getParentFile(), target.getName() + ".tmp." + i);
        }
        return outFile;
    }

    public File releaseOutputFile(File outFile) {
        int idx = outFile.getName().indexOf(".tmp.");
        File target = outFile;
        if (idx > -1) {
            target = new File(outFile.getParentFile(), outFile.getName().substring(0, idx));
            String[] curLines = loadFile(outFile);
            String[] prevLines = loadFile(target);
            String[] mergedLines = mergeLines(curLines, prevLines);
            if (mergedLines != prevLines) {
                if (mergedLines == curLines) {
                    if (target.exists() && !target.delete()) {
                        Util.logerr("file replace failed: " + target);
                        outFile.delete();
                    } else {
                        outFile.renameTo(target);
                        Util.logwarn("file replaced: " + target);
                    }
                } else {
                    outFile.delete();
                    if (target.exists() && !target.delete()) {
                        Util.logerr("file replace failed: " + target);
                    } else {
                        writeFile(target, mergedLines);
                        Util.logwarn("file merged: " + target);
                    }
                }
            } else {
                outFile.delete();
            }
        }
        return target;
    }

    private static String[] mergeLines(String[] curLines, String[] prevLines) {
        if (prevLines.length == 0) {
            return curLines;
        }
        ArrayList<String> merged = new ArrayList<String>();
        int curIdx = 0, prevIdx = 0;
        String cur, prev;
        boolean classScope = false;
        boolean importMerged = false;
        for (int i = 0; i < Math.max(curLines.length, prevLines.length); i++) {
            cur = curIdx < curLines.length ? curLines[curIdx] : "";
            prev = prevIdx < prevLines.length ? prevLines[prevIdx] : "";
            if (classScope) {
                merged.addAll(Arrays.asList(curLines).subList(curIdx, curLines.length));
                break;
            } else if (prev.trim().startsWith("import ") || cur.trim().startsWith("import ")) {
                if (importMerged) {
                    continue;
                }
                importMerged = true;
                int[] indices = new int[]{curIdx, prevIdx};
                mergeImports(merged, curLines, prevLines, indices);
                curIdx = indices[0];
                prevIdx = indices[1];
            } else if (cur.equals(prev)) {
                if (cur.trim().startsWith("public interface ")
                    || cur.trim().startsWith("public enum ")) {
                    classScope = true;
                }
                merged.add(cur);
                curIdx++;
                prevIdx++;
            } else if (prev.trim().startsWith("@")) {
                merged.add(prev);
                prevIdx++;
            } else if (cur.trim().startsWith("@")) {
                merged.add(cur);
                curIdx++;
            } else if (cur.trim().startsWith("package  ") && prev.trim().startsWith("package ")) {
                merged.add(prev);
                curIdx++;
                prevIdx++;
            } else if (cur.trim().startsWith("public interface ") && prev.trim().startsWith("public interface ")) {
                classScope = true;
                prevIdx = addAllStringsUpTo(merged, prevLines, prevIdx, "{");
                curIdx = addAllStringsUpTo(null, curLines, curIdx, "{");
            } else if (cur.trim().startsWith("* ")) {
                curIdx = addAllStringsUpTo(merged, curLines, curIdx, "*/");
                if (prev.trim().startsWith("* ") || prev.trim().endsWith("*/")) {
                    prevIdx = addAllStringsUpTo(null, prevLines, prevIdx, "*/");
                }
            } else {
                merged.add(cur);
                curIdx++;
                prevIdx++;
            }
        }
        String[] mergedLines = ArrayUtil.toStringArray(merged);
        if (compareLines(mergedLines, prevLines, 2) == 0) {
            return prevLines;
        } else if (compareLines(mergedLines, curLines, 2) == 0) {
            return curLines;
        } else {
            return mergedLines;
        }
    }

    private static void mergeImports(ArrayList<String> merged, String[] curLines, String[] prevLines, int[] indices) {
        TreeSet<String> externalClasses = new TreeSet<String>();
        for (int i = 0; i < curLines.length; i++) {
            String line = curLines[i].trim();
            if (line.startsWith("import ") && line.endsWith(";")) {
                indices[0] = i + 1;
                final String name = line.substring("import ".length(), line.length() - 1).trim();
                if (name.endsWith("*")) {
                    continue;
                }
                externalClasses.add(name);
            }
        }
        for (int i = 0; i < prevLines.length; i++) {
            String line = prevLines[i].trim();
            if (line.startsWith("import ") && line.endsWith(";")) {
                indices[1] = i + 1;
                final String name = line.substring("import ".length(), line.length() - 1).trim();
                if (name.endsWith("*")) {
                    continue;
                }
                externalClasses.add(name);
            }
        }
        boolean javaLang = false;
        for (String s : externalClasses) {
            if (s.startsWith("java.")) {
                javaLang = true;
                continue;
            }
            merged.add("import " + s + ";");
        }
        if (javaLang) {
            merged.add("");
            for (String s : externalClasses) {
                if (!s.startsWith("java.")) {
                    continue;
                }
                merged.add("import " + s + ";");
            }
        }
    }

    private static int addAllStringsUpTo(ArrayList<String> merged, String[] lines, int startIdx, String upTo) {
        String str;
        do {
            str = startIdx < lines.length ? lines[startIdx] : upTo;
            if (merged != null) {
                merged.add(str);
            }
            startIdx++;
        } while (!str.trim().endsWith(upTo) && startIdx < lines.length);
        return startIdx;
    }

    private static int compareLines(String[] mergedLines, String[] curLines, int start) {
        if (mergedLines.length < curLines.length) {
            return -1;
        }
        if (mergedLines.length > curLines.length) {
            return 1;
        }
        for (int i = start; i < mergedLines.length; i++) {
            final int comp = mergedLines[i].compareTo(curLines[i]);
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    }


    private static void writeFile(File target, String[] mergedLines) {
        PrintWriter out = null;
        try {
            int lineCount = mergedLines.length;
            while (lineCount > 0 && mergedLines[lineCount - 1].length() == 0) {
                lineCount--;
            }
            out = new PrintWriter(new FileWriter(target));
            for (int i = 0; i < lineCount; i++) {
                String mergedLine = mergedLines[i];
                out.println(mergedLine);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }


    private static String[] loadFile(File f1) {
        if (!f1.exists()) {
            return ArrayUtil.EMPTY_STRING_ARRAY;
        }
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(f1));
            String str;
            while ((str = in.readLine()) != null) {
                list.add(str);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return ArrayUtil.toStringArray(list);
    }

}
