package com.ncodata.votar.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by perez.juan.jose on 10/01/2018.
 */

public class Cifrado {
    public static SecretKey generateKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        SecretKeySpec secret;
        String password="JuanJuanJuanJuan";//AES only supports key sizes of 16, 24 or 32 bytes
        return secret = new SecretKeySpec(password.getBytes(), "AES");
    }



    public static byte[] encryptMsg(String message, SecretKey secret)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
    {
   /* Encrypt the message. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return cipherText;
    }

    public static String decryptMsg(byte[] cipherText, SecretKey secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException
    {
    /* Decrypt the message, given derived encContentValues and initialization vector. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        String decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
        return decryptString;
    }

    public static String encriptadoJJ (String texto){
        String textoEncriptado="";
        byte[] bytesEncriptados=new byte[texto.length()];
        if(texto==null){
            return null;
        }
        for(int i=0;i<texto.length();i++){
            int a=texto.charAt(i);
            // ascii van de 32 espacio a 126 ~
            Log.d("cifrado", " a" + a);
            Log.d("cifrado", " a" + (a+i)*3);
            Log.d("cifrado", " a" + (((a+i)*3)%90));

            a=((((a+i)*3)%90))+33;
            Log.d("cifrado", " a " + a);
            bytesEncriptados[i]= (byte) a;
        }

        try {
            textoEncriptado=new String( bytesEncriptados, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            textoEncriptado=null;
        }

        return textoEncriptado;
    }

}
