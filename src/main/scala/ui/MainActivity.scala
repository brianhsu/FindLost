package org.bone.findlost

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Toast

import scala.concurrent._
import scala.concurrent.duration._

import AsyncUI._

class MainActivity extends Activity with TypedViewHolder
{

  private implicit val runOnUIActivity = this

  private var actionShowDetailHolder: Option[MenuItem] = None
  private lazy val indicator = findView(TR.lostItemListLoadingIndicator)
  private lazy val errorMessageRetryButton = findView(TR.errorMessageRetryButton)
  private lazy val errorMessageSpace = findView(TR.errorMessageSpace)

  private var adapterHolder: Future[GroupAdapter] = _

  private def loadingData(): Future[GroupAdapter] = {
    val lostItemsFuture = LostItem.getLostItemData(this)
    lostItemsFuture.map { groups =>
      new GroupAdapter(this, groups)
    }
  }

  private def setupGroupList() {
    indicator.setVisibility(View.VISIBLE)
    adapterHolder = loadingData()
    adapterHolder.runOnUIThread { adapter => showDateGroupListView(adapter) }
    adapterHolder.onFailure { 
      case e: Exception => this.runOnUIThread { displayErrorMessage(e) }
    }
  }

  private def displayErrorMessage(exception: Exception)
  {
    val message = findView(TR.errorMessageContent)
    setLoadingIndicatorState(false)
    errorMessageSpace.setVisibility(View.VISIBLE)
    errorMessageRetryButton.setEnabled(true)
    message.setText("無法從網路取得資料\n請確認網路連線是否已開啟")
    Toast.makeText(this, "失敗原因：" + exception.getMessage, Toast.LENGTH_LONG).show()
  }

  private def disableErrorMessage()
  {
    errorMessageSpace.setVisibility(View.GONE)
    errorMessageRetryButton.setEnabled(false)
  }

  private def showDateGroupListView(adapter: GroupAdapter) {
    setLoadingIndicatorState(false)
    disableErrorMessage()

    val listView = findView(TR.lostItemList)

    listView.setAdapter(adapter)
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
        adapter.toggleState(position, view)
        setActionShowDetailEnabled(adapter.hasItemSelected)
      }
    })
  }

  private def setLoadingIndicatorState(isLoading: Boolean) {
    isLoading match {
      case true => indicator.setVisibility(View.VISIBLE)
      case false => indicator.setVisibility(View.GONE)
    }
  }

  private def setActionShowDetailEnabled(isEnabled: Boolean) {
    actionShowDetailHolder.foreach(_.setEnabled(isEnabled).setVisible(isEnabled))
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater = getMenuInflater
    inflater.inflate(R.menu.main_activity_actions, menu)
    actionShowDetailHolder = Option(menu.findItem(R.id.mainActivityActionShowDetail)).map(_.asInstanceOf[MenuItem])
    setActionShowDetailEnabled(false)
    super.onCreateOptionsMenu(menu)
  }

  override def onCreate(savedInstanceState: Bundle)  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    setupGroupList()
  }

  def onRetryClicked(view: View) {
    this.errorMessageRetryButton.setEnabled(false)
    setupGroupList()
  }

  def onActionShowDetailClicked(menuItem: MenuItem) {
    adapterHolder.runOnUIThread { adapter =>
      val selectedGroups = adapter.getSelectedGroups
      val intent = new Intent(this, classOf[LostItemListActivity])
      intent.putExtra("org.bone.findlost.selectedGroups", selectedGroups)
      startActivity(intent)
    }
  }

}
