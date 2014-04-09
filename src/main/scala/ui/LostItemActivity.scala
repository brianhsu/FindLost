package org.bone.findlost

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView

import scala.concurrent._
import scala.concurrent.duration._
import android.content.Intent
import android.widget.Toast

import AsyncUI._

object LostItemActivity {

  val ExtraLostItem = "org.bone.findlost.lostItem"

  def startActivity(currentActivity: Activity, lostItem: LostItem) {
    val intent = new Intent(currentActivity, classOf[LostItemActivity])
    intent.putExtra(ExtraLostItem, lostItem)
    currentActivity.startActivity(intent)
  }
}

class LostItemActivity extends Activity with TypedViewHolder
{
  import LostItemActivity.ExtraLostItem
  
  private lazy val lostItem = getIntent.getSerializableExtra(ExtraLostItem).asInstanceOf[LostItem]
  private lazy val invoiceID = findView(TR.activityLostItemInvoiceID)
  private lazy val department = findView(TR.activityLostItemDepartment)
  private lazy val dateTime = findView(TR.activityLostItemDateTime)
  private lazy val location = findView(TR.activityLostItemLocation)
  private lazy val description = findView(TR.activityLostItemDescription)
  private lazy val starList = new StarList(this)

  private var actionStar: MenuItem = _

  def onActionStarClicked(menuItem: MenuItem) {
    starList.isInStarList(lostItem) match {
      case false => 
        starList.insertToStarList(lostItem) 
        actionStar.setIcon(R.drawable.ic_action_important_color)
        Toast.makeText(this, R.string.addedToStarList, Toast.LENGTH_SHORT).show()
      case true => 
        starList.removeFromStarList(lostItem); 
        actionStar.setIcon(R.drawable.ic_action_important)
        Toast.makeText(this, R.string.removedFromStarList, Toast.LENGTH_SHORT).show()

    }
  }

  def searchDepartment(view: View) {
    import android.net.Uri
    import android.content.Intent
    val url = Uri.parse(s"https://www.google.com.tw/#q=${department.getText}")
    val intent = new Intent(Intent.ACTION_VIEW, url)
    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivity(intent)
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater = getMenuInflater
    inflater.inflate(R.menu.activity_lost_item_actions, menu)
    actionStar = menu.findItem(R.id.activityLostItemActionStar).asInstanceOf[MenuItem]

    if (starList.isInStarList(lostItem)) {
      actionStar.setIcon(R.drawable.ic_action_important_color)
    }

    super.onCreateOptionsMenu(menu)
  }

  override def onCreate(savedInstanceState: Bundle)  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_lost_item)
    invoiceID.setText(lostItem.invoiceID)
    department.setText(lostItem.department)
    dateTime.setText(lostItem.formatedDateTime)
    location.setText(lostItem.location)
    description.setText(lostItem.description)
  }

  override def onDestroy() {
    starList.close()
    super.onDestroy()
  }

}
