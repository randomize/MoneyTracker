package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class AccumEditActivity extends Activity {
	
	// Easy access to database
	private DatabaseFacade db;
	
	
	private int account_spinner_ids[] = null;              // index => id mapping
	private String account_spinner_lables[] = null;        // index => label (like Cash)

	private Currency account_spinner_currencies[] = null;  // index => Currency

	private float account_limits[] = null;
	
	private Spinner accountSpinner;
	private TextView currencyLabel;
	private EditText transactionAmount;
	
	private int accumID;
	private boolean isAccumulating;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_accum_edit);

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("AccumID")) {
			accumID = extras.getInt("AccumID");
			isAccumulating = extras.getBoolean("Accumulating", true);
			if (isAccumulating) {
				setTitle("Add to accumulation");
				
			} else {
				setTitle("Put money back to");
			}
		} else {
			finish();
		}
		
		db = new DatabaseFacade(this);
		
		accountSpinner = (Spinner) findViewById(R.id.AccumSpinner);
		currencyLabel = (TextView) findViewById(R.id.TextViewAccumCurrency);
		transactionAmount = (EditText) findViewById(R.id.EditTextAccumAmount);
		
		if (isAccumulating == false) {
			transactionAmount.setVisibility(View.GONE); // Hide amount when not accum
		}
		SetupAccountSpinner();
	}
	
	
	public void CancelAccumAdd( View button ) {
		finish();
	}

	public void CommitAccumAdd( View button ) {

		boolean valida = true;

		int selFrom = accountSpinner.getSelectedItemPosition();


		String amount_str = transactionAmount.getText().toString();

		float amount = 0;
		if (amount_str == null || amount_str.isEmpty()) {
			transactionAmount.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {

			try {
				amount = Float.valueOf(amount_str);
			}  catch(NumberFormatException e) {
				amount = 0;
			}

			if (amount <= 0) {
				transactionAmount.setBackgroundColor(getResources().getColor(R.color.errorous));
				valida = false;
			} else {

				boolean alow_neg_balan = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("allow_negative", false) ;

				if ( alow_neg_balan == false && amount > account_limits[selFrom]) {
					transactionAmount.setBackgroundColor(getResources().getColor(R.color.errorous));
					valida = false;
				} else {
					transactionAmount.setBackgroundColor(0);
				}
			}
		}

		if (valida == false) return;
		
		db.open();
		db.AddAmountToAccum(accumID, account_spinner_ids[selFrom], amount * account_spinner_currencies[selFrom].rate);
		db.close();
		
		// check self assing
		finish();
	}

	private void SetupAccountSpinner() {

		db.open();
		ArrayList<Account> acs = db.GetAccounts();
		db.close();
		
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
		accountSpinner.setAdapter(adapter);
		accountSpinner.setSelection(1);

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
		currencyLabel.setText(account_spinner_currencies[index].name);
	}
}
