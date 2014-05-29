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

	public GroupType type;
	public String name;
	public float amount;

	public final List<TransactionCatagoryItem> children = new ArrayList<TransactionCatagoryItem>();
	
	public TransactionCategoryGroup(String name, GroupType type) {
		this.name = name;
		this.type = type;
	}

	// Auto calculate child
	public void AddChild(TransactionCatagoryItem child) {
		children.add(child);
		amount+=child.amount;
	}

	public boolean isEmpty() {
		return children.isEmpty();
	}

}
