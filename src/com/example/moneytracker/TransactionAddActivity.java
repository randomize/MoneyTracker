package com.example.moneytracker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class TransactionAddActivity extends Activity {

	// Easy access to database
	private DatabaseFacade db;
	
	
	// Datas
	
	private int income_spinner_ids[] = null;         // index => id mapping
	private String income_spinner_lables[] = null;   // index => label (like Salary)
	private ArrayAdapter income_ad;
	private int income_spinned_add_index = -1;

	private int outcome_spinner_ids[] = null;        // index => id mapping
	private String outcome_spinner_lables[] = null;  // index => label (like Provisions)
	private ArrayAdapter outcome_ad;
	private int outcom_spinned_add_index = -1;
	
	private int member_ids[] = null;
	private String member_labels[] = null;
	private int add_member_index = -1;
	private int member_newman_id = -1; // To handle cool selection of last added member
	private HashMap<Integer, Integer> member_map = new HashMap<Integer, Integer>();

	private int account_spinner_ids[] = null;              // index => id mapping
	private String account_spinner_lables[] = null;        // index => label (like Cash)
	private Currency account_spinner_currencies[] = null;  // index => label (like USD)
	private float selected_currency_rate = 1.0f;
	private float selected_account_limit = 0;
	private float account_limits[] = null;
	
	// Views
	
	private Spinner typeSpinner;
	private Spinner categorySpinner;
	private Spinner accountSpinner;
	private NDSpinner memberSpinner;
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
		accountSpinner = (Spinner) findViewById(R.id.SpinnerDebtAccount);
		memberSpinner = (NDSpinner) findViewById(R.id.SpinnerMember);
		currencyLabel = (TextView) findViewById(R.id.TextViewCurrency1);
		
		SetupAccountSpinner();
		SetupCategorySpinner();
		LoadMembers();
		

		// On mmber click - handle "new member" option
		memberSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				if (position == add_member_index) {
					// Show dialog
					ShowNewMemberDialog();
					parentView.setSelection(0);
				}
				
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}

		});
		
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				SetAdapterForCategory();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) { }

		});

		categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				int req_indx = 0;
				int type = 0;
				if (typeSpinner.getSelectedItemPosition() == 0) // outcome 
				{
					type = 0;
					req_indx = outcom_spinned_add_index;
				} else {
					type = 1;
					req_indx = income_spinned_add_index;
				}
				if (position == req_indx) {
					parentView.setSelection(0);
					CreateNewCategory(type);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) { }

		});
	}
	
	private void CreateNewCategory(final int type) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (type == 0) 
			builder.setTitle(getString(R.string.new_category_outcome));
		else 
			builder.setTitle(getString(R.string.new_category_income));

		// Set up the input
		final EditText input = new EditText(this);
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		//input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String m_Text = input.getText().toString();
				if (m_Text.isEmpty() == false) {
					TransactionCatagory newman = new TransactionCatagory();
					newman.type = type;
					newman.name = m_Text;
					
					db.open();
					db.AddNewCategory(newman);
					db.close();
				 
					SetupCategorySpinner();

				}
			}
		});


		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		final AlertDialog dialog = builder.create();

       // The TextWatcher will look for changes to the Dialogs field.
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence c, int i, int i2, int i3) {}
            @Override public void onTextChanged(CharSequence c, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // Will be called AFTER text has been changed.
                if (editable.toString().length() == 0){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

		dialog.show();
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
	}

	private void LoadMembers() {

		db.open();
		ArrayList<Member> mems = db.GetMembers();
		db.close();
		
		member_ids = new int[mems.size()+1];
		member_labels = new String[mems.size()+1];
		
		for (int i = 0 ; i < mems.size(); i++ ) {
			Member m = mems.get(i);
			member_ids[i] = m.id;
			member_labels[i] = Member.GetLocalized(this, m.name);
			member_map.put(m.id, i);
		}
		member_labels[mems.size()] = getString(R.string.member_new);
		add_member_index = mems.size();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, member_labels);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		memberSpinner.setAdapter(adapter);
		
		if (member_newman_id >= 0 ) {
			Integer id = member_map.get(member_newman_id);
			if (id != null) {
				member_newman_id = id;
				if (member_newman_id >= 0 && member_newman_id < mems.size()) {
					memberSpinner.setSelection(member_newman_id, true);
				}
			}
		}
		
	}
	
	private void ShowNewMemberDialog() {
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.member_new));

		// Set up the input
		final EditText input = new EditText(this);
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		//input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String m_Text = input.getText().toString();
				if (m_Text.isEmpty() == false) {
					Member newman = new Member();
					newman.name = m_Text;
					
					db.open();
					member_newman_id = db.AddNewMember(newman);
					db.close();
					
					Log.i("ggg", "ggg id = " + member_newman_id);
					
					
					LoadMembers();

				}
			}
		});


		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		final AlertDialog dialog = builder.create();

       // The TextWatcher will look for changes to the Dialogs field.
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence c, int i, int i2, int i3) {}
            @Override public void onTextChanged(CharSequence c, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // Will be called AFTER text has been changed.
                if (editable.toString().length() == 0){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

		dialog.show();
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
	}
	
	private void SetupCategorySpinner() {
		
		db.open();
		ArrayList<TransactionCatagory> cats = db.GetCategories();
		db.close();
		
		if (cats.size() <= 2) {
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

			dlgAlert.setMessage(getString(R.string.new_trans_no_category));
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
		
		ArrayList<TransactionCatagory> in = new ArrayList<TransactionCatagory>();
		ArrayList<TransactionCatagory> out = new ArrayList<TransactionCatagory>();
		
		//SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		boolean alow_debts_man = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("allow_debts_manual", false) ;
		for (TransactionCatagory cat : cats) {
			if (alow_debts_man == false) {
				if (cat.id == 1 || cat.id == 2) continue; // skipping debts default categs
			}
			if (cat.type == 0) {
				out.add(cat);
			} else {
				in.add(cat);
			}
		}
		
		income_spinner_ids = new int[in.size()+1];
		income_spinner_lables = new String[in.size()+1];
		outcome_spinner_ids = new int[out.size()+1];
		outcome_spinner_lables = new String[out.size()+1];
		
		
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
		
		income_spinned_add_index = in.size();
		income_spinner_ids[income_spinned_add_index] = -1;
		income_spinner_lables[income_spinned_add_index] = getString(R.string.new_category_income);
		outcom_spinned_add_index = out.size();
		outcome_spinner_ids[outcom_spinned_add_index] = -1;
		outcome_spinner_lables[outcom_spinned_add_index] = getString(R.string.new_category_outcome);
		
		
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

		boolean valida = true;
		
		transactionAmount = (EditText) findViewById(R.id.EditTextAmountStart);
		float amount = Float.valueOf(transactionAmount.getText().toString());
		if (amount <= 0) {
			transactionAmount.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {
			transactionAmount.setBackgroundColor(0);
		}

		int mem_index = memberSpinner.getSelectedItemPosition();
		if (mem_index == add_member_index) {
			memberSpinner.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {
			memberSpinner.setBackgroundColor(0);
		}

		if (typeSpinner.getSelectedItemPosition() == 0) // outcome
		{
			if ( categorySpinner.getSelectedItemPosition() == outcom_spinned_add_index) {
				categorySpinner.setBackgroundColor(getResources().getColor(R.color.errorous));
				valida = false;
			} else {
				categorySpinner.setBackgroundColor(0);
			}
		} else {
			if ( categorySpinner.getSelectedItemPosition() == income_spinned_add_index) {
				categorySpinner.setBackgroundColor(getResources().getColor(R.color.errorous));
				valida = false;
			} else {
				categorySpinner.setBackgroundColor(0);
			}
		}

		boolean alow_neg_balan = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("allow_negative", false) ;
		//SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		if ( alow_neg_balan == false) {
			if (typeSpinner.getSelectedItemPosition() == 0) // outcome 
			{
				if (amount > account_limits[accountSpinner.getSelectedItemPosition()]) {
					transactionAmount.setBackgroundColor(getResources().getColor(R.color.errorous));
					valida = false;
				} else {
					transactionAmount.setBackgroundColor(0);
				}
			}
		}
		
		if (valida == false) return;

		
		Transaction newman = new Transaction();
		newman.amount = amount * selected_currency_rate;
		newman.accountID = account_spinner_ids[accountSpinner.getSelectedItemPosition()];
		if (typeSpinner.getSelectedItemPosition() == 0) // outcome
		{
			newman.categoryID = outcome_spinner_ids[categorySpinner.getSelectedItemPosition()];
		} else {
			newman.categoryID = income_spinner_ids[categorySpinner.getSelectedItemPosition()];
		}

		newman.memberID = member_ids[mem_index];

		final EditText commentField = (EditText) findViewById(R.id.EditTextTransactionComment);
		String comment = commentField.getText().toString();
		newman.desc = comment.isEmpty() ? null : comment;

		final DatePicker dp = (DatePicker) findViewById(R.id.date_end);
		newman.date = dp.getCalendarView().getDate();
		
		db.open();
		db.AddNewTransaction(newman);
		db.close();
		
		finish();
		
	}

}
