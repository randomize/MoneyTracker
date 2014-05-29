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
		final TransactionCatagory child = ((TransactionCatagory) getChild(groupPosition, childPosition));
		final String ch_text = TransactionCatagory.GetLocalizedCategory(activity, child.name);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.transrow_details, null);
		}

		TextView text = (TextView) convertView.findViewById(R.id.TextViewCategoryItemName);
		text.setText(ch_text);

		TextView amount = (TextView) convertView.findViewById(R.id.TextViewCategortItemAmount);
		if (child.parent.type == GroupType.INCOME) {
			amount.setTextColor(activity.getResources().getColor(R.color.income));
		} else {
			amount.setTextColor(activity.getResources().getColor(R.color.outcome));
		}
		amount.setText(String.format("%.2f", child.amount / rate));

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((SummaryActivity)activity).OpenDetailedCategory(child.id, ch_text);
				//Toast.makeText(activity, ch_text, Toast.LENGTH_SHORT).show();
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
		
		String title = TransactionCategoryGroup.GetLocalizedCategory(activity, group.name);

		TextView amount = (TextView) convertView.findViewById(R.id.TextViewTransCategoryAmount);
		if (group.type == GroupType.INCOME) {
			amount.setTextColor(activity.getResources().getColor(R.color.income));
		} else {
			amount.setTextColor(activity.getResources().getColor(R.color.outcome));
		}
		amount.setText(String.format("%.2f", group.amount / rate));

		CheckedTextView groupTextView = (CheckedTextView) convertView.findViewById(R.id.TextViewTransCategoryName);
		//convertView.set
		groupTextView.setText(title + " ("+suffix+")");
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
