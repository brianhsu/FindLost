package org.bone.findlost

import android.app.Activity
import android.os.Bundle
import android.app.ActionBar
import android.app.FragmentTransaction
import android.app.ActionBar.Tab
import android.app.Fragment

class MainActivity extends Activity with TypedViewHolder
{
  class TabListener[T <: Fragment](activity: Activity, tag: String, 
                                   clz: Class[T]) extends ActionBar.TabListener {
  
    private var fragmentHolder: Option[Fragment] = None
  
    override def onTabSelected(tab: Tab, ft: FragmentTransaction) {
      if (fragmentHolder.isEmpty) {
        fragmentHolder = Some(Fragment.instantiate(activity, clz.getName))
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

  private def createTab[T <: Fragment](title: String, iconResource: Int, clz: Class[T]) {
    val actionBar = getActionBar
    val tabListener = new TabListener(this, title, clz)
    val tab = actionBar.newTab.setText(title).setIcon(iconResource).setTabListener(tabListener)
    actionBar.addTab(tab)
  }

  override def onCreate(savedInstanceState: Bundle)  {
    super.onCreate(savedInstanceState)
    val actionBar = getActionBar
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
    if (actionBar.getTabCount == 0) {
      createTab(getString(R.string.lostItemGroupList), android.R.drawable.ic_menu_view, classOf[MainFragment])
      createTab(getString(R.string.possibleMineList), android.R.drawable.btn_star_big_on, classOf[MineFragment])
    }
  }

}
