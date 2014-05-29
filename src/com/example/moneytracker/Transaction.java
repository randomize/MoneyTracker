package com.example.moneytracker;

public class Transaction {
	
	// Id in database
	public int id;
	
	// Description
	public String desc;
	
	// Amount in parrots
	public float amount;
	
	// DateTime string
	public String date;

	// Account
	public int accountID;

	// Category
	public int categoryID;

	// Category
	public int memberID;
	
	///// Aggregated values
	
	// Member name
	public String member;
	
	// Target account name
	public String account;

}
