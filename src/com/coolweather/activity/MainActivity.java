package com.coolweather.activity;

import com.coolweather.app.R;
import com.coolweather.db.CoolWeatherOpenHelper;

import android.app.Activity;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private Button create_table;
	private SQLiteOpenHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		dbHelper = new CoolWeatherOpenHelper(this, "CoolwWeather.db", null, 1);
		create_table = (Button)findViewById(R.id.create_table);
		create_table.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dbHelper.getWritableDatabase();
			}
		});
	}

}
