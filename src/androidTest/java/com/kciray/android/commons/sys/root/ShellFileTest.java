package com.kciray.android.commons.sys.root;

import android.test.InstrumentationTestCase;

public class ShellFileTest extends InstrumentationTestCase {
    private static final String mockLineData = "drwxrwx--x   25 system   system      4.0K Fri Aug  1 22:30:28 2014 data/";

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testBuild() throws Exception {
        FileMgr.getIns().setRootShell();

        //1)Test build from LS
        ShellFile shellFileData = ShellFile.buildFromLs(mockLineData, "/");
        checkDataFile(shellFileData);

        //2)Test constructor + load (public api)
        ShellFile shellFileData2 = new ShellFile("/data");
        checkDataFile(shellFileData2);

        //Test root
        ShellFile shellFileRoot = new ShellFile("/");
        checkRootFile(shellFileRoot);
    }

    private void checkDataFile(ShellFile file) {
        assertEquals("/", file.getParent().getFullPath());
        assertEquals("/data", file.getFullPath());
        assertTrue(file.isDir());
        assertEquals("drwxrwx--x", file.getPerm());
        assertEquals("data", file.getName());
        assertEquals("4.0K", file.getShortSize());
        assertEquals("/data", file.toString());
    }

    private void checkRootFile(ShellFile file) {
        assertEquals(null, file.getParent());
        assertEquals("/", file.getFullPath());
        assertTrue(file.isDir());
        assertEquals("", file.getName());
        assertEquals("/", file.toString());
    }
}
