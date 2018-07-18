package obd.mapbar.com.bleperipheraldemo;

import android.text.TextUtils;

/**
 * 输出字符辅助类
 * Created by zhangyunfei on 16/8/11.
 */
public final class OutputStringUtil {

  /**
   * 转换成 16进制字符串
   *
   * @param b 字节
   * @return 字符串
   */
  private static String toHexStr(byte b) {
    String str = Integer.toHexString(0xFF & b);
    if (str.length() == 1) {
      str = "0" + str;
    }
    return str.toUpperCase();
  }

  /**
   * 转换成 16进制字符串
   *
   * @param bytes 字节
   * @return 字符串
   */
  public static String toHexString(byte... bytes) {
    if (bytes == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    if (bytes.length < 20) {
      sb.append("[");
      for (byte aByte : bytes) {
        sb.append(toHexStr(aByte)).append(",");
      }
      sb.append("]");
    } else {
      sb.append("[");
      for (int i = 0; i < 4; i++) {
        sb.append(toHexStr(bytes[i])).append(",");
      }
      sb.append("...");
      for (int i = bytes.length - 5; i < bytes.length; i++) {
        sb.append(toHexStr(bytes[i])).append(",");
      }
      sb.setLength(sb.length() - 1);
      sb.append("]");
    }
    return sb.toString();
  }
}
