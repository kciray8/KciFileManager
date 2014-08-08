package com.kciray.android.commons.sys.root;

import com.kciray.commons.ThreadUtils;
import com.kciray.commons.core.Consumer;
import com.kciray.commons.io.BaseExFile;
import com.kciray.commons.io.ExFile;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

public class ShellFile extends BaseExFile {
    private String parent;
    String fullPath;
    private String name;
    private String permissions;
    private String shortSize;
    private boolean isDirectory;
    private boolean loadInfoDone;

    public ShellFile(String fullPath) {
        name = FilenameUtils.getName(fullPath);
        parent = new File(fullPath).getParent();
        updateFullPath();
    }

    private ShellFile() {
    }

    public static ShellFile buildFromLs(String line, String parentDir) {
        ShellFile file = new ShellFile();
        buildFromLs(line, parentDir, file);
        return file;
    }

    protected static void buildFromLs(String line, String parentDir, ShellFile file) {
        String name = line.substring(67, line.length()).trim();
        if (name.contains("->")) {
            name = name.substring(0, name.indexOf("->") - 1);
        }
        if (name.contains("/")) {
            name = name.substring(0, name.length() - 1);
            file.isDirectory = true;
        }

        if(!name.equals(".") || file.name==null) {
            file.name = name;
        }

        if (!file.name.equals("/")) {
            file.parent = parentDir;
        }
        file.updateFullPath();
        file.permissions = line.substring(0, 10);
        file.shortSize = line.substring(34, 41).trim();
        file.loadInfoDone = true;
    }

    public void updateFullPath() {
        if (parent == null) {
            setFullPath("/");
        } else {
            String addStr = parent.equals("/") ? "" : "/";
            setFullPath(parent + addStr + name);
        }
    }

    private void setInfo(String line, String dir) {

    }

    public void setName(String newName) {
        name = newName;
        updateFullPath();
    }

    @Override
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    @Override
    public String getFullPath() {
        return fullPath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public String getShortSize() {
        loadDataIfNeed();
        return shortSize;
    }

    @Override
    public boolean isDir() {
        loadDataIfNeed();
        return isDirectory;
    }

    private void loadDataIfNeed(){
        if (!loadInfoDone) {
            ThreadUtils.runAndWait((lock)->{
                loadDirInfo(() -> {
                    lock.countDown();
                });
            });
        }
    }

    @Override
    public ExFile getParent() {
        String parentStr = new File(fullPath).getParent();
        if (parentStr != null) {
            return new ShellFile(parentStr);
        } else {
            return null;
        }
    }

    @Override
    public void getSubDirsAsync(Consumer<List<ExFile>> listFileConsumer) {
        LSCommand lsCommand = new LSCommand(fullPath);
        lsCommand.setOnComplete(result -> {
            listFileConsumer.accept(result.listOfFiles);
        });
        FileMgr.getIns().execute(lsCommand);
    }

    @Override
    public File toFile() {
        return new File(fullPath);
    }

    @Override
    public ExFile append(String name) {
        return new ShellFile(fullPath + "/" + name);
    }

    @Override
    public String toString() {
        return fullPath;
    }

    @Override
    public String getPerm() {
        loadDataIfNeed();
        return permissions;
    }

    @Override
    public void changeName(String newName, Consumer<Boolean> onDone) {
        MVCommand mvCommand = new MVCommand(getFullPath(), getParent() + "/" + newName);
        mvCommand.setOnComplete(result -> {
            onDone.accept(result.ok);
        });
        FileMgr.getIns().execute(mvCommand);
    }

    @Override
    public void makeDir(Consumer<Boolean> onDone) {
        MKDIRCommand command = new MKDIRCommand(getFullPath());
        command.setOnComplete(result -> {
            onDone.accept(result.ok);
        });
        FileMgr.getIns().execute(command);
    }

    @Override
    public void loadDirInfo(Runnable onComplete) {
        LSCommand lsCommand = new LSCommand(fullPath);
        lsCommand.setSingleModeFile(this);
        lsCommand.setOnComplete(result -> {
            onComplete.run();
        });
        FileMgr.getIns().execute(lsCommand);
    }

    @Override
    public void loadFileInfo(Consumer<ExFile> consumer) {

    }

    @Override
    public void getDirSize(Consumer<Long> consumer, Runnable onFail) {
        DUCommand command = new DUCommand(fullPath);
        command.setOnComplete(result -> {
            consumer.accept(result.someLong);
        });
        command.setOnDenyRoot(onFail);
        FileMgr.getIns().execute(command);
    }
}
