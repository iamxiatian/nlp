import ruc.nlp._

object HelloWorld extends App {
  println("Hello, world! :-)")
  val url = "http://news.ifeng.com/a/20160924/50017503_0.shtml"
  println(WebExtractor.extractArticle(url))
}

