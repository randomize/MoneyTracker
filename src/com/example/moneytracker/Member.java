package com.example.moneytracker;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class Member {
	
	public int id;
	
	public String name;

	private static final Map<String, Integer> member_hardcoded_names;
    static
    {
        member_hardcoded_names = new HashMap<String, Integer>();
        member_hardcoded_names.put("Myself", R.string.myself);
    }
    
    public static String GetLocalized(Context context, String key) {
		if (member_hardcoded_names.containsKey(key)) {
			return context.getString(member_hardcoded_names.get(key));
		} else {
			return key;
		}
    }
}
