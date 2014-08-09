package com.kciray.android.commons.sys.root;

import com.kciray.android.commons.sys.KLog;

import java.util.ArrayList;


public class LSCommand extends SmartCommand {
    private String path;
    private ShellFile singleModeFile;

    public LSCommand(String path) {
        super(0, "busybox ls -lcahpAe '" + path + "'");

        this.path = path;
        result.listOfFiles = new ArrayList<>();
    }

    @Override
    public void commandOutput(int id, String line) {
        KLog.v(line);
        if (line.contains("Permission denied")) {
            setNeedRootFlag(true);
        } else {
            ShellFile file = ShellFile.buildFromLs(line, path);

            if (singleModeFile != null) {
                if (file.getName().equals(".")) {
                    String parentPath;
                    if (singleModeFile.getParent() != null) {
                        parentPath = singleModeFile.getParent().getFullPath();
                    } else {
                        parentPath = null;
                    }
                    ShellFile.buildFromLs(line, parentPath, singleModeFile);
                }
            } else {
                if (!file.getName().equals(".") && !file.getName().equals("..")) {
                    result.listOfFiles.add(file);
                }
            }
        }

    }

    @Override
    public void commandTerminated(int i, String s) {
        KLog.v("term " + s);
    }

    public void setSingleModeFile(ShellFile singleModeFile) {
        this.singleModeFile = singleModeFile;
    }

    @Override
    public SmartCommand getCopy() {
        return copyCore(new LSCommand(path));
    }
}
