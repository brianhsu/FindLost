package org.bone.findlost

import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.content.Intent
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.view.MenuInflater
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import android.util.Log
 
import scala.concurrent._

import AsyncUI._
import CallbackConversions._
import TypedResource._

class MainFragment extends Fragment 
{
  import LostItem._
  import CallbackConversions._

  private implicit def runOnUIActivity = getActivity

  private var actionShowDetailHolder: Option[MenuItem] = None

  private var viewHolder: Option[View] = None
  private lazy val indicator = getView.findView(TR.moduleLoadingIndicator)
  private lazy val errorMessageRetryButton = getView.findView(TR.errorMessageRetryButton)
  private lazy val errorMessageSpace = getView.findView(TR.errorMessageSpace)
  private lazy val listView = getView.findView(TR.fragmentMainGroupList)

  private var adapterHolder: Future[GroupAdapter] = _
  private var allowMobileData: Boolean = false
  private var isLoading: Boolean = false

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view = viewHolder.getOrElse(inflater.inflate(R.layout.fragment_main, container, false))
    viewHolder = Some(view)
    view
  }

  private def loadingData(isRefresh: Boolean): Future[GroupAdapter] = {

    val lostItemsFuture = LostItem.getLostItemData(getActivity, allowMobileData, isRefresh)

    lostItemsFuture.map { groups =>
      new GroupAdapter(getActivity, groups)
    }
  }

  private def setupGroupList(isRefresh: Boolean = false) {
    Log.v("FindLost", "setupGroupList:" + isRefresh)
    this.isLoading = true
    indicator.setVisibility(View.VISIBLE)
    adapterHolder = loadingData(isRefresh)
    adapterHolder.runOnUIThread { adapter => 
      showDateGroupListView(adapter) 
    }
    adapterHolder.onFailure { 
      case UsingMobileConnectionException => this.runOnUIThread { Log.v("FindLost", "UsingMobileConnectionException"); showMobileNetworkWarning() }
      case e: Exception => this.runOnUIThread { Log.v("FindLost", "error:" + e, e); displayErrorMessage(e) }
    }
  }

  private def showMobileNetworkWarning() {
    val alertDialog = new AlertDialog.Builder(getActivity)
    alertDialog.setTitle(R.string.mobileWarningTitle)
    alertDialog.setMessage(R.string.mobileWarningContent)
    alertDialog.setCancelable(false)
    alertDialog.setPositiveButton(R.string.mobileWarningConfirm, 
      new DialogInterface.OnClickListener() {
        override def onClick(dialog: DialogInterface, which: Int) {
          MainFragment.this.allowMobileData = true
          MainFragment.this.setupGroupList()
        }
      }
    )
    alertDialog.setNegativeButton(R.string.mobileWarningCancel, 
      new DialogInterface.OnClickListener() {
        override def onClick(dialog: DialogInterface, which: Int) {
          getActivity.finish()
        }
      }
    )

    alertDialog.show()
  }

  private def displayErrorMessage(exception: Exception) 
  {
    val message = getView.findView(TR.errorMessageContent)
    val retryButton = getView.findView(TR.errorMessageRetryButton)
    setLoadingIndicatorState(false)
    errorMessageSpace.setVisibility(View.VISIBLE)
    errorMessageRetryButton.setEnabled(true)
    retryButton.setOnClickListener { view: View => onRetryClicked() }
    message.setText(R.string.downloadErrorMessage)
    Toast.makeText(getActivity, getString(R.string.errorCause) + exception.getMessage, Toast.LENGTH_LONG).show()
  }

  private def disableErrorMessage() {
    errorMessageSpace.setVisibility(View.GONE)
    errorMessageRetryButton.setEnabled(false)
  }

  private def showDateGroupListView(adapter: GroupAdapter) {
    setLoadingIndicatorState(false)
    disableErrorMessage()

    listView.setAdapter(adapter)
    listView.setOnItemClickListener { (position: Int, view: View) =>
      adapter.toggleState(position, view)
      setActionShowDetailEnabled(adapter.hasItemSelected)
    }
    this.isLoading = false
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

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.mainFragmentActionRefresh => onActionRefreshClicked(); true
      case R.id.mainFragmentActionShowDetail => onActionShowDetailClicked(); true
      case _ => super.onOptionsItemSelected(item)
    }
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.main_fragment_actions, menu)
    actionShowDetailHolder = Option(menu.findItem(R.id.mainFragmentActionShowDetail)).map(_.asInstanceOf[MenuItem])
    setActionShowDetailEnabled(false)
  }

  override def onCreate(savedInstanceState: Bundle) {
    setHasOptionsMenu(true)
    super.onCreate(savedInstanceState)
  }

  override def onResume()  {
    super.onResume()
    setupGroupList()
  }

  def onRetryClicked() {
    this.errorMessageRetryButton.setEnabled(false)
    setupGroupList(true)
  }

  def onActionShowDetailClicked() {
    adapterHolder.runOnUIThread { adapter =>
      val selectedGroups = adapter.getSelectedGroups
      val intent = new Intent(getActivity, classOf[LostItemListActivity])
      intent.putExtra("org.bone.findlost.selectedGroups", selectedGroups)
      startActivity(intent)
    }
  }

  def onActionRefreshClicked() {
    if (!isLoading) {
      setupGroupList(true)
    }
  }

}


