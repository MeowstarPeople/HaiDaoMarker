package com.eric.newmapapi.activity;


import com.eric.newmapapi.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
public class IslandinfoActivity extends Activity{
	private TextView txtname;
	private String islandName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.islandinfo);
		init();
		Intent intent=getIntent();
		islandName=intent.getStringExtra("island_name");
		txtname.setText(islandName);
	}
	private void init(){
		txtname=(TextView) findViewById(R.id.BZname);
	}
}
