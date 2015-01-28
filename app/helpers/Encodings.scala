package helpers

import java.net.URLEncoder
import play.api.mvc.Codec

object Encodings {

  def queryString(params: (String, Any)): String = queryString(Map(params))

  def queryString(params: TraversableOnce[(String, Any)]): String = queryString(params.toMap)

  def queryString(params: Map[String, Any]): String = {
    encodeValues(params).mkString("?", "&", "")
  }

  def postData(params: (String, Any)): String = postData(Map(params))

  def postData(params: TraversableOnce[(String, Any)]): String = postData(params.toMap)

  def postData(params: Map[String, Any]): String = {
    encodeValues(params).mkString("", "&", "")
  }

  private def encodeValues(params: Map[String, Any]): Iterable[String] = {
    val charset = Codec.utf_8.charset
    for {
      (name, value) <- params if value != None
      encodedValue = value match {
        case Some(v: TraversableOnce[Any]) => URLEncoder.encode(v.mkString(","), charset).replace("+", "%20")
        case v: TraversableOnce[Any]       => URLEncoder.encode(v.mkString(","), charset).replace("+", "%20")
        case Some(v)                       => URLEncoder.encode(v.toString, charset).replace("+", "%20")
        case v                             => URLEncoder.encode(v.toString, charset).replace("+", "%20")
      }
    } yield name + "=" + encodedValue
  }
}