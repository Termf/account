package com.binance.account.utils;

import java.util.regex.Pattern;

public class InvitationCodeUtil {
    public static final String[] AGENT_CODE_ARR = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","G","H","I","J","K","M","L","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};

    /**
     * 创建一个8位String,第一个是字母，后续7个为字母或数字
     * @return
     */
    public static String generateCode(){
        StringBuilder randomStr = new StringBuilder();
        String first = AGENT_CODE_ARR[(int) (Math.random() * 26)+10];
        randomStr.append(first);
        for (int i=0;i<7;i++){
            randomStr.append(AGENT_CODE_ARR[(int) (Math.random() * 36)]);
        }
        return randomStr.toString();
    }


    /**
     * 创建一个6位String,数字
     * @return
     */
    public static String generateDeviceVerifyCode(){
        StringBuilder randomStr = new StringBuilder();
        for (int i=0;i<6;i++){
            randomStr.append(AGENT_CODE_ARR[(int) (Math.random() * 10)]);
        }
        return randomStr.toString();
    }

    /**
     * 随机生成10位用户密码
     * 账号密码要求：
     * 至少8位字符
     * 必须包含一位大写字母
     * 必须包含一位数字
     * @return
     */
    public static String generatePassword(){
        StringBuilder randomStr = new StringBuilder();
        for (int i=0;i<=10;i++){
            randomStr.append(AGENT_CODE_ARR[(int) (Math.random() * 36)]);
        }
        String password = randomStr.toString();
        if (!validatePasswordFormat(password)) {
            password = generatePassword();
        }
        return password;
    }

    /**
     * 创建一个n位随机String,字母或数字
     * @return
     */
    public static String generateRandomCode(int length){
        StringBuilder randomStr = new StringBuilder();
        for (int i=0;i<length;i++){
            randomStr.append(AGENT_CODE_ARR[(int) (Math.random() * 36)]);
        }
        return randomStr.toString();
    }

    /**
     * 密码格式校验
     * 至少八个字符，至少一个字母和一个数字
     * @param password
     * @return
     */
    public static boolean validatePasswordFormat(String password) {
        return Pattern.matches("^(?=.*\\d)(?=.*[A-Z])[\\s\\S]{8,}$", password);
    }

    public static void main(String[] args) {
        String result=generateRandomCode(10);
        System.out.println(result);
    }
}
