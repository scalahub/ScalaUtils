package org.sh.utils

import java.net.NetworkInterface

import org.sh.utils.encoding.Hex

import scala.collection.JavaConverters._

object NetUtil {
  def getNetInterfaces = NetworkInterface.getNetworkInterfaces.asScala
  def isNetInterfaceUp = getNetInterfaces.exists(i => i.isUp && ! i.isLoopback)
  def getLoopbackInterfaces = getNetInterfaces.filter(_.isLoopback)
  def getNonLoopbackInterfaces = getNetInterfaces.filterNot(_.isLoopback)

  def requireOffline = {
    if (isNetInterfaceUp) {
      val up = getNetInterfaces.find(_.isUp).get
      val name = up.getDisplayName
      val mac = Hex.encodeBytes(up.getHardwareAddress).grouped(2).mkString(":")
      val ip = up.getInetAddresses.nextElement().getHostAddress
      throw new Exception(
        s"Disable interface ${name} with MAC ${mac} and IP $ip")
    }
  }
}
object TestNetUtil extends App {
  NetUtil.requireOffline
}