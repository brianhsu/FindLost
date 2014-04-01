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
import android.widget.SectionIndexer

import TypedResource._

class SectionIndex(val sections: Vector[String], sortedItems: Vector[LostItem]) {

  val positionForSection = (0 until sections.size).map { sectionIndex => 
    sortedItems.indexWhere(_.formatedMonthDate == sections(sectionIndex))
  }

  val sectionForPosition = (0 until sortedItems.size).map { position =>
    sections.indexWhere(_ == sortedItems(position).formatedMonthDate)
  }

}

class ItemAdapter(context: Context, lostItems: Vector[LostItem]) extends BaseAdapter with Filterable with SectionIndexer
{
  import android.util.Log
  private lazy val inflater = LayoutInflater.from(context)
  private lazy val defaultSortedItems = lostItems.sortWith(_.formatedDateTime > _.formatedDateTime)
  private var sortedItems = defaultSortedItems

  private var sectionIndex = updateSectionIndex
  private def updateSectionIndex() = new SectionIndex(
    sortedItems.map(_.formatedMonthDate).distinct,
    sortedItems
  )

  override def getPositionForSection(sectionIndex: Int): Int = {
    val clippedIndex = sectionIndex min (this.sectionIndex.sections.size - 1)
    this.sectionIndex.positionForSection(clippedIndex)
  }

  override def getSectionForPosition(position: Int): Int = sectionIndex.sectionForPosition(position)
  override def getSections = sectionIndex.sections.toArray

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
        sectionIndex = updateSectionIndex()
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

