package com.example.moneytracker;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class TransactionCategory {
	
	
	// Special categories
	// ids can be anu numbers, not req to be sequential
	
	public static final int EXCHANGE_INCOME = 1; 
	public static final int EXCHANGE_OUTCOME = 2;
	public static final int DEBT_INCOME = 3;
	public static final int DEBT_OUTCOME = 4;
	public static final int ACCUM_INCOME = 5;
	public static final int ACCUM_OUTCOME = 6;
	
	public static boolean IsSpecial(int id) {
		return 
				id == EXCHANGE_INCOME ||
				id == EXCHANGE_OUTCOME ||
				id == DEBT_INCOME ||
				id == DEBT_OUTCOME ||
				id == ACCUM_INCOME ||
				id == ACCUM_OUTCOME
				;
	}
	
	
	// Base members
	
	public String name;
	
	public int id;
	
	public float amount;
	
	public TransactionCategoryGroup parent;
	
	public int type;

	private static final Map<String, Integer> category_names;
    static
    {
        category_names = new HashMap<String, Integer>();
        category_names.put("Salary", R.string.category_salary);
        category_names.put("Interest", R.string.category_interests);
        category_names.put("Foodstuffs", R.string.category_food);
        category_names.put("Services", R.string.category_services);
        category_names.put("Car", R.string.category_car);
        category_names.put("Debts", R.string.debts);
        category_names.put("Accumulations", R.string.accumumations);
    }
    
    public static String GetLocalizedCategory(Context context, String key) {
		if (category_names.containsKey(key)) {
			return context.getString(category_names.get(key));
		} else {
			return key;
		}
    }
}
