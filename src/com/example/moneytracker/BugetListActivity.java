package com.example.moneytracker;

import java.text.DateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
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
import android.widget.ListView;
import android.widget.TextView;


public class BugetListActivity extends Activity {

	private DatabaseFacade db;
	private int ids[];
	ListView lv ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		db = new DatabaseFacade(this);
		
		setContentView(R.layout.activity_bugets);
		
		lv = (ListView) findViewById(R.id.bugets_view);

		registerForContextMenu(lv);
		
		lv.setEmptyView(findViewById(R.id.bugets_empter));

	}
	
	@Override
	protected void onResume() {
		LoadData();
		super.onResume();
	}
	
	private void LoadData() {
		
       db.open();
       ArrayList<Buget> bug = db.GetBugetsList();
       db.close();
       
       ids = new int[bug.size()];
       for (int i = 0; i < bug.size(); i++) {
    	   ids[i] = bug.get(i).id;
       }

       ArrayAdapter<Buget> adapter = new BugetListAdapter(this, bug);
       lv.setAdapter(adapter);
			
	}
	
	
	/*
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

		dlgAlert.setMessage(desc[position]);
		dlgAlert.setTitle(list[position]);
		dlgAlert.setPositiveButton(getString(R.string.ok), null);
		dlgAlert.setCancelable(false);
		dlgAlert.create().show();
	}*/

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			return;
		}
		
		menu.add(0, 142, 0, getString(R.string.delete_buget));

	};

	private void DeleteTransaction(final int id) 
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					
					db.open();
					db.RemoveBuget(id);
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

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if(item.getItemId() == 142)
		{
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			//long id = this.listView.getItemIdAtPosition(info.position);
			DeleteTransaction(ids[info.position]);
		}
		else
		{
			return false;
		}
		return true;
	}

	/// Main menu
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.buget_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_buget:
			AddBuget();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void AddBuget() {
		Intent intent = new Intent(this, BugetAddActivity.class);
		startActivity(intent);
	}

}
