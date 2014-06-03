package com.example.moneytracker;

import android.content.Context;

public class Buget {
	
	public int id;

	public int type;
	
	// Predefined types
	public static final int TYPE_WEEK = 0;
	public static final int TYPE_MONTH = 1;

	
	public String name;
	public float amount;
	public int currencyID;
	public int categoryID;
	public int memberID;
	
	public String desc;
	
	// Calculated
	
	public String currencyName;
	public float currencyRate;
	public float currentAmount; // week or month
	public String categoryName;
	public String memberName;
	
	public static String LocalizeType(Context context, int type) {
		
		if (type == 0) {
			return context.getString(R.string.weekly);
		} else 
			return context.getString(R.string.monthly);
		
		
	}

}
