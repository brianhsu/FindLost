package org.bone.findlost

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.CheckBox
import android.widget.Filterable
import android.widget.Filter


import TypedResource._

class ItemAdapter(context: Context, lostItems: Vector[LostItem]) extends BaseAdapter with Filterable
{
  private lazy val inflater = LayoutInflater.from(context)
  private lazy val defaultSortedItems = lostItems.sortWith(_.formatedDateTime > _.formatedDateTime)
  private var sortedItems = defaultSortedItems

  private lazy val filter = new Filter() {
    import Filter.FilterResults

    private def getDefaultResults = {
      val results = new FilterResults
      results.count = defaultSortedItems.size
      results.values = defaultSortedItems
      results
    }

    private def getSearchResults(constraint: String) = {
      val columns = constraint.split("⌘")
      val mustHaveKeywords = if (columns.size >= 1) columns(0).split("\\s+").toList else Nil
      val optionalKeywords = if (columns.size >= 2) columns(1).split("\\s+").toList else Nil
      val afterFilter = defaultSortedItems.filter(_.hasKeywords(mustHaveKeywords, optionalKeywords))

      val results = new FilterResults
      results.count = afterFilter.size
      results.values = afterFilter
      results
    }

    override def performFiltering(constraint: CharSequence): FilterResults = {

      val isEmptySearchBar = (constraint == null || constraint.toString.trim.size == 0 || constraint.toString.trim == "⌘")
      isEmptySearchBar match {
        case true  => getDefaultResults
        case false => getSearchResults(constraint.toString)
      }
    }

    override def publishResults(constraint: CharSequence, results: FilterResults) {
      if (results.values != sortedItems) {
        sortedItems = results.values.asInstanceOf[Vector[LostItem]]
        notifyDataSetChanged()
      }
    }
  }

  override def getFilter = filter
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

