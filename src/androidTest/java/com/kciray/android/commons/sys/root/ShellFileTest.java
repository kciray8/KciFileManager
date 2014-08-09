package com.kciray.android.commons.sys.root;

import android.test.AndroidTestCase;

import java.io.File;

public class ShellFileTest extends AndroidTestCase {
    // '/'
    private static final String mockLineData =                "drwxrwx--x   25 system   system      4.0K Fri Aug  1 22:30:28 2014 data/";
    // '/storage'
    private static final String mockLineStorageDot =          "d---r-x---    3 root     sdcard_r      80 Fri Aug  8 21:38:25 2014 ./";
    private static final String mockLineStorageDoubleDot =    "drwxr-xr-x   15 root     root           0 Fri Aug  8 21:38:25 2014 ../";

    private static final String tempDirName = "unitTestKciFMTemp";
    File tempDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + tempDirName);
        tempDir.mkdirs();
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

        //TODO add "/proc" unit test
    }

    private void checkDataFile(ShellFile file) {
        assertEquals("/", file.getParent().getFullPath());
        assertEquals("/data", file.getFullPath());
        assertTrue(file.isDir());
        assertEquals("drwxrwx--x", file.getPerm());
        assertEquals("data", file.getName());
        assertEquals(file.getShortName(), file.getName());
        assertEquals("4.0K", file.getShortSize());
        assertEquals("/data", file.toString());
    }

    private void checkRootFile(ShellFile file) {
        assertEquals(null, file.getParent());
        assertEquals("/", file.getFullPath());
        assertTrue(file.isDir());
        assertEquals("", file.getName());
        assertEquals(file.getShortName(), file.getName());
        assertEquals("/", file.toString());
    }

    public void testNewFolder() {
        ShellFile fileWithSpaces = new ShellFile(tempDir + File.separator + "abc");
        fileWithSpaces.makeDir(ok->{});
        assertTrue(fileWithSpaces.isDir());
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        //TODO  - fix
        //FileUtils.deleteDirectory(tempDir);
    }
}
