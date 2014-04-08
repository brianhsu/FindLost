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

import AsyncUI._

class LostItemActivity extends Activity with TypedViewHolder
{
  import LostItemListActivity._
  
  private lazy val lostItem = getIntent.getSerializableExtra(ExtrasLostItem).asInstanceOf[LostItem]
  private lazy val invoiceID = findView(TR.activityLostItemInvoiceID)
  private lazy val department = findView(TR.activityLostItemDepartment)
  private lazy val dateTime = findView(TR.activityLostItemDateTime)
  private lazy val location = findView(TR.activityLostItemLocation)
  private lazy val description = findView(TR.activityLostItemDescription)

  def searchDepartment(view: View) {
    import android.net.Uri
    import android.content.Intent
    val url = Uri.parse(s"https://www.google.com.tw/#q=${department.getText}")
    val intent = new Intent(Intent.ACTION_VIEW, url)
    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivity(intent)
    }
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
}
