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
	
	private View v, v2;
	private DatabaseFacade db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.ButtonStatus).setOnClickListener(this);
		findViewById(R.id.ButtonAccounts).setOnClickListener(this);
		findViewById(R.id.ButtonAccumulations).setOnClickListener(this);

		v = findViewById(R.id.ButtonBudget);
		v.setOnClickListener(this);
		v2 = findViewById(R.id.ButtonDebts);
		v2.setOnClickListener(this);

		findViewById(R.id.ButtonEventsTasks).setOnClickListener(this);
		db = new DatabaseFacade(this);
		
	
	}
	
	private void SetupColors() {
		
		db.open();
		
		if (db.ThereIsBrokenBuget()) {
			v.setBackgroundColor(getResources().getColor(R.color.errorous));
		}

		if (db.ThereIsExpiredDebt()) {
			v2.setBackgroundColor(getResources().getColor(R.color.errorous));
		}

		db.close();
	}

	@Override
	protected void onResume() {
		SetupColors();
		super.onResume();
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
		case R.id.action_members_list:
			OpenMembersList();
			return true;
		case R.id.action_currency_list:
			OpenCurrencyList();
			return true;
		case R.id.action_about:
			OpenAbout();
			return true;
		case R.id.action_exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void OpenAbout() {
		// TODO Auto-generated method stub
		
	}

	private void OpenMembersList() {
		Intent intent = new Intent(this, MembersListActivity.class);
		startActivity(intent);
		
	}

	private void OpenCurrencyList() {
		Intent intent = new Intent(this, CurrencyListActivity.class);
		startActivity(intent);
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
		    
		    case R.id.ButtonDebts: {
                Intent intent = new Intent(this, DebtsListActivity.class);
                startActivity(intent);
                break;
		    }
		    case R.id.ButtonBudget: {
                Intent intent = new Intent(this, BugetListActivity.class);
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



