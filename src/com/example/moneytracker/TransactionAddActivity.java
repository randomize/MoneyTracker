package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class TransactionAddActivity extends Activity {

	// Easy access to database
	private DatabaseFacade db;
	
	
	// Datas
	
	private int income_spinner_ids[] = null;         // index => id mapping
	private String income_spinner_lables[] = null;   // index => label (like Salary)

	private int outcome_spinner_ids[] = null;        // index => id mapping
	private String outcome_spinner_lables[] = null;  // index => label (like Provisions)

	private int account_spinner_ids[] = null;              // index => id mapping
	private String account_spinner_lables[] = null;        // index => label (like Cash)
	private Currency account_spinner_currencies[] = null;  // index => label (like USD)
	private float selected_currency_rate = 1.0f;
	
	// Views
	
	private Spinner typeSpinner;
	private Spinner categorySpinner;
	private Spinner accountSpinner;
	private TextView currencyLabel;
	private EditText transactionAmount;
	private EditText commentEdit;
	private DatePicker dater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trans_add);
		setTitle(getString(R.string.new_trans));
		
		db = new DatabaseFacade(this);
		
		typeSpinner = (Spinner) findViewById(R.id.SpinnerTransType);
		categorySpinner = (Spinner) findViewById(R.id.SpinnerTransCategory);
		accountSpinner = (Spinner) findViewById(R.id.SpinnerTransAccount);
		currencyLabel = (TextView) findViewById(R.id.TextViewCurrency);
		
		SetupAccountSpinner();
		SetupCategorySpinner();

	}
	
	private void SetupCategorySpinner() {
		
		db.open();
		db.close();
		
	}
	
	private void SetupAccountSpinner() {

		db.open();
		ArrayList<Account> acs = db.GetAccounts();
		db.close();
		
		if (acs.size() == 0) {
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

			dlgAlert.setMessage(getString(R.string.new_trans_no_account));
			dlgAlert.setTitle(getString(R.string.error));
			dlgAlert.setPositiveButton(getString(R.string.ok), null);
			dlgAlert.setCancelable(false);
			dlgAlert.create().show();

			/*dlgAlert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//dismiss the dialog 
					
				}
			});*/
			
			finish();
		}
		
		account_spinner_ids = new int[acs.size()];
		account_spinner_lables = new String[acs.size()];
		account_spinner_currencies = new Currency[acs.size()];

		for (int i = 0; i < acs.size(); i++) {
			Account ac = acs.get(i);
			account_spinner_ids[i] = ac.id;
			account_spinner_lables[i] = Account.GetLocalized(this,ac.name) + " (" + ac.currencyName + ")";
			account_spinner_currencies[i] = new Currency();
			account_spinner_currencies[i].id = ac.currencyId;
			account_spinner_currencies[i].name = ac.currencyName;
			account_spinner_currencies[i].rate = ac.currencyRate;
			
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, account_spinner_lables);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		accountSpinner.setAdapter(adapter);

		// Set handler
		accountSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				OnSelectedAccount(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				OnSelectedAccount(0); //assume default is always there
			}

		});
		
		OnSelectedAccount(0); // assume there is at least one
		
	}
	
	private void OnSelectedAccount(int index) {
		selected_currency_rate = account_spinner_currencies[index].rate;
		currencyLabel.setText(account_spinner_currencies[index].name);
	}

	// On button cancel
	public void CancelTransAdd( View button ) {
		finish();
	}

	public void CommitTransAdd( View button ) {

		transactionAmount = (EditText) findViewById(R.id.EditTextTransactionAmount);
		float amount = Float.valueOf(transactionAmount.getText().toString());
		if (amount <= 0) {
			transactionAmount.setBackgroundColor(getResources().getColor(R.color.errorous));
			return;
		} else {
			transactionAmount.setBackgroundColor(0);
		}
		
		Transaction newman = new Transaction();
		newman.amount = amount * selected_currency_rate;
		newman.accountID = account_spinner_ids[accountSpinner.getSelectedItemPosition()];
		if (typeSpinner.getSelectedItemPosition() == 0) // outcome
		{
			newman.categoryID = outcome_spinner_ids[categorySpinner.getSelectedItemPosition()];
		} else {
			newman.categoryID = income_spinner_ids[categorySpinner.getSelectedItemPosition()];
		}


		final EditText commentField = (EditText) findViewById(R.id.EditTextAccountComment);
		String comment = commentField.getText().toString();

		
	}

}
