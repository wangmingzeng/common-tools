package cn.com.zach.tools.arithmetic;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * AES 加密工具类
 * @author zach
 */
public class AESUtil {

    private static final String KEY_ALGORITHM = "AES";
    /**
     * 默认的加密算法
     */
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * AES 加密操作
     *
     * @param content   待加密内容
     * @param secretStr 密钥
     * @return 返回Base64转码后的加密数据
     */
    public static String encrypt(String content, String secretStr) {
        try {
            // 创建密码器
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            byte[] byteContent = content.getBytes("utf-8");
            // 初始化为加密模式的密码器
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secretStr));
            // 加密
            byte[] encrypted = cipher.doFinal(byteContent);
            //通过Base64转码返回
            return Base64.encodeBase64URLSafeString(encrypted);
        } catch (Exception ex) {
        		ex.printStackTrace();
        }
        return null;
    }

    /**
     * AES 解密操作
     *
     * @param content   解密内容
     * @param secretStr 密钥
     * @return 解密结果
     */
    public static String decrypt(String content, String secretStr) {
        try {
            //实例化
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secretStr));
            //执行操作
            byte[] original = cipher.doFinal(Base64.decodeBase64(content));
            return new String(original, "utf-8");
        } catch (Exception ex) {
        		ex.printStackTrace();
        }
        return null;
    }

    /**
     * 生成加密秘钥
     *
     * @return SecretKeySpec
     */
    private static SecretKeySpec getSecretKey(final String secretStr) {
		byte[] arrBTmp = secretStr.getBytes();
		byte[] arrB = new byte[16]; // 创建一个空的16位字节数组（默认值为0）
		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
			arrB[i] = arrBTmp[i];
		}
		SecretKeySpec skeySpec = new SecretKeySpec(arrB, KEY_ALGORITHM);
		return skeySpec;
	}
    
    public static void main(String[] args) throws Exception {
		String test = "我爱你aasdsfasdfasdfasdfasd";
		System.out.println("加密前：" + test);
		String key = "4D691F702E3D454DB1D7419C5B51A061";
		System.out.println("密钥：" + key);
		String encrypt = encrypt(test, key);
		System.out.println("加密后：" + encrypt);
		String decrypt = decrypt(encrypt, key);
		System.out.println("解密后：" + decrypt);
	}
}
