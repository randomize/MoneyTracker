package com.example.moneytracker;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class Account {
	
	///// Database values

	// ID
	public int id;

	// Account name
	public String name;

	// Type id
	public int typeId;

	// Currency id
	public int currencyId;

	// Account name
	public String comment;

	///// Calculated values
	
	// Total of money on this account item
	public float totalAmount;
	
	// Rate of currenct
	public float currencyRate;

	// Name of currency
	public String currencyName;

	// Name of type
	public String typeName;

	private static final Map<String, Integer> category_names;
    static
    {
        category_names = new HashMap<String, Integer>();
        category_names.put("Cash", R.string.type_cash);
    }
    
    public static String GetLocalized(Context context, String key) {
		if (category_names.containsKey(key)) {
			return context.getString(category_names.get(key));
		} else {
			return key;
		}
    }

}
