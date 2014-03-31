package org.bone.findlost

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.CheckBox

import TypedResource._

class ItemAdapter(context: Context, lostItems: Vector[LostItem]) extends BaseAdapter
{
  private lazy val inflater = LayoutInflater.from(context)

  private lazy val sortedItems = lostItems.sortWith(_.formatedDateTime > _.formatedDateTime)

  override def getCount: Int = sortedItems.size
  override def getItem(position: Int): Object = sortedItems(position)
  override def getItemId(position: Int): Long = position
  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val view = convertView match {
      case view: View => convertView
      case _ => inflater.inflate(R.layout.lost_item_list_row, null)
    }

    val item = sortedItems(position)
    val rowTitle = view.findView(TR.lostItemListTitle)
    val rowLocation = view.findView(TR.lostItemListLocation)
    val rowDate = view.findView(TR.lostItemListDate)

    rowTitle.setText(item.items)
    rowLocation.setText(item.location)
    rowDate.setText(item.formatedDateTime)

    view
  }
}

