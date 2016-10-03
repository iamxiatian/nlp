package ruc.nlp

import org.zhinang.extract.auto.{ArticleExtractor, Article}
import org.zhinang.extract.auto.impl.ArticleExtractorImpl

/**
  * 正文自动抽取的Scala包装器
  * 
  * @author <a href="mailto:xiat@ruc.edu.cn">Tian Xia</a>
  */
object WebExtractor {
  def extractArticle(url: String): Article = {
    val articleExtractor:ArticleExtractor = new ArticleExtractorImpl(new org.zhinang.conf.Configuration)
    articleExtractor.extracting(url)
    articleExtractor.getArticle
  }
}
