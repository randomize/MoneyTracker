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
	
	private TransactionCategoryExpandableListAdapter main_adapter;
	private	ExpandableListView mainList;

	private ArrayList<TransactionCategoryGroup> groups = new ArrayList<TransactionCategoryGroup>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status);

		mainList = (ExpandableListView) findViewById(R.id.trans_categories_listview);
		
		db = new DatabaseFacade(this);
		
		LoadCurrency();
		CreateAdapter();
		//LoadActualData();

	}
	
	public void OpenDetailedCategory(int catId, String name) {

		Intent intent = new Intent(this, TransactionListActivity.class);
		intent.putExtra("Title", name);
		intent.putExtra("CategoryID", catId);
		startActivity(intent);
		
	}
	
	// Create adapter for transaction groups
	private void CreateAdapter() {
		
		TransactionCategoryExpandableListAdapter adapter = new TransactionCategoryExpandableListAdapter(this, groups);
		main_adapter = adapter;
		SwitchTo(0); // Default use main currency
		mainList.setAdapter(adapter);
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
		LoadActualData();
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
		main_adapter.rate = cur_rates[i];
		main_adapter.suffix = cur_names[i];
		main_adapter.notifyDataSetChanged();
	}

	private void OpenNewTransactionActivity() {
		Intent intent = new Intent(this, TransactionAddActivity.class);
		startActivity(intent);
	}

	private void LoadActualData() {
		groups.clear();
		db.open();
		groups.addAll(db.GetIncomeAndOutcome());
		db.close();

		main_adapter.notifyDataSetChanged();
		mainList.invalidateViews();

		int count = main_adapter.getGroupCount();
		for (int position = 1; position <= count; position++){
			mainList.collapseGroup(position - 1);
			mainList.expandGroup(position - 1);
		}
	}
}
