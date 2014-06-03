package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BugetLustAdapter extends ArrayAdapter<Buget> {

  private final Activity context;
  private final ArrayList<Buget> values;

  public BugetLustAdapter(Activity context, ArrayList<Buget> values) {
    super(context, R.layout.buget_row, values);
    this.context = context;
    this.values = values;
  }

  static class ViewHolder {
    protected TextView amount;
    protected TextView limit;
    protected TextView desc;
    protected TextView name;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

	  View view = null;

	  if (convertView == null) {

		  LayoutInflater inflator = context.getLayoutInflater();
		  view = inflator.inflate(R.layout.buget_row, null);

		  final ViewHolder viewHolder = new ViewHolder();
		  viewHolder.amount = (TextView) view.findViewById(R.id.bugetRowAmount);
		  viewHolder.limit = (TextView) view.findViewById(R.id.bugetRowAmountLimit);
		  viewHolder.name = (TextView) view.findViewById(R.id.bugetRowName);
		  viewHolder.desc = (TextView) view.findViewById(R.id.bugetRowDesc);
		  view.setTag(viewHolder);

	  } 
	  else
	  {
		  view = convertView;
	  }

	  ViewHolder holder = (ViewHolder) view.getTag();
	  Buget m = (Buget) values.get(position);
	  holder.amount.setText(String.format("%.2f", m.currentAmount / m.currencyRate ));
	  holder.limit.setText(String.format("%.2f", m.amount / m.currencyRate ));
	  if (m.currentAmount < m.amount) {
		  holder.amount.setBackgroundColor(0);
		  holder.limit.setBackgroundColor(0);
	  } else {
		  holder.amount.setBackgroundColor(context.getResources().getColor(R.color.errorous));
		  holder.limit.setBackgroundColor(context.getResources().getColor(R.color.errorous));
	  }
	  holder.desc.setText(m.desc != null ? m.desc : "" );
	  holder.name.setText(m.name);
	  return view;
  }


}
