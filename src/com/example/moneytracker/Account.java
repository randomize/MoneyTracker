package com.example.moneytracker;

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

	// Name of currency
	public String currencyName;

	// Name of type
	public String typeName;

}
