package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;


public class AccountsActivity extends Activity {

	// Easy db access
	private DatabaseFacade db = null;

	// groups of accounts to display in activity
	private ArrayList<AccountsGroup> accountGroups = new ArrayList<AccountsGroup>();

	// Expandable list root view and its adapter
	ExpandableListView mainList;
	AccountExpandableListAdapter main_adapter;

	protected Object mActionMode;
	public int selectedItem = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_accounts);

		db = new DatabaseFacade(this);

		mainList = (ExpandableListView) findViewById(R.id.accounts_list_view);
		main_adapter = new AccountExpandableListAdapter(this, accountGroups);


		mainList.setAdapter(main_adapter);

		mainList.setEmptyView(findViewById(R.id.accounts_list_empter));

	}

	@Override
	protected void onResume() {
		super.onResume();

		LoadData();

	}


	public void LoadData() {

		accountGroups.clear();

		ArrayList<AccountsGroup> gotGroups;

		db.open();
		gotGroups = db.GetGroupsWithTotalsAndAccounts();
		db.close();

		accountGroups.addAll(gotGroups);

		main_adapter.notifyDataSetChanged();
		mainList.invalidateViews();

		int count = main_adapter.getGroupCount();
		for (int position = 1; position <= count; position++){
			mainList.collapseGroup(position - 1);
			mainList.expandGroup(position - 1);
		}
		
		invalidateOptionsMenu();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.accounts_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (accountGroups.size() >= 2 || (accountGroups.size() == 1 && accountGroups.get(0).children.size() >= 2)) {
			menu.getItem(1).setVisible(true);
		} else {
			menu.getItem(1).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.accounts_menu_new:
			CreateNewAccount();
			return true;
		case R.id.accounts_exchange:
			CreateExchange();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void CreateExchange() {
		Intent intent = new Intent(this, ExchangeActivity.class);
		startActivity(intent);
	}

	private void CreateNewAccount() {
		Intent intent = new Intent(this, AccountAddActivity.class);
		startActivity(intent);
	}

	private void EditAccount(int accountID) {
		Log.w("ggg", "Editing " + accountID);
		Intent intent = new Intent(this, AccountAddActivity.class);
		intent.putExtra("EditIndex", (int)accountID);
		//myIntent.putExtra("LongValue", (int)-80142777);
		startActivity(intent);
	}

	private void DeleteAccount(final int accountID) {
		Log.w("ggg", "deleting " + accountID);
		/*Intent intent = new Intent(this, AccountAddActivity.class);
		startActivity(intent);*/
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					
					db.open();
					db.DeleteAccount(accountID);
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

	public void PopupActionsMenu(final int accountID) {

		/*if (accountID == 1)  // never edit or delete default one
			return;*/

		String[] cur_names = getResources().getStringArray(R.array.account_actions);

		// display chooser
		new AlertDialog.Builder(this)
		.setCancelable(true)
		.setItems(cur_names,
			new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				if (i == 0) {
					DeleteAccount(accountID);
				} else if (i == 1) {
					EditAccount(accountID);
				}
			}
		})
		.show();
	}

}
