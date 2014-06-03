package com.example.moneytracker;

import java.text.DateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class SummaryActivity extends Activity {
	
	private DatabaseFacade db = null;

	private String[] cur_names = null;
	private float[] cur_rates = null;
	private int[] cur_ids = null;
	
	private TransactionCategoryExpandableListAdapter main_adapter;
	private	ExpandableListView mainList;

	private ArrayList<TransactionCategoryGroup> groups = new ArrayList<TransactionCategoryGroup>();
	final Context context = this;
	
	private boolean isSlices = false;
	private long sliceFrom = 0;
	private long sliceTo = 0;
	private TextView slice_display;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status);

		mainList = (ExpandableListView) findViewById(R.id.trans_categories_listview);
		mainList.setEmptyView(findViewById(R.id.trans_cat_list_empter));
		slice_display = (TextView) findViewById(R.id.trans_slice_range);
		
		db = new DatabaseFacade(this);
		
		LoadCurrency();
		CreateAdapter();
		//LoadActualData();

	}
	
	public void OpenDetailedCategory(int catId, String name) {

		Intent intent = new Intent(this, TransactionListActivity.class);
		intent.putExtra("Title", name);
		intent.putExtra("CategoryID", catId);
		startActivity(intent);
		
	}
	
	// Create adapter for transaction groups
	private void CreateAdapter() {
		
		TransactionCategoryExpandableListAdapter adapter = new TransactionCategoryExpandableListAdapter(this, groups);
		main_adapter = adapter;
		SwitchTo(0); // Default use main currency
		mainList.setAdapter(adapter);
	}

	// Load currency list and rates
	private void LoadCurrency() {
		db.open();
		ArrayList<Currency> curList = db.GetCurrencyList();
		cur_names = new String[curList.size()];
		cur_ids = new int[curList.size()];
		cur_rates = new float[curList.size()];
		for (int i = 0; i < curList.size(); i++) {
			cur_names[i] = curList.get(i).name;
			cur_ids[i] = curList.get(i).id;
			cur_rates[i] = curList.get(i).rate;
		}
		db.close();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Load data from db
		LoadActualData();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.status_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_tranaction_new:
			OpenNewTransactionActivity();
			return true;
		case R.id.action_change_currency:
			PopupCurrencySelector();
			return true;
		case R.id.action_category_new_outcome:
			CreateNewCategory(0);
			return true;
		case R.id.action_category_new_income:
			CreateNewCategory(1);
		case R.id.action_set_interval:
			SetInterval(null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void SetInterval(View v) {

		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.interval_picker);
		dialog.setTitle(R.string.pick_a_date_slice);

		if (isSlices) {
			DatePicker dp1 = (DatePicker) dialog.findViewById(R.id.date_start);
			DatePicker dp2 = (DatePicker) dialog.findViewById(R.id.date_end);
			dp1.getCalendarView().setDate(sliceFrom);
			dp2.getCalendarView().setDate(sliceTo);
		}

		Button dialogButton = (Button) dialog.findViewById(R.id.ButtonCancel);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				isSlices = false;
				LoadActualData();
			}
		});

		Button dialogButton2 = (Button) dialog.findViewById(R.id.ButtonSave);
		dialogButton2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePicker dp1 = (DatePicker) dialog.findViewById(R.id.date_start);
				sliceFrom = dp1.getCalendarView().getDate();
				DatePicker dp2 = (DatePicker) dialog.findViewById(R.id.date_end);
				sliceTo = dp2.getCalendarView().getDate();
				dialog.dismiss();
				isSlices = true;
				LoadActualData();
			}
		});

		dialog.show();
	}

	private void PopupCurrencySelector() {

		new AlertDialog.Builder(this)
		.setTitle(R.string.currency)
		.setCancelable(false)
		.setItems(cur_names,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				SwitchTo(i);
			}
		})
		.show();
	}
	
	private void SwitchTo(int i) {
		main_adapter.rate = cur_rates[i];
		main_adapter.suffix = cur_names[i];
		main_adapter.notifyDataSetChanged();
	}

	private void OpenNewTransactionActivity() {
		Intent intent = new Intent(this, TransactionAddActivity.class);
		startActivity(intent);
	}

	private void LoadActualData() {
		

		if (isSlices && sliceFrom != 0 && sliceTo != 0) {
			String dateFrom = DateFormat.getDateInstance().format(sliceFrom);
			String dateToo = DateFormat.getDateInstance().format(sliceTo);
			slice_display.setText(dateFrom + " -- " + dateToo);
			slice_display.setVisibility(View.VISIBLE);
		} else {
			slice_display.setVisibility(View.GONE);
		}

		groups.clear();
		db.open();
		if (isSlices) {
			groups.addAll(db.GetIncomeAndOutcome(true, sliceFrom, sliceTo));
		} else {
			groups.addAll(db.GetIncomeAndOutcome());
		}
		db.close();

		main_adapter.notifyDataSetChanged();
		mainList.invalidateViews();

		int count = main_adapter.getGroupCount();
		for (int position = 1; position <= count; position++){
			mainList.collapseGroup(position - 1);
			mainList.expandGroup(position - 1);
		}
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

	public void PopupActionsMenu(int id, String ch_text) {
		// TODO Auto-generated method stub
		
	}
}
