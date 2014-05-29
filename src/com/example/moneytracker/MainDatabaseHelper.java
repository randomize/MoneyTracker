package com.example.moneytracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract.DataUsageFeedback;
import android.util.Log;

public class MainDatabaseHelper extends SQLiteOpenHelper {


	private static final String DATABASE_NAME = "applicationdata";

	private static final int DATABASE_VERSION = 1;

	// DB creation SQL code string
	private static final String DATABASE_CREATE =

		// Old crap
		"create table todo (" +
             "_id integer primary key autoincrement," +
             "category text not null, " +
             "summary text not null, " + 
             "description text not null" + 
		");" +

        // Transaction categories table
        "create table transaction_category (" + 
             "_id integer primary key autoincrement, " + 
             "type integer, " +        // (0 - out) / (1 - in) -coming trans
             "name text not null " +   // Names 
        ");" + 

        // Currency table
        "create table currency (" + 
             "_id integer primary key autoincrement, " + 
             "name text not null, " + // 3 letter code (EUR/MLD)
             "rate real " + 
        ");" + 

        // Accounts table
        "create table accounts (" + 
             "_id integer primary key autoincrement, " + 
             "currency integer references currency(_id) on delete cascade, " +  // FK
             "name text not null," + 
             "type integer, " +  // 0 cash, 1 card 2 bank_acc
             "comment text" + 
        ");" + 

        // Members
        "create table members (" + 
             "_id integer primary key autoincrement, " + 
             "name text not null " + 
        ");" + 

   		// Transactions
   		"create table transactions (" + 
   		     "_id integer primary key autoincrement, " + 
   		     "amount real not null, " + 
   		     "category integer not null references transaction_category(_id) on delete cascade," + // Category id FK
   		     "date text," +
   		     "account integer not null references accounts(_id) on delete cascade," + // Account id FK
   		     "desc text," +
   		     "member integer not null references members(_id) on delete cascade" + // Member id FK
   		");" +
   		     
   		// Views
   		"create view accounts_outcome as " +
   		"select sum(amount) as amount, account from transactions "+
   		" where category in (select _id from transaction_category where type = 0) " + 
   		" group by account ;" +
   		"create view accounts_income as " +
   		"select sum(amount) as amount, account from transactions "+
   		" where category in (select _id from transaction_category where type = 1) " + 
   		" group by account ;"+
   		     
   		// Base values
   		" insert into currency (name, rate) " + 
   		" values ('MLD', 1.0); " + 
   		" insert into currency (name, rate) " + 
   		" values ('USD', 13.8); " + 
   		" insert into currency (name, rate) " + 
   		" values ('EUR', 18.8); " + 
   		" insert into members (name) " + 
   		" values ('Myself'); " + 
   		" insert into accounts (currency,name,type) " + 
   		" values (1,'MDL',0); " + 
   		" insert into transaction_category (type, name) " + 
   		" values (1,'Salary'); " + 
   		" insert into transaction_category (type, name) " + 
   		" values (1,'Interest'); " + 
   		" insert into transaction_category (type, name) " + 
   		" values (0,'Foodstuffs'); " + 
   		" insert into transaction_category (type, name) " + 
   		" values (0,'Services'); " + 
   		" insert into transaction_category (type, name) " + 
   		" values (0,'Car') ";

	private static final String DATABASE_FILL_WITH_CRAP = 
   		" insert into accounts (currency,name,type) " + 
   		" values (2,'VISA',1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (1010,1,'2012-01-19', 1, 'test_1', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (1020,1,'2012-02-19', 2, 'test_2', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (120,2,'2012-02-19', 1, 'test_3', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (200,2,'2012-01-19', 2, 'test_4', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (200,3,'2012-02-19', 1, 'test_1', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (110,3,'2012-02-29', 2, 'test_4', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (110,4,'2012-01-29', 1, 'test_4', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (120,4,'2012-01-19', 2, 'test_4', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (120,1,'2012-05-15', 1, 'test_1', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (122,1,'2012-01-15', 2, 'test_3', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (100,2,'2012-01-13', 2, null, 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (100,2,'2012-03-19', 2, null, 1) ";

	public MainDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	private void SuperExecSQL(SQLiteDatabase database, String s) {

		String[] queries = s.split(";");
		for(String query : queries){
			Log.i(MainDatabaseHelper.class.getName(), "Executing :" + query );
			database.execSQL(query);
		}
		
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.i(MainDatabaseHelper.class.getName(), "Creating DB" );
		SuperExecSQL(database, DATABASE_CREATE);
		SuperExecSQL(database, DATABASE_FILL_WITH_CRAP);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.e(MainDatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS todo");
		database.execSQL("DROP TABLE IF EXISTS trans");
		database.execSQL("DROP TABLE IF EXISTS trans_type");
		onCreate(database);
	}
}
