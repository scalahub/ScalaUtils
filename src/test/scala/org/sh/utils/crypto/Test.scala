package org.sh.utils.crypto

import org.sh.utils.encoding.Hex
import org.sh.utils.encoding.Base64

object TestSymKeyCrypto extends App {
  val key = SymKeyCryptoUtil.generateAes128KeyHex
  val e = new SymKeyCrypto(Hex.decode(key))
  val m = "Hello"
  val c = e.encryptAES(m)
  val p = e.decryptAES(c)
  assert(p == m)
  val c1 = e.encryptAES1(m)
  val p1 = e.decryptAES1(c1)
  assert(p1 == m)
}

object TestPubKeyCrypto {
  def main(args:Array[String]):Unit = {
    val (pub, prv) = Util.generateRSAKeyPair    
    val m = "hello"
    
    val c0 = Util.encrypt(pub, m)
    val p0 = Util.decrypt(prv, c0)
    assert (m == p0)
    
    val pubEncoded = pub.getEncoded
    val pubEncodedStr = Base64.encodeBytes(pubEncoded)
    val pubEncodedBytes = Base64.decode(pubEncodedStr)
    val pubDecoded = Util.pubKeyFromDERBytes(pubEncodedBytes)
    println ("public key DER encoded in Base64:\n"+pubEncodedStr)
    val c = Util.encrypt(pubDecoded, m)

    val prvEncoded = prv.getEncoded
    val prvEncodedStr = Base64.encodeBytes(prvEncoded)
    val prvEncodedBytes = Base64.decode(prvEncodedStr)
    val prvDecoded = Util.prvKeyFromDERBytes(prvEncodedBytes)
    val decrypted = Util.decrypt(prvDecoded, c)
    assert(decrypted==m)
  }
}
object TestKeyGen extends App {
  // generates PUBLIC/PRIVATE key pair
  generateKeys
  def generateKeys = {
    val (pub, prv) = Util.generateRSAKeyPair    
    val pubEncoded = pub.getEncoded
    val pubEncodedStr = Base64.encodeBytes(pubEncoded)
    val prvEncoded = prv.getEncoded
    val prvEncodedStr = Base64.encodeBytes(prvEncoded)
    val randomString = org.sh.utils.Util.randomAlphanumericString(10)
    val pubKeyFile = s"pubKey_$randomString.txt"
    val prvKeyFile = s"prvKey_$randomString.txt"
    org.sh.utils.file.Util.writeToTextFile(pubKeyFile, pubEncodedStr)
    org.sh.utils.file.Util.writeToTextFile(prvKeyFile, prvEncodedStr)
    println ("Public key written to: "+pubKeyFile)
    println ("Private key written to: "+prvKeyFile)
  }
}
