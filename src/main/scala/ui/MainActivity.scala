package org.bone.findlost

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView

import scala.concurrent._
import scala.concurrent.duration._

import AsyncUI._

class MainActivity extends Activity with TypedViewHolder
{

  private implicit val runOnUIActivity = this

  private var actionShowDetailHolder: Option[MenuItem] = None
  private lazy val indicator = findView(TR.lostItemListLoadingIndicator)
  private lazy val adapterHolder: Future[GroupAdapter] = initLoadingData()

  def groupingFunction(lostItem: LostItem): String = lostItem.formatedDate

  def initLoadingData(): Future[GroupAdapter] = {
    val lostItemsFuture = LostItem.getDataFromNetwork recoverWith {
      case LostItem.IncorrectFormatException => LostItem.getDataFromNetwork
    }

    lostItemsFuture.map { lostItems =>
      new GroupAdapter(this, lostItems, groupingFunction _)
    }
  }

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

    adapterHolder.runOnUIThread { adapter => showDateGroupListView(adapter) }
  }

  def showDateGroupListView(adapter: GroupAdapter) {
    setLoadingIndicatorState(false)

    val listView = findView(TR.lostItemList)

    listView.setAdapter(adapter)
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
        adapter.toggleState(position, view)
        setActionShowDetailEnabled(adapter.hasItemSelected)
      }
    })
  }

  def setLoadingIndicatorState(isLoading: Boolean) {
    isLoading match {
      case true => indicator.setVisibility(View.VISIBLE)
      case false => indicator.setVisibility(View.GONE)
    }
  }

  def onActionShowDetailClicked(menuItem: MenuItem) {
    adapterHolder.runOnUIThread { adapter =>
      val selected = adapter
    }
  }

  def setActionShowDetailEnabled(isEnabled: Boolean) {
    actionShowDetailHolder.foreach(_.setEnabled(isEnabled).setVisible(isEnabled))
  }
}
