package com.binance.account.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 *
 */
public class EncryptUtil {

    /**
     * 加密算法
     */
    private static final String ENCRY_ALGORITHM = "AES";

    /**
     * 加密算法/加密模式/填充类型
     * 本例采用AES加密，ECB加密模式，PKCS5Padding填充
     */
    private static final String CIPHER_MODE = "AES/ECB/PKCS5Padding";

    /**
     * 设置iv偏移量
     * 本例采用ECB加密模式，不需要设置iv偏移量
     */
    private static final String IV_ = null;

    /**
     * 设置加密字符集
     * 本例采用 UTF-8 字符集
     */
    private static final String CHARACTER = "UTF-8";

    /**
     * 设置加密密码处理长度。
     * 不足此长度补0；
     */
    private static final int PWD_SIZE = 16;

    /**
     * 密码处理方法
     * 如果加解密出问题，
     * 请先查看本方法，排除密码长度不足补"0",导致密码不一致
     * @param password 待处理的密码
     * @return
     * @throws UnsupportedEncodingException
     */
    private static byte[] pwdHandler(String password) throws UnsupportedEncodingException {
        byte[] data = null;
        if (password == null) {
            password = "";
        }
        StringBuffer sb = new StringBuffer(PWD_SIZE);
        sb.append(password);
        while (sb.length() < PWD_SIZE) {
            sb.append("0");
        }
        if (sb.length() > PWD_SIZE) {
            sb.setLength(PWD_SIZE);
        }

        data = sb.toString().getBytes("UTF-8");

        return data;
    }


    /**
     * 原始加密
     * @param clearTextBytes 明文字节数组，待加密的字节数组
     * @param pwdBytes 加密密码字节数组
     * @return 返回加密后的密文字节数组，加密错误返回null
     */
    public static byte[] encrypt(byte[] clearTextBytes, byte[] pwdBytes)throws Exception {
            // 1 获取加密密钥
            SecretKeySpec keySpec = new SecretKeySpec(pwdBytes, ENCRY_ALGORITHM);

            // 2 获取Cipher实例
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);

            // 查看数据块位数 默认为16（byte） * 8 =128 bit

            // 3 初始化Cipher实例。设置执行模式以及加密密钥
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // 4 执行
            byte[] cipherTextBytes = cipher.doFinal(clearTextBytes);

            // 5 返回密文字符集
            return cipherTextBytes;
    }

    /**
     * 原始解密
     * @param cipherTextBytes 密文字节数组，待解密的字节数组
     * @param pwdBytes 解密密码字节数组
     * @return 返回解密后的明文字节数组，解密错误返回null
     */
    public static byte[] decrypt(byte[] cipherTextBytes, byte[] pwdBytes)throws Exception {
            // 1 获取解密密钥
            SecretKeySpec keySpec = new SecretKeySpec(pwdBytes, ENCRY_ALGORITHM);

            // 2 获取Cipher实例
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);

            // 查看数据块位数 默认为16（byte） * 8 =128 bit

            // 3 初始化Cipher实例。设置执行模式以及加密密钥
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // 4 执行
            byte[] clearTextBytes = cipher.doFinal(cipherTextBytes);

            // 5 返回明文字符集
            return clearTextBytes;
    }


    /**
     * HEX加密
     * @param clearText 明文，待加密的内容
     * @param password 密码，加密的密码
     * @return 返回密文，加密后得到的内容。加密错误返回null
     */
    public static String encryptHex(String clearText, String password) throws Exception{
            // 1 获取加密密文字节数组
            byte[] cipherTextBytes = encrypt(clearText.getBytes(CHARACTER), pwdHandler(password));

            // 2 对密文字节数组进行 转换为 HEX输出密文
            String cipherText = byte2hex(cipherTextBytes);

            // 3 返回 HEX输出密文
            return cipherText;
    }

    /**
     * HEX解密
     * @param cipherText 密文，带解密的内容
     * @param password 密码，解密的密码
     * @return 返回明文，解密后得到的内容。解密错误返回null
     */
    public static String decryptHex(String cipherText, String password) throws Exception{
            // 1 将HEX输出密文 转为密文字节数组
            byte[] cipherTextBytes = hex2byte(cipherText);

            // 2 将密文字节数组进行解密 得到明文字节数组
            byte[] clearTextBytes = decrypt(cipherTextBytes, pwdHandler(password));

            // 3 根据 CHARACTER 转码，返回明文字符串
            return new String(clearTextBytes, CHARACTER);
    }

    /*字节数组转成16进制字符串  */
    private static String byte2hex(byte[] bytes) { // 一个字节的数，
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        String tmp = "";
        for (int n = 0; n < bytes.length; n++) {
            // 整数转成十六进制表示
            tmp = (Integer.toHexString(bytes[n] & 0XFF));
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
        }
        return sb.toString().toUpperCase(); // 转成大写
    }

    /*将hex字符串转换成字节数组 */
    private static byte[] hex2byte(String str) {
        if (str == null || str.length() < 2) {
            return new byte[0];
        }
        str = str.toLowerCase();
        int l = str.length() / 2;
        byte[] result = new byte[l];
        for (int i = 0; i < l; ++i) {
            String tmp = str.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }
        return result;
    }


    //十六进制
    public static boolean isHexNumber(String value) {

        char[] pBuf = value.toCharArray();
        int len = value.length();
        for (int i = 0; i < len; ++i)
        {
            if (('0' > pBuf[i] || '9' < pBuf[i])
                    && ('A' > pBuf[i] || 'F' < pBuf[i])
                    && ('a' > pBuf[i] || 'f' < pBuf[i]))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * SHA512加密
     */
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 512;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    public static String hashPassword (String password, String salt) {
        char[] chars = password.toCharArray();
        byte[] bytes = salt.getBytes();

        PBEKeySpec spec = new PBEKeySpec(chars, bytes, ITERATIONS, KEY_LENGTH);
        Arrays.fill(chars, Character.MIN_VALUE);
        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] securePassword = fac.generateSecret(spec).getEncoded();
            Optional<String> result =  Optional.of(Base64.getEncoder().encodeToString(securePassword));
            return result.get();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new RuntimeException("Exception encountered in hashPassword");
        } finally {
            spec.clearPassword();
        }
    }
}
