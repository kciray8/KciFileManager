package com.kciray.android.commons.sys.root;

import com.kciray.commons.io.ExFile;
import com.kciray.commons.io.StdFile;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class FileMgr {
    private static FileMgr instance = new FileMgr();

    public static FileMgr getIns() {
        return instance;
    }

    @Nullable
    Shell shell;//If null - use Java API

    private FileMgr() {
    }

    public static boolean busyboxAndRootAvailable() {
        return (RootTools.isRootAvailable() && RootTools.isBusyboxAvailable());
    }

    public void setRootShell() throws RootDeniedException {
        try {
            shell = RootTools.getShell(true);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void execute(SmartCommand command) {
        SmartCommand copyCommand = command.getCopy();

        try {
            if (shell != null) {
                command.setOnNeedRoot(() -> {
                    try {
                        setRootShell();
                        shell.add(copyCommand);
                    } catch (RootDeniedException e) {
                        copyCommand.onDenyRoot();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                shell.add(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean nullShell() {
        return shell == null;
    }

    public static ExFile getFile(String absPath) {
        if (getIns().shell != null) {
            return new ShellFile(absPath);
        } else {
            return new StdFile(absPath);
        }
    }

    public static ExFile getFile(ExFile dir, String name) {
        return getFile(dir.getFullPath()).append(name);
    }

    public void setPrefEngine(Integer integer) {
        boolean prefShell = false;
        if (integer == 1) {
            prefShell = true;
        }

        if (RootTools.isRootAvailable() && RootTools.isBusyboxAvailable()) {
            try {
                if (prefShell) {
                    shell = RootTools.getShell(false);
                } else {
                    shell = null;
                }
            } catch (IOException | TimeoutException | RootDeniedException e) {
                e.printStackTrace();
            }
        }
    }
}