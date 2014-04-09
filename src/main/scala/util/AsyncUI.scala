package org.bone.findlost

import scala.concurrent._
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.LinkedBlockingQueue

import android.app.Activity
import android.app.Fragment


object AsyncUI {
  implicit val exec = ExecutionContext.fromExecutor(
    new ThreadPoolExecutor(100, 100, 1000, TimeUnit.SECONDS, new LinkedBlockingQueue[Runnable])
  )

  implicit class FragmentUIFuture(fragment: Fragment) {
    def runOnUIThread(callback: => Any) {
      fragment.getActivity.runOnUiThread(
        new Runnable() {
          override def run() { callback }
        }
      )
    }
  }

  implicit class ActivityUIFuture(activity: Activity) {
    def runOnUIThread(callback: => Any) {
      activity.runOnUiThread(
        new Runnable() {
          override def run() { callback }
        }
      )
    }
  }

  implicit class UIFuture[T](future: Future[T]) {
    def runOnUIThread(callback: T => Any)(implicit activity: Activity) {
      future.foreach { result => 
        activity.runOnUiThread(new Runnable() {
          override def run() {
            callback(result)
          }
        })
      }
    }
  }

}

