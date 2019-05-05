package org.sh.utils.common.curl

import org.sh.utils.common.Util._
import java.net.URL
import java.io.StringWriter
import java.net.HttpURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader

import org.sh.utils.common.json.JSONUtil
import org.sh.utils.common.Util.debug

import scala.concurrent.Future
import scala.util.Try
import scala.xml.XML

@deprecated("Use CurlAlt")
object Curl {
  
  val defaultUserAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.1"     

  val get = "GET"
  val post = "POST"
  // main curl method
  @deprecated("Does not release resources properly. Use the new one", "20 Oct 2017")
  def curl(url     : String, 
           headers : Array[(String, String)], 
           reqType : String, 
           params  : Array[(String, String)],
           userAgent: String=defaultUserAgent
          ):String = {
    if (debug) println(Console.RED+" [CURL] "+Console.RESET+s"USING OLD CURL FOR $url")
    val charset = "UTF-8";
    val paramsStr = if (params.size > 0) params.map(x => x._1+"="+java.net.URLEncoder.encode(x._2, charset)).reduceLeft((x, y) => x+"&"+y) else ""
    val actualUrl = if (reqType!=get || params.size == 0) url else url+"?"+paramsStr
    val httpcon = new URL(actualUrl).openConnection().asInstanceOf[HttpURLConnection]
    try {
      httpcon.addRequestProperty("User-Agent", userAgent);     
      headers.foreach(x => httpcon.addRequestProperty(x._1, x._2))
      httpcon.setRequestMethod(reqType)
      if (reqType == post){
        httpcon.setDoOutput(true); // Triggers POST.
        using(httpcon.getOutputStream) { output => output.write(paramsStr.getBytes(charset)) }
      }

      var line = ""
      var str = ""
      val respCode = httpcon.getResponseCode
      if (respCode >= 400 && respCode <= 500) {
        if (debug) Try {
          using(httpcon.getErrorStream){responseStream => 
            using(new InputStreamReader(responseStream)) {isr =>
              using(new BufferedReader(isr)) {reader =>
                while({line = reader.readLine; line != null}) str = str + line
              }
            }
          }
          println(s" [CURL] Error querying $url. Code "+respCode)
          println(str)
        } 
        throw new Exception(s"Error querying $url. Code "+respCode)
      }
      using(httpcon.getInputStream){responseStream => 
        using(new InputStreamReader(responseStream)) {isr =>
          using(new BufferedReader(isr)) {reader =>
            while({line = reader.readLine; line != null}) str = str + line
          }
        }
      }
      str
    } finally httpcon.disconnect      
  }

  // wrapper 1 around main curl
  def curl(s:String, headers:Array[(String, String)], reqType:String):String = 
      curl(s, headers, reqType, Array[(String, String)]())

  // wrapper 2 around wrapper 1
  def curl(s:String, headers:Array[(String, String)]):String = 
      curl(s, headers, get) 

  // wrapper 3 around wrapper 2
  def curl(url:String):String = 
      curl(url, Array[(String, String)]())
  
  // wrapper 4 around wrapper 3
  def query(s:String, headers:Array[(String, String)]):String = 
      curl(s, headers) 

  // wrapper 5 around wrapper 4
  def query(s:String):String = 
      query(s,Array[(String, String)]()) 

  // wrapper 6 around wrapper 5
  def queryXML(s:String) = JSONUtil.jsonStringToXML(query(s))
  def queryXML(s:String, headers:Array[(String, String)]) = JSONUtil.jsonStringToXML(query(s, headers))
  def queryDirectXML(s:String) = XML.loadString(query(s))
}

import java.net.URL
import java.net.HttpURLConnection
import org.apache.commons.io.IOUtils

object CurlJsonData {
  def curlXML(url:String, jsonEncodedString:String) = JSONUtil.jsonStringToXML(curl(url, jsonEncodedString))
  
  def curl(url:String, jsonEncodedString:String) = {
    val httpcon = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
    httpcon.setDoOutput(true);
    httpcon.setRequestProperty("Content-Type", "application/json");
    httpcon.setRequestProperty("Accept", "application/json");
    httpcon.setRequestMethod("POST");
    httpcon.connect;
    
    val outputBytes = jsonEncodedString.getBytes("UTF-8");
    using(httpcon.getOutputStream){os =>
      os.write(outputBytes)
    }
    //https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
    val code = httpcon.getResponseCode
    val isError = code >= 400 && code <= 500
    val resp = using{
      // using method from here: https://stackoverflow.com/a/5218279/243233
      if (isError) httpcon.getErrorStream else httpcon.getInputStream
    }{is =>
      val writer = new StringWriter;
      IOUtils.copy(is, writer, "UTF-8");
      writer.toString;
    }
    httpcon.disconnect
    if (isError) throw new Exception(s"Resp code $code. Error: ${resp.take(200)}") else resp
  }
}
object CurlTest extends App {
  val urls = Seq(
    "http://www.google.com",
    "http://www.yahoo.com",
    "http://www.ebay.com"
  )
  import scala.concurrent.ExecutionContext.Implicits.global
  (1 to 100).map {i =>
    Future{
      val url = urls(rand % urls.size)
      val x = Curl.query(url).take(50)
      println(s"---> $i: $url: $x\n")
      s"$i -> $x"
    }
  }
  Thread.sleep(100000)
}