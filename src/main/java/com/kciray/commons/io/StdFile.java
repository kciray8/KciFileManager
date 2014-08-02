package com.kciray.commons.io;

import com.kciray.commons.ThreadUtils;
import com.kciray.commons.core.Consumer;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StdFile extends BaseExFile {
    private File file;

    public StdFile(String path) {
        setFullPath(path);
    }

    public StdFile() {

    }

    @Override
    public void setFullPath(String path) {
        file = new File(path);
    }

    @Override
    public String getFullPath() {
        return file.getAbsolutePath();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public String getShortSize() {
        return FileUtils.byteCountToDisplaySize(file.length());
    }

    @Override
    public boolean isDir() {
        return file.isDirectory();
    }

    @Override
    public ExFile getParent() {
        String parentStr = file.getParent();
        if (parentStr != null) {
            return new StdFile(parentStr);
        } else {
            return null;
        }
    }

    private List<ExFile> getSubDirsSync() {
        List<ExFile> list = new ArrayList<>();
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        for (File someFile : files) {
            StdFile stdFile = new StdFile();
            stdFile.setFullPath(someFile.getAbsolutePath());
            list.add(stdFile);
        }
        return list;
    }

    @Override
    public void getSubDirsAsync(Consumer<List<ExFile>> listFileConsumer) {
        ThreadUtils.run(() -> {
            listFileConsumer.accept(getSubDirsSync());
        });
    }

    @Override
    public File toFile() {
        return file;
    }

    @Override
    public StdFile append(String name) {
        StdFile stdFile = new StdFile();
        File newFile = new File(file, name);
        stdFile.setFullPath(newFile.getAbsolutePath());

        return stdFile;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    @Override
    public String getPerm() {
        return null;
    }

    @Override
    public void changeName(String newName, Consumer<Boolean> onDone) {
        ThreadUtils.run(() -> {
            onDone.accept(file.renameTo(new File(file.getParent(), newName)));
        });
    }

    @Override
    public void makeDir(Consumer<Boolean> onDone) {
        ThreadUtils.run(() -> {
            onDone.accept(file.mkdirs());
        });
    }

    @Override
    public void loadFileInfo(Consumer<ExFile> consumer) {
        consumer.accept(this);
    }

    @Override
    public void loadDirInfo(Runnable onComplete) {
        onComplete.run();
    }

    @Override
    public void getDirSize(Consumer<Long> consumer, Runnable onFail) {
        ThreadUtils.run(() -> {
            consumer.accept(FileUtils.sizeOfDirectory(file));
        });
    }
}
