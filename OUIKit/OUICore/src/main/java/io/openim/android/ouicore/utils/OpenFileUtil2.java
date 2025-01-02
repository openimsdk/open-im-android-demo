//package io.openim.android.ouicore.utils;
//
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Build;
//
//import androidx.core.content.FileProvider;
//
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Locale;
//
//import io.openim.android.ouicore.base.BaseApp;
//
//public class OpenFileUtil2 {
//    public static Intent openFile(String filePath) {
//        File file = new File(filePath);
//        if (!file.exists())
//            return null;
//        /* 取得扩展名 */
//        String end = GetFilePathFromUri.getFileSuffix(filePath);
//
//        /* 依扩展名的类型决定MimeType */
//        if (end.equals("m4a") || end.equals("mp3")
//            || end.equals("mid") || end.equals("xmf")
//            || end.equals("ogg") || end.equals("wav")) {
//            return getAudioFileIntent(filePath);
//        } else if (end.equals("3gp") || end.equals("mp4")) {
//            return getVideoFileIntent(filePath);
//        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals(
//            "jpeg") || end.equals("bmp")) {
//            return getImageFileIntent(filePath);
//        } else if (end.equals("apk")) {
//            return getApkFileIntent(filePath);
//        } else if (end.equals("ppt")) {
//            return getPptFileIntent(filePath);
//        } else if (end.equals("xls")) {
//            return getExcelFileIntent(filePath);
//        } else if (end.equals("doc")) {
//            return getWordFileIntent(filePath);
//        } else if (end.equals("pdf")) {
//            return getPdfFileIntent(filePath);
//        } else if (end.equals("chm")) {
//            return getChmFileIntent(filePath);
//        } else if (end.equals("txt")) {
//            return getTextFileIntent(filePath, false);
//        } else {
//            return getAllIntent(filePath);
//        }
//    }
//
//    // Android获取一个用于打开APK文件的intent
//    public static Intent getAllIntent(String param) {
//        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction(Intent.ACTION_VIEW);
//        Uri uri = FileProvider.getUriForFile(BaseApp.inst(), BaseApp.inst().getPackageName() +
//            ".fileProvider", new File(param));
//        intent.setDataAndType(uri, "*/*");
//        return intent;
//    }
//
//    // Android获取一个用于打开APK文件的intent
//    public static Intent getApkFileIntent(String param) {
//        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction(Intent.ACTION_VIEW);
//        Uri uri = FileProvider.getUriForFile(BaseApp.inst(), BaseApp.inst().getPackageName() +
//            ".fileProvider", new File(param));
//        intent.setDataAndType(uri, "application/vnd.android.package-archive");
//        return intent;
//    }
//
//    // Android获取一个用于打开VIDEO文件的intent
//    public static Intent getVideoFileIntent(String param) {
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("oneshot", 0);
//        intent.putExtra("configchange", 0);
//        Uri uri = FileProvider.getUriForFile(BaseApp.inst(), BaseApp.inst().getPackageName() +
//            ".fileProvider", new File(param));
//        intent.setDataAndType(uri, "video/*");
//        return intent;
//    }
//
//    // Android获取一个用于打开AUDIO文件的intent
//    public static Intent getAudioFileIntent(String param) {
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("oneshot", 0);
//        intent.putExtra("configchange", 0);
//        Uri uri = FileProvider.getUriForFile(BaseApp.inst(), BaseApp.inst().getPackageName() +
//            ".fileProvider", new File(param));
//        intent.setDataAndType(uri, "audio/*");
//        return intent;
//    }
//
//    // Android获取一个用于打开Html文件的intent
//    public static Intent getHtmlFileIntent(String param) {
//        Uri uri =
//            Uri.parse(param).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme(
//                "content").encodedPath(param).build();
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.setDataAndType(uri, "text/html");
//        return intent;
//    }
//
//    // Android获取一个用于打开图片文件的intent
//    public static Intent getImageFileIntent(String param) {
//
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addCategory("android.intent.category.DEFAULT");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Uri uri = FileProvider.getUriForFile(BaseApp.inst(), BaseApp.inst().getPackageName() +
//            ".fileProvider", new File(param));
//        intent.setDataAndType(uri, "image/*");
//        return intent;
//    }
//
//    // Android获取一个用于打开PPT文件的intent
//    public static Intent getPptFileIntent(String param) {
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addCategory("android.intent.category.DEFAULT");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Uri uri = FileProvider.getUriForFile(BaseApp.inst(), BaseApp.inst().getPackageName() +
//            ".fileProvider", new File(param));
//        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
//        return intent;
//    }
//
//    // Android获取一个用于打开Excel文件的intent
//    public static Intent getExcelFileIntent(String param) {
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addCategory("android.intent.category.DEFAULT");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Uri uri = FileProvider.getUriForFile(BaseApp.inst(), BaseApp.inst().getPackageName() +
//            ".fileProvider", new File(param));
//        intent.setDataAndType(uri, "application/vnd.ms-excel");
//        return intent;
//    }
//
//    // Android获取一个用于打开Word文件的intent
//    public static Intent getWordFileIntent(String param) {
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addCategory("android.intent.category.DEFAULT");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Uri uri = FileProvider.getUriForFile(BaseApp.inst(), BaseApp.inst().getPackageName() +
//            ".fileProvider", new File(param));
//        intent.setDataAndType(uri, "application/msword");
//        return intent;
//    }
//
//    // Android获取一个用于打开CHM文件的intent
//    public static Intent getChmFileIntent(String param) {
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addCategory("android.intent.category.DEFAULT");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Uri uri = FileProvider.getUriForFile(BaseApp.inst(), BaseApp.inst().getPackageName() +
//            ".fileProvider", new File(param));
//        intent.setDataAndType(uri, "application/x-chm");
//        return intent;
//    }
//
//    // Android获取一个用于打开文本文件的intent
//    public static Intent getTextFileIntent(String param, boolean paramBoolean) {
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addCategory("android.intent.category.DEFAULT");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Uri uri = getUriForFile(new File(param));
//        intent.setDataAndTypeAndNormalize(uri,"text/plain");
//        intent.setDataAndType(uri, "text/plain");
//        return intent;
//    }
//
//    /**
//     * 获取Android 7.0 以上的文件映射
//     *
//     * @param file 文件对象
//     * @return 映射 uri
//     */
//    public static Uri getUriForFile(File file) {
//        Uri uri;
//        if (Build.VERSION.SDK_INT >= 24) {
//            uri = FileProvider.getUriForFile(BaseApp.inst(),
//                BaseApp.inst().getPackageName() + ".fileProvider", file);
//        } else {
//            uri = Uri.fromFile(file);
//        }
//        return uri;
//    }
//
//
//    // Android获取一个用于打开PDF文件的intent
//    public static Intent getPdfFileIntent(String param) {
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addCategory("android.intent.category.DEFAULT");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Uri uri = FileProvider.getUriForFile(BaseApp.inst(), BaseApp.inst().getPackageName() +
//            ".fileProvider", new File(param));
//        intent.setDataAndType(uri, "application/pdf");
//        return intent;
//    }
//
//}
