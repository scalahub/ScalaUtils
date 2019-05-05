
package org.sh.utils.common.file

import org.sh.utils.common.file.prop._
import org.sh.utils.common.file.prop.GlobalEncryptorDecryptor._
import java.io.{InputStream => IS}
import java.io.{OutputStream => OS}

import org.sh.utils.common.file.prop.{EncryptorDecryptor, TraitCommonFilePropReader}

trait TraitFilePropertyReader extends TraitCommonFilePropReader {
  val isEncrypted:Boolean = isEncryptionEnabled
  val showDefaultValues:Boolean = false
  def processIS(is:IS):IS = 
    if (isEncrypted) getDecryptedStream(is) else is
  def processOS(os:OS):OS = 
    if (isEncrypted) getEncryptedStream(os) else os
}

trait TraitPlaintextFileProperties extends TraitCommonFilePropReader {
  val isEncrypted:Boolean = false
  val showDefaultValues:Boolean = true
  def processIS(is:IS):IS = is
  def processOS(os:OS):OS = os  
}

class EncryptedFileProperties(aesKey:String, iv:String, val propertyFile:String) extends TraitCommonFilePropReader {
  val ed = new EncryptorDecryptor(aesKey, iv)
  val isEncrypted:Boolean = true
  val showDefaultValues:Boolean = false
  
  def processIS(is:IS):IS = ed.getDecryptedStream(is)
  def processOS(os:OS):OS = ed.getEncryptedStream(os)  
}

class PlaintextFileProperties(val propertyFile:String) extends TraitPlaintextFileProperties {
  initialize
}

