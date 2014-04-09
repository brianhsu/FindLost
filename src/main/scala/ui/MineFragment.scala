package org.bone.findlost

import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.app.Fragment

import scala.concurrent._
import scala.concurrent.duration._

import AsyncUI._
import CallbackConversions._
import TypedResource._

class MineFragment extends Fragment {

  private lazy val starList = new StarList(this.getActivity)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, 
                            savedInstanceState: Bundle): View = {

    inflater.inflate(R.layout.fragment_mine, container, false)
  }

  override def onResume() {
    val adapter = new ItemAdapter(this.getActivity, starList.getStarItems.toVector)
    val listView = getView.findView(TR.fragmentMineList)
    val loadingIndicator = getView.findView(TR.moduleLoadingIndicator)
    listView.setAdapter(adapter)
    listView.setEmptyView(getView.findView(TR.fragmentMineEmpty))
    listView.setOnItemClickListener { position: Int =>
      val lostItem = adapter.getItem(position).asInstanceOf[LostItem]
      LostItemActivity.startActivity(getActivity, lostItem)
    }
    loadingIndicator.setVisibility(View.GONE)
    super.onResume()
  }

  override def onDestroy() {
    starList.close()
    super.onDestroy()
  }

}

