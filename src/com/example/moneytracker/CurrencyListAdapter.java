package com.example.moneytracker;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class CurrencyListAdapter extends ArrayAdapter<Currency>{

	private Activity context;
	private ArrayList<Currency> list;
	
	private int acctives = 0;

	public CurrencyListAdapter(Activity context, ArrayList<Currency> list) {
		super(context, R.layout.currency_row ,list);

		this.context = context;
		this.list = list;
		
		for (Currency c : list) {
			if (c.isActive) {
				acctives++;
			}
		}
	}
	static class ViewHolder {
		protected TextView text;
		protected TextView rate;
		protected CheckBox checkbox;
	}

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = null;
    if (convertView == null) {

      LayoutInflater inflator = context.getLayoutInflater();
      view = inflator.inflate(R.layout.currency_row, null);

      final ViewHolder viewHolder = new ViewHolder();
      viewHolder.text = (TextView) view.findViewById(R.id.TextView_curr_name);
      viewHolder.rate = (TextView) view.findViewById(R.id.TextView_rate);
      viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
      viewHolder.checkbox
          .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
              Currency element = (Currency) viewHolder.checkbox.getTag();
              element.isActive = buttonView.isChecked();
              if (isChecked) {
            	  acctives++;
              } else {
            	  if (acctives <= 1) {
            		  element.isActive = true;
            		  buttonView.setChecked(true);
            		  //notifyDataSetChanged();
            		  Toast toast = Toast.makeText(context, R.string.at_least_one_currency_is_mandatory, Toast.LENGTH_SHORT); toast.show(); 
            	  } else {
            		  acctives--;
            	  }
              }
              //buttonView.setText(element.isActive ? context.getString(R.string.active) : context.getString(R.string.inactive));
            }

          });

      view.setTag(viewHolder);
      viewHolder.checkbox.setTag(list.get(position));
    } else {
      view = convertView;
      ((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
    }

    ViewHolder holder = (ViewHolder) view.getTag();
    holder.text.setText(list.get(position).name);
    holder.rate.setText(String.format("%.2f", list.get(position).rate));
    holder.checkbox.setChecked(list.get(position).isActive);

    return view;
  }
}
