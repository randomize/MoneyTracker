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

public class BugetAddActivity extends Activity {

	// Easy access to database
	private DatabaseFacade db;
	
	
	private int outcome_spinner_ids[] = null;        // index => id mapping
	private String outcome_spinner_lables[] = null;  // index => label (like Provisions)
	private ArrayAdapter outcome_ad;
	private int outcom_spinned_add_index = -1;
	
	private int member_ids[] = null;
	private String member_labels[] = null;
	private int add_member_index = -1;
	private int member_newman_id = -1; // To handle cool selection of last added member
	private HashMap<Integer, Integer> member_map = new HashMap<Integer, Integer>();

	// Views
	
	private Spinner typeSpinner;        // 0 - week, 1 - month
	private Spinner categorySpinner;    // only outcome

	private Spinner currencySpinner;
	private TextView currencyLabel;
	// Currency spinner helpers
	private int currency_spinner_ids[] = null;                        // index => id mapping
	private String currency_spinner_lables[] = null;                  // index => label (like USD)
	private float currency_spinner_rates[] = null;                  // index => rate (like USD)
	private int currency_spinner_add_index = 0;
	
	private NDSpinner memberSpinner;

	private EditText bugetName;
	private EditText bugetAmount;
	private EditText commentEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buget_add);
		setTitle(getString(R.string.new_buget));
		
		db = new DatabaseFacade(this);
		
		typeSpinner = (Spinner) findViewById(R.id.SpinnerBugetType);
		categorySpinner = (Spinner) findViewById(R.id.SpinnerBugetCategory);

		currencySpinner = (Spinner) findViewById(R.id.SpinnerBugetCurrency);
		currencyLabel = (TextView) findViewById(R.id.TextViewBugetCurrency);

		memberSpinner = (NDSpinner) findViewById(R.id.SpinnerBugetMember);
		
		bugetName = (EditText) findViewById(R.id.EditTextBugetName);
		bugetAmount = (EditText) findViewById(R.id.EditTextBugetAmount);
		commentEdit = (EditText) findViewById(R.id.EditTextBugetComment);
		
		SetupCategorySpinner();
		SetupCurrencySpinner();
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
		
		categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				if (position == outcom_spinned_add_index) {
					parentView.setSelection(0);
					CreateNewCategory(0); // create outcome
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) { }

		});
		
		// Set handler
		currencySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				
				// If user clicked "New ..." item
				if (position == currency_spinner_add_index) {
					ShowNewCurrencyDialog();
				} else {
					currencyLabel.setText(currency_spinner_lables[position]);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}

		});
		
	}
	
	private void SetupCurrencySpinner() {
		
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
		currency_spinner_rates[currency_spinner_add_index] = 0;

		// Set spinner items(using adapter)
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currency_spinner_lables);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		currencySpinner.setAdapter(adapter);

	}
	
	private void ShowNewCurrencyDialog() {

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
					TransactionCategory newman = new TransactionCategory();
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
		ArrayList<TransactionCategory> cats = db.GetCategories();
		db.close();
		
		ArrayList<TransactionCategory> out = new ArrayList<TransactionCategory>();
		
		boolean alow_debts_man = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("allow_debts_manual", false) ;
		for (TransactionCategory cat : cats) {
			if (alow_debts_man == false && cat.id <= 4) continue; // skipping debts default hidden catags
			if (cat.type == 0) {
				out.add(cat);
			}
		}
		
		if (out.size() == 0) { // error if no categs for outcome
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
		
		outcome_spinner_ids = new int[out.size()+1];
		outcome_spinner_lables = new String[out.size()+1];
		
		
		for (int i = 0; i < out.size(); i++) {
			TransactionCategory ac = out.get(i);
			outcome_spinner_ids[i] = ac.id;
			outcome_spinner_lables[i] = TransactionCategory.GetLocalizedCategory(this, ac.name);
		}
		
		outcom_spinned_add_index = out.size();
		outcome_spinner_ids[outcom_spinned_add_index] = -1;
		outcome_spinner_lables[outcom_spinned_add_index] = getString(R.string.new_category_outcome);
		
		
		outcome_ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, outcome_spinner_lables);
		outcome_ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(outcome_ad);
		
		
	}
	

	// On button cancel
	public void CancelBugetAdd( View button ) {
		finish();
	}

	public void CommitBugetAdd( View button ) {

		boolean valida = true;
		
		float amount = Float.valueOf(bugetAmount.getText().toString());
		if (amount <= 0) {
			bugetAmount.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {
			bugetAmount.setBackgroundColor(0);
		}

		int mem_index = memberSpinner.getSelectedItemPosition();
		if (mem_index == add_member_index) {
			memberSpinner.setBackgroundColor(getResources().getColor(R.color.errorous));
			valida = false;
		} else {
			memberSpinner.setBackgroundColor(0);
		}

		String name = bugetName.getText().toString();
		if (name.isEmpty()) {
			bugetName.setBackgroundColor(getResources().getColor(R.color.errorous));
		} else {
			bugetName.setBackgroundColor(0);
		}

		if (valida == false) return;

		
		// TODO:
		Buget newman = new Buget();
		
		int cur_pos = currencySpinner.getSelectedItemPosition();
		newman.amount = amount / currency_spinner_rates[cur_pos];
		newman.currencyID = currency_spinner_ids[cur_pos];
		
		int cat_pos = categorySpinner.getSelectedItemPosition();
		newman.categoryID = outcome_spinner_ids[cat_pos];
		
		int mem_pos = memberSpinner.getSelectedItemPosition();
		newman.memberID = member_ids[mem_pos];
		
		int mem_type = typeSpinner.getSelectedItemPosition();
		newman.type = mem_type;
		
		newman.name = name;
		
		
		
		String s = commentEdit.getText().toString();
		if (s.isEmpty() == false) {
			newman.desc = s;
		}
		
		db.open();
		db.AddNewBuget(newman);
		db.close();
		
		finish();
		
	}

}
