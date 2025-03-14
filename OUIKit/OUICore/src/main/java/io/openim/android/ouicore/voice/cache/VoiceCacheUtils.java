package io.openim.android.ouicore.voice.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.Objects;

@SuppressLint("StaticFieldLeak")
public class VoiceCacheUtils {
    private static VoiceCacheUtils cacheUtils;
    private String cacheDirPath = Objects.requireNonNull(mContext.getExternalCacheDir()).getAbsolutePath();
    private String cachePath = "/VoiceCache";
    private File file;
    private static Context mContext;

    public VoiceCacheUtils() {
        file = new File(cacheDirPath + cachePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void init(Context context) {
        mContext = context;
    }

    public static VoiceCacheUtils instance() {
        if (cacheUtils == null) {
            synchronized (VoiceCacheUtils.class) {
                if (cacheUtils == null) {
                    cacheUtils = new VoiceCacheUtils();
                }
            }
        }
        return cacheUtils;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }

    public void setCacheDirPath(String cacheDirPath) {
        this.cacheDirPath = cacheDirPath;
    }

    public String getCachePath() {
        return cacheDirPath + cachePath;
    }

    public String hasCache(String url) {
        String fileName;
        fileName = getMediaID(url);
        File tempFile = new File(file, fileName);
        if (tempFile.exists()) {
            return tempFile.getAbsolutePath();
        } else {
            return null;
        }
    }

    public String getMediaID(String url) {
        return url.replaceAll("\\.", "").replaceAll("/", "").replaceAll(":", "") + ".aud";
    }

    /**
     * 获取当前文件夹大小，不递归子文件夹
     *
     * @return 文件夹大小
     */
    public long getCurrentFolderSize() {
        long size = 0;
        try {
            File file = new File(getCachePath());
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    //跳过子文件夹
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     */
    public String getFormatSize() {
        double kiloByte = getCurrentFolderSize() / 1024;
        if (kiloByte < 1) {
            return getCurrentFolderSize() + "MB";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

    /**
     * 删除指定目录下文件
     */
    public void deleteCache() {
        if (!TextUtils.isEmpty(getCachePath())) {
            try {
                File file = new File(getCachePath());
                File[] fileList = file.listFiles();
                if (fileList != null) {
                    for (File value : fileList) {
                        if (!value.isDirectory()) {
                            value.delete();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
