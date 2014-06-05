package com.example.moneytracker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.moneytracker.R;

class Updater implements Runnable {

    private Context var;
    private DatabaseFacade db;

    public Updater(Context var) {
        this.var = var;
        db = new DatabaseFacade(var);
    }

	private Document parseXML(InputStream stream) throws Exception
    {
        DocumentBuilderFactory objDocumentBuilderFactory = null;
        DocumentBuilder objDocumentBuilder = null;
        Document doc = null;
        try
        {
            objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();

            doc = objDocumentBuilder.parse(stream);
        }
        catch(Exception e)
        {
			e.printStackTrace();
        }       
        return doc;
    }

	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	private float getFloatValue(Element ele, String tagName) {
		float res = 0;
		try {
			res = Float.parseFloat(getTextValue(ele,tagName));
		}  catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return res;
	}

    public void run() {
		HashMap<String, Float> map = new HashMap<String, Float>();

		try {
			
			Calendar c = Calendar.getInstance();

			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			String formattedDate = df.format(c.getTime());
			Log.e("ggg", formattedDate);

			URL url = new URL("http://www.bnm.md/ru/official_exchange_rates?get_xml=1&date=" + formattedDate);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.connect();


			Document doc = parseXML(urlConnection.getInputStream());
			NodeList descNodes = doc.getElementsByTagName("Valute");

			for(int i=0; i<descNodes.getLength();i++)
			{
				//get the employee element
				Element el = (Element)descNodes.item(i);

				String name = getTextValue(el,"CharCode");
				float curr = getFloatValue(el,"Value");

				map.put(name, curr);
			}
			
			Log.e("ghh", "Got " + map.size() );
			
			db.open();
			db.UpdateCurrencies(map);
			db.close();
			

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}

public class MainActivity extends Activity implements OnClickListener {
	
	private View v, v2;
	private DatabaseFacade db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.ButtonStatus).setOnClickListener(this);
		findViewById(R.id.ButtonAccounts).setOnClickListener(this);
		findViewById(R.id.ButtonAccumulations).setOnClickListener(this);

		v = findViewById(R.id.ButtonBudget);
		v.setOnClickListener(this);
		v2 = findViewById(R.id.ButtonDebts);
		v2.setOnClickListener(this);

		findViewById(R.id.ButtonEventsTasks).setOnClickListener(this);
		db = new DatabaseFacade(this);
		
		CheckForUpdates();
		
	
	}
	
	private void CheckForUpdates() {
		
		long curr = System.currentTimeMillis();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		long was = settings.getLong("lastUpdate", 0);
		
		if ((curr - was) / (1000 * 60 * 60) > 8 ) {

			Toast toast = Toast.makeText(this, "Updating currency rates", Toast.LENGTH_SHORT);
			toast.show(); 
			
			Update();
			
			Editor edit = settings.edit();
			edit.putLong("lastUpdate", curr);
			edit.apply(); 

		}
		
	}
	
	private void Update() {
		
		Updater myRunnable = new Updater(this);
		Thread t = new Thread(myRunnable);
		t.start();

	}
	

	private void SetupColors() {
		
		db.open();
		
		if (db.ThereIsBrokenBuget()) {
			v.setBackgroundColor(getResources().getColor(R.color.errorous));
		}

		if (db.ThereIsExpiredDebt()) {
			v2.setBackgroundColor(getResources().getColor(R.color.errorous));
		}

		db.close();
	}

	@Override
	protected void onResume() {
		SetupColors();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	private void OpenPreferences() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_settings:
			OpenPreferences();
			return true;
		case R.id.action_members_list:
			OpenMembersList();
			return true;
		case R.id.action_currency_list:
			OpenCurrencyList();
			return true;
		case R.id.action_about:
			OpenAbout();
			return true;
		case R.id.action_exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void OpenAbout() {
		// TODO Auto-generated method stub
		
	}

	private void OpenMembersList() {
		Intent intent = new Intent(this, MembersListActivity.class);
		startActivity(intent);
		
	}

	private void OpenCurrencyList() {
		Intent intent = new Intent(this, CurrencyListActivity.class);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		    case  R.id.ButtonAccounts: {
                Intent intent = new Intent(this, AccountsActivity.class);
                startActivity(intent);
			    break;
		    }

		    case  R.id.ButtonStatus: {
                Intent intent = new Intent(this, SummaryActivity.class);
                startActivity(intent);
			    break;
		    }
		    
		    case R.id.ButtonDebts: {
                Intent intent = new Intent(this, DebtsListActivity.class);
                startActivity(intent);
                break;
		    }
		    case R.id.ButtonBudget: {
                Intent intent = new Intent(this, BugetListActivity.class);
                startActivity(intent);
                break;
		    }
    
		   /* case R.id.clickButton2: {
			    // do something for button 2 click
			    break;
		    }*/

		    //.... etc

		}

	}

}



