package org.sh.utils.servlet

import javax.servlet.http.{HttpServlet, HttpServletRequest => HSReq, HttpServletResponse => HSResp}

abstract class AbstractClientServlet extends HttpServlet {
  // println ("getServletContext().getRealPath"+ getServletContext().getRealPath(""))
  //  System.setProperty("user.dir", "C:\\") //getServletContext().getRealPath(""))
  def extractString(s:String):String = s match {case null => ""; case _ => s.trim}
  def extractBoolean(s:String):Boolean = try {s.trim.toBoolean} catch {case _ : Throwable => true}
  var req:HSReq = null
  def answer:Any
  override def doPost(request: HSReq, response: HSResp) = doGet(request, response)
  override def doGet(request: HSReq, response: HSResp) = {
    req = request
    // System.setProperty("user.dir", getServletContext().getRealPath(""))
    response.getWriter.print(asXML(answer))
  }
  def asXML(m:Any):xml.Elem = m match {
    case a:List[_] => <list length={a.length.toString}>{a.map(asXML _)}</list>
    case s:String => <value>{s}</value>
    case _ => <unknown/>
  }
}
