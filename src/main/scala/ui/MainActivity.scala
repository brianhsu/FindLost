package org.bone.findlost

import android.app.Activity
import android.os.Bundle

class MainActivity extends Activity with TypedViewHolder with AsyncUI
{
  override def onCreate(savedInstanceState: Bundle) 
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
  }
}
