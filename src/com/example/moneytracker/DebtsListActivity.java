package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

public class DebtsListActivity extends Activity {
	
	// Easy db access
	private DatabaseFacade db = null;

	// groups of accounts to display in activity
	private ArrayList<DebtsGroup> groups = new ArrayList<DebtsGroup>();

	// Expandable list root view and its adapter
	ExpandableListView mainList;
	DebtsCategoriesExpandableListAdapter main_adapter;

	private String[] cur_names = null;
	private float[] cur_rates = null;
	private int[] cur_ids = null;
	private int cur_relevant = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debts);
		//
		mainList = (ExpandableListView) findViewById(R.id.debts_categories_listview);
		mainList.setEmptyView(findViewById(R.id.debts_category_empter));
		
		db = new DatabaseFacade(this);
		
		LoadCurrency();
		CreateAdapter();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Load data from db
		LoadActualData();
	}

	private void LoadActualData() {
		groups.clear();
		db.open();
		groups.addAll(db.GetDebts());
		db.close();

		main_adapter.notifyDataSetChanged();
		mainList.invalidateViews();

		int count = main_adapter.getGroupCount();
		for (int position = 1; position <= count; position++){
			mainList.collapseGroup(position - 1);
			mainList.expandGroup(position - 1);
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
		if (i != cur_relevant) {
			main_adapter.relevant = false;
			main_adapter.rate = cur_rates[i];
			main_adapter.suffix = cur_names[i];
		} else {
			main_adapter.relevant = true;
		}
		main_adapter.notifyDataSetChanged();
	}

	// Create adapter for transaction groups
	private void CreateAdapter() {
		
		DebtsCategoriesExpandableListAdapter adapter = new DebtsCategoriesExpandableListAdapter(this, groups);
		main_adapter = adapter;
		SwitchTo(0); // Default use main currency
		mainList.setAdapter(adapter);
	}

	// Load currency list and rates
	private void LoadCurrency() {
		db.open();
		ArrayList<Currency> curList = db.GetCurrencyList();
		db.close();
		cur_names = new String[curList.size()+1];
		cur_ids = new int[curList.size()+1];
		cur_rates = new float[curList.size()+1];
		for (int i = 0; i < curList.size(); i++) {
			cur_names[i] = curList.get(i).name;
			cur_ids[i] = curList.get(i).id;
			cur_rates[i] = curList.get(i).rate;
		}
		cur_relevant = curList.size();
		cur_names[cur_relevant] = getString(R.string.relevant_currency);
	}
	
	private void CreateNewDebt(int type) {
		
		Intent intent = new Intent(this, DebtAddActivity.class);
		intent.putExtra("Type", (int)type);
		startActivity(intent);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.debts_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.debts_new_income:
			CreateNewDebt(1);
			return true;
		case R.id.debts_new_outcome:
			CreateNewDebt(0);
			return true;
		case R.id.action_change_currency:
			PopupCurrencySelector();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void CommitDebt(int debtId) {

		db.open();
		db.RemoveDebt(debtId);
		db.close();
		
		LoadActualData();
		
	}

	public void PopupActionsMenu(final int debtId, String name) {

		String[] cur_names = getResources().getStringArray(R.array.debt_actions);

		// display chooser
		new AlertDialog.Builder(this)
		.setTitle(name)
		.setCancelable(true)
		.setItems(cur_names,
			new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				if (i == 0) {
					CommitDebt(debtId);
				} else if (i == 1) {
					DeleteDebt(debtId);
				}
			}
		})
		.show();
	}

	protected void DeleteDebt(int debtId) {
		db.open();
		db.DeleteDebt(debtId);
		db.close();
		
		LoadActualData();
		
	}

}
