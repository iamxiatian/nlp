import org.http4s.MediaType.{`text/html`, `text/xml`}
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze._
import org.http4s.server.{Server, ServerApp}
import ruc.nlp._

import scalaz.concurrent.Task

object MyService {
  val service = HttpService {
    case _ => Ok(Task {
      println("hello world!")
      "hello world!"
    })
  }
}

object ExtractQueryParamMatcher extends QueryParamDecoderMatcher[String]("url")

object ExtractService {

  def extractArticleTask(url: String): Task[String] = Task {
    println("fetching " + url)
    val article = WebExtractor.extractArticle(url)
    article.toXML
  }.handleWith {
    case e: Throwable =>
      e.printStackTrace
      Task.now({
        <error>
          {e}
        </error>
      }.toString)
  }

  val service = HttpService {
    case req@GET -> Root =>
      Ok(Task {"""<p>接口：/api/extract?url=xxx</p>"""})
        .withContentType(Some(`Content-Type`(`text/html`)))

    case request@GET -> Root / "extract" :? ExtractQueryParamMatcher(url) =>
      Ok(extractArticleTask(url).run)
        .putHeaders(`Content-Type`(`text/xml`))

    //    case GET -> Root / "extract" /url => {
    //      Ok(s"Extract from $url.")
    //    }
    case _ => Ok(Task {
      "echo!"
    })
  }
}

object HTTP extends ServerApp {
  override def server(args: List[String]): Task[Server] = {
    case class Config(host: String = "localhost",
                      port: Int = 8080,
                      path: String = "/api")

    val parser = new scopt.OptionParser[Config]("HTTP API") {
      head("Web Article Extractor", "2.8")

      opt[String]('h', "host").action((x, c) =>
        c.copy(host = x)).text("listen address")

      opt[Int]('p', "port").action((x, c) =>
        c.copy(port = x)).text("listen port")

      opt[String]('a', "path").action((x, c) =>
        c.copy(path = x)).text("servlet context path, default is /api")


      help("help").text("prints this usage text")

      note("\n xiatian, xia@ruc.edu.cn.")
    }

    // parser.parse returns Option[C]
    val (host: String, port: Int, path: String) = parser.parse(args, Config()) match {
      case Some(config) =>
        println(s"Listen on address $config")
        (config.host, config.port, config.path)
      case None => println("Wrong parameters, use default settings.")
        ("localhost", 8080, "/api")
    }

    BlazeBuilder.bindHttp(port, host)
      .mountService(ExtractService.service, path)
      .start
  }
}
