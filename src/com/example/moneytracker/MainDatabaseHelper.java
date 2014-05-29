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
   		     "category integer not null references transaction_category(_id) on delete cascade," + // Category id FK
   		     "date text not null," +
   		     "account integer not null," + // Account id FK
   		     "desc text," +
   		     "member integer not null" + // Member id FK
   		");" +
   		     
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


	public MainDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.i(MainDatabaseHelper.class.getName(), "Creating DB" );
		//database.execSQL(DATABASE_CREATE);
		String[] queries = DATABASE_CREATE.split(";");
		for(String query : queries){
			Log.i(MainDatabaseHelper.class.getName(), "Executing :" + query );
			database.execSQL(query);
		}
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
