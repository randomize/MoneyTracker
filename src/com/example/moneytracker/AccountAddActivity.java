package com.example.moneytracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;


public class AccountAddActivity extends Activity {

	// Easy access to database
	private DatabaseFacade db;

	// Currency spinner helpers
	private int currency_spinner_ids[] = null;                        // index => id mapping
	private String currency_spinner_lables[] = null;                  // index => label (like USD)
	private int currency_spinner_add_index = 0;
	
	private enum Mode {
		NEW,
		EDIT
	}
	
	private Mode mode = Mode.NEW;
	private int editableIndex = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_add);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("EditIndex")) { // if passed => edit mode
			mode = Mode.EDIT;
			editableIndex = extras.getInt("EditIndex");
		}

		db = new DatabaseFacade(this);
		db.open();
		ArrayList<Currency> currency_map = db.GetCurrencyList();
		db.close();

		currency_spinner_ids = new int[currency_map.size()+1]; // Reservin one for "new..."
		currency_spinner_lables = new String[currency_map.size()+1];
		for (int i = 0; i < currency_map.size(); i++) {
			Currency c = currency_map.get(i);
			currency_spinner_ids[i] = c.id;
			currency_spinner_lables[i] = c.name;
		}

		// Always add custom new currency item option
		currency_spinner_add_index = currency_map.size();
		currency_spinner_lables[currency_spinner_add_index] = getString(R.string.new_curr);
		currency_spinner_ids[currency_spinner_add_index] = -1; // JIC

		/*final Spinner currSpinner = (Spinner) findViewById(R.id.SpinnerCurrency);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(activity, R.layout.simple_spinner_item);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);*/

		// Set spinner items(using adapter)
		Spinner spinner_cur = (Spinner) findViewById(R.id.SpinnerCurrency);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currency_spinner_lables);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_cur.setAdapter(adapter);

		// Set handler
		spinner_cur.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				
				// If user clicked "New ..." item
				if (position == currency_spinner_add_index) {
					ShowNewCurrencyDialog();
				}
				// your code here
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// your code here
			}

		});
		
		if (mode == Mode.EDIT) {
			LoadCurrentValues();
		}
	}

	private void LoadCurrentValues() {
		
		// TODO: load from db
		
	}

	private void ShowNewCurrencyDialog() {
		// TODO Show activity and get result
		Spinner spinner_cur = (Spinner) findViewById(R.id.SpinnerCurrency);
		// spinner_cur.sele // select new currency
	}
	
	// On result

	// On button cancel
	public void CancelAccountAdd( View button ) {
		finish();
	}

	public void CommitAccountAdd( View button ) {

		final EditText nameField = (EditText) findViewById(R.id.EditTextTransactionAmount);
		String name = nameField.getText().toString();
		if (name.isEmpty())
		{
			nameField.setBackgroundColor(getResources().getColor(R.color.errorous));
			return;
		}

		final EditText commentField = (EditText) findViewById(R.id.EditTextAccountComment);
		String comment = commentField.getText().toString();

		final Spinner typeSpinner = (Spinner) findViewById(R.id.SpinnerAccountType);
		String accountType = typeSpinner.getSelectedItem().toString();
		int indx_type = typeSpinner.getSelectedItemPosition(); 

		final Spinner currSpinner = (Spinner) findViewById(R.id.SpinnerCurrency);
		String currecy = currSpinner.getSelectedItem().toString();
		int indx_cur = currSpinner.getSelectedItemPosition();
		
		Account newman = new Account();
		newman.name = name;
		newman.currencyId = currency_spinner_ids[indx_cur];
		newman.typeId = indx_type;
		if (comment.isEmpty()) {
			newman.comment = null;
		} else {
			newman.comment = comment;
		}
		
		db.open();
		if (mode == Mode.NEW) {
			db.AddNewAccount(newman);
			Log.i("debug", "Adding account " + indx_type + " (" + accountType + ") + name="+name+ " comm="+comment);
		} else {
			db.UpdateAccount(newman);
		}
		db.close();

		finish();
	}

}
