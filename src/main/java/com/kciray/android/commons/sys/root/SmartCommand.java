package com.kciray.android.commons.sys.root;

import com.kciray.commons.io.ExFile;
import com.stericson.RootTools.execution.Command;

import java.util.List;

public abstract class SmartCommand extends Command{
    protected Result result = new Result();
    private Runnable onNeedRoot;
    private Runnable onDenyRoot;
    private boolean needRootFlag;
    protected OnComplete onCompleteFunc;

    public void onDenyRoot() {
        if(onDenyRoot != null){
            onDenyRoot.run();
        }
    }

    public void setOnDenyRoot(Runnable onDenyRoot) {
        this.onDenyRoot = onDenyRoot;
    }

    public static class Result {
        boolean ok;
        long someLong;
        List<ExFile> listOfFiles;
        public ExFile file;
    }

    public abstract SmartCommand getCopy();

    protected SmartCommand copyCore(SmartCommand res){
        res.onNeedRoot = onNeedRoot;
        res.onCompleteFunc = onCompleteFunc;
        res.onDenyRoot = onDenyRoot;
        return res;
    }

    public static interface OnComplete {
        public void onComplete(Result result);
    }

    public void setOnComplete(OnComplete onCompleteFunc) {
        this.onCompleteFunc = onCompleteFunc;
    }

    public SmartCommand(int id, String command) {
        super(id, command);
    }

    public static interface OnNeedRoot {
        public void onNeedRoot(Command command);
    }

    public void setOnNeedRoot(Runnable onNeedRoot) {
        this.onNeedRoot = onNeedRoot;
    }

    public void setNeedRootFlag(boolean needRootFlag) {
        this.needRootFlag = needRootFlag;
    }


    @Override
    public void commandCompleted(int i, int i2) {
        if (needRootFlag) {
            if (onNeedRoot != null) {
                onNeedRoot.run();
            }
            return;
        }

        if (onCompleteFunc != null) {
            onCompleteFunc.onComplete(result);
        }
    }
}
