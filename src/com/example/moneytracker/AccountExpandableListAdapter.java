package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.drawable.Drawable;
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


public class AccountExpandableListAdapter extends BaseExpandableListAdapter  {

	private final ArrayList<AccountsGroup> groups;
	public LayoutInflater inflater;
	public Activity activity;
	

	public AccountExpandableListAdapter(Activity act, ArrayList<AccountsGroup> groups) {
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
		final Account acc = ((Account) getChild(groupPosition, childPosition));
		final String ch_text = Account.GetLocalized(activity, acc.name) + " (" + acc.currencyName + ")";
		TextView text = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.accountrow_details, null);
		}
		text = (TextView) convertView.findViewById(R.id.TextViewAccountName);
		text.setText(ch_text);

		text = (TextView) convertView.findViewById(R.id.TextViewCurrency1);
		if (acc.comment != null) {
			text.setText(acc.comment);
		} else {
			text.setText("");
		}

		text = (TextView) convertView.findViewById(R.id.TextViewAmount);
		//text.setText(String.valueOf(acc.totalAmount));
		text.setText(String.format("%.2f", acc.totalAmount / acc.currencyRate));

		convertView.setOnLongClickListener(
				new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						((AccountsActivity) activity).PopupActionsMenu(acc.id);
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
			convertView = inflater.inflate(R.layout.accountrow_group, null);
		}
		AccountsGroup group = (AccountsGroup) getGroup(groupPosition);
		CheckedTextView cv = (CheckedTextView) convertView;
		//convertView.set
		cv.setText(group.name);
		cv.setChecked(isExpanded);
		
		Drawable gg = activity.getResources().getDrawable(group.icon);
		gg.setBounds(0, 0, 128, 128);
		//cv.get
		cv.setCompoundDrawables(null, null, gg ,null);
		//Log.e("ggg", group.icon + "");
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
