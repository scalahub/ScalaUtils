package org.sh.utils.crypto

import org.sh.utils.common.encoding.Base64
import org.sh.utils.common.file.TraitFilePropertyReader

object PubKeyEncryptor extends TraitFilePropertyReader {
  lazy val propertyFile = "pubKeyEncryption.properties"
  lazy val pubKeyEncodedDER = read("pubKeyEncodedDER")
  lazy val pubKey = Util.pubKeyFromDERBytes(Base64.decode(pubKeyEncodedDER))
  def encrypt(m:String, doEncrypt:Boolean=true) = if (doEncrypt) Util.encrypt(pubKey, m) else m
  def encryptBytes(b:Array[Byte]) = Util.encryptRSA(pubKey, b)
}

object PubKeyDecryptor extends TraitFilePropertyReader {
  lazy val propertyFile = "pubKeyDecryption.properties"
  lazy val prvKeyEncodedDER = read("pubKeyEncodedDER")
  lazy val prvKey = Util.prvKeyFromDERBytes(Base64.decode(prvKeyEncodedDER))
  def decrypt(c:String, doDecrypt:Boolean=true) = if (doDecrypt) Util.decrypt(prvKey, c) else c
  def decryptBytes(b:Array[Byte]) = Util.decryptRSA(prvKey, b)
}
