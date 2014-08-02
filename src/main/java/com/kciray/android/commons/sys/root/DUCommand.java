package com.kciray.android.commons.sys.root;

import com.kciray.android.commons.sys.KLog;
import com.kciray.commons.core.RegExp;

public class DUCommand extends SmartCommand {
    private String dir;

    public DUCommand(String dir) {
        super(0, "busybox du -s " + dir);
        this.dir = dir;
    }

    @Override
    public void commandOutput(int id, String line) {
        KLog.v(line);
        if (line.contains("Permission denied")) {
            setNeedRootFlag(true);
        } else {
            result.someLong = Long.valueOf(RegExp.findOne("\\d+", line));
        }
    }

    @Override
    public void commandTerminated(int id, String reason) {

    }

    @Override
    public SmartCommand getCopy() {
        return copyCore(new DUCommand(dir));
    }
}
