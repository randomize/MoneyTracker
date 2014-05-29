package com.example.moneytracker;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.moneytracker.R;



public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.ButtonStatus).setOnClickListener(this);
		findViewById(R.id.ButtonAccounts).setOnClickListener(this);
		findViewById(R.id.ButtonAccumulations).setOnClickListener(this);
		findViewById(R.id.ButtonBudget).setOnClickListener(this);
		findViewById(R.id.ButtonDebts).setOnClickListener(this);
		findViewById(R.id.ButtonEventsTasks).setOnClickListener(this);
		DatabaseFacade db = new DatabaseFacade(this);
		db.open();
		db.close();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	private void OpenPreferences() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_settings:
			OpenPreferences();
			return true;
		case R.id.action_exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		    case  R.id.ButtonAccounts: {
                Intent intent = new Intent(this, AccountsActivity.class);
                startActivity(intent);
			    break;
		    }

		    case  R.id.ButtonStatus: {
                Intent intent = new Intent(this, SummaryActivity.class);
                startActivity(intent);
			    break;
		    }
    
		   /* case R.id.clickButton2: {
			    // do something for button 2 click
			    break;
		    }*/

		    //.... etc

		}

	}

}



