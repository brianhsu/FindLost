package org.bone.findlost

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.widget.BaseAdapter
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.BaseExpandableListAdapter
import android.util.Log

case class Group(title: String, var isSelected: Boolean) {
  def toggleState = {
    isSelected = !isSelected
    isSelected
  }
}

class MyAdapter(context: Context, groups: List[Group]) extends BaseAdapter
{
  import TypedResource._
  import android.widget.CompoundButton.OnCheckedChangeListener
  import android.widget.CompoundButton
  import android.widget.TextView
  import android.widget.CheckBox

  case class ViewTag(title: TextView, checkBox: CheckBox)

  private lazy val inflater = LayoutInflater.from(context)
  private var sortedGroup = groups.sortWith(_.title > _.title)

  def hasItemSelected = sortedGroup.exists(_.isSelected)
  def toggleState(position: Int, view: View) {
    val newState = sortedGroup(position).toggleState
    view.getTag.asInstanceOf[ViewTag].checkBox.setChecked(newState)
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
    view.setTag(ViewTag(rowTitle, rowCheckbox))
    view
  }
}

class MainActivity extends Activity with TypedViewHolder with AsyncUI
{
  import scala.concurrent._
  import scala.concurrent.duration._
  import android.util.Log
  import scala.util.Failure
  import scala.util.Success
  import android.view.Menu
  import android.view.MenuItem

  private val Tag = "FindLost"
  private var actionShowDetailHolder: Option[MenuItem] = None

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater = getMenuInflater
    inflater.inflate(R.menu.main_activity_actions, menu)
    actionShowDetailHolder = Option(menu.findItem(R.id.mainActivityActionShowDetail)).map(_.asInstanceOf[MenuItem])
    setActionShowDetailEnabled(false)
    super.onCreateOptionsMenu(menu)
  }

  override def onCreate(savedInstanceState: Bundle)  {

    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    val lostItems = LostItem.getDataFromNetwork recoverWith {
      case LostItem.IncorrectFormatException => LostItem.getDataFromNetwork
    }

    lostItems.onComplete {
      case Failure(e)    => Log.v(Tag, "Error occurs:" + e)
      case Success(list) => runOnUiThread {
        val listView = findView(TR.lostItemList)
        val indicator = findView(TR.lostItemListLoadingIndicator)
        val dateGroup = list.map(x => new Group(x.formatedDate, false)).distinct
        val adapter = new MyAdapter(this, dateGroup.distinct)

        listView.setAdapter(adapter)
        indicator.setVisibility(View.GONE)

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
            adapter.toggleState(position, view)
            setActionShowDetailEnabled(adapter.hasItemSelected)
          }
        })
      }
    }
  }

  def onActionShowDetailClicked(menuItem: MenuItem) {
    Log.v(Tag, "on actionShowDetail clicked...")
  }

  def setActionShowDetailEnabled(isEnabled: Boolean) {
    actionShowDetailHolder.foreach(_.setEnabled(isEnabled).setVisible(isEnabled))
  }
}
