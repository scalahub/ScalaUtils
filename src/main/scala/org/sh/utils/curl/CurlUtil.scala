package org.sh.utils.curl

import org.sh.utils.Util._
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.ArrayList

import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.sh.utils.json.JSONUtil

import scala.xml.XML

object Curl {

  val defaultUserAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.1"     

  val get = "GET"
  val post = "POST"

  def curl(
    url     : String, 
    headers : Array[(String, String)], 
    reqType : String, 
    params  : Array[(String, String)],
    userAgent: String=defaultUserAgent
  ):String = {
    /*
     * https://hc.apache.org/httpcomponents-client-ga/quickstart.html
     * https://stackoverflow.com/a/30207433/243233
     * 
     */
    if (debug) println(Console.YELLOW+" [CURL] "+Console.RESET+s"USING NEW CURL FOR $url")
    val http = reqType match {
      case `get` => 
        val charset = "UTF-8";
        val paramsStr = if (params.size > 0) params.map(x => x._1+"="+java.net.URLEncoder.encode(x._2, charset)).reduceLeft((x, y) => x+"&"+y) else ""
        val actualUrl = if (params.size == 0) url else url+"?"+paramsStr
        val httpGet = new HttpGet(actualUrl);
        httpGet
      case `post` => 
        val nvps = new ArrayList[NameValuePair]();
        val httpPost = new HttpPost(url);
        params.foreach{
          case (name, value) =>
            nvps.add(new BasicNameValuePair(name, value));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));          
        httpPost
      case any => throw new Exception(s"Unsupported req type $any")
    }
    headers.foreach{
      case (key, value) =>
        http.addHeader(key, value)
    }
    http.setHeader("User-Agent", userAgent);
    using(HttpClients.createDefault()){httpclient => 
      using (httpclient.execute(http)) {resp =>
        val entity = resp.getEntity
        val answer = using(new InputStreamReader(entity.getContent)){streamReader =>
          using(new BufferedReader(streamReader)){reader =>
            var line = ""
            var str = ""
            while({line = reader.readLine; line != null}) str = str + line
            str
          }
        }
        EntityUtils.consume(entity)
        answer
      }
    }

  }

  // wrapper 1 around main curl
  def curl(url:String, headers:Array[(String, String)], reqType:String):String =
      curl(url, headers, reqType, Array[(String, String)]())

  // wrapper 2 around wrapper 1
  def curl(url:String, headers:Array[(String, String)]):String =
      curl(url, headers, get)

  // wrapper 3 around wrapper 2
  def curl(url:String):String = 
      curl(url, Array[(String, String)]())
  
  // wrapper 4 around wrapper 3
  def query(url:String, headers:Array[(String, String)]):String =
      curl(url, headers)

  // wrapper 5 around wrapper 4
  def query(url:String):String =
      query(url,Array[(String, String)]())

  // wrapper 6 around wrapper 5
  def queryXML(url:String) = JSONUtil.jsonStringToXML(query(url))
  def queryXML(url:String, headers:Array[(String, String)]) = JSONUtil.jsonStringToXML(query(url, headers))
  def queryDirectXML(url:String) = XML.loadString(query(url))
}

object CurlJsonData {
  // https://stackoverflow.com/questions/12059278/how-to-post-json-request-using-apache-httpclient
  // https://stackoverflow.com/questions/13743205/how-to-add-set-and-get-header-in-request-of-httpclient
  def curl(url:String, jsonEncodedString:String) = {
    val http = new HttpPost(url)
    http.setEntity(new StringEntity(jsonEncodedString, ContentType.APPLICATION_JSON))

    using(HttpClients.createDefault()){httpclient => 
      using (httpclient.execute(http)) {resp =>
        val entity = resp.getEntity
        val answer = using(new InputStreamReader(entity.getContent)){streamReader =>
          using(new BufferedReader(streamReader)){reader =>
            var line = ""
            var str = ""
            while({line = reader.readLine; line != null}) str = str + line
            str
          }
        }
        EntityUtils.consume(entity)
        //println("ANSWER -> "+answer)
        answer
      }
    }
  }
}
