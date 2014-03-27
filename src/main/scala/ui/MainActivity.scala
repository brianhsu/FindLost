package org.bone.findlost

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.widget.BaseAdapter
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater

class MyAdapter(context: Context, lostItems: List[LostItem]) extends BaseAdapter 
{
  import TypedResource._

  private lazy val inflater = LayoutInflater.from(context)

  override def getCount: Int = lostItems.size
  override def getItem(position: Int): Object = lostItems(position)
  override def getItemId(position: Int): Long = position
  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    convertView match {
      case view: View => convertView
      case null =>
        val newView = inflater.inflate(R.layout.lost_item_list_row, null)
        val rowTitle = newView.findView(TR.lostItemListTitle)
        val lostItem = lostItems(position)
        rowTitle.setText(lostItem.items)
        newView
    }
  }
}

class MainActivity extends Activity with TypedViewHolder with AsyncUI
{
  import scala.concurrent._
  import scala.concurrent.duration._
  import android.util.Log
  import scala.util.Failure
  import scala.util.Success


  private val Tag = "FindLost"


  override def onCreate(savedInstanceState: Bundle) 
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    val lostItems = LostItem.getDataFromNetwork recoverWith {
      case LostItem.IncorrectFormatException => LostItem.getDataFromNetwork
    }

    lostItems.onComplete {
      case Failure(e)    => Log.v(Tag, "Error occurs:" + e)
      case Success(list) => runOnUiThread {
        Log.v(Tag, "onSucces..." + list.size)
        val listView = findView(TR.lostItemList)
        listView.setAdapter(new MyAdapter(this, list))
      }
    }

  }
}
