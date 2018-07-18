package obd.mapbar.com.bleperipheraldemo;

/**
 * created by  huangkeyuan on 2018/5/28.
 *
 * @author huangkeyuan
 * @描述 回复指令
 */
public class Constant {

  private Constant() {
  }

  /**
   * 收到SSID并校验通过后回复.
   */
  public static final byte[] RSP_SSID = new byte[]{(byte) 0XB6, 0X01, 0X00, 0X01};

  /**
   * 收到PASSWORD并校验通过后回复.
   */
  public static final byte[] RSP_PASSWORD = new byte[]{(byte) 0XB6, 0X02, 0X00, 0X02};

  /**
   * 配网成功回复.
   */
  public static final byte[] RSP_RESULT_SUCCESS = new byte[]{(byte) 0XB6, 0X03, 0X01, 0X00, 0X02};

  /**
   * 配网失败回复.
   */
  public static final byte[] RSP_RESULT_ERROR = new byte[]{(byte) 0XB6, 0X03, 0X01, 0X01, 0X03};

}
