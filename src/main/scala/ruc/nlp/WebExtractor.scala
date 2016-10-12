package ruc.nlp

import org.zhinang.extract.ExtractorConf
import org.zhinang.extract.auto.{ArticleExtractor, Article}
import org.zhinang.extract.auto.impl.ArticleExtractorImpl

/**
  * 正文自动抽取的Scala包装器
  * 
  * @author <a href="mailto:xiat@ruc.edu.cn">Tian Xia</a>
  */
object WebExtractor {
  val conf = ExtractorConf.create.setBoolean("extractor.mining.sentiment", true)

  def extractArticle(url: String): Article = {
    val articleExtractor:ArticleExtractor = new ArticleExtractorImpl(conf)
    articleExtractor.extracting(url)
    articleExtractor.getArticle
  }

  def extractArticle(url: String, content:String): Article = {
    val articleExtractor:ArticleExtractor = new ArticleExtractorImpl(conf)
    articleExtractor.extracting(url, content)
    articleExtractor.getArticle
  }
}
