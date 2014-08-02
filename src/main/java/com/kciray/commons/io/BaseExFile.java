package com.kciray.commons.io;

public abstract class BaseExFile implements ExFile{
    @Override
    public boolean equals(Object o) {
        if(o instanceof ExFile){
            ExFile exFile = (ExFile) o;
            return getFullPath().equals(exFile.getFullPath());
        }
        return super.equals(o);
    }
}
