package com.atguigu.gmall.util;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class MD5 {

    public static String encrypt(String strSrc) {
        try {
            char hexChars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                    '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            byte[] bytes = strSrc.getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            bytes = md.digest();
            int j = bytes.length;
            char[] chars = new char[j * 2];
            int k = 0;
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                chars[k++] = hexChars[b >>> 4 & 0xf];
                chars[k++] = hexChars[b & 0xf];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("MD5加密出错！！+" + e);
        }
    }

    public static String encrypt(String skuId, HttpServletRequest request) {

        /**
         * 生成抢购码的算法包
         */
        // String sessionId = session.getId();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
        String format = sdf.format(new Date());
        String userId = request.getParameter("userId");
        String encrypt = MD5.encrypt(userId + skuId + format);

//        String currentTimeMillis = System.currentTimeMillis()+"";
//        int length = currentTimeMillis.length();
//        String substring = currentTimeMillis.substring(0,(length - 2));
        return encrypt;
    }
}
