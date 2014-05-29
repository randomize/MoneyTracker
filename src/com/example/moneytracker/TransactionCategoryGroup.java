package com.example.moneytracker;

import java.util.ArrayList;
import java.util.List;

public class TransactionCategoryGroup {
	
	// 1 - cash
	// 2 - card
	// 3 - bank_account
	public enum GroupType { 

		INCOME(1),    
		OUTCOME(0);

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

	public GroupType Type;
	public String Name;
	public float Amount;

	public final List<TransactionCatagoryItem> children = new ArrayList<TransactionCatagoryItem>();
	
	public TransactionCategoryGroup(String name, GroupType type) {
		this.Name = name;
		this.Type = type;
	}

}
