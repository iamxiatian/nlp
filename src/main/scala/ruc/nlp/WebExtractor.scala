package ruc.nlp

import org.zhinang.extract.ExtractorConf
import org.zhinang.extract.auto.{Article, ArticleExtractor}
import org.zhinang.extract.auto.impl.ArticleExtractorImpl
import org.zhinang.nlp.keyword.KeywordExtractor
import org.zhinang.nlp.similarity.Fingerprint
import org.zhinang.nlp.sentiment.SentimentParser
import org.zhinang.protocol.http.HttpClientAgent

/**
  * 自动抽取的Scala包装器
  * 
  * @author <a href="mailto:xiat@ruc.edu.cn">Tian Xia</a>
  */
object WebExtractor {
  val conf = ExtractorConf.create
    .setBoolean("extractor.mining.sentiment", true)
    .setBoolean("nlp.keyword.show.tf", true)
    .setInt("extractor.keywords.topN", 15)

  val client = new HttpClientAgent(conf)

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

  def extractKeywords(title: String, content: String) = {
    val keywordExtractor = new KeywordExtractor(conf)
    keywordExtractor.extractAsString(title, content, conf.getInt("extractor.keywords.topN", 10))
  }

  def fetch(url:String, refer: String) = {
    val response = client.execute(url, refer)
    (response.getContentType, response.getContent)
  }

  def fingerprint(title: String, content: String) = {
    val keywordExtractor = new KeywordExtractor(conf, false)
    //Fingerprint.fingerprint(title, keywordExtractor.extractAsList(title, content, 5))
    //只用标题计算指纹
    Fingerprint.fingerprint(title, keywordExtractor.extractAsList(title, title, 5))
  }

  def sentiment(title:String, content: String) = {
    SentimentParser.parse(conf, title).getSentiment*0.8 + SentimentParser.parse(conf, content).getSentiment*0.2
  }
}
