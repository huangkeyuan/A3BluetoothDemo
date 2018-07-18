package obd.mapbar.com.bleperipheraldemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author huangkeyuan
 */
public class SharedPrefsUtil {

    private static final String SHAREDPREFS_NAME = "A3";

    /**
     * 向SharedPreferences中写入int类型数据
     *
     * @param context 上下文环境
     * @param key     键
     * @param value   值
     */
    public static void putValue(Context context, String key,
                                int value) {
        SharedPreferences.Editor sp = getEditor(context, SHAREDPREFS_NAME);
        sp.putInt(key, value);
        sp.commit();
    }

    /**
     * 向SharedPreferences中写入boolean类型的数据
     *
     * @param context 上下文环境
     * @param key     键
     * @param value   值
     */
    public static void putValue(Context context, String key,
                                boolean value) {
        SharedPreferences.Editor sp = getEditor(context, SHAREDPREFS_NAME);
        sp.putBoolean(key, value);
        sp.commit();
    }

    /**
     * 向SharedPreferences中写入String类型的数据
     *
     * @param context 上下文环境
     * @param key     键
     * @param value   值
     */
    public static void putValue(Context context, String key,
                                String value) {
        SharedPreferences.Editor sp = getEditor(context, SHAREDPREFS_NAME);
        sp.putString(key, value);
        sp.commit();
    }

    /**
     * 向SharedPreferences中写入float类型的数据
     *
     * @param context 上下文环境
     * @param key     键
     * @param value   值
     */
    public static void putValue(Context context, String key,
                                float value) {
        SharedPreferences.Editor sp = getEditor(context, SHAREDPREFS_NAME);
        sp.putFloat(key, value);
        sp.commit();
    }

    /**
     * 向SharedPreferences中写入long类型的数据
     *
     * @param context 上下文环境
     * @param key     键
     * @param value   值
     */
    public static void putValue(Context context, String key,
                                long value) {
        SharedPreferences.Editor sp = getEditor(context, SHAREDPREFS_NAME);
        sp.putLong(key, value);
        sp.commit();
    }

    /**
     * 从SharedPreferences中读取int类型的数据
     *
     * @param context  上下文环境
     * @param key      键
     * @param defValue 如果读取不成功则使用默认值
     * @return 返回读取的值
     */
    public static int getValue(Context context, String key,
                               int defValue) {
        SharedPreferences sp = getSharedPreferences(context, SHAREDPREFS_NAME);
        int value = sp.getInt(key, defValue);
        return value;
    }

    /**
     * 从SharedPreferences中读取boolean类型的数据
     *
     * @param context  上下文环境
     * @param key      键
     * @param defValue 如果读取不成功则使用默认值
     * @return 返回读取的值
     */
    public static boolean getValue(Context context, String key,
                                   boolean defValue) {
        SharedPreferences sp = getSharedPreferences(context, SHAREDPREFS_NAME);
        boolean value = sp.getBoolean(key, defValue);
        return value;
    }

    /**
     * 从SharedPreferences中读取String类型的数据
     *
     * @param context  上下文环境
     * @param key      键
     * @param defValue 如果读取不成功则使用默认值
     * @return 返回读取的值
     */
    public static String getValue(Context context, String key,
                                  String defValue) {
        SharedPreferences sp = getSharedPreferences(context, SHAREDPREFS_NAME);
        String value = sp.getString(key, defValue);
        return value;
    }

    /**
     * 从SharedPreferences中读取float类型的数据
     *
     * @param context  上下文环境
     * @param key      键
     * @param defValue 如果读取不成功则使用默认值
     * @return 返回读取的值
     */
    public static float getValue(Context context, String key,
                                 float defValue) {
        SharedPreferences sp = getSharedPreferences(context, SHAREDPREFS_NAME);
        float value = sp.getFloat(key, defValue);
        return value;
    }

    /**
     * 从SharedPreferences中读取long类型的数据
     *
     * @param context  上下文环境
     * @param key      键
     * @param defValue 如果读取不成功则使用默认值
     * @return 返回读取的值
     */
    public static long getValue(Context context, String key,
                                long defValue) {
        SharedPreferences sp = getSharedPreferences(context, SHAREDPREFS_NAME);
        long value = sp.getLong(key, defValue);
        return value;
    }

    //获取Editor实例
    private static SharedPreferences.Editor getEditor(Context context, String name) {
        return getSharedPreferences(context, name).edit();
    }

    //获取SharedPreferences实例
    private static SharedPreferences getSharedPreferences(Context context, String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }
}
