package org.bone.findlost

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.AdapterView
import android.content.Intent
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.Toast

import scala.concurrent._
import scala.concurrent.duration._

import AsyncUI._

object LostItemListActivity {
  val BundleMustHaveKeywords = "org.bone.findlost.mustHaveKeywords"
  val BundleOptionalKeywords = "org.bone.findlost.optionalKeywords"
}

class LostItemListActivity extends Activity with TypedViewHolder
{
  import LostItemListActivity._
  import CallbackConversions._

  implicit val context = this

  private lazy val selectedGroup = getIntent.getSerializableExtra("org.bone.findlost.selectedGroups").asInstanceOf[Vector[Group]]
  private lazy val lostItems: Future[Vector[LostItem]] = getLostItems(selectedGroup)
  private lazy val adapterHolder: Future[ItemAdapter] = lostItems.map(new ItemAdapter(this, _))
  private lazy val searchMustHave = findView(TR.activityLostItemSearchMustHave)
  private lazy val searchOptional = findView(TR.activityLostItemSearchOptional)
  private lazy val searchBar = findView(TR.activityLostItemSearchBar)
  private lazy val starList = new StarList(this)

  private def getLostItems(selectedGroup: Vector[Group]): Future[Vector[LostItem]] = {
    LostItem.getLostItems(this, selectedGroup.map(_.title).toList)
  }

  private def startSearching() {
    val mustHaveKeywords = searchMustHave.getQuery.toString.trim
    val optionalKeywords = searchOptional.getQuery.toString.trim
    val searchConstraint = s"${mustHaveKeywords}⌘${optionalKeywords}"
    adapterHolder.runOnUIThread { adapter => adapter.getFilter.filter(searchConstraint) }
  }

  def setupSearchView(searchView: SearchView) {
    searchView.setOnQueryListener(false, true) { query => startSearching() }
  }

  def onActionSearchClicked(menuItem: MenuItem) {
    searchBar.getVisibility match {
      case View.VISIBLE => searchBar.setVisibility(View.GONE)
      case View.GONE => searchBar.setVisibility(View.VISIBLE)
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.activity_lost_item_list_actions, menu)
    super.onCreateOptionsMenu(menu)
  }

  override def onSaveInstanceState(bundle: Bundle) {
    if (!searchMustHave.getQuery.toString.trim.isEmpty) {
      bundle.putString(BundleMustHaveKeywords, searchMustHave.getQuery.toString)
    }

    if (!searchOptional.getQuery.toString.trim.isEmpty) {
      bundle.putString(BundleOptionalKeywords, searchOptional.getQuery.toString)
    }
  }

  override def onCreate(savedInstanceState: Bundle)  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_lost_item_list)

    val indicator = findView(TR.moduleLoadingIndicator)
    val listView = findView(TR.activityLostItemListList)

    listView.setEmptyView(findView(TR.activityLostItemListEmpty))

    adapterHolder.runOnUIThread { adapter =>
      listView.setAdapter(adapter)
      listView.setFastScrollEnabled(true)
      listView.setOnItemClickListener { position: Int =>
        val lostItem = adapter.getItem(position).asInstanceOf[LostItem]
        LostItemActivity.startActivity(LostItemListActivity.this, lostItem)
      }

      if (savedInstanceState != null) {
        val mustHaveKeywords = savedInstanceState.getString(BundleMustHaveKeywords)
        val optionalKeywords = savedInstanceState.getString(BundleOptionalKeywords)

        if (mustHaveKeywords != null && !mustHaveKeywords.trim.isEmpty) {
          searchMustHave.setQuery(mustHaveKeywords, true)
          searchBar.setVisibility(View.VISIBLE)
        }

        if (optionalKeywords != null && !optionalKeywords.trim.isEmpty) {
          searchOptional.setQuery(optionalKeywords, true)
          searchBar.setVisibility(View.VISIBLE)
        }

      }

      indicator.setVisibility(View.GONE)
    }

    setupSearchView(searchMustHave)
    setupSearchView(searchOptional)
    registerForContextMenu(listView)
  }

  object ContextMenu {
    val GroupID = 0
    val AddToStarList = 0
    val RemoveFromStarList = 1
  }

  override def onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo) {
    super.onCreateContextMenu(menu, view, menuInfo)
    val adapter = adapterHolder.value.get.get
    val position = menuInfo.asInstanceOf[AdapterContextMenuInfo].position
    val lostItem = adapter.getItem(position).asInstanceOf[LostItem]

    starList.isInStarList(lostItem) match {
      case false => menu.add(ContextMenu.GroupID, ContextMenu.AddToStarList, 0, R.string.addToStarList)
      case true => menu.add(ContextMenu.GroupID, ContextMenu.RemoveFromStarList, 0, R.string.removeFromStarList)
    }

    menu.setHeaderTitle(lostItem.items)
  }

  def removeFromStarList(menuItem: MenuItem) {
    adapterHolder.runOnUIThread { adapter =>
      val position = menuItem.getMenuInfo.asInstanceOf[AdapterContextMenuInfo].position
      val lostItem = adapter.getItem(position).asInstanceOf[LostItem]
      starList.removeFromStarList(lostItem)
      adapter.updateStarView()
      Toast.makeText(this, R.string.removedFromStarList, Toast.LENGTH_SHORT).show()
    }
  }

  def addToStarList(menuItem: MenuItem) {
    adapterHolder.runOnUIThread { adapter =>
      val position = menuItem.getMenuInfo.asInstanceOf[AdapterContextMenuInfo].position
      val lostItem = adapter.getItem(position).asInstanceOf[LostItem]
      starList.insertToStarList(lostItem)
      adapter.updateStarView()
      Toast.makeText(this, R.string.addedToStarList, Toast.LENGTH_SHORT).show()
    }
  }

  override def onResume() {
    super.onResume()
    adapterHolder.runOnUIThread { adapter =>
      adapter.updateStarView()
    }
  }

  override def onContextItemSelected(menuItem: MenuItem): Boolean = menuItem.getItemId match {
    case ContextMenu.AddToStarList => addToStarList(menuItem); true
    case ContextMenu.RemoveFromStarList => removeFromStarList(menuItem); true
    case _ => super.onContextItemSelected(menuItem)
  }

}
