package org.sh.utils.common.file

import prop.util.EncPropUtil._
import prop.PropFileInfo._
import prop.GlobalEncryptorDecryptor._
import prop.PropFileInfo._

object Test extends App {
//  EncryptedPropUtil.encryptPlaintextFile("unspentDB.properties", "hello")
//  EncryptedPropUtil.encryptPlaintextFile("unusedAddressDB.properties", "hello")
//  EncryptedPropUtil.encryptPlaintextFile("userAddressDB.properties", "hello")
//  EncryptedPropUtil.encryptPlaintextFile("sendDB.properties", "hello")
//  EncryptedPropUtil.encryptPlaintextFile("reflect.properties", "hello")
}
object EncryptedPropUtil {
  def encryptionEnabled = isEncryptionEnabled
  def encryptPlaintextFile(fileName:String, secret:String) = {
    val $info$ = """Encrypts the given plaintext file. File MUST NOT have an extension of '.enc', 
since only those files are encrypted. The result is a new encrypted file created with the same name as the original file with '.enc' appended to it.
The new file must not exist.
"""
    if (fileName.endsWith(encrExtension)) throw new Exception("file already encrypted? "+fileName+" (extension "+encrExtension+" not allowed)")
    val dec = new PlaintextFileProperties(fileName)
    if (!dec.fileExists) fileNotExistsException(dec.fullFileName)
    val enc = getEncProp(fileName, secret)
    if (enc.fileExists) fileExistsException(enc.fullFileName)
    encrypt(dec, enc)
  } 
  def decryptEncryptedFile(fileName:String, secret:String) = {
    val $info$ = """Decrypts the given encrypted file. Do not append '.enc' to the file name, since that is automatically added, 
The result is a new decrypted file created with the same name as the original file but with the '.enc' extension dropped.
The new file must not exist. DO NOT ADD .enc FILE EXTENSION. USE THE PLAINTEXT FILE NAME.
"""
    val enc = getEncProp(fileName, secret)
    if (!enc.fileExists) fileNotExistsException(enc.fullFileName)
    val dec = new PlaintextFileProperties(fileName)
    if (dec.fileExists)  fileExistsException(dec.fullFileName) // if (decProp.fullFileName.endsWith(encrExtension)) throw new Exception("plaintext file cannot have extension: "+encrExtension)    
    decrypt(enc, dec)
  }
  def decryptValidEncryptedFiles(secret:String) = {
    val $info$ = """Decrypts all files in the properties directory ending with '.enc' that are decryptable using the secret.
None of the target files should exist. This will create new file(s) in the same directory with extensions '.enc' stripped."""
    val enc = getValidEncryptedFiles(secret) // getValidEncryptedPropFiles(secret, getAllEncryptedFiles)
    val dec = enc.map(e => new PlaintextFileProperties(e.propertyFile))
    dec.foreach(d => if (d.fileExists) fileExistsException(d.fullFileName))
    (enc zip dec).map{case (e, d) => decrypt(e, d)}
  }
  def encryptAllPlaintextFiles(secret:String) = {
    val $info$ = """Encrypts all files in the properties directory ending with '.properties' using the secret.
None of the target files should exist. This will create new file(s) in the same directory with extension '.enc' added."""
    val dec = getAllPlaintextFiles.map(getSimpleFileName).map(new PlaintextFileProperties(_))
    val enc = dec.map(d => getEncProp(d.propertyFile, secret))
    enc.foreach(e => if (e.fileExists) fileExistsException(e.fullFileName))
    (dec zip enc).map{case (d, e) => encrypt(d, e)}
  }
  
  def decryptValidEncryptedFilesDefaultSecret(token:String) = {
    val $info$ = """As <u><a href='#org.sh.utils.common.file.EncryptedPropUtil.decryptValidEncryptedFiles'>decryptValidEncryptedFiles</a></u> but uses default secret. To prevent accidental clicking, a token is required. The token is 83y4ufdhj8uhf3iqnj"""
    if (token == "83y4ufdhj8uhf3iqnj") decryptValidEncryptedFiles(getDefaultLocalSecret) else invalidPasswordException
  }
  def encryptAllPlaintextFilesDefaultSecret(token:String) = {
    val $info$ = """As <u><a href='#org.sh.utils.common.file.EncryptedPropUtil.encryptAllPlaintextFiles'>encryptAllPlaintextFiles</a></u> but uses default secret. To prevent accidental clicking, a token is required. The token is 83y4ufdhj8uhf3iqnj"""
    if (token == "83y4ufdhj8uhf3iqnj") encryptAllPlaintextFiles(getDefaultLocalSecret) else invalidPasswordException
  }
  def encryptPlaintextFileDefaultSecret(fileName:String) = {
    val $info$ = """As <u><a href='#org.sh.utils.common.file.EncryptedPropUtil.encryptPlaintextFile'>encryptPlaintextFile</a></u> but uses default secret. DO NOT ADD .enc FILE EXTENSION. USE THE PLAINTEXT FILE NAME."""
    encryptPlaintextFile(fileName, getDefaultLocalSecret)
  }
  def decryptEncryptedFileDefaultSecret(fileName:String) = {
    val $info$ = """As <u><a href='#org.sh.utils.common.file.EncryptedPropUtil.decryptEncryptedFile'>decryptEncryptedFile</a></u> but uses default secret. DO NOT ADD .enc FILE EXTENSION. USE THE PLAINTEXT FILE NAME."""
    decryptEncryptedFile(fileName, getDefaultLocalSecret)
  }
      
  def getAllEncryptedFiles = {
    val $info$ = "Returns the files having extension 'enc' in the default property dir. These are all encrypted, because only encrypted files should have extension .enc."
    Util.getAllFiles(propertyDirectory, Array(encrExtension), false).map(getSimpleFileName)
  }
  def getValidEncryptedFiles(secret:String) = {
    val $info$ = "Returns the files having extension 'enc' in the default property dir that can be decrypted using the given secret."
    getValidEncryptedPropFiles(secret, getAllEncryptedFiles) // .map(_.fullFileName).map(getSimpleFileName)
  }
  
  def getValidEncryptedFilesDefaultSecret = {
    val $info$ = """As <u><a href='#org.sh.utils.common.file.EncryptedPropUtil.getValidEncryptedFiles'>getValidEncryptedFiles</a></u> but uses default secret."""
    getValidEncryptedFiles(getDefaultLocalSecret)
  }
  def getAllPlaintextFiles = {
    val $info$ = "Returns the files having extension 'properties' in the default property dir. These are all plaintext, because encrypted files have extension .enc"
    Util.getAllFiles(propertyDirectory, Array("properties"), false)
  }
}











