package org.sh.utils.common.file.prop

import java.io.{InputStream => IS}
import java.io.{OutputStream => OS}
import org.sh.utils.common.Util.sha256

object GlobalEncryptorDecryptor  {
  
  private val secretVariable = "en_cs_homedir"
  private def noLocalSecretException = throw new Exception("local secret cannot be null. Add Java option: -D"+secretVariable+"=[some secret]")
  private val hardcodedSecret = (1 to 20).foldLeft("d1OmoHOqmSKF"+sha256("5AvjmOD2VXbgS")+sha256("zmUNoQazJnjUtauTUJ9w"))((x, y) => sha256(sha256(x)+y))
  // using sha256 etc above to prevent direct reverse engineering of bytecode. otherwise the above string (easily visible) can be read
  // private val localSecret = System.getenv(secretVariable)
  private val localSecret = System.getProperty(secretVariable)
  System.setProperty(secretVariable, "") // remove key from JVM property
  // val isEncryptedFile = System.getenv("isEncryptionEnabled") == "true"
  val isEncryptionEnabled = System.getProperty("isEncryptionEnabled") != null
  
  if (isEncryptionEnabled && localSecret == null) noLocalSecretException
  def getDefaultLocalSecret = if (localSecret == null) noLocalSecretException else localSecret  
  
  def getKeyIV(localSecret:String) = {
    val actualSecret = sha256(hardcodedSecret+localSecret).substring(0, 32)
    val aesKey = actualSecret.substring(0, 16);
    val iv = actualSecret.substring(16);
    (aesKey, iv)
  }
  private val (aesKey, iv) = getKeyIV(localSecret)
  private val encDec = new EncryptorDecryptor(aesKey, iv)
  def getDecryptedStream(cTxt:IS) = encDec.getDecryptedStream(cTxt)
  def getEncryptedStream(pTxt:OS) = encDec.getEncryptedStream(pTxt)
}














