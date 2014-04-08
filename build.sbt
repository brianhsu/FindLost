import android.Keys._

android.Plugin.androidBuild
 
name := "FindLost"
 
scalaVersion := "2.10.4"

organization := "org.bone.findlost"

scalacOptions := Seq("-feature")

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test"

platformTarget in Android := "android-19"

proguardScala in Android := true
 
// run <<= run in Android
 
install <<= install in Android

