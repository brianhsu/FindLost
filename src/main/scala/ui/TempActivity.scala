package org.bone.findlost

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.app.ActionBar
import android.app.FragmentTransaction
import android.app.ActionBar.Tab
import android.view.LayoutInflater
import android.view.ViewGroup
import android.app.Fragment

import scala.concurrent._
import scala.concurrent.duration._

import AsyncUI._

class TabListener[T <: Fragment](activity: Activity, tag: String, fragmentClass: Class[T]) extends ActionBar.TabListener {

  private var fragmentHolder: Option[Fragment] = None

  override def onTabSelected(tab: Tab, ft: FragmentTransaction) {
    if (fragmentHolder.isEmpty) {
      fragmentHolder = Some(Fragment.instantiate(activity, fragmentClass.getName))
      fragmentHolder.foreach(fragment => ft.replace(android.R.id.content, fragment, tag))
    } else {
      fragmentHolder.foreach(ft.attach _)
    }
  }

  override def onTabUnselected(tab: Tab, ft: FragmentTransaction) {
    fragmentHolder.foreach(ft.detach _)
  }

  override def onTabReselected(tab: Tab, ft: FragmentTransaction) {}

}


import TypedResource._
import android.view.View
import android.content.Intent
import CallbackConversions._

class MineFragment extends Fragment {

  private lazy val starList = new StarList(this.getActivity)


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view = inflater.inflate(R.layout.fragment_mine, container, false)
    view
  }

  override def onResume() {
    val adapter = new ItemAdapter(this.getActivity, starList.getStarItems.toVector)
    val listView = getView.findView(TR.fragmentMineList)
    val loadingIndicator = getView.findView(TR.moduleLoadingIndicator)
    listView.setAdapter(adapter)
    listView.setOnItemClickListener { position: Int =>
      val lostItem = adapter.getItem(position).asInstanceOf[LostItem]
      LostItemActivity.startActivity(getActivity, lostItem)
    }
    loadingIndicator.setVisibility(View.GONE)
    super.onResume()
  }

}

class ListFragment extends Fragment {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    inflater.inflate(R.layout.fragment_list, container, false)
  }
}


class TempActivity extends Activity with TypedViewHolder
{
  private def createTab[T <: Fragment](title: String, fragmentClass: Class[T]) {
    val actionBar = getActionBar
    val tabListener = new TabListener(this, title, fragmentClass)
    val tab = actionBar.newTab.setText(title).setTabListener(tabListener)
    actionBar.addTab(tab)
  }

  override def onCreate(savedInstanceState: Bundle)  {
    super.onCreate(savedInstanceState)
    val actionBar = getActionBar
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
    if (actionBar.getTabCount == 0) {
      createTab("失物列表", classOf[ListFragment])
      createTab("可能是我的", classOf[MineFragment])
    }
  }

}
