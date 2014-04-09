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

  val maxSectionIndex = sections.size - 1
  val positionForSection = for {
    sectionIndex <- (0 until sections.size)
  } yield { sortedItems.indexWhere(_.formatedMonthDate == sections(sectionIndex)) }

  val sectionForPosition = for {
    position <- (0 until sortedItems.size)
  } yield { sections.indexWhere(_ == sortedItems(position).formatedMonthDate) }

}

class ItemAdapter(context: Context, lostItems: Vector[LostItem]) extends BaseAdapter with Filterable with SectionIndexer
{
  private lazy val inflater = LayoutInflater.from(context)
  private var defaultSortedItems = lostItems.sortWith(_.formatedDateTime > _.formatedDateTime)
  private var sortedItems = defaultSortedItems
  private lazy val starList = new StarList(context)

  // SectionIndex API
  private var sectionIndex = updateSectionIndex

  private def updateSectionIndex() = new SectionIndex(
    sortedItems.map(_.formatedMonthDate).distinct,
    sortedItems
  )

  def removeItem(item: LostItem) {
    defaultSortedItems = defaultSortedItems.filterNot(_ == item)
    sortedItems = sortedItems.filterNot(_ == item)
    notifyDataSetChanged()
  }

  override def getSections = sectionIndex.sections.toArray
  override def getSectionForPosition(position: Int): Int = sectionIndex.sectionForPosition(position)
  override def getPositionForSection(sectionIndex: Int): Int = {
    val clippedIndex = sectionIndex min this.sectionIndex.maxSectionIndex
    this.sectionIndex.positionForSection(clippedIndex)
  }

  // Filterable API
  override def getFilter = filter
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

  // BaseAdapter API
  override def getCount: Int = sortedItems.size
  override def getItem(position: Int): Object = sortedItems(position)
  override def getItemId(position: Int): Long = position
  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val view = convertView match {
      case view: View => convertView
      case _ => 
        val view = inflater.inflate(R.layout.row_lost_item_list, null)
        val viewTag = new ViewTag(
          title = view.findView(TR.lostItemListTitle),
          location = view.findView(TR.lostItemListLocation),
          date = view.findView(TR.lostItemListDate),
          star = view.findView(TR.lostItemListStar)
        )
        view.setTag(viewTag)
        view
    }

    val item = sortedItems(position)
    val viewTag = view.getTag.asInstanceOf[ViewTag]
    viewTag.title.setText(item.items)
    viewTag.location.setText(item.location)
    viewTag.date.setText(item.formatedDateTime)
    starList.isInStarList(item) match {
      case false => viewTag.star.setVisibility(View.GONE)
      case true => viewTag.star.setVisibility(View.VISIBLE)
    }
    view
  }

  def updateStarView() = {
    notifyDataSetChanged()
  }
}

import android.widget.TextView
import android.widget.ImageView

case class ViewTag(title: TextView, location: TextView, date: TextView, star: ImageView)

