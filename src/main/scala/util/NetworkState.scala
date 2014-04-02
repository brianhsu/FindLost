package org.bone.findlost

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object NetworkState {

  def getActiveNetworkInfo(context: Context): Option[NetworkInfo] = {

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                                     .asInstanceOf[ConnectivityManager]

    Option(connectivityManager.getActiveNetworkInfo)
  }

  def isUsingMobileDataConnection(context: Context) = {
    val networkType = getNetworkType(context).getOrElse(-1)
    networkType match {
      case ConnectivityManager.TYPE_MOBILE | 
           ConnectivityManager.TYPE_MOBILE_DUN |
           ConnectivityManager.TYPE_MOBILE_HIPRI | 
           ConnectivityManager.TYPE_MOBILE_MMS |
           ConnectivityManager.TYPE_MOBILE_SUPL => true
      case _ => false
    }
  }

  def getNetworkType(context: Context): Option[Int] = {
    getActiveNetworkInfo(context).map(_.getType)
  }
}
