
package org.sh.utils.common.xml

//import java.io.BufferedInputStream
//import java.io.BufferedOutputStream
//import java.io.BufferedReader
//import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML
import org.sh.utils.common.Util._
import org.sh.utils.common.file.Util._
object Util {
 
  def getAttributeText(xml:NodeSeq, attrName:String) = {
    printdbg ("    ===> getAttributeTest "+xml+": "+attrName)
    (xml \ ("@"+attrName)).text
  }
  
  def loadXML(fileName:String) = using(new FileInputStream(fileName)){
    fis => using(new InputStreamReader(fis)){
      isr => XML.load(isr)
    }
  }
  def getElementName (x:Node) = x.nameToString(new StringBuilder).toString
  def filterByAttr(xml:NodeSeq, attrName:String, attrValue:String) = 
    xml.filter(x  => (x \ ("@"+attrName)).text == attrValue)
  
  def getElementsWithAttribute(xml:NodeSeq, elementName:String, attrName:String, attrVal:String) = 
    xml \ elementName collect { x => (x \ ("@"+attrName)).map(_.text).contains(attrVal) match {
        case true => x
      }   
    }
  def getElementWithAttribute(xml:NodeSeq, elementName:String, attrName:String, attrVal:String) = 
    ((xml \ elementName).find{ x => (x \ ("@"+attrName)).map(_.text).contains(attrVal) }).get
  def getChildren(xml:NodeSeq) = xml \ "_"
}


