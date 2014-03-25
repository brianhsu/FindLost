package org.bone.findlost

import scala.io.Source
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

case class LostItem(id: String, department: String, dateTime: String, 
                    location: String, description: String) 
{
  def items = {
    description.replace("拾得人拾獲：", "").
                replace("，請失主於公告期間六個月內攜帶本人印章及身分證件前來認領。", "").
                replace(".00", "")
  }
}

object LostItem {

  val DataSourceURL = "http://data.moi.gov.tw/DownLoadFile.aspx?sn=44&type=CSV"

  def apply(line: String): Option[LostItem] = {
    line.split(",").toList match {
      case id :: department :: rawDateTime :: location :: description :: Nil => 
        Some(new LostItem(id, department, rawDateTime.substring(2, 14), location, description))
      case id :: department :: rawDateTime :: location :: rest =>
        Some(new LostItem(id, department, rawDateTime.substring(2, 14), location, rest.mkString))
      case _ => None
    }
  }

  def getDataFromNetwork: Future[List[LostItem]] = future {
    val source = Source.fromURL(DataSourceURL)("UTF-8")
    source.getLines.flatMap(line => LostItem.apply(line)).toList
  }
}
