import android.Keys._

import android.Dependencies.{apklib,aar}
 
android.Plugin.androidBuild
 
name := "FindLost"
 
scalaVersion := "2.10.4"
 
platformTarget in Android := "android-19"
 
run <<= run in Android
 
install <<= install in Android

