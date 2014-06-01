package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ExchangeActivity extends Activity {
	
	// Easy access to database
	private DatabaseFacade db;
	
	
	private int account_spinner_ids[] = null;              // index => id mapping
	private String account_spinner_lables[] = null;        // index => label (like Cash)

	private Currency account_spinner_currencies[] = null;  // index => label (like USD)
	private float selected_currency_rate = 1.0f;

	private float account_limits[] = null;
	private float selected_account_limit = 0;
	
	private Spinner accountSpinner1;
	private Spinner accountSpinner2;
	private TextView currencyLabel;
	private EditText transactionAmount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_exchange);
		
		db = new DatabaseFacade(this);
		
		accountSpinner1 = (Spinner) findViewById(R.id.SpinnerDebtAccount);
		accountSpinner2 = (Spinner) findViewById(R.id.SpinnerDebtAccount);
		currencyLabel = (TextView) findViewById(R.id.TextViewExCurrency);
		
		SetupAccountSpinner();
	}
	
	
	public void CancelExchAdd( View button ) {
		finish();
	}

	public void CommitExchAdd( View button ) {

		boolean valida = true;

		transactionAmount = (EditText) findViewById(R.id.EditTextExAmount);
		float amount = Float.valueOf(transactionAmount.getText().toString());
		if (amount <= 0) {
			transactionAmount.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {
			transactionAmount.setBackgroundColor(0);
		}

		if (valida == false) return;
		
		db.open();
		//db.PerformExchange(, tra);
		db.close();
		
		// check self assing
		finish();
	}

	private void SetupAccountSpinner() {

		db.open();
		ArrayList<Account> acs = db.GetAccounts();
		db.close();
		
		if (acs.size() < 2) {
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

			dlgAlert.setMessage(getString(R.string.account_exchange_too_few_accounts));
			dlgAlert.setTitle(getString(R.string.error));
			dlgAlert.setPositiveButton(getString(R.string.ok),
					
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}
			);

			dlgAlert.setCancelable(false);
			dlgAlert.create().show();
			
			return;
			
		}
		
		account_spinner_ids = new int[acs.size()];
		account_spinner_lables = new String[acs.size()];
		account_spinner_currencies = new Currency[acs.size()];
		account_limits = new float[acs.size()];

		db.open();
		for (int i = 0; i < acs.size(); i++) {
			Account ac = acs.get(i);
			account_spinner_ids[i] = ac.id;
			account_spinner_currencies[i] = new Currency();
			account_spinner_currencies[i].id = ac.currencyId;
			account_spinner_currencies[i].name = ac.currencyName;
			account_spinner_currencies[i].rate = ac.currencyRate;
			
			float max = db.GetCurrentBalance(ac.id) / ac.currencyRate;
			account_limits[i] = max;

			account_spinner_lables[i] = Account.GetLocalized(this,ac.name) + " = "+ String.format("%.2f",max) +" (" + ac.currencyName + ")";
			
		}
		db.close();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, account_spinner_lables);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		accountSpinner1.setAdapter(adapter);
		accountSpinner2.setAdapter(adapter);

		// Set handler
		accountSpinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
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
}
