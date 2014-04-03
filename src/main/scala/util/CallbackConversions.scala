package org.bone.findlost

import language.implicitConversions

import android.view.View
import android.widget.SearchView
import android.widget.AdapterView

object CallbackConversions
{
  implicit def toAdapterOnItemClicked(callback: Int => Any) = {
    new AdapterView.OnItemClickListener() {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
        callback(position)
      }
    }
  }

  implicit def toAdapterOnItemClicked(callback: (Int, View) => Any) = {
    new AdapterView.OnItemClickListener() {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
        callback(position, view)
      }
    }
  }

  implicit class RichSearchView(searchView: SearchView) {
    def setOnQueryListener(changeReturn: Boolean, submitReturn: Boolean)(callback: String => Any) = {
      searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        override def onQueryTextChange(newText: String) = { 
          callback(newText)
          changeReturn 
        }

        override def onQueryTextSubmit(text: String) = { 
          callback(text)
          submitReturn
        }
      })
    }
  }
}

