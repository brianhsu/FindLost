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

class LostItemListActivity extends Activity with TypedViewHolder
{
  private lazy val lostItems = getIntent.getSerializableExtra("org.bone.findlost.lostItems").asInstanceOf[Vector[LostItem]]

  override def onCreate(savedInstanceState: Bundle)  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_lost_item)

    val adapter = new ItemAdapter(this, lostItems)
    val indicator = findView(TR.activityLostItemListLoadingIndicator)
    val listView = findView(TR.activityLostItemListList)
    listView.setAdapter(adapter)
    indicator.setVisibility(View.GONE)
  }
}
