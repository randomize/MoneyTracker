package com.example.moneytracker;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class DebtAddActivity extends Activity {
	
	
	// Easy access to database
	private DatabaseFacade db;
	
	// Datas

	private int type;
	
	private static final int category_income_debt_id = 1;
	private static final int category_outcome_debt_id = 2;
	
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
	
	private EditText descEdit;
	//private Spinner categorySpinner;
	private Spinner accountSpinner;
	private NDSpinner memberSpinner;

	private TextView currencyLabel1;
	private TextView currencyLabel2;

	private EditText amount_start;
	private EditText amount_end;
	//private DatePicker dater_start;
	private DatePicker dater_end;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("Type")) { // if passed => edit mode
			type = extras.getInt("Type");
		}
		
		if (type == 1) {
			setTitle(R.string.new_debt_income);
		} else {
			setTitle(R.string.new_debt_outcome);
		}
		
		setContentView(R.layout.activity_debt_add);

		db = new DatabaseFacade(this);
		
		accountSpinner = (Spinner) findViewById(R.id.SpinnerDebtAccount);
		memberSpinner = (NDSpinner) findViewById(R.id.SpinnerMember);
		currencyLabel1 = (TextView) findViewById(R.id.TextViewCurrency1);
		currencyLabel2 = (TextView) findViewById(R.id.TextViewCurrency2);
		descEdit = (EditText) findViewById(R.id.EditTextDescriptio);
		dater_end = (DatePicker) findViewById(R.id.date_end);
		amount_start = (EditText) findViewById(R.id.EditTextAmountStart);
		amount_end = (EditText) findViewById(R.id.EditTextAmountEnd);
		
		SetupAccountSpinner();
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
		currencyLabel1.setText(account_spinner_currencies[index].name);
		currencyLabel2.setText(account_spinner_currencies[index].name);
	}
	public void CancelDebtAdd(View v) {
		finish();
	}

	public void CommitDebtAdd(View v) {
		VerifyAndAddDebt();
	}
	
	private void VerifyAndAddDebt() {

		boolean valida = true;
		
		float amount_start_val = Float.valueOf(amount_start.getText().toString());
		if (amount_start_val <= 0) {
			amount_start.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {
			amount_start.setBackgroundColor(0);
		}

		float amount_end_val = Float.valueOf(amount_end.getText().toString());
		if (amount_end_val <= 0) {
			amount_end.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {
			amount_end.setBackgroundColor(0);
		}

		int mem_index = memberSpinner.getSelectedItemPosition();
		if (mem_index == add_member_index) {
			memberSpinner.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {
			memberSpinner.setBackgroundColor(0);
		}
		
		String desc = descEdit.getText().toString();
		if (desc.isEmpty()) {
			descEdit.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {
			descEdit.setBackgroundColor(0);
		}
		
		if (valida == false) return;
		
		Debt newman = new Debt();
		newman.desc = desc;
		newman.accountID = account_spinner_ids[accountSpinner.getSelectedItemPosition()];
		newman.amount_start = amount_start_val * selected_currency_rate;
		newman.amount_end = amount_end_val * selected_currency_rate;
		newman.date_start =  System.currentTimeMillis() / 1000L;
		newman.date_end = dater_end.getCalendarView().getDate();


		finish();
		
	}
	
	
}
