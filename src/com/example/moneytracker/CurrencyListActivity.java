package com.example.moneytracker;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class CurrencyListActivity extends ListActivity {

	private DatabaseFacade db;
	private ArrayList<Currency> curs;
	private ArrayAdapter<Currency> adapter ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new DatabaseFacade(this);

		curs = new ArrayList<Currency>();

		adapter = new CurrencyListAdapter(this, curs);
		getListView().setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();

		LoadCurrency();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		SaveCurrency();
	}

	private void SaveCurrency() {
		
		db.open();
		for (Currency c : curs) {
			db.UpdateCurrency(c);
		}
		db.close();
		
	}

	private void LoadCurrency() {

		db.open();
		curs.clear();
		curs.addAll(db.GetCurrencyList(false));
		db.close();
		adapter.notifyDataSetChanged();

	}

}
