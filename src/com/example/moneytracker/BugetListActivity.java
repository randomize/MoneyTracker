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


public class BugetListActivity extends Activity {

	private DatabaseFacade db;
	private int ids[];
	private float amounts[];
	private String names[];
	private float rates[];
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
       names = new String[bug.size()];
       amounts = new float[bug.size()];
       rates = new float[bug.size()];

       for (int i = 0; i < bug.size(); i++) {

    	   Buget b = bug.get(i);
    	   ids[i] = b.id;
    	   names[i] = b.name;
    	   amounts[i] = b.amount;
    	   rates[i] = b.currencyRate;

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
		
		menu.setHeaderTitle(names[info.position]);
		menu.add(0, 142, 0, getString(R.string.delete_buget));
		menu.add(0, 143, 0, getString(R.string.edit_amount));

	};

	private void DeleteBuget(final int id) 
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
			DeleteBuget(ids[info.position]);
			return true;
		}
		else if (item.getItemId() == 143)
		{
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			EditBuget(info.position);
			return true;
		} 
		
		return false;
	}

	private void EditBuget(final int pos) {
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.enter_new_amount));

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		float cur_val = amounts[pos] / rates[pos];
		input.setText(String.valueOf(cur_val));
		//input.setText(String.format("%.2f", amounts[pos] ));
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String m_Text = input.getText().toString();
				if (m_Text.isEmpty() == false) {

					float amount= Float.parseFloat(m_Text);
					db.open();
					db.UpdateBugetAmount(ids[pos], amount * rates[pos]);
					db.close();
					LoadData();
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
