package org.sh.utils.file

import prop.util.EncPropUtil._
import prop.PropFileInfo._
import org.sh.utils.file.prop.PropDataStructures._
//////////////////////////////////////////////

object MissingPropUtil {      
  //////////////////////////////////////////////
  def getMissingPropFiles = {
    val $info$ = "Returns the property files that are missing"
    nonExistingFiles.map{case (fileName, isEncrypted) =>
      FileInfo(fileName, isEncrypted)
    }.toArray
  }
  def getLoadedPropFiles = {
    val $info$ = """Returns the property files loaded by the app till now. Not all files may be loaded. A file is loaded when code accessing it is first executed. 
If the code is never executed, the file will not be loaded. Also, just because the file is not loaded now does not mean it will not be loaded in future.
To get all files (not just loaded ones), use the methods 
<u><a href='#org.sh.utils.file.EncryptedPropUtil.getAllPlaintextFiles'>EncryptedPropUtil.getAllPlaintextFiles</a></u> and <u><a href='#org.sh.utils.file.EncryptedPropUtil.getAllEncryptedFiles'>EncryptedPropUtil.getAllEncryptedFiles</a></u>."""
    existingFiles.map{case (fileName, isEncrypted) =>
      FileInfo(fileName, isEncrypted)
    }.toArray
  }.sortBy(_.fileName)
  def getMissingProps = {
    val $info$ = "Returns the properties that were not found in the given file."
    nonExistingProps.map{
    case (fileProp) =>    
      val file = fileProp.split(":")(0)
      val prop = fileProp.split(":")(1)
      val encrypted = (nonExistingFiles.get(file) ++ existingFiles.get(file)).reduceLeft(_ || _)
      PropInfo(prop, file, encrypted)
    }.toArray.filterNot(_.propName == encrTagKey)
  }.sortBy(x => (x.fileName, x.propName))
}
