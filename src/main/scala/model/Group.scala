package org.bone.findlost

case class Group(title: String, var isSelected: Boolean) {
  def toggleState = {
    isSelected = !isSelected
    isSelected
  }
}

