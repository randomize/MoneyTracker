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

public class CurrencyListAdapter extends ArrayAdapter<Currency>{

	Activity context;
	ArrayList<Currency> list;

	public CurrencyListAdapter(Activity context, ArrayList<Currency> list) {
		super(context, R.layout.currency_row ,list);

		this.context = context;
		this.list = list;
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
              buttonView.setText(element.isActive ? context.getString(R.string.active) : context.getString(R.string.inactive));
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
