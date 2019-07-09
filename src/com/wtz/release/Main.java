package com.wtz.release;

import com.wtz.release.utils.CommandUtil;
import com.wtz.release.utils.FileUtil;
import com.wtz.release.utils.Md5Util;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        int needPramCount = 1;
        if (args == null || args.length < needPramCount) {
            System.out.println("Missing parameters!");
            System.out.println("Usage: java -jar ReleaseInfo.jar target_file_path");
            System.out.println("Note: You should execute in the root path of git project");
            return;
        }

        File targetFile = new File(args[0]);
        if (!targetFile.exists() || targetFile.isDirectory()) {
            System.out.println("targetFile is not exist:\n" + args[0]);
            return;
        }

        String md5 = Md5Util.getFileMD5(targetFile);

        File execDir = new File(".");
        File outInfo = new File(execDir, "ReleaseInfo.txt");

        StringBuilder builder = new StringBuilder();
        builder.append("[file name]:");
        builder.append(targetFile.getName());
        builder.append("\n");

        builder.append("[md5]:");
        builder.append(md5);
        builder.append("\n");

        builder.append("[git addr]:");
        builder.append(getRemoteAddress());
        builder.append("\n");

        builder.append("[git branch]:");
        builder.append(getBranch());
        builder.append("\n");

        builder.append("[git commit id]:");
        builder.append(getSha());
        builder.append("\n");

//        System.out.println("result:\n " + builder.toString());
        FileUtil.writeStringToFile(builder.toString(), outInfo.getAbsolutePath(), false);
    }

    private static String getRemoteAddress() {
        File execDir = new File(".");
        String cmd = "git remote -v";
        String addr = CommandUtil.executeCommand(cmd, execDir);
        String keyStart = "origin";
        String keyEnd = "(fetch)";
        if (addr != null && addr.contains(keyStart) && addr.contains(keyEnd)) {
            int start = addr.indexOf(keyStart);
            start = start + keyStart.length();
            int end = addr.indexOf(keyEnd);
            return addr.substring(start, end).trim();
        }
        return "";
    }

    private static String getBranch() {
        File execDir = new File(".");
        String cmd = "git symbolic-ref --short -q HEAD";
        String ret = CommandUtil.executeCommand(cmd, execDir);
        if (ret != null && ret.endsWith("\n")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        if (ret == null || ret.trim().equals("")) {
            System.out.println("Note: You should execute in the root path of git project");
        }
        return ret;
    }

    private static String getSha() {
        File execDir = new File(".");
        String cmd = "git rev-parse HEAD";
        String ret = CommandUtil.executeCommand(cmd, execDir);
        if (ret != null && ret.endsWith("\n")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        if (ret == null || ret.trim().equals("")) {
            System.out.println("Note: You should execute in the root path of git project");
        }
        return ret;
    }

}
