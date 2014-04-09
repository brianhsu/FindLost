package org.bone.findlost

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor

import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues

object StarList {
  val DBVersion = 1
  val DBName = "FindLost.db"
  val TableSchema = """
    |CREATE TABLE StarList (
    |  _id INTEGER PRIMARY KEY AUTOINCREMENT,
    |  invoiceID TEXT NOT NULL,
    |  department TEXT NOT NULL,
    |  dateTime TEXT NOT NULL,
    |  location TEXT NOT NULL,
    |  description TEXT NOT NULL
    |);
  """.stripMargin
}

class StarList(context: Context) {

  private var database = dbOpenHelper.getWritableDatabase

  private def dbOpenHelper = {
    new SQLiteOpenHelper(context, StarList.DBName, null, StarList.DBVersion) {
      override def onCreate(database: SQLiteDatabase) {
        database.execSQL(StarList.TableSchema)
      }

      override def onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
  }

  private def cursorToLostItem(cursor: Cursor): LostItem = {
    LostItem(
      invoiceID = cursor.getString(0),
      department = cursor.getString(1),
      dateTime = cursor.getString(2),
      location = cursor.getString(3),
      description = cursor.getString(4)
    )
  }

  def isInStarList(item: LostItem): Boolean = {
    val cursor = database.query(
      "StarList", 
      Array("invoiceID"), 
      "invoiceID = ?", Array(item.invoiceID), null, null, "dateTime", null
    )
    val count = cursor.getCount

    cursor.close
    count > 0
  }

  def insertToStarList(item: LostItem) {
    val contentValues = new ContentValues
    contentValues.put("invoiceID", item.invoiceID)
    contentValues.put("department", item.department)
    contentValues.put("dateTime", item.dateTime)
    contentValues.put("location", item.location)
    contentValues.put("description", item.description)
    database.insert("StarList", null, contentValues)
  }

  def removeFromStarList(item: LostItem) {
    database.delete("StarList", "invoiceID = ?", Array(item.invoiceID))
  }

  def getStarItems(): List[LostItem] = {
    var lostItems: List[LostItem] = Nil
    val cursor = database.query(
      "StarList", 
      Array("invoiceID", "department", "dateTime", "location", "description"), 
      null, null, null, null, "dateTime", null
    )

    cursor.moveToFirst()

    while (!cursor.isAfterLast) {
      lostItems ::= cursorToLostItem(cursor)
      cursor.moveToNext()
    }

    cursor.close()
    lostItems
  }

  def open() {
    database = dbOpenHelper.getWritableDatabase
  }

  def close() {
    database.close()
  }

}
