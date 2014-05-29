package com.example.moneytracker;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TransactionListActivity extends ListActivity {
	
	private DatabaseFacade db;
	String[] list;
	String[] desc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		DatabaseFacade db = new DatabaseFacade(this);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey("CategoryID")) {
			int categoryId = extras.getInt("CategoryID");
			setTitle(extras.getString("Title"));
			
			
			db.open();
			ArrayList<Transaction> trans = db.GetTransactionsInCategory(categoryId);
			db.close();
			
			list = new String[trans.size()];
			desc = new String[trans.size()];
			
			for (int i = 0; i < trans.size(); i++) {
				Transaction t = trans.get(i);
				list[i] = " [" +t.date + "] : " + String.format("%.2f", t.amount / t.cur_rate) + " "
				         + t.cur_name + " : " + Account.GetLocalized(this, t.account);
				desc[i] = (t.desc == null ? " " : t.desc ) + "\n" + getString(R.string.member) + " : " + Member.GetLocalized(this, t.member);
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, list);
			getListView().setAdapter(adapter);
			
			
		} else {
			finish();
		}

	}
  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
	  
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

			dlgAlert.setMessage(desc[position]);
			dlgAlert.setTitle(getTitle());
			dlgAlert.setPositiveButton(getString(R.string.ok), null);
			dlgAlert.setCancelable(false);
			dlgAlert.create().show();
  }
}
