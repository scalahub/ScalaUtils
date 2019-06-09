package org.sh.utils.file.prop

import java.io.{InputStream => IS}
import java.io.{OutputStream => OS}
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.{CipherInputStream => CIS}
import javax.crypto.{CipherOutputStream => COS}
import javax.crypto.spec.IvParameterSpec;import javax.crypto.spec.SecretKeySpec


// from http://www.java2s.com/Tutorial/Java/0490__Security/UsingCipherInputStream.htm
// Note: Uses DES. Might be insecure. Using AES from below link
// http://stackoverflow.com/a/15922832/243233
class EncryptorDecryptor(private val secretKey:String, private val iv:String) {
  if (secretKey.size != 16 || iv.size != 16) 
    throw new Exception("incorrect HOMEDIR [org.sh.utils]")
  val CIPHER_MODE = "AES/CFB8/NoPadding";
  val CHARSET = Charset.forName("UTF8");
  val keySpec = new SecretKeySpec(secretKey.getBytes(CHARSET), "AES");
  val ivSpec = new IvParameterSpec(iv.getBytes(CHARSET));
  def getDecryptedStream(cTxt:IS) = {
    val cipher = Cipher.getInstance(CIPHER_MODE);
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
    new CIS(cTxt, cipher)    
  }
  def getEncryptedStream(pTxt:OS) = {
    val cipher = Cipher.getInstance(CIPHER_MODE);
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
    new COS(pTxt, cipher)    
  }
}




