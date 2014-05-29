package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

public class SummaryActivity extends Activity {
	
	private DatabaseFacade db = null;

	private String[] cur_names = null;
	private float[] cur_rates = null;
	private int[] cur_ids = null;
	
	private TransactionCategoryExpandableListAdapter main_adapber;
	private	ExpandableListView listView;

	private ArrayList<TransactionCategoryGroup> groups = new ArrayList<TransactionCategoryGroup>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status);

		listView = (ExpandableListView) findViewById(R.id.trans_categories_listview);
		
		db = new DatabaseFacade(this);
		
		LoadCurrency();
		CreateAdapter();

	}
	
	// Create adapter for transaction groups
	private void CreateAdapter() {
		
		TransactionCategoryExpandableListAdapter adapter = new TransactionCategoryExpandableListAdapter(this, groups);
		main_adapber = adapter;
		SwitchTo(0); // Default use main currency
		listView.setAdapter(adapter);
	}

	// Load currency list and rates
	private void LoadCurrency() {
		db.open();
		ArrayList<Currency> curList = db.GetCurrencyList();
		cur_names = new String[curList.size()];
		cur_ids = new int[curList.size()];
		cur_rates = new float[curList.size()];
		for (int i = 0; i < curList.size(); i++) {
			cur_names[i] = curList.get(i).name;
			cur_ids[i] = curList.get(i).id;
			cur_rates[i] = curList.get(i).rate;
		}
		db.close();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Load data from db
		CreateTreeData();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.status_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_tranaction_new:
			OpenNewTransactionActivity();
			return true;
		case R.id.action_change_currency:
			PopupCurrencySelector();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void PopupCurrencySelector() {


		// display chooser
		new AlertDialog.Builder(this)
		.setTitle(R.string.currency)
		.setCancelable(false)
		.setItems(cur_names,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				SwitchTo(i);
			}
		})
		.show();
	}
	
	private void SwitchTo(int i) {
		main_adapber.rate = cur_rates[i];
		main_adapber.suffix = cur_names[i];

		main_adapber.notifyDataSetChanged();
		//listView.invalidateViews();

	}

	private void OpenNewTransactionActivity() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, TransactionAddActivity.class);
		startActivity(intent);
		
	}

	public void CreateTreeData() {
		db.open();
		groups.clear();
		groups.add(db.GetIncome());
		groups.add(db.GetOutcome());
		db.close();
	}
}
