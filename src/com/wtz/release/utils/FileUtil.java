package com.wtz.release.utils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {
    private final static String TAG = FileUtil.class.getSimpleName();
    
    private static DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
    private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.0#");

    /**
     * 单位换算
     * 
     * @param size
     *            单位为B
     * @param isInteger
     *            是否返回取整的单位
     * @return 转换后的单位
     */
    public static String formatFileSize(long size, boolean isInteger) {
        DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
        String fileSizeString = "0M";
        if (size < 1024 && size > 0) {
            fileSizeString = size + "B";
        } else if (size < 1024 * 1024) {
            fileSizeString = df.format((double) size / 1024) + "K";
        } else if (size < 1024 * 1024 * 1024) {
            fileSizeString = df.format((double) size / (1024 * 1024)) + "M";
        } else {
            fileSizeString = df.format((double) size / (1024 * 1024 * 1024)) + "G";
        }
        return fileSizeString;
    }

    public static long getFileSize(File file) throws Exception {
        if (file != null && file.exists()) {
            if (file.isFile()) {
                return file.length();
            } else {
                return getDirectorySize(file);
            }
        }
        return 0;
    }

    private static long getDirectorySize(File directory) {
        long totalLength = 0;
        File[] files = directory.listFiles();

        if (files == null || files.length == 0) {
            return totalLength;
        }

        for (File file : files) {
            if (file.exists()) {
                if (file.isFile()) {
                    totalLength += file.length();
                } else {
                    totalLength += getDirectorySize(file);
                }
            }
        }

        return totalLength;
    }
    
    public static void deleteFiles(File[] files) {
        for (File f : files) {
            if (f != null && f.exists()) {
                deleteFile(f);
            }
        }
    }

    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File child : children) {
                boolean success = deleteFile(child);
                if (!success) {
                    return false;
                }
            }
        }

        // delete file or empty folder
        return file.delete();
    }
    
    public static boolean copy(File srcFile, File desFile) {
        boolean result = false;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            if (srcFile == null || !srcFile.exists() || desFile == null) {
                return false;
            }

            File desDir = desFile.getParentFile();
            if (!desDir.exists()) {
                desDir.mkdirs();
            }

            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(desFile);
            byte buffer[] = new byte[4096];
            int readSize = 0;
            while ((readSize = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, readSize);
                fos.flush();
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
        }

        return result;
    }
    

    public static String readFile(String filePath) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        String lineEnd = System.getProperty("line.separator");
        try {
            FileInputStream fin = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fin);
            reader = new BufferedReader(new InputStreamReader(bis));

            String tempRead = reader.readLine();
            while (tempRead != null) {
                builder.append(tempRead);
                builder.append(lineEnd);
                tempRead = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return builder.toString();
    }

    public static void writeStringToFile(String content, String fileName, boolean append) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(fileName, append);
            writer.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static boolean saveFileFromSteam(InputStream inStream, long totalSize, String saveDir, String fileName) {
        boolean result = false;
        FileOutputStream fos = null;
        try {
            File dir = new File(saveDir);
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdirs();
            }

            File target = new File(saveDir, fileName);
            if (target.exists() && target.isFile()) {
                target.delete();
            }

            long currentSize = 0;
            byte buffer[] = new byte[1024 * 8];
            int readSize = 0;
            fos = new FileOutputStream(target);
            while ((readSize = inStream.read(buffer)) > 0) {
                fos.write(buffer, 0, readSize);
                fos.flush();
                currentSize += readSize;
            }

            if (totalSize < 0 || currentSize == totalSize) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
            } catch (Exception e) {
            }
        }

        return result;
    }

    public static void mergeFile(File[] files, File target) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(target, false);
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }
                String content = readFile(file.getAbsolutePath());
                writer.write(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
            }
        }
    }
    
    public static void saveSoFromApk(String apkPath, String soSavePath) {
        String ZIPENTRY_NAME_SEPARATOR = "/";
        File soFile = new File(soSavePath);
        if (!soFile.exists()) {
            soFile.mkdirs();
        }

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(new File(apkPath));
            Enumeration<?> enumeration = zipFile.entries();
            ZipEntry zipEntry = null;
            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                System.out.println("zipEntry name: " + zipEntry.getName());

                String name = zipEntry.getName();
                if (name.endsWith(".so")) {
                    FileOutputStream fos = null;
                    BufferedOutputStream dest = null;
                    InputStream in = null;
                    try {
                        in = zipFile.getInputStream(zipEntry);
                        int index = name.lastIndexOf(ZIPENTRY_NAME_SEPARATOR);
                        String fileName = name.substring(index + 1);
                        String subFolderName = name.substring(0, index);
                        File subFolder = new File(soSavePath + File.separator + subFolderName);
                        if (!subFolder.exists()) {
                            subFolder.mkdirs();
                        }
                        String destPath = subFolder.getAbsolutePath() + File.separator + fileName;
                        File file = new File(destPath);
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();
                        int count;
                        int DATA_BUFFER = 8 * 1024;
                        byte data[] = new byte[DATA_BUFFER];
                        fos = new FileOutputStream(file);
                        dest = new BufferedOutputStream(fos, DATA_BUFFER);
                        while ((count = in.read(data, 0, DATA_BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        // 在循环中及时关闭创建的流对象
                        if (dest != null) {
                            dest.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (dest != null) {
                                dest.close();
                            }
                            if (in != null) {
                                in.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 按文件名升序排列
     */
    public static List<File> ascendOrderByName(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return null;
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        List<File> fileList = new ArrayList<File>();
        for (File f : files) {
            fileList.add(f);
        }
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });

        return fileList;
    }

}
