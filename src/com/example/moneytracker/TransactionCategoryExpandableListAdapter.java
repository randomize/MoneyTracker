package com.example.moneytracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.moneytracker.TransactionCategoryGroup.GroupType;

import android.app.Activity;
import android.content.res.Resources;
import android.provider.Telephony.Sms.Conversations;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;


public class TransactionCategoryExpandableListAdapter extends BaseExpandableListAdapter  {

	private final ArrayList<TransactionCategoryGroup> groups;
	public LayoutInflater inflater;
	public Activity activity;
	
	public float rate = 1.0f;
	public String suffix = "";
	
	private static final Map<String, Integer> group_names;
    static
    {
        group_names = new HashMap<String, Integer>();
        group_names.put("Income", R.string.income);
        group_names.put("Outcome", R.string.outcome);
    }

	public TransactionCategoryExpandableListAdapter(Activity act, ArrayList<TransactionCategoryGroup> groups) {
		activity = act;
		this.groups = groups;
		inflater = act.getLayoutInflater();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return groups.get(groupPosition).children.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) 
	{
		final String ch_text = ((TransactionCatagoryItem) getChild(groupPosition, childPosition)).name;
		TextView text = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.transrow_details, null);
		}
		text = (TextView) convertView.findViewById(R.id.TextViewAccountName);
		text.setText(ch_text);

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(activity, ch_text,
						Toast.LENGTH_SHORT).show();
			}
		});
		
			
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return groups.get(groupPosition).children.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		super.onGroupCollapsed(groupPosition);
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		super.onGroupExpanded(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) 
	{
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.transrow_group, null);
		}
		TransactionCategoryGroup group = (TransactionCategoryGroup) getGroup(groupPosition);
		
		String s;
		if (group_names.containsKey(group.name)) {
			s = activity.getString(group_names.get(group.name));
		} else {
			s = group.name;
		}

		TextView amount = (TextView) convertView.findViewById(R.id.TextViewTransCategoryAmount);
		if (group.type == GroupType.INCOME) {
			amount.setTextColor(activity.getResources().getColor(R.color.income));
		} else {
			amount.setTextColor(activity.getResources().getColor(R.color.outcome));
		}
		amount.setText(String.format("%.2f", group.amount / rate));

		CheckedTextView groupTextView = (CheckedTextView) convertView.findViewById(R.id.TextViewTransCategoryName);
		//convertView.set
		groupTextView.setText(s + " ("+suffix+")");
		groupTextView.setChecked(isExpanded);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		Log.e("ggg", "sg" + groupPosition + " => " + childPosition);
		return true;
	}
	
}
