
package org.sh.utils.common.servlet

import javax.servlet.http.HttpServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.server.handler.HandlerList

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.webapp.WebAppContext

object RunServlets {
  var server:Server = _
  def load(servlets:List[HttpServlet], htmlDir:String, port:Int, url:String):Unit = {
    println(" ===========> HTML dir is "+htmlDir)     

    // for html
    val htmlFiles = new ResourceHandler
    htmlFiles.setResourceBase(htmlDir)
    
    // for servlets
    val context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    servlets.foreach(x => context.addServlet(new ServletHolder(x),"/"+x.getClass.getSimpleName+"/*"))
    
    //for jsp
//    val jspHandler = new WebAppContext(htmlDir, "/")
    val jspHandler = new WebAppContext()
    jspHandler.setContextPath("/");
    jspHandler.setResourceBase(htmlDir);
        
    
                                       
    val handlers = new HandlerList();
    handlers.setHandlers(Array(context, htmlFiles, jspHandler))    
//    handlers.setHandlers(Array(jspHandler))    
    server = new Server(port);
    server.setHandler(handlers);
    
    


    server.start();
    java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
    server.join();    
 
    
// for localhost:port/admin/index.html and whatever else is in the webapp directory
//final URL warUrl = this.class.getClassLoader().getResource(WEBAPPDIR);
//final String warUrlString = warUrl.toExternalForm();
//server.setHandler(new WebAppContext(warUrlString, CONTEXTPATH));
 
// for localhost:port/servlets/cust, etc.
//final Context context = new Context(server, "/servlets", Context.SESSIONS);
//context.addServlet(new ServletHolder(new CustomerServlet(whatever)), "/cust");
//context.addServlet(new ServletHolder(new UserServlet(whatever)), "/user");
 
    
    
  }

  def unload = server.stop
}