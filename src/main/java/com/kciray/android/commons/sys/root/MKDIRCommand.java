package com.kciray.android.commons.sys.root;

import com.kciray.android.commons.gui.ToastUtils;
import com.kciray.android.commons.sys.KLog;

public class MKDIRCommand extends SmartCommand{
    private String dir;

    public MKDIRCommand(String dir) {
        super(0, "mkdir -p '" + dir + "'");
        this.dir = dir;
        result.ok = true;
    }

    @Override
    public void commandOutput(int id, String line) {
        KLog.v(line);
        result.ok = false;
        setNeedRootFlag(true);
        ToastUtils.show(line);
    }

    @Override
    public void commandTerminated(int id, String reason) {

    }

    @Override
    public SmartCommand getCopy() {
        return copyCore(new MKDIRCommand(dir));
    }
}
