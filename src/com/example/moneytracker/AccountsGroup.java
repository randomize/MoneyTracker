package com.example.moneytracker;

import java.util.ArrayList;
import java.util.List;


// Model of accounts group

public class AccountsGroup {

	// 1 - cash
	// 2 - card
	// 3 - bank_account
	public enum GroupType { 

		CASH(1),    
		CARD(2), 
		BANK_ACCOUNT(3);

		private int value;

		private GroupType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int val) {
			this.value = val;
		}
	}

	public GroupType type;

	// Group name
	public String name;

	// Total of money in this group
	public float amount;

	// Group contents
	public final List<Account> children = new ArrayList<Account>();
	
	// Drawable icon
	public int icon;

	public AccountsGroup(String string, float amount, GroupType type) {
		this.name = string;
		this.amount = amount;
		this.type =  type;
	}

	// Auto calculate child
	public void AddChild(Account child) {
		children.add(child);
		amount+=child.totalAmount;
	}
	
	public boolean isEmpty() {
		return children.isEmpty();
	}

} 
