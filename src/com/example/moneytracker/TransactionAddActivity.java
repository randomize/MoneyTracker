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
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class TransactionAddActivity extends Activity {

	// Easy access to database
	private DatabaseFacade db;
	
	
	// Datas
	
	private int income_spinner_ids[] = null;         // index => id mapping
	private String income_spinner_lables[] = null;   // index => label (like Salary)
	private ArrayAdapter income_ad;

	private int outcome_spinner_ids[] = null;        // index => id mapping
	private String outcome_spinner_lables[] = null;  // index => label (like Provisions)
	private ArrayAdapter outcome_ad;
	
	private int member_ids[] = null;
	private String member_labels[] = null;

	private int account_spinner_ids[] = null;              // index => id mapping
	private String account_spinner_lables[] = null;        // index => label (like Cash)
	private Currency account_spinner_currencies[] = null;  // index => label (like USD)
	private float selected_currency_rate = 1.0f;
	
	// Views
	
	private Spinner typeSpinner;
	private Spinner categorySpinner;
	private Spinner accountSpinner;
	private Spinner memberSpinner;
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
		memberSpinner = (Spinner) findViewById(R.id.SpinnerMember);
		currencyLabel = (TextView) findViewById(R.id.TextViewCurrency);
		
		SetupAccountSpinner();
		SetupCategorySpinner();
		
		db.open();
		ArrayList<Member> mems = db.GetMembers();
		db.close();
		
		member_ids = new int[mems.size()];
		member_labels = new String[mems.size()];
		
		for (int i = 0 ; i < mems.size(); i++ ) {
			Member m = mems.get(i);
			member_ids[i] = m.id;
			member_labels[i] = Member.GetLocalized(this, m.name);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, member_labels);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		memberSpinner.setAdapter(adapter);
		
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				SetAdapterForCategory();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}

		});

	}
	
	private void SetupCategorySpinner() {
		
		db.open();
		ArrayList<TransactionCatagory> cats = db.GetCategories();
		db.close();
		
		if (cats.size() == 0) {
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

			dlgAlert.setMessage(getString(R.string.new_trans_no_category));
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
		
		ArrayList<TransactionCatagory> in = new ArrayList<TransactionCatagory>();
		ArrayList<TransactionCatagory> out = new ArrayList<TransactionCatagory>();
		
		for (TransactionCatagory cat : cats) {
			if (cat.type == 0) {
				out.add(cat);
			} else {
				in.add(cat);
			}
		}
		
		income_spinner_ids = new int[in.size()];
		income_spinner_lables = new String[in.size()];
		outcome_spinner_ids = new int[out.size()];
		outcome_spinner_lables = new String[out.size()];
		
		
		for (int i = 0; i < in.size(); i++) {
			TransactionCatagory ac = in.get(i);
			income_spinner_ids[i] = ac.id;
			income_spinner_lables[i] = TransactionCatagory.GetLocalizedCategory(this, ac.name);
		}
		for (int i = 0; i < out.size(); i++) {
			TransactionCatagory ac = out.get(i);
			outcome_spinner_ids[i] = ac.id;
			outcome_spinner_lables[i] = TransactionCatagory.GetLocalizedCategory(this, ac.name);
		}
		
		
		income_ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, income_spinner_lables);
		income_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		outcome_ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, outcome_spinner_lables);
		outcome_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		SetAdapterForCategory();
		
		
	}
	
	private void SetAdapterForCategory() {

		if (typeSpinner.getSelectedItemPosition() == 0) // outcome
		{
			categorySpinner.setAdapter(outcome_ad);
		} else {
			categorySpinner.setAdapter(income_ad);
		}
		//categorySpinner.invalidate();
		
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


		newman.memberID = member_ids[memberSpinner.getSelectedItemPosition()];

		final EditText commentField = (EditText) findViewById(R.id.EditTextTransactionComment);
		String comment = commentField.getText().toString();
		newman.desc = comment.isEmpty() ? null : comment;

		final DatePicker dp = (DatePicker) findViewById(R.id.datePicker1);
		String date = dp.toString();
		newman.date = date;
		
		db.open();
		db.AddNewTransaction(newman);
		db.close();
		
		finish();
		
	}

}
