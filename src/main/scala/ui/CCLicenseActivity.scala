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

class CCLicenseActivity extends Activity with TypedViewHolder
{
  private lazy val webView = findView(TR.activityCCLicenseWebView)

  override def onCreate(savedInstanceState: Bundle)  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_cc_license)
    val html = """
      <html>
        <body>
           <div>
             <img src="file:///android_res/drawable/ic_launcher.png"/>
             Icon made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="http://www.flaticon.com/free-icon/picnic-box-ios-7-interface-symbol_20657" title="Flaticon">www.flaticon.com</a>
           </div>
        </body>
      </html>
    """
    webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null)
  }

}
