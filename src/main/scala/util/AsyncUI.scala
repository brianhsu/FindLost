package org.bone.findlost

import scala.concurrent._
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.LinkedBlockingQueue

import android.app.Activity

trait AsyncUI {

  this: Activity =>

  implicit val exec = ExecutionContext.fromExecutor(
    new ThreadPoolExecutor(100, 100, 1000, TimeUnit.SECONDS, new LinkedBlockingQueue[Runnable])
  )

  def runOnUiThread(callback: => Any) {
    this.runOnUiThread ( new Runnable() { def run() { callback } } )
  }

}


