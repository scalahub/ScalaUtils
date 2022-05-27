name := "ScalaUtils"

ThisBuild / version := "1.0"

libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.4"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.0-M5"
  
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.0-M1"

libraryDependencies += "commons-codec" % "commons-codec" % "1.12"

libraryDependencies += "commons-io" % "commons-io" % "2.6"

libraryDependencies += "org.json" % "json" % "20140107"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.3.3"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided"

libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "9.4.18.v20190429"

libraryDependencies += "org.eclipse.jetty" % "jetty-servlet" % "9.4.18.v20190429"

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.4.18.v20190429"

libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.61"
