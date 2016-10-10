organization  := "ruc.nlp"
version       := "0.1"
scalaVersion  := "2.11.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

fork in run := true
cancelable in Global := true

libraryDependencies  ++= Seq(
  // other dependencies here
//  "org.scalanlp" %% "breeze" % "0.12",
  // native libraries are not included by default. add this if you want them (as of 0.7)
  // native libraries greatly improve performance, but increase jar sizes. 
  // It also packages various blas implementations, which have licenses that may or may not
  // be compatible with the Apache License. No GPL code, as best I know.
//  "org.scalanlp" %% "breeze-natives" % "0.12"

  // the visualization library is distributed separately as well. 
  // It depends on LGPL code.
  // "org.scalanlp" %% "breeze-viz" % "0.12"
)

val http4sVersion = "0.14.7"
//val http4sVersion = "0.15.0a-SNAPSHOT"
libraryDependencies ++= Seq(
  //"com.chuusai" %% "shapeless" % "2.3.2",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion
//  "org.http4s" % "http4s-blaze-client" % http4sVersion
)

//for http post test
//libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0"

libraryDependencies += "" %% "scopt" % "3.5.0"

//command line parser
libraryDependencies += "com.github.scopt" % "scopt" % "3.5.0"

//add jars for zhinang modules
libraryDependencies += "commons-cli" % "commons-cli" % "1.2"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.1"
libraryDependencies += "com.google.guava" % "guava" % "18.0"
libraryDependencies += "org.bouncycastle" % "bcpg-jdk15on" % "1.55" force()
//libraryDependencies += "bouncycastle" % "bcprov-jdk14" % "138"


//http jars
libraryDependencies += "org.apache.james" % "apache-mime4j-core" % "0.7.2"
libraryDependencies += "commons-codec" % "commons-codec" % "1.6"
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.0"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.3.4"
libraryDependencies += "xalan" % "xalan" % "2.7.1"

//NLP libraries
libraryDependencies += "com.hankcs" % "hanlp" % "portable-1.2.11"

//HTML Process
libraryDependencies += "org.jsoup" % "jsoup" % "1.9.2"
//libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "3.4.1"

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq(
  // other resolvers here
  // if you want to use snapshot builds (currently 0.12-SNAPSHOT), use this.
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

assemblyJarName in assembly := "nlp.jar"
test in assembly := {}
mainClass in assembly := Some("HTTP")

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case "logback.xml"                                 => MergeStrategy.last
  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
