package com.kciray.android.commons.sys.root;

import com.kciray.android.commons.sys.KLog;

import java.util.ArrayList;


public class LSCommand extends SmartCommand {
    private String path;
    private ShellFile singleModeFile;

    public LSCommand(String path) {
        super(0, "busybox ls -lcahpAe " + path);

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
            if ((!file.getName().equals("."))) {
                if (!file.getName().equals("..")) {
                    if (singleModeFile == null) {
                        result.listOfFiles.add(file);
                    }
                }
            } else {
                if (singleModeFile != null) {
                    String parentPath;
                    if(singleModeFile.getParent() != null){
                        parentPath = singleModeFile.getParent().getFullPath();
                    }else{
                        parentPath = null;
                    }

                    ShellFile.buildFromLs(line, parentPath, singleModeFile);
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
