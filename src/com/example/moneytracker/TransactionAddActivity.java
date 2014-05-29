package com.example.moneytracker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

public class TransactionAddActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trans_add);

	}

	// On button cancel
	public void CancelTransAdd( View button ) {
		finish();
	}

	public void CommitTransAdd( View button ) {

		/*
		final EditText nameField = (EditText) findViewById(R.id.EditTextAccountName);
		String name = nameField.getText().toString();
		if (name.isEmpty())
		{
			nameField.setBackgroundColor(getResources().getColor(R.color.errorous));
		}

		final EditText commentField = (EditText) findViewById(R.id.EditTextAccountComment);
		String comment = commentField.getText().toString();

		final Spinner typeSpinner = (Spinner) findViewById(R.id.SpinnerAccountType);
		String accountType = typeSpinner.getSelectedItem().toString();
		int indx_type = typeSpinner.getSelectedItemPosition() + 1; 

		final Spinner currSpinner = (Spinner) findViewById(R.id.SpinnerCurrency);
		String currecy = currSpinner.getSelectedItem().toString();
		int indx_cur = currSpinner.getSelectedItemPosition() + 1; 
		
		Log.w("ggg", "Adfing " + indx_type + " (" + accountType + ") + name="+name+ " comm="+comment);*/
	}

}
