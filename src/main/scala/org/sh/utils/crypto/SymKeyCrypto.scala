package org.sh.utils.crypto

import Util._
import org.sh.utils.file.TraitFilePropertyReader
import org.sh.utils.encoding.Base64
import org.sh.utils.encoding.Hex
import javax.crypto.spec.SecretKeySpec
import java.security.Security
import javax.crypto.Cipher
import org.bouncycastle.jce.provider.BouncyCastleProvider
//import sun.misc.BASE64Decoder
import org.sh.utils.Util._

private object SymKeyCryptoUtil extends TraitFilePropertyReader {
  def generateAes128KeyHex = Hex.encodeBytes(strongRandomBytes(16)) // 128 bits = 16 bytes
  lazy val propertyFile = "symKeyCrypto.properties"
  lazy val aesKeyHex = read("aesKeyHex")  
  lazy val key = {
    if (aesKeyHex == "") throw new CryptoException("AES Key needs to be specified")
    Hex.decode(aesKeyHex)
  }
}
object SymKeyCrypto extends SymKeyCrypto(SymKeyCryptoUtil.key) // for demo only
class SymKeyCrypto(key:Array[Byte]) {
  def this(aesKeyHex:String) = {
    this((if (aesKeyHex == "") throw new CryptoException("AES Key needs to be specified") else Hex.decode(aesKeyHex)))
  }
  Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
  ///////////////////////////////////////////////////////
  // for symmetric key crypto
  ///////////////////////////////////////////////////////
  //  def this(aesKeyHex:String) = this(Hex.decode(aesKeyHex))
  val encryptorAES = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC")
  val decryptorAES = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC")
  encryptorAES.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES")); // set key
  decryptorAES.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES")); // set key
  def encryptAESBytes(pTxt:Array[Byte]) = encryptorAES.doFinal(pTxt)
  def decryptAESBytes(cTxt:Array[Byte]) = decryptorAES.doFinal(cTxt)
//  import sun.misc.BASE64Encoder
//  val encoder = new BASE64Encoder
//  val decoder = new BASE64Decoder

  def encryptAES(m:String) = Hex.encodeBytes(encryptAESBytes(m.getBytes("UTF-8")))
  def decryptAES(base64c:String) = new String(decryptAESBytes(Hex.decode(base64c)), "UTF-8")    
  def encryptAES1(m:String) = Base64.encodeBytes(encryptAESBytes(m.getBytes("UTF-8")))
  def decryptAES1(base64c:String) = new String(decryptAESBytes(Base64.decode(base64c)), "UTF-8")    
  
  def getBase64EncSize(chars:Int) = ((( chars + 16 /* bytes for padding */)/3d).ceil * 4).toInt
  def getBinEncSize(chars:Int) = chars + 16
  if (debug) println(s" [SYM DEBUG] ${key.size} byte keyHash: "+shaSmall(new String(key)))
}

