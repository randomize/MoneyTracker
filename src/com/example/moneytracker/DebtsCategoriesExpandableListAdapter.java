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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;


public class DebtsCategoriesExpandableListAdapter extends BaseExpandableListAdapter  {

	private final ArrayList<DebtsGroup> groups;
	public LayoutInflater inflater;
	public Activity activity;
	
	public float rate = 1.0f;
	public String suffix = "";
	public boolean relevant = false;
	

	public DebtsCategoriesExpandableListAdapter(Activity act, ArrayList<DebtsGroup> groups) {
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
		final Debt child = ((Debt) getChild(groupPosition, childPosition));
		String ch_text = child.desc +  " " +  ( relevant ? ("(" + child.currencyName + ")") : "" );

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.transrow_details, null);
		}


		TextView amount = (TextView) convertView.findViewById(R.id.TextViewCategortItemAmount);
		if (child.type == 1) {
			amount.setTextColor(activity.getResources().getColor(R.color.income));
		} else {
			amount.setTextColor(activity.getResources().getColor(R.color.outcome));
		}
		if (relevant) {
			amount.setText(String.format("%.2f", child.amount_end / child.currencyRate));
		} else {
			amount.setText(String.format("%.2f", child.amount_end / rate));
		}
		
		long date_cur =  System.currentTimeMillis();
		
		//Log.w("ggg","ggg " + date_cur + "  " + ch );
		
		int diffInDays = (int)( (child.date_end - date_cur) / (1000 * 60 * 60 * 24) );

		if (diffInDays < 0) { // Outdated
			convertView.setBackgroundColor(activity.getResources().getColor(R.color.errorous));
			ch_text += (activity.getString(R.string.expired) + " " + String.valueOf(-diffInDays)
					+ " " + activity.getString(R.string.ago) );
		} else {
			ch_text += " (" + (String.valueOf(diffInDays) + " " + activity.getString(R.string.left)) + ")";
		}

		TextView text = (TextView) convertView.findViewById(R.id.TextViewCategoryItemName);
		text.setText(ch_text);

		
		convertView.setOnLongClickListener(
				new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						((DebtsListActivity) activity).PopupActionsMenu(child.id, child.desc);
						return false;
					}
				}
				);
		
		convertView.setLongClickable(true);
			
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
		DebtsGroup group = (DebtsGroup) getGroup(groupPosition);
		
		String title = DebtsGroup.GetLocalizedDebt(activity, group.name);

		TextView amount = (TextView) convertView.findViewById(R.id.TextViewTransCategoryAmount);
		if (group.type == 1) {
			amount.setTextColor(activity.getResources().getColor(R.color.income));
		} else {
			amount.setTextColor(activity.getResources().getColor(R.color.outcome));
		}
		
		if (relevant) {
			amount.setText("");
		} else {
			amount.setText(String.format("%.2f", group.amount / rate));
		}

		CheckedTextView groupTextView = (CheckedTextView) convertView.findViewById(R.id.TextViewTransCategoryName);
		//convertView.set
		if (relevant) {
			groupTextView.setText(title);
		} else {
			groupTextView.setText(title + " ("+suffix+")");
		}

		groupTextView.setChecked(isExpanded);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
}
