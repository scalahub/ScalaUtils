package org.sh.utils.json

import org.sh.utils.Util
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.sh.utils.Util

import scala.jdk.CollectionConverters._

object JSONUtil {
  def decodeJSONArray(jsonString:String) = {
    val ja = new JSONArray(jsonString)
    new Array[Int](ja.length).indices.map(i => ja.get(i)).toArray
  }
  def getJSONKeys(jsonString:String) = try {
    val jo = new JSONTokener(jsonString).nextValue().asInstanceOf[JSONObject];
    jo.keys.asScala.map(_.toString).toList
  } catch {
    case e:Throwable => if (Util.debug) println(" [JSON] error with: "+jsonString)
      throw e
  }
  
  
  def getJSONParams(names:List[String], jsonString:String) = {
    val jo = new JSONTokener(jsonString).nextValue().asInstanceOf[JSONObject];
    names.indices.map(i => jo.getString(names.apply(i)))
  }
  def encodeJSONSeq(s:Seq[_]) = encodeJSONArray(s.toArray)
  def encodeJSONArray(a:Array[_]) = new JSONArray(a)
  def createJSONString(keys:Array[String], vals:Array[_]) = createJSONObject(keys, vals).toString
  def createJSONObject(keys:Array[String], vals:Array[_]) = {
    val jo = new JSONObject
    keys.indices.foreach{i => 
      jo.put(keys.apply(i), vals.apply(i))
    }
    jo
  }

  def jsonStringToXML(s:String) = try scala.xml.XML.loadString("<JSON>"+org.json.XML.toString(new JSONObject(s))+"</JSON>") catch {
    case e:Any =>
      println(" [JSONStringToXML Error] (invalid JSON): "+s.take(100))
      if (Util.debug) {
        e.printStackTrace         
      }
      <error>{e.getMessage}</error>
  }
  trait JsonFormatted {
    val keys:Array[String]
    val vals:Array[Any]
    override def toString = createJSONString(keys, vals)
  }
}
