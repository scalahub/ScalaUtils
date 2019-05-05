package org.sh.utils.common.file

import prop.util.EncPropUtil._
import prop.PropFileInfo._
import org.sh.utils.common.file.{PlaintextFileProperties => PF}
import org.sh.utils.common.file.prop.TraitCommonFilePropReader
import prop.GlobalEncryptorDecryptor._

object PropertyModifierUtil extends TraitFilePropertyReader {
  val propertyFile = "propertyMaintenance.properties"
  // security logic
  // this will be visible in command prompt/in properties file but not the remote UI for admin 
  val storedMasterPassword = read("masterPassword", "ahmrGqgOud0YqR8VSV7zmZAv9KlouOPY5sR8ztqE")
  def usingMasterPassword[T](someSecret:String)(f: => T):T = if (someSecret == storedMasterPassword) f else invalidPasswordException
  def getPropsIfExists(prop:TraitCommonFilePropReader) = if (prop.fileExists) getProps(prop) else fileNotExistsException(prop.fullFileName)
  protected[file] def addOrModifyProp(propName:String, propValue:String, comment:String, p:TraitCommonFilePropReader) = {
    val existing = p.fileExists // important, p.fileExists will return true later on when we tag, etc so we need to store this now
    val backupFile = p.write(propName, propValue, comment, true)
    "Done! (New file created: "+(!existing)+"). Old file (if any) backed up to "+backupFile
  }    
}
object PropertyModifier {
  import PropertyModifierUtil._
  def getPlaintextProps(fileName:String, masterPassword:String) = usingMasterPassword(masterPassword){
    val $info$ = """Returns the properties defined in the given plaintext file."""
    getPropsIfExists(new PlaintextFileProperties(fileName))
  }
  def getEncryptedProps(fileName:String, secret:String) = {
    val $info$ = """Returns the properties defined in the given encrypted file. DO NOT ADD .enc FILE EXTENSION. USE THE PLAINTEXT FILE NAME."""
    getPropsIfExists(getEncProp(fileName, secret))
  }
  def getEncryptedPropsDefaultSecret(fileName:String, masterPassword:String) = usingMasterPassword(masterPassword){
    val $info$ = """As <u><a href='#org.sh.utils.common.file.PropertyModifier.getEncryptedProps'>getEncryptedProps</a></u> but uses default secret. DO NOT ADD .enc FILE EXTENSION. USE THE PLAINTEXT FILE NAME."""
    getEncryptedProps(fileName, getDefaultLocalSecret)
  }
  def addOrModifyPlaintextProp(fileName:String, propName:String, propValue:String, comment:String, masterPassword:String) = usingMasterPassword(masterPassword){
    val $info$ = """Adds (or edits if the property exists) to the given file. Backs up original file."""
    addOrModifyProp(propName, propValue, comment, new PlaintextFileProperties(fileName))
  }
  def addOrModifyEncryptedProp(fileName:String, propName:String, propValue:String, comment:String, secret:String) = {
    val $info$ = """Adds (or modifies if the property exists) to the given file. Backs up original file. DO NOT ADD .enc FILE EXTENSION. USE THE PLAINTEXT FILE NAME."""
    val p = getEncProp(fileName, secret)
    if (!p.fileExists) tagEncFile(p) 
    addOrModifyProp(propName, propValue, comment, p)
  }  
  def addOrModifyEncryptedPropDefaultSecret(fileName:String, propName:String, propValue:String, comment:String, masterPassword:String) = usingMasterPassword(masterPassword){
    val $info$ = """As <u><a href='#org.sh.utils.common.file.PropertyModifier.addOrModifyEncryptedProp'>addOrModifyEncryptedProp</a></u> but uses default secret. DO NOT ADD .enc FILE EXTENSION. USE THE PLAINTEXT FILE NAME."""
    addOrModifyEncryptedProp(fileName:String, propName:String, propValue:String, comment:String, getDefaultLocalSecret) 
  }
}
