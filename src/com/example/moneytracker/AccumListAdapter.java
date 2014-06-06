package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AccumListAdapter extends ArrayAdapter<Accumulation> {
	
	public float curr_rate = 1.0f;
	public String curr_suffix = "MLD";

  private final Activity context;
  private final ArrayList<Accumulation> values;

  public AccumListAdapter(Activity context, ArrayList<Accumulation> values) {
    super(context, R.layout.buget_row, values);
    this.context = context;
    this.values = values;
  }

  static class ViewHolder {
    protected TextView amount;
    protected TextView target;
    protected TextView desc;
    protected TextView name;
    protected ProgressBar progress;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

	  View view = null;

	  if (convertView == null) {

		  LayoutInflater inflator = context.getLayoutInflater();
		  view = inflator.inflate(R.layout.accum_row, null);

		  final ViewHolder viewHolder = new ViewHolder();
		  viewHolder.amount = (TextView) (view.findViewById(R.id.accumRowAmount));
		  viewHolder.target = (TextView) (view.findViewById(R.id.accumRowTargetAmount));
		  viewHolder.name = (TextView) (view.findViewById(R.id.accumRowName));
		  viewHolder.desc = (TextView) (view.findViewById(R.id.accumRowDesc));
		  viewHolder.progress = (ProgressBar) (view.findViewById(R.id.accumRowProgressBar));
		  view.setTag(viewHolder);

	  } 
	  else
	  {
		  view = convertView;
	  }

	  ViewHolder holder = (ViewHolder) view.getTag();
	  Accumulation m = (Accumulation) values.get(position);
	  holder.target.setText(String.format("%.2f", m.target_amount / curr_rate ) + " ("+ curr_suffix +")");
	  holder.amount.setText(String.format("%.2f", m.amount / curr_rate ) + " (" + curr_suffix + ")");

	  if (m.target_amount > m.amount) {
		  view.setBackgroundColor(0);
	  } else {
		  view.setBackgroundColor(context.getResources().getColor(R.color.income));
	  }
	  
	  holder.progress.setProgress((int)(m.amount / m.target_amount * 100));

	  holder.name.setText(m.description);

	  return view;
  }


}
