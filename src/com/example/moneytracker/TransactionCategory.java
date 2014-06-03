package com.example.moneytracker;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class TransactionCategory {
	
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
    }
    
    public static String GetLocalizedCategory(Context context, String key) {
		if (category_names.containsKey(key)) {
			return context.getString(category_names.get(key));
		} else {
			return key;
		}
    }
}