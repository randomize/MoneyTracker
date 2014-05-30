package com.example.moneytracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class DebtsGroup {
	
	public String name;
	
	public ArrayList<Debt> children = new ArrayList<Debt>();
	
	public int type; // 0 - outcome 1 - income
	
	public float amount;
	
	public DebtsGroup(String name, int type) {
		this.name = name;
		this.type = type;
	}

	// Auto calculate child
	public void AddChild(Debt child) {
		children.add(child);
		amount+=child.amount_end;
	}

	public boolean isEmpty() {
		return children.isEmpty();
	}

	private static final Map<String, Integer> group_names;
    static
    {
        group_names = new HashMap<String, Integer>();
        group_names.put("TheyOwe", R.string.i_am_owed);
        group_names.put("IOwe", R.string.i_owe);
    }

    public static String GetLocalizedDebt(Context context, String key) {
		if (group_names.containsKey(key)) {
			return context.getString(group_names.get(key));
		} else {
			return key;
		}
    }

}
