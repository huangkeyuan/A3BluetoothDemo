package obd.mapbar.com.bleperipheraldemo;

import java.util.Arrays;

public class Utils {
    /**
     * 拆分字节数组
     *
     * @param bytes  需要拆分的字节数组
     * @param copies 拆分的长度
     * @return 拆分后的二维字节数组
     */
    public static byte[][] splitBytes(byte[] bytes, int copies) {
        double splitLength = Double.parseDouble(copies + "");
        int arrayLength = (int) Math.ceil(bytes.length / splitLength);
        byte[][] result = new byte[arrayLength][];
        int from, to;
        for (int i = 0; i < arrayLength; i++) {
            from = (int) (i * splitLength);
            to = (int) (from + splitLength);
            if (to > bytes.length) {
                to = bytes.length;
            }
            result[i] = Arrays.copyOfRange(bytes, from, to);
        }
        return result;
    }

    /**
     * 异或字节数组
     *
     * @param byteArr 需要异或的字节数组
     * @param start   异或开始的下标
     * @param end     异或结束的下标
     * @return 异或结果
     */
    public static byte xorResult(byte[] byteArr, int start, int end) {
        if (null == byteArr || 0 == byteArr.length) {
            return 0;
        }
        if (0 > start) {
            start = 0;
        }
        if (byteArr.length <= end) {
            end = byteArr.length - 1;
        }
        byte result = byteArr[start];
        for (int i = start + 1; i <= end; i++) {
            result = (byte) (result ^ byteArr[i]);
        }
        return result;
    }

    /**
     * 根据SSID获取指令
     *
     * @param ssid WIFI的SSID
     * @return SSID指令
     */
    public static byte[] getSsidData(String ssid) {
        byte[] ssidData = null;
        byte[] tmpSsid = ssid.getBytes();
        ssidData = new byte[4 + tmpSsid.length];
        ssidData[0] = (byte) 0XA6;
        ssidData[1] = (byte) 0X01;
        ssidData[2] = (byte) tmpSsid.length;
        System.arraycopy(tmpSsid, 0, ssidData, 3, tmpSsid.length);
        ssidData[ssidData.length - 1] = xorResult(ssidData, 1, ssidData.length - 2);
        return ssidData;
    }

    /**
     * 根据WIFI密码获取指令
     *
     * @param password   WIFI密码
     * @param encryption 加密方式
     * @return WIFI密码指令
     */
    public static byte[] getPasswordData(String password, int encryption) {
        byte[] passwordData = null;
        byte[] tmppassword = password.getBytes();
        passwordData = new byte[5 + tmppassword.length];
        passwordData[0] = (byte) 0XA6;
        passwordData[1] = (byte) 0X02;
        passwordData[2] = (byte) (tmppassword.length + 1);
        passwordData[3] = (byte) encryption;
        System.arraycopy(tmppassword, 0, passwordData, 4, tmppassword.length);
        passwordData[passwordData.length - 1] = xorResult(passwordData, 1, passwordData.length - 2);
        return passwordData;
    }
}
