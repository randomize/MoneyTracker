package com.example.moneytracker;

import java.util.ArrayList;
import java.util.Calendar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

// Each activity will have such facade to DB
public class DatabaseFacade {


	// Tables in DB 
	private static final String DATABASE_TABLE_TRANS_CATEGORY = "transaction_category";
	private static final String DATABASE_TABLE_CURRENCY = "currency";
	private static final String DATABASE_TABLE_ACCOUNTS = "accounts";
	private static final String DATABASE_TABLE_MEMBERS = "members";
	private static final String DATABASE_TABLE_TRANSACTIONS = "transactions";
	private static final String DATABASE_TABLE_DEBTS = "debts";
	private static final String DATABASE_TABLE_BUGET = "bugets";
	private static final String DATABASE_VIEW_INCOME_SUMS = "accounts_income";
	private static final String DATABASE_VIEW_OUTCOME_SUMS = "accounts_outcome";
	private static final String DATABASE_VIEW_TRANS_SUMMARY = "trans_summary";
	private static final String DATABASE_VIEW_TRANS_DETAILS = "trans_details";


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
		return GetCurrencyList(true);

	}

	public ArrayList<Currency> GetCurrencyList(boolean onlyActive) {

		ArrayList<Currency> result = new ArrayList<Currency>(10);

		Cursor c = database.query(DATABASE_TABLE_CURRENCY, null, onlyActive ? "active = 1" : null, null, null, null, null);

		if (c.moveToFirst()) {

			int idColIndex = c.getColumnIndex("_id");
			int nameColIndex = c.getColumnIndex("name");
			int rateColIndex = c.getColumnIndex("rate");
			int activeColIndex = c.getColumnIndex("active");

			do {
				Currency s = new Currency();
				s.id = c.getInt(idColIndex);
				s.name = c.getString(nameColIndex);
				s.rate = c.getFloat(rateColIndex);
				s.isActive = c.getInt(activeColIndex) == 1;
				result.add(s);
			} while (c.moveToNext());

		}

		c.close();
		return result;
	}
	
	public void UpdateCurrency(Currency replacer) {
		
		ContentValues cv = new ContentValues();
		cv.put("active", replacer.isActive ? 1 : 0);
		cv.put("name", replacer.name);
		cv.put("rate", replacer.rate);
		database.update(DATABASE_TABLE_CURRENCY, cv, "_id = ?", new String[] {String.valueOf(replacer.id)});
	}

	public void NewCurrency(String name, float rate) {

		ContentValues cv = new ContentValues();
		cv.put("name", name);
		cv.put("rate", rate);
		database.insert(DATABASE_TABLE_CURRENCY, null, cv);
	}
	
	public Currency GetCurrencyByID(int id) {

		Currency result = new Currency();
		result.id  = id;
		String[] columns = new String[]{"name", "rate"};
		String[] id_value = new String[]{String.valueOf(id)};
		Cursor c = database.query(DATABASE_TABLE_CURRENCY, columns, "_id=?", id_value, null, null, null);
		if (c.moveToFirst()) {
			/*int nameColIndex = c.getColumnIndex("name");
			int nameColIndex = c.getColumnIndex("rate");*/
			result.name = c.getString(0);
			result.rate = c.getFloat(1);
		} else {
			result = null;
		}
		c.close();
		return result;
	}

	// Only used ones are returned(empty are dismissed)
	public ArrayList<TransactionCategoryGroup> GetIncomeAndOutcome() {
		return GetIncomeAndOutcome(false, 0, 0);
	}

	public ArrayList<TransactionCategoryGroup> GetIncomeAndOutcome(boolean slice, long f, long t) {
		
		ArrayList<TransactionCategoryGroup> result = new ArrayList<TransactionCategoryGroup>(2);

		TransactionCategoryGroup in = new TransactionCategoryGroup("Income", TransactionCategoryGroup.GroupType.INCOME);
		TransactionCategoryGroup out = new TransactionCategoryGroup("Outcome", TransactionCategoryGroup.GroupType.OUTCOME);

		Cursor c;
		
		if (slice) {
			//String[] params = new String[] { String.valueOf(f), String.valueOf(t)};

			String raw = "select sum(amount) as amount, type, category, name from transactions " +
					"inner join transaction_category on transactions.category = transaction_category._id " +
					"where transactions.date between " + f + " and " + t + " " +
					"group by type, category " +
					"order by type ; ";
			c = database.rawQuery(raw, null);

			//c = database.query(DATABASE_VIEW_TRANS_SUMMARY, null, "date between ? and ?", null , null, null, null);
		} else {
			c = database.query(DATABASE_VIEW_TRANS_SUMMARY, null, null, null , null, null, null);
		}

		if (c.moveToFirst()) {

			do {
				if (c.getInt(2) <= 2) // if transaction cat is exchange
				{
					continue; // skip to next
				}
				TransactionCategory s = new TransactionCategory();
				s.amount = c.getFloat(0);
				s.type = c.getInt(1);
				s.id = c.getInt(2);
				s.name = c.getString(3);
				
				if (s.type == 0) {
					out.AddChild(s);
					s.parent = out;
				} else {
					in.AddChild(s);
					s.parent = in;
				}

			} while (c.moveToNext());

		}

		c.close();
		
		if (in.isEmpty() == false) result.add(in);
		if (out.isEmpty() == false) result.add(out);

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
		
		return result;
		
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
					Currency cur = GetCurrencyByID(s.currencyId);
					s.currencyName = cur.name;
					s.currencyRate = cur.rate;
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

	// Not typeName and cat name
	public ArrayList<Account> GetAccounts() {

		ArrayList<Account> result = new ArrayList<Account>(10);

		Cursor c = database.query(DATABASE_TABLE_ACCOUNTS, null, null, null, null, null, null);

		if (c.moveToFirst()) {

			int idColIndex = c.getColumnIndex("_id");
			int nameColIndex = c.getColumnIndex("name");
			int currencyColIndex = c.getColumnIndex("currency");
			int typeColIndex = c.getColumnIndex("type");
			int commentColIndex = c.getColumnIndex("comment");

			do {
				Account s = new Account();
				s.id = c.getInt(idColIndex);
				s.name = c.getString(nameColIndex);
				s.currencyId = c.getInt(currencyColIndex);
				Currency cur = GetCurrencyByID(s.currencyId);
				s.currencyName = cur.name;
				s.currencyRate = cur.rate;
				s.typeId = c.getInt(typeColIndex);

				if (c.isNull(commentColIndex) == false) {
					s.comment = c.getString(commentColIndex);
				}

				result.add(s);
			} while (c.moveToNext());

		}

		c.close();
		return result;
	}
	
	public Account GetAcccountByID(int id) {

		Account s = new Account();
		s.id = id;

		Cursor c = database.query(DATABASE_TABLE_ACCOUNTS, null, "_id = ?", new String[] {String.valueOf(id)}, null, null, null);

		if (c.moveToFirst()) {

			int nameColIndex = c.getColumnIndex("name");
			int currencyColIndex = c.getColumnIndex("currency");
			int typeColIndex = c.getColumnIndex("type");
			int commentColIndex = c.getColumnIndex("comment");

			s.name = c.getString(nameColIndex);
			s.currencyId = c.getInt(currencyColIndex);
			Currency cur = GetCurrencyByID(s.currencyId);
			s.currencyName = cur.name;
			s.currencyRate = cur.rate;
			s.typeId = c.getInt(typeColIndex);

			if (c.isNull(commentColIndex) == false) {
				s.comment = c.getString(commentColIndex);
			}

		}

		c.close();
		return s;
		
	}

	public ArrayList<TransactionCategory> GetCategories() {
		ArrayList<TransactionCategory> result = new ArrayList<TransactionCategory>();
		
		Cursor c = database.query(DATABASE_TABLE_TRANS_CATEGORY, null, null, null, null, null, null);

		if (c.moveToFirst()) {

			int idColIndex = c.getColumnIndex("_id");
			int nameColIndex = c.getColumnIndex("name");
			int typeColIndex = c.getColumnIndex("type");

			do {
				TransactionCategory s = new TransactionCategory();
				s.id = c.getInt(idColIndex);
				s.name = c.getString(nameColIndex);
				s.type = c.getInt(typeColIndex);

				result.add(s);
			} while (c.moveToNext());

		}

		c.close();
		return result;
		
	}

	public ArrayList<Member> GetMembers() {

		ArrayList<Member> result = new ArrayList<Member>();
		Cursor c = database.query(DATABASE_TABLE_MEMBERS, null, null, null, null, null, null);

		if (c.moveToFirst()) {
			int idColIndex = c.getColumnIndex("_id");
			int nameColIndex = c.getColumnIndex("name");
			do {
				Member s = new Member();
				s.id = c.getInt(idColIndex);
				s.name = c.getString(nameColIndex);
				result.add(s);
			} while (c.moveToNext());
		}

		c.close();
		return result;
		
	}

	public int AddNewTransaction(Transaction newman) {

		ContentValues cv = new ContentValues();

		cv.put("amount", newman.amount);
		cv.put("category", newman.categoryID);
		cv.put("date", newman.date);
		cv.put("account", newman.accountID);
		cv.put("member", newman.memberID);

		if (newman.desc != null) {
			cv.put("desc", newman.desc);
		} else {
			cv.putNull("desc");
		}
		
		return (int) database.insert(DATABASE_TABLE_TRANSACTIONS, null, cv);
	}

	public ArrayList<Transaction> GetTransactionsInCategory(int categID) {

		ArrayList<Transaction> result = new ArrayList<Transaction>();
		
		Cursor c = database.query(DATABASE_VIEW_TRANS_DETAILS, null, "category = ?", new String[] { String.valueOf(categID)}, null, null, null);

		if (c.moveToFirst()) {

			int idColIndex = c.getColumnIndex("_id");
			int amColIndex = c.getColumnIndex("amount");
			int dateColIndex = c.getColumnIndex("date");
			int accIdColIndex = c.getColumnIndex("account");
			int descColIndex = c.getColumnIndex("desc");
			//int currIdColIndex = c.getColumnIndex("currency");
			int accNameColIndex = c.getColumnIndex("account_name");
			int accTypeColIndex = c.getColumnIndex("accounts_type");
			int catTypeColIndex = c.getColumnIndex("cat_type");
			int memNemeColIndex = c.getColumnIndex("member_name");
			int curNameColIndex = c.getColumnIndex("cur_name");
			int curRateColIndex = c.getColumnIndex("cur_rate");

			do {
				Transaction s = new Transaction();
				s.categoryID = categID;

				s.id = c.getInt(idColIndex);
				s.amount = c.getFloat(amColIndex);
				s.date = c.getLong(dateColIndex);
				s.accountID = c.getInt(accIdColIndex);
				s.desc = c.isNull(descColIndex) == false ? c.getString(descColIndex) : null;
				s.account = c.getString(accNameColIndex);
				s.accType = c.getInt(accTypeColIndex);
				s.type = c.getInt(catTypeColIndex);
				s.cur_name = c.getString(curNameColIndex);
				s.cur_rate = c.getFloat(curRateColIndex);
				s.member = c.getString(memNemeColIndex);
				result.add(s);
			} while (c.moveToNext());

		}

		c.close();
		return result;
		
	}

	public void DeleteTransaction(int id) {
		database.delete(DATABASE_TABLE_TRANSACTIONS, "_id = ?", new String[] { String.valueOf(id)});
	}

	public int AddNewCategory(TransactionCategory newman) {
		ContentValues cv = new ContentValues();
		cv.put("name", newman.name);
		cv.put("type", newman.type);
		return (int) database.insert(DATABASE_TABLE_TRANS_CATEGORY, null, cv);
	}

	public int AddNewMember(Member newman) {
		ContentValues cv = new ContentValues();
		cv.put("name", newman.name);
		return (int) database.insert(DATABASE_TABLE_MEMBERS, null, cv);
	}
	
	public ArrayList<DebtsGroup> GetDebts() {
		// set names for grups
		ArrayList<DebtsGroup> result = new ArrayList<DebtsGroup>(2);

		DebtsGroup in = new DebtsGroup("TheyOwe", 1);
		DebtsGroup out = new DebtsGroup("IOwe", 0);

		Cursor c = database.query(DATABASE_TABLE_DEBTS, null, null, null , null, null, null);

		if (c.moveToFirst()) {

			do {
				Debt s = new Debt();
				s.id = c.getInt(0);
				s.desc = c.getString(1);
				s.type = c.getInt(2);
				s.amount_start = c.getFloat(3);
				s.amount_end = c.getFloat(4);
				s.category_start = c.getInt(5);
				s.category_end = c.getInt(6);
				s.date_start = c.getLong(7);
				s.date_end = c.getLong(8);
				s.accountID = c.getInt(9);
				s.memberID = c.getInt(10);
				
				Account ac = GetAcccountByID(s.accountID);

				s.currencyName = ac.currencyName;
				s.currencyRate = ac.currencyRate;
				s.currencyID = ac.currencyId;

				if (s.type == 0) {
					out.AddChild(s);
					s.parent = out;
				} else {
					in.AddChild(s);
					s.parent = in;
				}

			} while (c.moveToNext());

		}

		c.close();

		if (in.isEmpty() == false) result.add(in);
		if (out.isEmpty() == false) result.add(out);

		return result;
	}

	public Debt GetDebt(int id) {


		Cursor c = database.query(DATABASE_TABLE_DEBTS, null, "_id = ?", new String[] { String.valueOf(id)} , null, null, null);

		if (c.moveToFirst()) {

				Debt s = new Debt();
				s.id = c.getInt(0);
				s.desc = c.getString(1);
				s.type = c.getInt(2);
				s.amount_start = c.getFloat(3);
				s.amount_end = c.getFloat(4);
				s.category_start = c.getInt(5);
				s.category_end = c.getInt(6);
				s.date_start = c.getLong(7);
				s.date_end = c.getLong(8);
				s.accountID = c.getInt(9);
				s.memberID = c.getInt(10);
				
				Account ac = GetAcccountByID(s.accountID);

				s.currencyName = ac.currencyName;
				s.currencyRate = ac.currencyRate;
				s.currencyID = ac.currencyId;
				
				c.close();
				
				return s;

		}
		c.close();

		return null;
	}
	
	public int AddDebt(Debt newman) {
		
		ContentValues cv = new ContentValues();
		cv.put("desc", newman.desc);
		cv.put("type", newman.type);
		cv.put("amount_start", newman.amount_start);
		cv.put("amount_end", newman.amount_end);
		cv.put("category_start", newman.category_start);
		cv.put("category_end", newman.category_end);
		cv.put("date_start", newman.date_start);
		cv.put("date_end", newman.date_end);
		cv.put("account", newman.accountID);
		cv.put("member", newman.memberID);
		int s = (int) database.insert(DATABASE_TABLE_DEBTS, null, cv);
		
		
		String desk = newman.type == 0 ? 
				context.getString(R.string.recieved_in_debt) :
				context.getString(R.string.sent_in_debt);
		
		Transaction tr = new Transaction();
		tr.accountID = newman.accountID;
		tr.amount = newman.amount_start;
		tr.memberID = newman.memberID;
		tr.categoryID = newman.category_start;
		tr.date = newman.date_start;
		tr.desc = desk + "(" + newman.desc + ")";
		
		AddNewTransaction(tr);
		
		return s;
		
	}

	public void RemoveDebt(int id) {
		
		Debt newman = GetDebt(id);
		
		String desk = newman.type == 1 ? 
				context.getString(R.string.they_returned_debt) :
				context.getString(R.string.i_returned_debt);
		
		Transaction tr = new Transaction();
		tr.accountID = newman.accountID;
		tr.amount = newman.amount_end;
		tr.memberID = newman.memberID;
		tr.categoryID = newman.category_end;
		tr.date = System.currentTimeMillis();
		tr.desc = desk + "(" + newman.desc + ")";
		
		AddNewTransaction(tr);
		
		database.delete(DATABASE_TABLE_DEBTS, "_id = ?", new String[] { String.valueOf(id)});

	}

	public void DeleteDebt(int id) {
		
		database.delete(DATABASE_TABLE_DEBTS, "_id = ?", new String[] { String.valueOf(id)});

	}

	public void	PerformExchange(int accForm, int accTo, float amount) {
		
		Transaction tr = new Transaction();
		tr.accountID = accForm;
		tr.amount = amount;
		tr.memberID = 1;
		tr.categoryID = 2;
		tr.date = System.currentTimeMillis();
		tr.desc = "exchange";
		AddNewTransaction(tr);

		Transaction tr2 = new Transaction();
		tr2.accountID = accTo;
		tr2.amount = amount;
		tr2.memberID = 1;
		tr2.categoryID = 1;
		tr2.date = System.currentTimeMillis();
		tr2.desc = "exchange";
		
		AddNewTransaction(tr2);
		
	}
	
	public boolean ThereIsBrokenBuget() {
		return false; // TODO: check
	}

	public boolean ThereIsExpiredDebt() {
		return false; // TODO: check
	}

	public int AddNewBuget(Buget newman) {

		ContentValues cv = new ContentValues();
		cv.put("name", newman.name);
		cv.put("type", newman.type);
		cv.put("amount", newman.amount);
		cv.put("category", newman.categoryID);
		cv.put("member", newman.memberID);
		cv.put("currency", newman.currencyID);
		
		if (newman.desc != null) {
			cv.put("desc", newman.desc);
		} else {
			cv.putNull("desc");
		}

		return (int) database.insert(DATABASE_TABLE_BUGET, null, cv);
	}

	public ArrayList<Buget> GetBugetsList() {

		ArrayList<TransactionCategory> cats = GetCategories();
		SparseArray<TransactionCategory> map = new SparseArray<TransactionCategory>();
		
		for (TransactionCategory c : cats) {
			map.put(c.id, c);
		}

		ArrayList<Member> members = GetMembers();
		SparseArray<Member> mmap = new SparseArray<Member>();
		
		for (Member m : members) {
			mmap.put(m.id, m);
		}
		
		
		ArrayList<Buget> result = new ArrayList<Buget>();

		Cursor c = database.query(DATABASE_TABLE_BUGET, null, null, null , null, null, null);

		if (c.moveToFirst()) {

			do {
				Buget s = new Buget();
				s.id = c.getInt(0);
				s.name = c.getString(1);
				s.type = c.getInt(2);
				s.amount = c.getFloat(3);
				s.categoryID = c.getInt(4);
				s.memberID = c.getInt(5);
				s.currencyID = c.getInt(6);
				s.desc = c.getString(7);

				Currency ac = GetCurrencyByID(s.currencyID);
				s.currencyName = ac.name;
				s.currencyRate = ac.rate;
				
				if (s.type == Buget.TYPE_MONTH) {
					s.currentAmount = GetCategoryAmountWithSlice(s.categoryID,
							GetThisMonthStart(), 
							System.currentTimeMillis());
				} else if (s.type == Buget.TYPE_WEEK) {
					s.currentAmount = GetCategoryAmountWithSlice(s.categoryID,
							GetThisWeekStart(), 
							System.currentTimeMillis());
				}
				
				TransactionCategory cat = map.get(s.categoryID);
				s.categoryName = cat.name;
				s.memberName = mmap.get(s.memberID).name;

				result.add(s);

			} while (c.moveToNext());

		}

		c.close();

		return result;
	}
	
	public void RemoveBuget(int id) {
		database.delete(DATABASE_TABLE_BUGET, "_id = ?", new String[] { String.valueOf(id)});
	}
	
	public void UpdateBugetAmount(int id, float amount ) {
		
	}

	public float GetCategoryAmountWithSlice(int cagetoryId, long from, long to) {
		String raw = "select sum(amount) as amount, type, category, name from transactions " +
				"inner join transaction_category on transactions.category = transaction_category._id " +
				"where category=" + cagetoryId + " and transactions.date between " + from + " and " + to + " " +
				"group by type, category " +
				"order by type ; ";
		Cursor c = database.rawQuery(raw, null);
		if (c.moveToFirst())
		{
			return c.getFloat(0);
		}
		else {
			return 0;
		}
	}
	
	public long GetThisMonthStart() {
		
		// get today and clear time of day
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);

		// get start of the month
		cal.set(Calendar.DAY_OF_MONTH, 1);
	    
	    return cal.getTime().getTime();
	}
	
	public long GetThisWeekStart() {

		// get today and clear time of day
	    Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
	    cal.clear(Calendar.MINUTE);
	    cal.clear(Calendar.SECOND);
	    cal.clear(Calendar.MILLISECOND);

	    // get start of this week in milliseconds
	    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
	    
	    return cal.getTime().getTime();
	}

	public void DeleteMember(int id) {
		database.delete(DATABASE_TABLE_MEMBERS, "_id = ?", new String[] { String.valueOf(id)});
	}
	
	
}
