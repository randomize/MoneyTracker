package com.example.moneytracker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

	private int outcome_spinner_ids[] = null;        // index => id mapping
	private String outcome_spinner_lables[] = null;  // index => label (like Provisions)
	private ArrayAdapter outcome_ad;
	
	private int member_ids[] = null;
	private String member_labels[] = null;
	private int add_member_index = -1;
	private int member_newman_id = -1; // To handle cool selection of last added member
	private HashMap<Integer, Integer> member_map = new HashMap<Integer, Integer>();

	private int account_spinner_ids[] = null;              // index => id mapping
	private String account_spinner_lables[] = null;        // index => label (like Cash)
	private Currency account_spinner_currencies[] = null;  // index => label (like USD)
	private float selected_currency_rate = 1.0f;
	
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
		
		if (cats.size() == 0) {
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
