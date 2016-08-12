package scheduler;

import java.io.*;
import java.security.*;

public class Utils
{
    public static String getFileChecksum(String file) throws IOException, NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        try (FileInputStream fis = new FileInputStream(file))
        {
            int bytesCount;
            byte[] byteArray = new byte[1024];
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        byte[] bytes = digest.digest();

        //Convert bytes to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bytes.length; i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
