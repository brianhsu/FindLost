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

import scala.concurrent._
import scala.concurrent.duration._

import AsyncUI._

object LostItemListActivity {
  val BundleMustHaveKeywords = "org.bone.findlost.mustHaveKeywords"
  val BundleOptionalKeywords = "org.bone.findlost.optionalKeywords"

  val ExtrasLostItem= "org.bone.findlost.lostItem"
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
  }
}
