package org.bone.findlost

import android.app.Activity
import android.os.Bundle
import android.content.Intent

import android.app.ActionBar
import android.app.FragmentTransaction
import android.app.ActionBar.Tab
import android.app.Fragment
import android.view.Menu
import android.view.MenuItem

trait TabInterface {

  this: MainActivity =>

  def removeTab() {
    if (getActionBar != null && getActionBar.getTabCount >= 2) {
      getActionBar.removeTabAt(1)
    }
  }

  def addTab() {
    if (getActionBar != null && getActionBar.getTabCount == 1) {
      createTab(
        title = getString(R.string.possibleMineList), 
        iconResource = android.R.drawable.btn_star_big_on, 
        clz = classOf[MineFragment]
      )
    }
  }

}

class MainActivity extends Activity with TypedViewHolder with TabInterface
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

  protected def createTab[T <: Fragment](title: String, iconResource: Int, clz: Class[T]) {
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

      createTab(
        title = getString(R.string.lostItemGroupList), 
        iconResource = android.R.drawable.ic_menu_view, 
        clz = classOf[MainFragment]
      )

      createTab(
        title = getString(R.string.possibleMineList), 
        iconResource = android.R.drawable.btn_star_big_on, 
        clz = classOf[MineFragment]
      )
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater = getMenuInflater
    inflater.inflate(R.menu.activity_main_menu, menu)
    super.onCreateOptionsMenu(menu)
  }

  def onCCLicenseClicked(menuItem: MenuItem) {
    val intent = new Intent(this, classOf[CCLicenseActivity])
    startActivity(intent)
  }

}
