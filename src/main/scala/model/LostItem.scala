package org.bone.findlost

import org.bone.findlost.AsyncUI._

import android.content.Context
import android.util.Log
import android.os.Environment
import android.util.Log

import scala.io.Source
import scala.concurrent._

import java.net.HttpURLConnection
import java.net.URL
import java.io.File
import java.io.PrintWriter
import java.io.FileNotFoundException

case class LostItem(invoiceID: String, department: String, dateTime: String, 
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

  def toLine = raw"""$invoiceID,$department,="${dateTime}",$location,$description"""

  def items = {
    description.replace("拾得人拾獲：", "").
                replace("，請失主於公告期間六個月內攜帶本人印章及身分證件前來認領。", "").
                replace("，請失主於公告期間15日內攜帶本人印章及身分證件前來認領。", "").
                replace("，請失主於公告期間內攜帶本人印章及身分證件前來認領。", "").
                replace(".00", "")
  }
}

object LostItem {

  val IncorrectFormatException = new Exception("Incorrect format from data source URL")
  val UsingMobileConnectionException = new Exception("Using mobile data connection")
  val NoNetworkException = new Exception("No active network")

  val LostItemCacheFileDir = "cachedFile"
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

  def getLostItemData(context: Context, allowMobile: Boolean, isRefresh: Boolean) = {

    def fromNetwork = getDataFromNetwork(context, allowMobile) recoverWith {
      case IncorrectFormatException => getDataFromNetwork(context, allowMobile)
    }

    isRefresh match {
      case true => fromNetwork
      case false => getGroupsFromCacheDir(context) recoverWith { 
        case e: FileNotFoundException => fromNetwork
      }
    }
  }

  def getLostItems(context: Context, dateList: List[String]): Future[Vector[LostItem]] = {

    def getItemsFromFile(date: String): List[LostItem] = {

      val cacheFile = new File(getCacheDir(context), date)
      val source = Source.fromFile(cacheFile)
      val lostItemsLines = source.getLines.toList
      lostItemsLines.flatMap (line => LostItem(line))
    }

    future {
      var items: List[LostItem] = Nil

      for {
        date <- dateList
        item <- getItemsFromFile(date)
      } { items ::= item }

      items.sortWith(_.formatedDate > _.formatedDate).toVector
    }
  }

  private def getGroupsFromCacheDir(context: Context): Future[List[Group]] = future {

    val cacheDir = getCacheDir(context)

    if (!cacheDir.exists) {
      throw new FileNotFoundException
    }

    val cacheFileList = cacheDir.listFiles.toList

    cacheFileList.isEmpty match {
      case true  => throw new FileNotFoundException
      case false => cacheFileList.map(filename => new Group(filename.getName, false))
    }
  }

  private def getDataFromNetwork(context: Context, 
                                 allowMobile: Boolean): Future[List[Group]] = future {

    if (NetworkState.getNetworkType(context).isEmpty) {
      throw NoNetworkException
    }

    if (!allowMobile && NetworkState.isUsingMobileDataConnection(context)) {
      throw UsingMobileConnectionException
    }

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
      val sortedItems = lostItems.sortWith(_.dateTime > _.dateTime)
      val groupedItems = sortedItems.groupBy(_.formatedDate)

      groupedItems.foreach { case(date, items) =>
        writeFileToCache(context, date, items)
      }

      groupedItems.keys.map(title => new Group(title, false)).toList
    }

  }

  private def writeFileToCache(context: Context, formatedDate: String, 
                               itemList: List[LostItem]) {

    val cacheFile = new File(getCacheDir(context), formatedDate)
    val writer = new PrintWriter(cacheFile)
    itemList.foreach { item => writer.println(item.toLine) }
    writer.close()
  }

  private def getCacheDir(context: Context): File = {

    val isSDCardMounted = Environment.getExternalStorageState == Environment.MEDIA_MOUNTED
    val cacheFile = isSDCardMounted match {
      case true => new File(context.getExternalCacheDir(), LostItemCacheFileDir)
      case false => new File(context.getCacheDir(), LostItemCacheFileDir)
    }

    cacheFile.mkdirs()
    cacheFile
  }
}
