package com.example.moneytracker;

import java.text.DateFormat;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TransactionListActivity extends ListActivity {

	private DatabaseFacade db;
	private String[] list;
	private String[] desc;
	private int[] item_ids;
	private int categoryId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		db = new DatabaseFacade(this);

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("CategoryID")) {
			categoryId = extras.getInt("CategoryID");
			setTitle(extras.getString("Title"));
			LoadData();

		} else {
			finish();
		}
		
		registerForContextMenu(getListView());

	}
	
	private void LoadData() {
		
			db.open();
			ArrayList<Transaction> trans = db.GetTransactionsInCategory(categoryId);
			db.close();
			
			if (trans.size() == 0) {
				finish();
				return;
			}

			list = new String[trans.size()];
			desc = new String[trans.size()];
			item_ids = new int[trans.size()];

			for (int i = 0; i < trans.size(); i++) {
				Transaction t = trans.get(i);
				String date = DateFormat.getDateInstance().format(t.date);
				list[i] = " [" +date + "] : " + String.format("%.2f", t.amount / t.cur_rate) + " "
						+ t.cur_name + "\n " + getString(R.string.account) + " : " + Account.GetLocalized(this, t.account);
				desc[i] = getString(R.string.desc) + " :\n"+ (t.desc == null ? getString(R.string.nodesc) : t.desc ) + "\n" + getString(R.string.member) + " : " + Member.GetLocalized(this, t.member);
				item_ids[i] = t.id;
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, list);
			getListView().setAdapter(adapter);
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

		dlgAlert.setMessage(desc[position]);
		dlgAlert.setTitle(list[position]);
		dlgAlert.setPositiveButton(getString(R.string.ok), null);
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			return;
		}
		int pos = info.position;
		//long id = getListAdapter().getItemId(info.position);
		
		menu.setHeaderTitle(list[(int) pos]);   
		menu.add(0, 142, 0, getString(R.string.trans_delete));
	};

	private void DeleteTransaction(final int id) 
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					
					Log.e("ggg", "ggg deleting " + id);
					
					db.open();
					db.DeleteTransaction(id);
					db.close();
					//Yes button clicked
					
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
			/*long id = this.listView.getItemIdAtPosition(info.position);
			Log.d(TAG, "Item ID at POSITION:"+id);*/
			DeleteTransaction(item_ids[info.position]);
		}
		else
		{
			return false;
		}
		return true;
	}
}
