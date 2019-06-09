package org.sh.utils.crypto

import org.sh.utils.encoding.Hex

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
