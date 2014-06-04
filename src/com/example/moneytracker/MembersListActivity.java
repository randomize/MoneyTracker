package com.example.moneytracker;

import java.text.DateFormat;
import java.util.ArrayList;

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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class MembersListActivity extends ListActivity {

	private DatabaseFacade db;

	private String[] list;
	private int[] item_ids;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		db = new DatabaseFacade(this);

		registerForContextMenu(getListView());

	}
	
	private void LoadData() {
		
			db.open();
			ArrayList<Member> mems = db.GetMembers();
			db.close();
			
			if (mems.size() == 0) {
				finish();
				return;
			}

			list = new String[mems.size()];
			item_ids = new int[mems.size()];

			for (int i = 0; i < mems.size(); i++) {
				Member t = mems.get(i);
				list[i] = t.name;
				item_ids[i] = t.id;
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, list);
			getListView().setAdapter(adapter);
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		Intent intent = new Intent(this, Grapher.class);
		startActivity(intent);

		/*
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		
		db.open();
		//db.geMe
		String mess = "";
		db.close();

		dlgAlert.setMessage(mess);
		dlgAlert.setTitle(list[position]);
		dlgAlert.setPositiveButton(getString(R.string.ok), null);
		dlgAlert.setCancelable(false);
		dlgAlert.create().show();*/
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
		menu.add(0, 142, 0, getString(R.string.member_delete));
	};

	private void DeleteMember(final int id) 
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					
					db.open();
					db.DeleteMember(id);
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
			DeleteMember(item_ids[info.position]);
		}
		else
		{
			return false;
		}
		return true;
	}

	private void ShowNewMemberDialog() {
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.member_new));

		// Set up the input
		final EditText input = new EditText(this);
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
					db.AddNewMember(newman);
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
}
