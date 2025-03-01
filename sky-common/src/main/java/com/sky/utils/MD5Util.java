package com.sky.utils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    /**
     * 对字符串进行 MD5 加密
     *
     * @param input 待加密的字符串
     * @return 加密后的 MD5 字符串
     */
    public static String md5(String input) {
        try {
            // 创建 MD5 摘要对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算 MD5 摘要
            byte[] digest = md.digest(input.getBytes());
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }

    public static void main(String[] args) {
        String text = "123456";
        String md5Result = md5(text);
        System.out.println("字符串 \"" + text + "\" 的 MD5 值为: " + md5Result);
    }
}