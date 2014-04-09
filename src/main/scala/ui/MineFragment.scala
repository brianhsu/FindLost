package org.bone.findlost

import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem

import android.widget.AdapterView.AdapterContextMenuInfo
import android.app.Fragment

import scala.concurrent._
import scala.concurrent.duration._

import AsyncUI._
import CallbackConversions._
import TypedResource._

class MineFragment extends Fragment {

  object ContextMenu {
    val GroupID = 0
    val RemoveFromStarList = 0
  }
 
  private lazy val starList = new StarList(this.getActivity)
  private var adapterHolder: Option[ItemAdapter] = None

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, 
                            savedInstanceState: Bundle): View = {

    inflater.inflate(R.layout.fragment_mine, container, false)
  }

  override def onResume() {
    this.adapterHolder = Some(new ItemAdapter(this.getActivity, starList.getStarItems.toVector))
    setupUI()
    super.onResume()
  }
 
  override def onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo) {
    super.onCreateContextMenu(menu, view, menuInfo)

    getLostItem(menuInfo).foreach { lostItem =>
      menu.setHeaderTitle(lostItem.items)
      menu.add(ContextMenu.GroupID, ContextMenu.RemoveFromStarList, 0, "移除星號")
    }
  }

  override def onDestroy() {
    starList.close()
    super.onDestroy()
  }

  override def onContextItemSelected(menuItem: MenuItem): Boolean = menuItem.getItemId match {
    case ContextMenu.RemoveFromStarList => removeFromStarList(menuItem); true
    case _ => super.onContextItemSelected(menuItem)
  }

  private def getLostItem(menuInfo: ContextMenuInfo): Option[LostItem] = {
    adapterHolder.map { adapter =>
      val position = menuInfo.asInstanceOf[AdapterContextMenuInfo].position
      adapter.getItem(position).asInstanceOf[LostItem]
    }
  }

  private def removeFromStarList(menuItem: MenuItem) {
    for {
      adapter <- adapterHolder
      lostItem <- getLostItem(menuItem.getMenuInfo)
    } {
      adapter.removeItem(lostItem)
    }
  }

  private def setupUI() {

    adapterHolder.foreach { adapter =>

      val listView = getView.findView(TR.fragmentMineList)
      val loadingIndicator = getView.findView(TR.moduleLoadingIndicator)

      listView.setAdapter(adapter)
      listView.setEmptyView(getView.findView(TR.fragmentMineEmpty))
      listView.setOnItemClickListener { position: Int =>
        val lostItem = adapter.getItem(position).asInstanceOf[LostItem]
        LostItemActivity.startActivity(getActivity, lostItem)
      }

      loadingIndicator.setVisibility(View.GONE)
      registerForContextMenu(listView)
    }

  }

}

