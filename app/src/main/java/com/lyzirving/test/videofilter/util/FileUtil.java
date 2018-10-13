package com.lyzirving.test.videofilter.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

/**
 * Created by Lai on 2018/5/21 0021.
 */
public class FileUtil {

    /**
     * DICM跟目录
     */
    public static final String DICM_ROOT_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();

    private static final Object sCacheDirLock = new Object();

    public static boolean saveStringToFile(final String str, final String filePathName) {
        boolean result = false;
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter writer = null;
        try {
            File newFile = createNewFile(filePathName, false);
            fileOutputStream = new FileOutputStream(newFile);
            writer = new OutputStreamWriter(fileOutputStream);
            writer.append(str);
            writer.flush();
            result = true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO: handle exception
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e1) {

            }
        }
        return result;
    }

    /**
     *
     * @param path：文件路径
     * @param append：若存在是否插入原文件
     * @return
     */
    public static File createNewFile(String path, boolean append) {
        File newFile = new File(path);
        if (!append) {
            if (newFile.exists()) {
                newFile.delete();
            }
        }
        if (!newFile.exists()) {
            try {
                File parent = newFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                newFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newFile;
    }

    public static String readFileToString(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        } else {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            } else {
                try {
                    InputStream inputStream = new FileInputStream(file);
                    return readInputStream(inputStream, "UTF-8");
                } catch (Exception var3) {
                    var3.printStackTrace();
                    return null;
                }
            }
        }
    }

    public static String readInputStream(InputStream in, String charset) throws IOException {
        if (in == null) {
            return "";
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean var3 = true;

            try {
                byte[] buf = new byte[1024];
                boolean var6 = false;

                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                byte[] data = out.toByteArray();
                String var7 = new String(data, TextUtils.isEmpty(charset) ? "UTF-8" : charset);
                return var7;
            } catch (Exception var11) {
                var11.printStackTrace();
            } finally {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }

            }

            return null;
        }
    }

    /**
     * 判断SD卡是否可用
     * @return SD卡是否可用
     */
    public static boolean isSDCardMounted() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取文件的名称
     * @param pathName
     * @return
     */
    public static String getFileName(String pathName){
        if (TextUtils.isEmpty(pathName)) {
            return null;
        }
        String name = pathName.substring(pathName.lastIndexOf(File.separator) + 1, pathName.length());
        return name;
    }

    /**
     * 获取父文件的路径
     * @param pathName
     * @return
     */
    public static String getParentFilePath(String pathName){
        if (TextUtils.isEmpty(pathName)) {
            return null;
        }
        return (pathName.substring(0, pathName.lastIndexOf(File.separator)));
    }

    // String.format("%.2f", Float.valueOf(size)/1024/1024);
    public static String formetFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.#");// #代表数字
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((float) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((float) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((float) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((float) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            }
            else if (file.isDirectory()) {
                deleteDirWihtFile(file);
            }
        }
    }

    public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    public static boolean copyFiles(File src, File dst) {
        return copyFiles(src, dst, (FileFilter)null);
    }

    public static final FileUtil.FileComparator SIMPLE_COMPARATOR = new FileUtil.FileComparator() {
        public boolean equals(File lhs, File rhs) {
            return lhs.length() == rhs.length() && lhs.lastModified() == rhs.lastModified();
        }
    };

    public static boolean copyFiles(File src, File dst, FileFilter filter) {
        return copyFiles(src, dst, filter, SIMPLE_COMPARATOR);
    }

    public static boolean copyFiles(File src, File dst, FileFilter filter, FileUtil.FileComparator comparator) {
        if(src != null && dst != null) {
            if(!src.exists()) {
                return false;
            } else if(src.isFile()) {
                return performCopyFile(src, dst, filter, comparator);
            } else {
                File[] paths = src.listFiles();
                if(paths == null) {
                    return false;
                } else {
                    boolean result = true;
                    File[] var6 = paths;
                    int var7 = paths.length;

                    for(int var8 = 0; var8 < var7; ++var8) {
                        File sub = var6[var8];
                        if(!copyFiles(sub, new File(dst, sub.getName()), filter)) {
                            result = false;
                        }
                    }

                    return result;
                }
            }
        } else {
            return false;
        }
    }

    public static void copyFile(String srcFilename, String destFilename, boolean overwrite) throws IOException {
        File srcFile = new File(srcFilename);
        if(!srcFile.exists()) {
            throw new FileNotFoundException("Cannot find the source file: " + srcFile.getAbsolutePath());
        } else if(!srcFile.canRead()) {
            throw new IOException("Cannot read the source file: " + srcFile.getAbsolutePath());
        } else {
            File destFile = new File(destFilename);
            if(!overwrite) {
                if(destFile.exists()) {
                    return;
                }
            } else if(destFile.exists()) {
                if(!destFile.canWrite()) {
                    throw new IOException("Cannot write the destination file: " + destFile.getAbsolutePath());
                }
            } else if(!destFile.createNewFile()) {
                throw new IOException("Cannot write the destination file: " + destFile.getAbsolutePath());
            }

            BufferedInputStream inputStream = null;
            BufferedOutputStream outputStream = null;
            byte[] block = new byte[1024];

            try {
                inputStream = new BufferedInputStream(new FileInputStream(srcFile));
                outputStream = new BufferedOutputStream(new FileOutputStream(destFile));

                while(true) {
                    int readLength = inputStream.read(block);
                    if(readLength == -1) {
                        return;
                    }

                    outputStream.write(block, 0, readLength);
                }
            } finally {
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException var17) {
                        ;
                    }
                }

                if(outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException var16) {
                        ;
                    }
                }

            }
        }
    }

    private static boolean performCopyFile(File srcFile, File dstFile, FileFilter filter, FileUtil.FileComparator comparator) {
        if(srcFile != null && dstFile != null) {
            if(filter != null && !filter.accept(srcFile)) {
                return false;
            } else {
                FileChannel inc = null;
                FileChannel ouc = null;

                try {
                    boolean var7;
                    try {
                        boolean e1;
                        if(!srcFile.exists() || !srcFile.isFile()) {
                            e1 = false;
                            return e1;
                        } else {
                            if(dstFile.exists()) {
                                if(comparator != null && comparator.equals(srcFile, dstFile)) {
                                    e1 = true;
                                    return e1;
                                }

                                delete(dstFile);
                            }

                            File e = dstFile.getParentFile();
                            if(e.isFile()) {
                                delete(e);
                            }

                            if(!e.exists() && !e.mkdirs()) {
                                var7 = false;
                                return var7;
                            } else {
                                inc = (new FileInputStream(srcFile)).getChannel();
                                ouc = (new FileOutputStream(dstFile)).getChannel();
                                ouc.transferFrom(inc, 0L, inc.size());
                                return true;
                            }
                        }
                    } catch (Throwable var20) {
                        var20.printStackTrace();
                        delete(dstFile);
                        var7 = false;
                        return var7;
                    }
                } finally {
                    try {
                        if(inc != null) {
                            inc.close();
                        }

                        if(ouc != null) {
                            ouc.close();
                        }
                    } catch (Throwable var19) {
                        ;
                    }

                }
            }
        } else {
            return false;
        }
    }

    public static void delete(File file) {
        delete(file, false);
    }

    public static void delete(File file, boolean ignoreDir) {
        if(file != null && file.exists()) {
            if(file.isFile()) {
                file.delete();
            } else {
                File[] fileList = file.listFiles();
                if(fileList != null) {
                    File[] var3 = fileList;
                    int var4 = fileList.length;

                    for(int var5 = 0; var5 < var4; ++var5) {
                        File f = var3[var5];
                        delete(f, ignoreDir);
                    }

                    if(!ignoreDir) {
                        file.delete();
                    }

                }
            }
        }
    }

    public static String getExternalCacheDir(Context context, String name) {
        return getExternalCacheDir(context, name, false);
    }

    public static String getExternalCacheDir(Context context, String name, boolean persist) {
        String dir = getExternalCacheDir(context, persist);
        if(dir == null) {
            return null;
        } else if(isEmpty(name)) {
            return dir;
        } else {
            File file = new File(dir + File.separator + name);
            if(!file.exists() || !file.isDirectory()) {
                Object var5 = sCacheDirLock;
                synchronized(sCacheDirLock) {
                    if(!file.isDirectory()) {
                        delete(file);
                        file.mkdirs();
                    } else if(!file.exists()) {
                        file.mkdirs();
                    }
                }
            }

            return file.getAbsolutePath();
        }
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    private static String getExternalCacheDir(Context context, boolean persist) {
        if(!isExternalAvailable()) {
            return null;
        } else {
            File externalDir = !persist?FileUtil.InnerEnvironment.getExternalCacheDir(context, false):FileUtil.InnerEnvironment.getExternalFilesDir(context, "cache", false);
            return externalDir == null?null:externalDir.getAbsolutePath();
        }
    }

    private static volatile String mSdcardState;

    public static boolean isExternalAvailable() {
        String state = null;
        if(state == null) {
            state = Environment.getExternalStorageState();
            mSdcardState = state;
        }

        return "mounted".equals(state);
    }

    public interface FileComparator {
        boolean equals(File var1, File var2);
    }

    static class InnerEnvironment {
        private static final String TAG = "InnerEnvironment";
        private static final String EXTEND_SUFFIX = "-ext";
        private static final File EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");

        InnerEnvironment() {
        }

        public static File getExternalStorageAndroidDataDir() {
            return EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY;
        }

        public static File getExternalStorageAppCacheDirectory(String packageName) {
            return new File(new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY, packageName), "cache");
        }

        public static File getExternalStorageAppFilesDirectory(String packageName) {
            return new File(new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY, packageName), "files");
        }

        public static File getExternalCacheDir(Context context, boolean extend) {
            if(!extend && Build.VERSION.SDK_INT >= 8) {
                return context.getExternalCacheDir();
            } else {
                Class var2 = FileUtil.InnerEnvironment.class;
                synchronized(FileUtil.InnerEnvironment.class) {
                    File externalCacheDir = getExternalStorageAppCacheDirectory(context.getPackageName() + (extend?"-ext":""));
                    if(!externalCacheDir.exists()) {
                        try {
                            (new File(getExternalStorageAndroidDataDir(), ".nomedia")).createNewFile();
                        } catch (IOException var6) {
                            var6.printStackTrace();
                        }

                        if(!externalCacheDir.mkdirs()) {
                            Log.w("InnerEnvironment", "Unable to create external cache directory");
                            return null;
                        }
                    }

                    return externalCacheDir;
                }
            }
        }

        public static File getExternalFilesDir(Context context, String type, boolean extend) {
            if(!extend && Build.VERSION.SDK_INT >= 8) {
                return context.getExternalFilesDir(type);
            } else {
                Class var3 = FileUtil.InnerEnvironment.class;
                synchronized(FileUtil.InnerEnvironment.class) {
                    File externalFilesDir = getExternalStorageAppFilesDirectory(context.getPackageName() + (extend?"-ext":""));
                    if(!externalFilesDir.exists()) {
                        try {
                            (new File(getExternalStorageAndroidDataDir(), ".nomedia")).createNewFile();
                        } catch (IOException var7) {
                            ;
                        }

                        if(!externalFilesDir.mkdirs()) {
                            Log.w("InnerEnvironment", "Unable to create external files directory");
                            return null;
                        }
                    }

                    if(type == null) {
                        return externalFilesDir;
                    } else {
                        File dir = new File(externalFilesDir, type);
                        if(!dir.exists() && !dir.mkdirs()) {
                            Log.w("InnerEnvironment", "Unable to create external media directory " + dir);
                            return null;
                        } else {
                            return dir;
                        }
                    }
                }
            }
        }
    }

}
