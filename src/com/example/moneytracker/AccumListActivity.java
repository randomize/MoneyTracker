package com.example.moneytracker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
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
	private float target_amounts[];
	private String names[];
	private float rates[];
	ListView lv ;

	private String[] cur_names = null;
	private float[] cur_rates = null;
	private int[] cur_ids = null;
	
	private AccumListAdapter main_adapter;
	
	private static final int ID_MENU_COMMIT = 142;
	private static final int ID_MENU_CANCEL = 143;
	private static final int ID_MENU_APPEN = 144;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setTitle(R.string.accumumations);

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
       target_amounts = new float[bug.size()];
       rates = new float[bug.size()];

       for (int i = 0; i < bug.size(); i++) {

    	   Accumulation b = bug.get(i);
    	   ids[i] = b.id;
    	   names[i] = b.description;
    	   amounts[i] = b.amount;
    	   target_amounts[i] = b.target_amount;
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
		menu.add(0, ID_MENU_COMMIT, 1, getString(R.string.accum_commit));
		menu.findItem(ID_MENU_COMMIT).setEnabled(amounts[info.position] >= target_amounts[info.position]);

		menu.add(0, ID_MENU_CANCEL, 2, getString(R.string.accum_delete));
		menu.add(0, ID_MENU_APPEN, 3, getString(R.string.accum_append));

	};

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId()) {
		case ID_MENU_APPEN:
			EditAccumulation(info.position);
			break;
		case ID_MENU_CANCEL:
			DeleteAccumulation(info.position);
			break;
		case ID_MENU_COMMIT:
			CommitAccumulation(info.position);
			break;

		default:
			return false;
		}
		
		return true;

	}

	private void EditAccumulation(int position) {
		
		Intent intent = new Intent(this, AccumEditActivity.class);
		intent.putExtra("AccumID", ids[position]);
		intent.putExtra("Accumulating", true);
		startActivity(intent);
	}

	private void CancelAccumulation(int position) {
		
		Intent intent = new Intent(this, AccumEditActivity.class);
		intent.putExtra("AccumID", ids[position]);
		intent.putExtra("Accumulating", false);
		intent.putExtra("Amount", amounts[position]);
		startActivity(intent);
		//startActivityForResult(intent, 42);
	}
	
	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if(resultCode == RESULT_OK){
				String result=data.getStringExtra("result");
			}
			if (resultCode == RESULT_CANCELED) {
				//Write your code if there's no result
			}
		}
	}*/

	private void DeleteAccumulation(final int pos) 
	{
		CancelAccumulation(pos);
		LoadData();
	}

	private void CommitAccumulation(final int pos) {
		db.open();
		db.CommitAccumulation(ids[pos]);
		db.close();
		LoadData();
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
		Intent intent = new Intent(this, AccumulationAddActivity.class);
		startActivity(intent);
	}

}
