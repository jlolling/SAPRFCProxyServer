package de.jlo.talendcomp.sap;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class TalendContextPasswordUtil {

    public static String ENCRYPT_KEY = "Encrypt"; //$NON-NLS-1$
    private static String rawKey = "Talend-Key"; //$NON-NLS-1$
    private static SecretKey key = null;
    private static SecureRandom secureRandom = new SecureRandom();
    private static String CHARSET = "UTF-8";
    
    public static void setMasterPassword(String passwd) {
    	if (passwd != null && passwd.trim().isEmpty() == false) {
    		rawKey = passwd.trim();
    	}
    }

    private static SecretKey getSecretKey() throws Exception {
        if (key == null) {
            byte rawKeyData[] = rawKey.getBytes(CHARSET);
            DESKeySpec dks = new DESKeySpec(rawKeyData);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES"); //$NON-NLS-1$
            key = keyFactory.generateSecret(dks);
        }
        return key;
    }

    public static String encryptPassword(String input) throws Exception {
        if (input == null) {
            return input;
        }
        SecretKey key = getSecretKey();
        Cipher c = Cipher.getInstance("DES"); //$NON-NLS-1$
        c.init(Cipher.ENCRYPT_MODE, key, secureRandom);
        String dec = input;
        byte[] inputBytes = input.getBytes(CHARSET);
        byte[] cipherByte = c.doFinal(inputBytes);
        dec = encodeHexString(cipherByte);
        return dec;
    }

    public static String decryptPassword(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }
        try {
            SecretKey key = getSecretKey();
        	byte[] dec = decodeHex(input.toCharArray());
            Cipher c = Cipher.getInstance("DES"); //$NON-NLS-1$
            c.init(Cipher.DECRYPT_MODE, key, secureRandom);
            byte[] clearByte = c.doFinal(dec);
            return new String(clearByte, CHARSET);
        } catch (Exception e) {
            //do nothing
        }
        return input;
    }

    private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

    private static char[] encodeHex(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return out;
    }

    public static byte[] decodeHex(char[] data) {

        int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    private static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

}
