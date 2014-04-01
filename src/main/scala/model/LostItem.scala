package org.bone.findlost

import android.content.Context
import android.util.Log
import android.os.Environment
import android.util.Log

import scala.io.Source
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import java.net.HttpURLConnection
import java.net.URL
import java.io.File
import java.io.PrintWriter
import java.io.FileNotFoundException

case class LostItem(id: String, department: String, dateTime: String, 
                    location: String, description: String) 
{
  private lazy val searchContent = s"$department $dateTime $location $description"

  def formatedDate = {
    val year = dateTime.substring(0,4)
    val month = dateTime.substring(4, 6)
    val day = dateTime.substring(6, 8)
    s"$year-$month-$day"
  }

  def formatedMonthDate = {
    val month = dateTime.substring(4, 6)
    val day = dateTime.substring(6, 8)
    s"$month-$day"
  }

  def formatedDateTime = {
    val year = dateTime.substring(0,4)
    val month = dateTime.substring(4, 6)
    val day = dateTime.substring(6, 8)
    val hour = dateTime.substring(8, 10)
    val miniute = dateTime.substring(10, 12)

    s"$year-$month-$day $hour:$miniute"
  }

  def hasKeywords(mustHaveKeywords: List[String], filterKeywords: List[String]) = {
    val hasMustHaveKeywords = mustHaveKeywords.forall(searchContent contains _)
    val optionalKeywordsNotSet = filterKeywords == Nil
    val hasOptionalKeywords = filterKeywords.exists(searchContent contains _)

    hasMustHaveKeywords && (optionalKeywordsNotSet || hasOptionalKeywords)

  }

  def toLine = s"$id,$dateTime,$location,$description"

  def items = {
    description.replace("拾得人拾獲：", "").
                replace("，請失主於公告期間六個月內攜帶本人印章及身分證件前來認領。", "").
                replace("，請失主於公告期間15日內攜帶本人印章及身分證件前來認領。", "").
                replace("，請失主於公告期間內攜帶本人印章及身分證件前來認領。", "").
                replace(".00", "")
  }
}

object LostItem {

  object IncorrectFormatException extends Exception("Incorrect format from data source URL")

  val LostItemCacheFileName = "cachedList.txt"
  val DataSourceURL = "http://data.moi.gov.tw/DownLoadFile.aspx?sn=44&type=CSV&nid=7317"

  def apply(line: String): Option[LostItem] = {
    line.split(",").toList match {
      case id :: department :: rawDateTime :: location :: description :: Nil => 
        Some(new LostItem(id, department, rawDateTime.substring(2, 14), location, description))
      case id :: department :: rawDateTime :: location :: rest =>
        Some(new LostItem(id, department, rawDateTime.substring(2, 14), location, rest.mkString))
      case _ => None
    }
  }

  def getLostItemData(context: Context) = {

    def fromNetwork = getDataFromNetwork(context) recoverWith {
      case IncorrectFormatException => getDataFromNetwork(context)
    }

    getDataFromCacheFile(context) recoverWith { case e: FileNotFoundException => fromNetwork }
  }

  private def getDataFromCacheFile(context: Context): Future[List[LostItem]] = future {
    Log.v("FindLost", "getDataFromCacheFile")
    val cacheFile = getCacheFile(context)

    if (!cacheFile.exists) {
      throw new FileNotFoundException
    }

    val source = Source.fromFile(cacheFile)
    val lostItemsLines = source.getLines.toList
    val lostItems = lostItemsLines.flatMap(line => LostItem(line))
    lostItems.sortWith(_.dateTime > _.dateTime)
  }

  private def getDataFromNetwork(context: Context): Future[List[LostItem]] = future {

    Log.v("FindLost", "getDataFromCacheNetwork")

    var items: List[LostItem] = Nil
    val url = new URL(DataSourceURL);
    var connection = url.openConnection().asInstanceOf[HttpURLConnection]

    connection.setReadTimeout(10000 /* milliseconds */)
    connection.setConnectTimeout(15000 /* milliseconds */)
    connection.setRequestMethod("GET")
    connection.setDoInput(true)
    connection.connect()

    if (connection.getResponseCode == -1) {
      throw IncorrectFormatException
    } else {
      val source = Source.fromInputStream(connection.getInputStream)("UTF-8")
      val lostItemsLines = source.getLines.toList
      val lostItems = lostItemsLines.flatMap(line => LostItem(line))
      writeFileToCache(context, lostItemsLines)
      lostItems.sortWith(_.dateTime > _.dateTime)
    }

  }

  private def writeFileToCache(context: Context, itemList: List[String]) {
    val cacheFile = getCacheFile(context)
    val writer = new PrintWriter(cacheFile)
    itemList.foreach { writer.println _ }
    writer.close()
  }

  private def getCacheFile(context: Context): File = {

    val isSDCardMounted = Environment.getExternalStorageState == Environment.MEDIA_MOUNTED
    val cacheFile = isSDCardMounted match {
      case true => new File(context.getExternalCacheDir(), LostItemCacheFileName)
      case false => new File(context.getCacheDir(), LostItemCacheFileName)
    }

    !cacheFile.getParentFile.mkdirs()
    cacheFile
  }
}
