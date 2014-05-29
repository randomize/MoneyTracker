package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
		/*mainList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				if (mActionMode != null) {
					return false;
				}
				selectedItem = position;

				// start the CAB using the ActionMode.Callback defined above
				mActionMode = AccountsActivity.this.startActionMode(mActionModeCallback);
				view.setSelected(true);
				return true;
				//return false;
			}
		});*/


		registerForContextMenu(mainList);


	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		LoadData();
		
		int count = main_adapter.getGroupCount();
		for (int position = 1; position <= count; position++)
			mainList.expandGroup(position - 1);
		
	}

	@ Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)             
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int child = ExpandableListView.getPackedPositionChild(info.packedPosition);

		// Only create a context menu for child items
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) 
		{
			// Array created earlier when we built the expandable list
			String page = accountGroups.get(group).children.get(child).name;
			int id = accountGroups.get(group).children.get(child).id;

			menu.setHeaderTitle(page);
			menu.add(1, id, 0, getString(R.string.account_delete));
			menu.add(0, id, 0, getString(R.string.account_edit));
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();

		if(item.getGroupId()==0)
		{
			DeleteAccount(item.getItemId());
			
		} else 
		if(item.getGroupId()==1)
		{
			EditAccount(item.getItemId());
		}
		return super.onContextItemSelected(item);
	}
	
	/*
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// called when the action mode is created; startActionMode() was called
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			// assumes that you have "contexual.xml" menu resources
			inflater.inflate(R.menu.accounts_contextmenu, menu);
			return true;
		}

		// the following method is called each time 
		// the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// called when the user selects a contextual menu item
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.accounts_context_delete:
				show();
				// the Action was executed, close the CAB
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		// called when the user exits the action mode
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			selectedItem = -1;
		}

	};

	private void show() {
		Toast.makeText(AccountsActivity.this, String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
	}*/


	public void LoadData() {

		accountGroups.clear();
		
		ArrayList<AccountsGroup> gotGroups;

		db.open();
		gotGroups = db.GetGroupsWithTotalsAndAccounts();
		db.close();
		
		accountGroups.addAll(gotGroups);
		
		main_adapter.notifyDataSetChanged();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.accounts_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.accounts_menu_new:
			CreateNewAccount();
			return true;
		/*case R.id.action_exit:
			//Application();
			return true;*/
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void CreateNewAccount() {
		Intent intent = new Intent(this, AccountAddActivity.class);
		startActivity(intent);
	}

	private void EditAccount(int accountID) {
		Log.w("ggg", "Editing " + accountID);
		/*Intent intent = new Intent(this, AccountAddActivity.class);
		startActivity(intent);*/
	}

	private void DeleteAccount(int accountID) {
		Log.w("ggg", "Editing " + accountID);
		/*Intent intent = new Intent(this, AccountAddActivity.class);
		startActivity(intent);*/
	}

	/*
	public void PopupActionsMenu(final int accountID) {


	
		String[] cur_names = getResources().getStringArray(R.array.account_actions);

		// display chooser
		new AlertDialog.Builder(this)
		.setTitle(R.string.currency)
		.setCancelable(false)
		.setItems(cur_names,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				EditAccount(accountID);
			}
		})
		.show();
	}*/

}
