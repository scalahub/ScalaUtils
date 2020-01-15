
package org.sh.utils.file.prop.util

import java.io.File

import org.sh.utils.file.prop.PropDataStructures.PropVal

import scala.jdk.CollectionConverters._

import org.sh.utils.file.prop.{GlobalEncryptorDecryptor, TraitCommonFilePropReader}
import org.sh.utils.file.{EncryptedFileProperties, TraitPlaintextFileProperties}

object EncPropUtil {
  import GlobalEncryptorDecryptor._
  private val (noEncr, noDecr) = ("$noEncryption", "$noDecryption")
//  def encrypt(decProp:PF, encProp:EF) = if (!decProp.read(noEncr, false)) {
  def encrypt(decProp:TraitPlaintextFileProperties, encProp:EncryptedFileProperties) = if (try {!decProp.props.getProperty(noEncr).toBoolean} catch {case _:Any => false}) {
    copyProps(decProp, encProp)
    tagEncFile(encProp) 
    encProp.fullFileName
  } else "Not encrypted due to "+noEncr+" tag: "+decProp.fullFileName
//  def decrypt(encProp:EF, decProp:PF) = if (!encProp.read(noDecr, false)) {
  def decrypt(encProp:EncryptedFileProperties, decProp:TraitPlaintextFileProperties) = if (try {!encProp.props.getProperty(noEncr).toBoolean} catch {case _:Any => false}) {
    copyProps(encProp, decProp)
    decProp.fullFileName
  } else "Not decrypted due to "+noDecr+" tag: "+encProp.fullFileName
  def tagEncFile(pr:EncryptedFileProperties) = pr.write(encrTagKey, encrTagVal, "encrypted data")
  def invalidPasswordException =  throw new Exception("invalid password/token")
  def getEncProp(fileName:String, secret:String) = {
    val (aesKey, iv) = getKeyIV(secret)
    val Props = new EncryptedFileProperties(aesKey, iv, fileName)
    Props.initialize
    validateEncryption(Props)
  }
  def copyProps(in:TraitCommonFilePropReader, out:TraitCommonFilePropReader) = {
    if (out.fileExists) fileExistsException(out.fullFileName)
    getProps(in).foreach{
      p => out.write(p.key, p.value.toString, "copied from: "+in)
    }
  }
  def getProps(pr:TraitCommonFilePropReader) = {
    pr.initialize//    val dummy = pr.read("dummy", "dummy") // read something to load data
    val p = pr.props
    p.keys.asScala.map{ k =>
      val key = k.asInstanceOf[String]
      PropVal(key, p.get(key))
    }.toArray.filterNot(_.key == encrTagKey)    
  }
  protected[file] val encrTagKey = "xOHbPFD3afGw1TpeA3uJL7lGoKoq7wcBcx6LZfWyh3sNn" // some property that shows that its encrypted with correct key
  protected[file] val encrTagVal = "JI9aqKOInac1oN4OdpEzLk8QulDySdtG0RTTRU50o0ylC" // some value that shows that its encrypted with correct key
  private def validateEncryption(p:EncryptedFileProperties) =
    if (p.fileExists && p.props.getProperty(encrTagKey) != encrTagVal) invalidPasswordException else p
  def getValidEncryptedPropFiles(secret:String, files:Seq[String]) = 
    files.map(_.reverse.substring(4).reverse).map{file =>
      try Some(getEncProp(file, secret))
      catch {case a:Any => None}
    }.collect{case Some(p) => p}  
  def getSimpleFileName(fileName:String) = new File(fileName).getName
  def fileExistsException(fileName:String) = throw new Exception("file already exists: "+fileName)
  def fileNotExistsException(fileName:String) = throw new Exception("file does not exist: "+fileName)
}




