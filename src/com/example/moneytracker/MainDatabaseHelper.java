package com.example.moneytracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
             "type integer, " +        // (0 - out) / (1 - in) -coming trans ;
             "name text not null " +   // Names 
        ");" + 

        // Currency table
        "create table currency (" + 
             "_id integer primary key autoincrement, " + 
             "name text not null, " + // 3 letter code (EUR/MLD)
             "rate real, " + 
             "active integer not null " + 
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
   		     "date integer not null," +
   		     "account integer not null references accounts(_id) on delete cascade," + // Account id FK
   		     "desc text," +
   		     "member integer not null references members(_id) on delete cascade" + // Member id FK
   		");" +

   		// Debts
   		"create table debts (" + 
   		     "_id integer primary key autoincrement, " + 
   		     "desc text not null," +
   		     "type integer not null," +      // 0 - outcome and 1 - income
   		     "amount_start real not null, " + 
   		     "amount_end real not null, " + 
   		     "category_start integer not null references transaction_category(_id) on delete cascade," + // Category id FK
   		     "category_end integer not null references transaction_category(_id) on delete cascade," + // Category id FK
   		     "date_start integer not null," +
   		     "date_end integer not null," +
   		     "account integer not null references accounts(_id) on delete cascade," + // Account id FK
   		     "member integer not null references members(_id) on delete cascade" + // Member id FK
   		");" +
   		     
   		// Bugets
   		"create table bugets (" + 
   		     "_id integer primary key autoincrement, " + 
   		     "name text not null," +
   		     "type integer not null," +      // 0 - week and 1 - month
   		     "amount real not null, " + 
   		     "category integer not null references transaction_category(_id) on delete cascade," + // Category id FK only outcome
   		     "member integer not null references members(_id) on delete cascade, " + // Member id FK
   		     "currency integer not null references currency(_id) on delete cascade," + // Currency id FK
   		     "desc text" +
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
   		
        "create view trans_summary as select sum(amount) as amount, type, category, name from transactions " +
        "inner join transaction_category on transactions.category = transaction_category._id " +
        "group by type, category " +
        "order by type ; " +
        
        "create view trans_details as select  transactions._id as _id, amount, category, date, account, desc, accounts.name as account_name, " +
        "accounts.type as accounts_type,  transaction_category.type as cat_type,  transaction_category.name as cat_name, " +
        "member, members.name as member_name, currency as cur,  currency.name as cur_name, rate as cur_rate " +
        "from transactions   " +
        "inner join accounts on transactions.account = accounts._id " +
        "inner join transaction_category on transactions.category = transaction_category._id " +
        "inner join members on transactions.member = members._id " +
        "inner join currency on accounts.currency = currency._id " +
        "order by date ; " +
   		     
   		// Base values
   		" insert into currency (name, rate, active) " + 
   		" values ('MLD', 1.0, 1); " + 
   		" insert into currency (name, rate, active) " + 
   		" values ('USD', 13.8, 1); " + 
   		" insert into currency (name, rate, active) " + 
   		" values ('EUR', 18.8, 1); " + 
   		" insert into currency (name, rate, active) " + 
   		" values ('XXX', 48.8, 0); " + 
   		" insert into members (name) " + 
   		" values ('Myself'); " + 
   		" insert into accounts (currency,name,type) " + 
   		" values (1,'Cash',0); " + 

   		" insert into transaction_category (_id, type, name) " + // Mandatory category for incoming exchange - id = 1
   		" values (1,1,'Exchange in'); " + 
   		" insert into transaction_category (_id, type, name) " +  // Mandatory category for outgoing exchange - id = 2
   		" values (2,0,'Exchange out'); " + 

   		" insert into transaction_category (_id, type, name) " +  // Mandatory category for incoming debt pays - id = 3
   		" values (3,1,'Debts'); " + 
   		" insert into transaction_category (_id, type, name) " +  // Mandatory category for outgoing debt pays - id = 4
   		" values (4,0,'Debts'); " + 

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
   		" insert into accounts (currency,name,type) " + 
   		" values (3,'MASTERCARD',1); " + 
   		" insert into debts (desc, type, amount_start, amount_end, category_start, category_end, date_start, date_end, account, member) " + 
   		" values ('vasea', 1, 10, 20, 4, 3, 1401388315000, 1401388315064, 2, 1 ); " + 
   		" insert into debts (desc, type, amount_start, amount_end, category_start, category_end, date_start, date_end, account, member) " + 
   		" values ('kolea', 0, 30, 35, 3, 4, 1401388315064, 1401561050207, 2, 1 ); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (10,4,310012233, 2, 'given vasea', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (30,3,3112233, 2, 'owe to kolea', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (1010,5,3112233, 1, 'test_1', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (1020,5,4132233, 2, 'test_2', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (120,6,5122233, 1, 'test_3', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (200,6,6112223, 2, 'test_4', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (200,7,7112233, 1, 'test_1', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (110,7,8162233, 2, 'test_4', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (110,8,9132233, 1, 'test_4', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (120,8,10112333, 2, 'test_4', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (120,5,11112233, 1, 'test_1', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (122,5,12112233, 2, 'test_3', 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (100,6,13112223, 2, null, 1); " + 
   		" insert into transactions (amount, category, date, account, desc, member) " + 
   		" values (100,6,14112223, 2, null, 1) ";

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
	public void onOpen(SQLiteDatabase database) {
		super.onOpen(database);
		database.execSQL("PRAGMA foreign_keys=ON");
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.i(MainDatabaseHelper.class.getName(), "Creating DB" );
		database.execSQL("PRAGMA foreign_keys=ON");
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
