package com.kciray.commons.io;

import com.kciray.commons.core.Consumer;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public interface ExFile {
    void setFullPath(String path);

    String getFullPath();

    String getName();

    long getSize();

    String getShortSize();

    boolean isDir();

    @Nullable
    ExFile getParent();

    void getSubDirsAsync(Consumer<List<ExFile>> listFileConsumer);

    File toFile();

    ExFile append(String name);

    @Nullable
    String getPerm();

    void changeName(String newName, Consumer<Boolean> onDone);

    void makeDir(Consumer<Boolean> onDone);

    void loadFileInfo(Consumer<ExFile> consumer);

    void loadDirInfo(Runnable onComplete);

    void getDirSize(Consumer<Long> consumer, Runnable onFail);
}
