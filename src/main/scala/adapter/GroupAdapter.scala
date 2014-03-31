package org.bone.findlost

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.CheckBox

import TypedResource._

class GroupAdapter(context: Context, groups: List[Group]) extends BaseAdapter
{
  case class GroupViewTag(title: TextView, checkBox: CheckBox)

  private lazy val inflater = LayoutInflater.from(context)
  private var sortedGroup = groups.sortWith(_.title > _.title)

  def hasItemSelected = sortedGroup.exists(_.isSelected)
  def toggleState(position: Int, view: View) {
    val newState = sortedGroup(position).toggleState
    view.getTag.asInstanceOf[GroupViewTag].checkBox.setChecked(newState)
  }

  override def getCount: Int = sortedGroup.size
  override def getItem(position: Int): Object = sortedGroup(position)
  override def getItemId(position: Int): Long = position
  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {

    val view = inflater.inflate(R.layout.lost_item_group_row, null)
    val rowTitle = view.findView(TR.groupTitle)
    val rowCheckbox = view.findView(TR.groupCheckbox)
    rowTitle.setText(sortedGroup(position).title)
    rowCheckbox.setChecked(sortedGroup(position).isSelected)
    view.setTag(GroupViewTag(rowTitle, rowCheckbox))
    view
  }
}

