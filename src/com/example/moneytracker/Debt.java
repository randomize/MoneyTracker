package com.example.moneytracker;

public class Debt {
	
	// database id
	int id;
	
	// Description
	String desc;
	
	// Type is based on end transaction
	public int type; // 0 - outcome(I got and should return), i - income(They got and return to me)
	
	// Amounts
	public float amount_start;
	public float amount_end;
	
	// Cateogries
	public int category_start;
	public int category_end;
	
	// Dates
	public long date_start;
	public long date_end;

	// Binded to
	public int accountID;

	// User
	public int memberID;

	
	// Calculated
	
	public String currencyName;

	public int currencyID;

	public float currencyRate;
	
	public DebtsGroup parent;


}
