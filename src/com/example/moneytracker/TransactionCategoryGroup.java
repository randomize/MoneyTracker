package com.example.moneytracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class TransactionCategoryGroup {
	
	// Iccome = 1
	// Outcome = 0
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

	private static final Map<String, Integer> group_names;
    static
    {
        group_names = new HashMap<String, Integer>();
        group_names.put("Income", R.string.income);
        group_names.put("Outcome", R.string.outcome);
    }

    public static String GetLocalizedCategory(Context context, String key) {
		if (group_names.containsKey(key)) {
			return context.getString(group_names.get(key));
		} else {
			return key;
		}
    }

	public GroupType type;
	public String name;
	public float amount;

	public final List<TransactionCategory> children = new ArrayList<TransactionCategory>();
	
	public TransactionCategoryGroup(String name, GroupType type) {
		this.name = name;
		this.type = type;
	}

	// Auto calculate child
	public void AddChild(TransactionCategory child) {
		children.add(child);
		amount+=child.amount;
	}

	public boolean isEmpty() {
		return children.isEmpty();
	}

}
