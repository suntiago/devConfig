package viroyal.com.dev.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Time:2020/1/10
 * Author:bichao
 * description:sha1加密
 */
public class SHA1Utils {

    //byte字节转换成16进制的字符串MD5Utils.hexString
    private byte[] eccrypt(String info, String shaType) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance(shaType);
        byte[] srcBytes = info.getBytes();
        // 使用srcBytes更新摘要
        sha.update(srcBytes);
        // 完成哈希计算，得到result
        return sha.digest();
    }

    byte[] eccryptSHA1(String info) throws NoSuchAlgorithmException {
        return eccrypt(info, "SHA1");
    }

    public byte[] eccryptSHA256(String info) throws NoSuchAlgorithmException {
        return eccrypt(info, "SHA-256");
    }

    public byte[] eccryptSHA384(String info) throws NoSuchAlgorithmException {
        return eccrypt(info, "SHA-384");
    }

    public byte[] eccryptSHA512(String info) throws NoSuchAlgorithmException {
        return eccrypt(info, "SHA-512");
    }

    public String hexString(byte[] bytes){
        StringBuilder hexValue = new StringBuilder();

        for (byte aByte : bytes) {
            int val = ((int) aByte) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }
}
