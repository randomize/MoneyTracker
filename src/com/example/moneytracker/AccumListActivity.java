package com.example.moneytracker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class AccumListActivity extends Activity {

	private DatabaseFacade db;

	private int ids[];
	private float amounts[];
	private String names[];
	private float rates[];
	ListView lv ;

	private String[] cur_names = null;
	private float[] cur_rates = null;
	private int[] cur_ids = null;
	
	private AccumListAdapter main_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		db = new DatabaseFacade(this);
		
		setContentView(R.layout.activity_accums);
		
		lv = (ListView) findViewById(R.id.accums_list_view);

		registerForContextMenu(lv);
		
		//lv.setOnItemClickListener()
		
		lv.setEmptyView(findViewById(R.id.accums_empter));
		
		LoadCurrency();

	}
	
	@Override
	protected void onResume() {
		LoadData();
		SwitchTo(0);
		super.onResume();
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

	private void LoadData() {
		
       db.open();
       ArrayList<Accumulation> bug = db.GetAccumulations();
       db.close();
       
       ids = new int[bug.size()];
       names = new String[bug.size()];
       amounts = new float[bug.size()];
       rates = new float[bug.size()];

       for (int i = 0; i < bug.size(); i++) {

    	   Accumulation b = bug.get(i);
    	   ids[i] = b.id;
    	   names[i] = b.description;
    	   amounts[i] = b.amount;
       }

       AccumListAdapter adapter = new AccumListAdapter(this, bug);
       lv.setAdapter(adapter);
       main_adapter = adapter;
			
	}
	
	
	public void OpenDetailedCategory(int catId, String name) {

		Intent intent = new Intent(this, TransactionListActivity.class);
		intent.putExtra("Title", name);
		intent.putExtra("CategoryID", catId);
		startActivity(intent);
		
	}

	
	public void onListItemClick(ListView l, View v, int position, long id) {


	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			return;
		}

		menu.setHeaderTitle(names[info.position]);
		menu.add(0, 143, 0, getString(R.string.accum_commit));
		menu.add(0, 142, 0, getString(R.string.accum_delete));
		menu.add(0, 141, 0, getString(R.string.accum_append));

	};

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if(item.getItemId() == 142)
		{
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			DeleteAccumulation(ids[info.position]);
			return true;
		}
		else if (item.getItemId() == 143)
		{
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			CommitAccumulation(info.position);
			return true;
		} 
		
		return false;
	}

	private void DeleteAccumulation(final int id) 
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					
					db.open();
					db.RemoveAccumulation(id, 1); // TODO: must choose
					db.close();
					
					LoadData();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					//No button clicked
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.sure)).setPositiveButton(getString(R.string.yes), dialogClickListener)
		.setNegativeButton(getString(R.string.no), dialogClickListener).show();
	}

	private void CommitAccumulation(final int pos) {
		db.open();
		db.CommitAccumulation(ids[pos]);
		db.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.accumulation_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_accumulation:
			AddAccumulation();
			return true;
		case R.id.action_change_currency:
			PopupCurrencySelector();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
		main_adapter.curr_rate = cur_rates[i];
		main_adapter.curr_suffix = cur_names[i];
		main_adapter.notifyDataSetChanged();
	}

	private void AddAccumulation() {
		//Intent intent = new Intent(this, BugetAddActivity.class);
		//startActivity(intent);
	}

}
