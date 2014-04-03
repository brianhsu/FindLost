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
  override def onCreate(savedInstanceState: Bundle)  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_lost_item)
  }
}
