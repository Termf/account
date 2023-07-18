package com.binance.account.data.utils;

import com.binance.master.error.BusinessException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @ClassName CryptoAlgoUtils
 * @Description
 * @Author Zhenlei Sun
 */
public class CryptoAlgoUtils {
    public final static String PASSWORD_PATTERN = "^[0-9a-f]{128}$";

    private static final int HASH_ITERATIONS = 100000;
    private static final int HASH_KEY_LENGTH = 512;
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA512";

    /**
     * 加密
     * @param password
     * @param salt
     * @return
     * @throws Exception
     */
    public static String validateAndHash512(String password, String salt)throws Exception {
        // check password length
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        if (!pattern.matcher(password).matches()){
            throw new Exception("validation failed!");
        }
        if(org.apache.commons.lang3.StringUtils.isAnyBlank(password,salt) ){
            throw new BusinessException("password or salt  is empty!");
        }
        return hashPassword(password, salt);
    }

    private static String hashPassword(String password, String salt) throws Exception{

        char[] chars = password.toCharArray();
        byte[] bytes = salt.getBytes();

        PBEKeySpec spec = new PBEKeySpec(chars, bytes, HASH_ITERATIONS, HASH_KEY_LENGTH);

        Arrays.fill(chars, Character.MIN_VALUE);

        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            return Hex.encodeHexString(fac.generateSecret(spec).getEncoded());
        } finally {
            spec.clearPassword();
        }
    }



    public static void main(String[] args) throws Exception {
        String passwordInPlainText = "plain text";
        String password = DigestUtils.sha512Hex(passwordInPlainText);
        String salt = "salt";
        System.out.println("password+salt:" + password+salt);
        System.out.println("hashPassword:" + hashPassword(password, salt));
        String encrypt = validateAndHash512(password, salt);
        System.out.println("encrypt:" + encrypt);
    }
}
