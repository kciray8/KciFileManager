package com.kciray.android.commons.sys.root;

import com.kciray.android.commons.gui.ToastUtils;

public class MVCommand extends SmartCommand {
    private final String fromFile;
    private final String toFile;

    public MVCommand(String fromFile, String toFile) {
        super(0, "mv " + fromFile + " " + toFile);
        this.fromFile = fromFile;
        this.toFile = toFile;
        result.ok = true;
    }

    @Override
    public void commandOutput(int id, String line) {
        result.ok = false;
        ToastUtils.show(line);
    }

    @Override
    public void commandTerminated(int id, String reason) {

    }

    @Override
    public SmartCommand getCopy() {
        return copyCore(new MVCommand(fromFile, toFile));
    }
}
