package org.sh.utils.encoding

import org.apache.commons.codec.binary.{Base64 => B64}

object Base64 {
  def encodeBytes(b:Array[Byte]) = new String(B64.encodeBase64(b))
  def decode(s:String) = B64.decodeBase64(s)
}


object Base64Test extends App {
  val a = Base64.encodeBytes("Hello".getBytes)
  println(" ---> "+a)
  println(" ---> "+new String(Base64.decode(a)))
}