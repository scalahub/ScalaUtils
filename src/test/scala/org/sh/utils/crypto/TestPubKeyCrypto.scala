package org.sh.utils.crypto

import org.sh.utils.encoding.Base64

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
