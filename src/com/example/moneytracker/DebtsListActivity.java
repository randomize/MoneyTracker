package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListView;

public class DebtsListActivity extends Activity {
	
	// Easy db access
	private DatabaseFacade db = null;

	// groups of accounts to display in activity
	private ArrayList<AccountsGroup> accountGroups = new ArrayList<AccountsGroup>();

	// Expandable list root view and its adapter
	ExpandableListView mainList;
	AccountExpandableListAdapter main_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//
	}

}
