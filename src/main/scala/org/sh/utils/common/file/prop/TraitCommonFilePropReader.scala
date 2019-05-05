package org.sh.utils.common.file.prop

//import java.io.FileInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Properties

import org.sh.utils.common.file.prop.util.EncPropUtil
import org.sh.utils.common.file.Util
import org.sh.utils.common.file.Util._
import org.sh.utils.common.Util._
import org.sh.utils.common.file.Util
import org.sh.utils.common.file.prop.util.EncPropUtil

object PropFileInfo {
  var existingFiles:Map[String, Boolean] = Map() // fileName -> isEncrypted
  var nonExistingFiles:Map[String, Boolean] = Map() // fileName -> isEncrypted
  var nonExistingProps:Set[String] = Set() // fileName:propName
  val propertyDirectory = "properties"  
  val encrExtension = "enc"
}
trait TraitCommonFilePropReader {  
  import PropFileInfo._
  val propertyFile : String
  val showDefaultValues : Boolean
  // createDir(propertyDirectory)
  val isEncrypted:Boolean
  protected [common] def fileExists = Util.fileExists(fullFileName)// existingFiles.contains(fullFileName)
  private def actualFileName = propertyFile+(if (isEncrypted) "."+encrExtension else "")
  def fullFileName : String = propertyDirectory + "/" +actualFileName   // System.getProperty("file.separator")
  override def toString = fullFileName
  var isInitialized = false
  val props = new Properties
  final val readOnlyTag = "$readOnly"
  lazy val readOnly = read(readOnlyTag, false)
  // the following processes the inputstream
  def processIS(is:InputStream):InputStream  
  def initialize = { // for command-line and web-server
    def stream1 = () => {
      if (debug) println (s"1 Trying $fullFileName")
      val s = new FileInputStream(fullFileName) // fullFileName is "properties/foo.properties"
      if (debug) println ("Success")
      s
    }
    def stream2 = () => {
      if (debug) println (s"2 Trying this.getClass().getClassLoader().getResourceAsStream($fullFileName)")
      val s = this.getClass().getClassLoader().getResourceAsStream(fullFileName) // loads from /classes folder of jar / war 
      if (debug) println ("Success")
      s
    }
    def stream3 = () => {
      if (debug) println (s"3 new FileInputStream(conf/{$actualFileName})")
      val s = new FileInputStream("conf/"+actualFileName) // for play framework
      if (debug) println ("Success")
      s
    }
    try {
      using(trycatch(List(stream1, stream2, stream3))){is =>
        props.load(processIS(is))
        if (debug) println("Loaded propertyfile ["+propertyFile+"] from: "+is)
        existingFiles += (fullFileName -> isEncrypted)
        nonExistingFiles -= fullFileName
      }
    } catch { case e : Throwable => 
        if (debug) e.printStackTrace
        println ("File not found: "+fullFileName)        
        nonExistingFiles += (fullFileName -> isEncrypted)
    }
    isInitialized = true
  }
  // def read[T](name:String, default: => T, func:String=>T): T = {
  protected def read[T](name:String, default: => T, func:String => T): T = {
    if (!isInitialized) initialize
    val p = props.getProperty(name)
    if (p == null) {
      if (name != EncPropUtil.encrTagKey) { // encrTagKey is an internally used property (to detect encrypted files); thus we ignore it.
        if (showDefaultValues) println("property ["+fullFileName+":"+name+"] not found. Using default value of "+default)
        nonExistingProps += (fullFileName+":"+name)
      }
      default
    } else func(p.trim)
  }
  protected def readOption(name:String):Option[String] = read(name, None,Some(_))
  protected def read(name:String, default:String):String = read[String](name, default, x => x)
  protected def read(name:String, default:Int):Int = read[Int](name, default, _.toInt)
  protected def read(name:String, default:Long):Long= read[Long](name, default, _.toLong)
  protected def read(name:String, default:Double):Double= read[Double](name, default, _.toDouble)

  protected def read(name:String):String = read(name, "")
  protected def read(name:String, default:Boolean):Boolean = read[Boolean](name, default, _.toBoolean)
  protected def processOS(os:OutputStream):OutputStream
//  def writeWithBackup(name:String, value:String, comment:String) = {
//    val backupFile = fullFileName+"_"+toDateString(getTime).replace(":", "-").replace(" ", "_")+".bak"
//    val backedUpFile = if (fileExists) copyTo(backupFile) else "None"
//    write(name:String, value:String, comment:String)
//  }
  def getBackUpFile = fullFileName+"_"+toDateString(getTime).replace(":", "-").replace(" ", "_")+".bak"
  def write(name:String, value:String, comment:String, backupFile:Boolean=false) = if (!readOnly) {
    val backedupFile = if (backupFile && fileExists) backup else "None"
    props.setProperty(name, value)
    props.store(processOS(new FileOutputStream(fullFileName)), comment)    
    backedupFile
  } else throw new Exception("File is readonly due to "+readOnlyTag+" tag: "+fullFileName)
  def backup = copyTo(getBackUpFile)
  def copyTo(fileName:String) = {
    if (Util.fileExists(fileName)) throw new Exception("file already exists: "+fileName)
    props.store(processOS(new FileOutputStream(fileName)), "backup")    
    fileName
  }
}








