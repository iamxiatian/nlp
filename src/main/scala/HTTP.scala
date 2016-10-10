import java.util.concurrent.Executors

import scala.xml.XML

import org.http4s.MediaType.{`text/html`, `text/xml`}
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze._
import org.http4s.server.{Server, ServerApp}
import ruc.nlp._

import scalaz.concurrent.Task
import scala.concurrent.duration._


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
  implicit val scheduledThreadPool = Executors.newScheduledThreadPool(5)

  def extractArticleByUrlTask(url: String): Task[String] = Task {
    println("fetching " + url)
    val article = WebExtractor.extractArticle(url)
    article.toXML
  }.timed(5 second).handleWith {
    case e: java.util.concurrent.TimeoutException => Task.now((<error>{e}</error>) toString)
    case e: Throwable => Task.now((<error>{e}</error>) toString)
  }

  def extractArticleByContentTask(url: String, content:String): Task[String] = Task {
    println(s"fetching $url by provided content...")
    val article = WebExtractor.extractArticle(url, content)
    article.toXML
  }.timed(3 second).handleWith {
    case e: java.util.concurrent.TimeoutException => Task.now((<error>{e}</error>) toString)
    case e: Throwable =>
      Task.now({
        <error>
          <message>{e}</message>
          <url>{url}</url>
          <content>{content}</content>
        </error>
      }.toString)
  }

  /**
    * 执行挖掘任务，要求输入的文档内容格式为：
    * <article>
    *   <title>中国人民大学信息资源管理学院</title>
    *   <content>中国人民大学在中关村大街59号</content>
    * </article>
    */
  def miningArticleTask(body: String): Task[String] = Task {
    val doc = XML.loadString(body)
    val title = (doc \\ "title").text
    val content = (doc \\ "content").text

    {<result>
        <keyword>{title}</keyword>
        <finger>233</finger>
        <sentiment>0.2</sentiment>
      </result>
    }.toString
  }.timed(2 second).handleWith {
    case e: java.util.concurrent.TimeoutException => Task.now((<error>
      {e}
    </error>) toString)
    case e: Throwable =>
      Task.now({
        <error>
          <message>
            {e}
          </message>
          <received>
            {body}
          </received>
        </error>
      }.toString)
  }

  val service = HttpService {
    case req@GET -> Root =>
      Ok(Task {"""<p>接口：/api/extract?url=xxx</p>"""})
        .withContentType(Some(`Content-Type`(`text/html`)))

    case request@POST -> Root / "extract" =>
      //val inputStream = scalaz.stream.io.toInputStream(request.body)
      val body:String = EntityDecoder.decodeString(request).run //获取传递的内容
      //按行分割
      val lines = body.split("\n").toList
      lines match {
        //如果以http开始
        case x::xs if x.toLowerCase.startsWith("http") => Ok(extractArticleByContentTask(x, xs.mkString("\n")).run)

        case _ => Ok( Task.now({<error>
          <message>"""说明：POST的文本内容格式，第一行为URL地址，后面为该URL对应的网页源代码，例如：
          http://www.test.com/article01.html
          <html>
            html content...
          </html>
          """
          </message>
          <received>{body}</received>
        </error>} toString))
      }

    case request@GET -> Root / "extract" :? ExtractQueryParamMatcher(url) =>
      Ok(extractArticleByUrlTask(url).run)
        .putHeaders(`Content-Type`(`text/xml`))

      //根据传入的XML格式的文章标题和正文，进行关键词提取/指纹处理等任务。
    case request@POST -> Root / "mining" =>
      val body:String = EntityDecoder.decodeString(request).run //获取传递的内容
      Ok(miningArticleTask(body).run)
        .putHeaders(`Content-Type`(`text/xml`))

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
