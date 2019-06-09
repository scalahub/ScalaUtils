package org.sh.utils.crypto

import org.sh.utils.encoding.Base64

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
