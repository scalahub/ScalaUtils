package org.sh.utils.curl

import Curl._

object TestCurl extends App {
  type Headers = Array[(String, String)]
  type Params = Array[(String, String)]

  case class ReqType(text:String)
  object Post extends ReqType(post)
  object Get extends ReqType(get)
  object PostJson extends ReqType(postJson)

  // curl -X GET "https://postman-echo.com/get?foo1=bar1&foo2=bar2"
  case class TestVector(url:String, headers:Headers, reqType:ReqType, params:Params, expected:String)

  val tvs = Seq(
    TestVector(
      "https://postman-echo.com/get", Array(), Get,
      Array(
        ("foo1", "bar1"),
        ("foo2", "bar2")
      ),
      """{"args":{"foo1":"bar1","foo2":"bar2"},"headers":{"x-forwarded-proto":"https","host":"postman-echo.com","accept-encoding":"gzip,deflate","user-agent":"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.1","x-forwarded-port":"443"},"url":"https://postman-echo.com/get?foo1=bar1&foo2=bar2"}""".stripMargin.trim
    ),
    TestVector(
      "https://postman-echo.com/post", Array(), Post,
      Array(
        ("foo1", "bar1"),
        ("foo2", "bar2")
      ),
      """{"args":{},"data":"","files":{},"form":{"foo1":"bar1","foo2":"bar2"},"headers":{"x-forwarded-proto":"https","host":"postman-echo.com","content-length":"19","accept-encoding":"gzip,deflate","content-type":"application/x-www-form-urlencoded","user-agent":"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.1","x-forwarded-port":"443"},"json":{"foo1":"bar1","foo2":"bar2"},"url":"https://postman-echo.com/post"}"""
    ),
    TestVector(
      "https://postman-echo.com/post", Array(), PostJson,
      Array(
        ("foo1", "bar1"),
        ("foo2", "bar2")
      ),
      """{"args":{},"data":{"foo1":"bar1","foo2":"bar2"},"files":{},"form":{},"headers":{"x-forwarded-proto":"https","host":"postman-echo.com","content-length":"29","accept-encoding":"gzip,deflate","content-type":"application/json; charset=UTF-8","user-agent":"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.1","x-forwarded-port":"443"},"json":{"foo1":"bar1","foo2":"bar2"},"url":"https://postman-echo.com/post"}"""
    )
  )

  tvs.foreach{
    case TestVector(url, headers, reqType, params, expected) =>
      val actual = curl(url, headers, reqType.text, params)
      assert(actual == expected, s"Expected:\n $expected.\nActual:\n $actual")
  }
}
