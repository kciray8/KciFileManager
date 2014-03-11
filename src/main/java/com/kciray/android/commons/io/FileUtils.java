package com.kciray.android.commons.io;

import java.io.File;
import java.io.IOException;

public class FileUtils extends org.apache.commons.io.FileUtils{

    /**
     * Extends apache FileUtils.sizeOfDirectory, because it work not fine with "/"
     * @param directory
     * @return
     */
    public static long sizeOfDirectory(File directory) {
        if(directory.getAbsolutePath().equals("/proc")){
            return 0;
        }

        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return 0L;
        }
        long size = 0;

        for (final File file : files) {
            try {
                if (!isSymlink(file)) {
                    size += sizeOf(file);
                    if (size < 0) {
                        break;
                    }
                }
            } catch (IOException ioe) {
                // Ignore exceptions caught when asking if a File is a symlink.
            }
        }

        return size;
    }

    public static long sizeOf(File file) {

        if (!file.exists()) {
            String message = file + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (file.isDirectory()) {
            return sizeOfDirectory(file);
        } else {
            return file.length();
        }

    }
}