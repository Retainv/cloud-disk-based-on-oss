package top.retain.nd.util;

import com.aliyun.oss.common.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Retain
 * @date 2021/10/18 15:11
 */
public class FileUtils {

//    public static void main(String[] args) {
//        List<String> dirFiles = getDirFiles("C:\\Users\\Retain\\Desktop\\upload");
//        dirFiles.forEach(System.out::println);
//    }

    /**
     * 获取文件夹所有文件
     * @param file 文件夹本地路径
     * @return 所有文件path
     */
    public static List<String> getDirFiles(File file) {
        List<String> files = null;
        if (file.isFile()) {
            System.out.println("您输入的是文件路径，请重新输入！");
        } else if (!file.exists()) {
            System.out.println("您输入的文件夹不存在，请重新输入");
        }else {
            try {
                files = getFiles(file, new ArrayList<>());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return files;
    }

    private static List<String> getFiles(File dir, List<String> files) throws IOException {
        File[] subFiles = dir.listFiles();
        assert subFiles != null;
        for (File f1 : subFiles) {
            if (f1.isFile()){
                files.add(f1.getCanonicalPath());
            }else if (f1.isDirectory()) {
                // 空文件夹直接返回
                if (Objects.requireNonNull(f1.listFiles()).length == 0) {
                    files.add(f1.getCanonicalPath());
                }else {
                    getFiles(f1, files);
                }
            }
        }
        return files;
    }

    public static Boolean isDir(String name) {
        if (StringUtils.isNullOrEmpty(name)) {
            return false;
        }
        if (name.lastIndexOf('.') > 0) {
            return false;
        }
        return true;
    }
    public static Boolean hasDir(String name) {
        if (StringUtils.isNullOrEmpty(name)) {
            return false;
        }
        if (name.lastIndexOf('/') > 0) {
            return true;
        }
        return false;
    }
    public static String[] seperate(String ossPath) {
        if (StringUtils.isNullOrEmpty(ossPath)) {
            return null;
        }
        String[] split = ossPath.split("/");
        return split;
    }


    // 文件大小转换
    public static String getFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else if (fileS < 1099511627776L){
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        } else {
            fileSizeString = df.format((double) fileS / 1099511627776L) + "TB";
        }
        return fileSizeString;
    }

}
