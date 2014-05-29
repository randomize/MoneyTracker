package com.example.moneytracker;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

// Each activity will have such facade to DB
public class DatabaseFacade {


	// Tables in DB 
	private static final String DATABASE_TABLE_TRANS_CATEGORY = "transaction_category";
	private static final String DATABASE_TABLE_CURRENCY = "currency";
	private static final String DATABASE_TABLE_ACCOUNTS = "accounts";
	private static final String DATABASE_TABLE_MEMBERS = "members";
	private static final String DATABASE_TABLE_TRANSACTIONS = "transactions";
	private static final String DATABASE_VIEW_INCOME_SUMS = "accounts_income";
	private static final String DATABASE_VIEW_OUTCOME_SUMS = "accounts_outcome";


	private Context context;
	private SQLiteDatabase database;
	private MainDatabaseHelper dbHelper;

	public DatabaseFacade(Context context) {
		this.context = context;
	}

	public void open() throws SQLException {
		dbHelper = new MainDatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
	}


	public void close() {
		dbHelper.close();
	}

	////////////////////////////////////////////////////////////////////////


	public ArrayList<Currency> GetCurrencyList() {

		ArrayList<Currency> result = new ArrayList<Currency>(10);

		Cursor c = database.query(DATABASE_TABLE_CURRENCY, null, null, null, null, null, null);

		if (c.moveToFirst()) {

			int idColIndex = c.getColumnIndex("_id");
			int nameColIndex = c.getColumnIndex("name");
			int rateColIndex = c.getColumnIndex("rate");

			do {
				Currency s = new Currency();
				s.id = c.getInt(idColIndex);
				s.name = c.getString(nameColIndex);
				s.rate = c.getFloat(rateColIndex);
				result.add(s);
			} while (c.moveToNext());

		}

		c.close();
		return result;
	}

	public void NewCurrency(String name, float rate) {
	}
	
	public String GetCurrencyByID(int id) {

		String result = "";
		String[] columns = new String[]{"name"};
		String[] id_value = new String[]{String.valueOf(id)};
		Cursor c = database.query(DATABASE_TABLE_CURRENCY, columns, "_id=?", id_value, null, null, null);
		if (c.moveToFirst()) {
			int nameColIndex = c.getColumnIndex("name");
			result = c.getString(nameColIndex);
		}
		c.close();
		return result;
	}

	public TransactionCategoryGroup GetIncome() {

		TransactionCategoryGroup result = new TransactionCategoryGroup("Income", TransactionCategoryGroup.GroupType.INCOME);
		/*cards.children = 
		groups.add(cards);
		groups.add(acc);*/

		return result;

	}

	public TransactionCategoryGroup GetOutcome() {

		TransactionCategoryGroup result = new TransactionCategoryGroup("Outcome", TransactionCategoryGroup.GroupType.OUTCOME);

		/*cards.children = 
		groups.add(cards);
		groups.add(acc);*/

		return result;

	}

	public float GetTotalIncomeOn(int accountId) {
		
		float result = 0;
		String[] columns = new String[]{"amount"};
		String[] id_value = new String[]{String.valueOf(accountId)};
		Cursor c = database.query(DATABASE_VIEW_INCOME_SUMS, columns, "account=?", id_value, null, null, null);
		if (c.moveToFirst()) {
			int amountIndex = c.getColumnIndex("amount");
			result = c.getFloat(amountIndex);
		}
		c.close();
		
		return result;
		
	}
	public float GetTotalOutcomeOn(int accountId) {
		
		float result = 0;
		String[] columns = new String[]{"amount"};
		String[] id_value = new String[]{String.valueOf(accountId)};
		Cursor c = database.query(DATABASE_VIEW_OUTCOME_SUMS, columns, "account=?", id_value, null, null, null);
		if (c.moveToFirst()) {
			int amountIndex = c.getColumnIndex("amount");
			result = c.getFloat(amountIndex);
		}
		c.close();
		
		return 0;
		
	}
	
	public float GetCurrentBalance(int accountId) {
		return GetTotalIncomeOn(accountId) - GetTotalOutcomeOn(accountId);
	}

	public ArrayList<AccountsGroup> GetGroupsWithTotalsAndAccounts() {
		ArrayList<AccountsGroup> result = new ArrayList<AccountsGroup>();

		int[] types_lables = new int[] {
				R.string.type_cash,
				R.string.type_cards,
				R.string.type_bank_accounts
		} ;
		AccountsGroup.GroupType[] types_ints = new AccountsGroup.GroupType[] {
				AccountsGroup.GroupType.CASH,
				AccountsGroup.GroupType.CARD,
				AccountsGroup.GroupType.BANK_ACCOUNT
		} ;
		int[] types_drawables = new int[] {
				R.drawable.cash,
				R.drawable.cards,
				R.drawable.accounts
		} ;

		for (int i = 0; i < types_lables.length; i++)  {

			String[] selectionArgs = new String[] { String.valueOf(i) };
			Cursor c = database.query(DATABASE_TABLE_ACCOUNTS, null, "type=?", selectionArgs  , null, null, null);

			if (c.moveToFirst()) {

				AccountsGroup group = new AccountsGroup(context.getString(types_lables[i]), 0.0f, types_ints[i] );
				group.icon = types_drawables[i];

				int idColIndex = c.getColumnIndex("_id");
				int nameColIndex = c.getColumnIndex("name");
				int currencyColIndex = c.getColumnIndex("currency");
				//int typeColIndex = c.getColumnIndex("type");
				int commentColIndex = c.getColumnIndex("comment");

				do {
					Account s = new Account();
					s.id = c.getInt(idColIndex);
					s.name = c.getString(nameColIndex);
					s.currencyId = c.getInt(currencyColIndex);
					s.currencyName = GetCurrencyByID(s.currencyId);
					s.typeName = group.name;
					s.typeId = i;

					if (c.isNull(commentColIndex) == false) {
						s.comment = c.getString(commentColIndex);
					}
					
					s.totalAmount = GetCurrentBalance(s.id);
					
					group.AddChild(s);

					
				} while (c.moveToNext());
				
				result.add(group);

			}

			c.close();

		}

		return result;

	}

	public void AddNewAccount(Account newman) {
		ContentValues cv = new ContentValues();
		cv.put("currency", newman.currencyId);
		cv.put("name", newman.name);
		cv.put("type", newman.typeId);
		if (newman.comment != null) {
			cv.put("comment", newman.comment);
		} else {
			cv.putNull("comment");
		}
		
		database.insert(DATABASE_TABLE_ACCOUNTS, null, cv);
	}
	
	public void DeleteAccount(int id) {
		database.delete(DATABASE_TABLE_ACCOUNTS, "_id = ?", new String[] { String.valueOf(id)});
	}

	public void UpdateAccount(Account replacer) {
		
		ContentValues cv = new ContentValues();
		cv.put("currency", replacer.currencyId);
		cv.put("name", replacer.name);
		cv.put("type", replacer.typeId);
		if (replacer.comment != null) {
			cv.put("comment", replacer.comment);
		} else {
			cv.putNull("comment");
		}
		
		database.update(DATABASE_TABLE_ACCOUNTS, cv, "_id = ?", new String[] {String.valueOf(replacer.id)});
		
	}

}
