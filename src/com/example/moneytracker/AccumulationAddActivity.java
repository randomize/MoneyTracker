package com.example.moneytracker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class AccumulationAddActivity extends Activity {

	// Easy access to database
	private DatabaseFacade db;
	
	
	// Datas

	// Currency spinner helpers
	private int currency_spinner_ids[] = null;                        // index => id mapping
	private String currency_spinner_lables[] = null;                  // index => label (like USD)
	private float currency_spinner_rates[] = null;                  // index => rate
	private int currency_spinner_add_index = 0;
	
	// Views
	
	private Spinner currecySpinner;
	private TextView currencyLabel;
	private EditText transactionAmount;
	private EditText desctEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accum_add);
		setTitle(getString(R.string.new_accum));
		
		db = new DatabaseFacade(this);
		
		currecySpinner = (Spinner) findViewById(R.id.SpinnerAccumCurrency);
		currencyLabel = (TextView) findViewById(R.id.TextViewAccumCurrency);
		transactionAmount = (EditText) findViewById(R.id.EditTextAccumAmount);
		desctEdit = (EditText) findViewById(R.id.editTextAccumDesc);
		
		// Set handler
		currecySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				
				// If user clicked "New ..." item
				if (position == currency_spinner_add_index) {
					ShowNewCurrencyDialog();
					parentView.setSelection(0);
				} else {
					currencyLabel.setText(currency_spinner_lables[position]);
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) { }

		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		LoadCurrency();
	}
	
	private void ShowNewCurrencyDialog() {
		OpenCurrencyList();
	}

	private void OpenCurrencyList() {
		Intent intent = new Intent(this, CurrencyListActivity.class);
		startActivity(intent);
	}

	private void LoadCurrency() {

		db.open();
		ArrayList<Currency> currency_map = db.GetCurrencyList();
		db.close();

		currency_spinner_ids = new int[currency_map.size()+1]; // Reservin one for "new..."
		currency_spinner_lables = new String[currency_map.size()+1];
		currency_spinner_rates = new float[currency_map.size()+1];

		for (int i = 0; i < currency_map.size(); i++) {
			Currency c = currency_map.get(i);
			currency_spinner_ids[i] = c.id;
			currency_spinner_lables[i] = c.name;
			currency_spinner_rates[i] = c.rate;
		}

		// Always add custom new currency item option
		currency_spinner_add_index = currency_map.size();
		currency_spinner_lables[currency_spinner_add_index] = getString(R.string.new_curr);
		currency_spinner_ids[currency_spinner_add_index] = -1; // JIC
		
		// Set spinner items(using adapter)
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currency_spinner_lables);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		currecySpinner.setAdapter(adapter);
		
	}

	public void CancelAccumAdd(View a) {
		finish();
	}
	
	public void CommitAccumAdd(View v) {
		
		boolean valida = true;
		
		float amount = 0;
		try {
			amount = Float.valueOf(transactionAmount.getText().toString());
		} catch (Exception e){
			amount = 0;
		}

		if (amount <= 0) {
			transactionAmount.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {
			transactionAmount.setBackgroundColor(0);
		}
		
		String desc = desctEdit.getText().toString();
		if (desc == null || desc.isEmpty()) {
			valida = false;
			desctEdit.setBackgroundColor(getResources().getColor(R.color.errorous));
		} else {
			desctEdit.setBackgroundColor(0);
		}
		

		if (valida == false) return;
		
		int ind = currecySpinner.getSelectedItemPosition();
		
		Accumulation a = new Accumulation();
		a.description = desc;
		a.target_amount = amount * currency_spinner_rates[ind];
		a.amount = 0;
		
		db.open();
		db.AddNewAccumulation(a);
		db.close();
		
		finish();
	}
}
