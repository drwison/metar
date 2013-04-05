package com.cmsm.metar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import ucar.nc2.dt.point.decode.MetarParseReport;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
//import android.os.StrictMode;
//import android.os.StrictMode.ThreadPolicy;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MetarActivity extends Activity {
	private static final String TAG = "Metar";
	private ListView list;
	private String display;
	private ProgressDialog pd;
	AlertDialog alert;
	CharSequence[] items;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        list = (ListView)this.findViewById(R.id.listStations);
        Resources res = getResources();
        items = res.getStringArray(R.array.prefStations);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.prefStations, R.layout.list_item);
        adapter.setDropDownViewResource(R.layout.list_item);
        list.setAdapter(adapter);
        
        list.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        		pressQuery(items[position].subSequence(items[position].length()-1-4, items[position].length()-1).toString());
        	}
        });
        
    	list.setOnItemLongClickListener(new OnItemLongClickListener() {
    		public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
    			AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
    			alertDialog.setTitle("Station löschen");
    			alertDialog.setMessage("Wirklich?");
    			alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    			       //here you can add functions
    			    }
    			}); 
    			alertDialog.show();
    			return true;
    		}
    	});
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu , menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case 1:
			Intent in1 = new Intent(this, Configuration.class);
			//in1.putExtra("display", display);
			startActivity(in1);
			break;
	    case 2:
			Intent in2 = new Intent(this, Configuration.class);
			//in2.putExtra("display", display);
			startActivity(in2);
			break;
		default:
			break;
	    }
	    return true;
	}

	private class DownloadMetarTask extends AsyncTask<String, Void, String> {
		//String metarDate;
		MetarParseReport mpr = new MetarParseReport();
		LinkedHashMap<String, String> lhm;
		HashMap<String, String> hm;
	
		protected String doInBackground(String... urls) {
			String display = "";
			
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();
					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						display += s + "\n";
						if (s.matches("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}")) {
							//metarDate = s;
						} else { // if (s.startsWith(station)) {
							mpr.parseReport(s);
							lhm = (LinkedHashMap<String, String>)mpr.getFields();
							hm = (HashMap<String, String>)mpr.getUnits();
							
							Iterator<String> it = lhm.keySet().iterator();
							int count = 0;
							while (it.hasNext()) {
								count++;
								Object o = it.next();
								String val = lhm.get(o).toString();
								Log.d(TAG, Integer.toString(count));
								Log.d(TAG, o.toString());
								Log.d(TAG, val);
								Log.d(TAG, hm.get(o).toString());
								display += "\n" + o.toString() + ": " + val + " " + hm.get(o).toString();
							}
						}
						
					}
					if (Debug.isDebuggerConnected()) {
						for (int j=0; j<200; j++) {
							display += "\n" + Integer.toString(j);
						}
					}
					//msgDisplay(display);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return display;
		}
		
		protected void onPostExecute(String display) {
			Log.d(TAG, "AsyncTask onPostExecute");
			Log.d(TAG, display);
			msgDisplay(display);
		}
	}
	
    public void pressQuery(String station) {
		Log.d(TAG, "Start");
		station = station.toUpperCase();
		String urlstring = "http://weather.noaa.gov/pub/data/observations/metar/stations/" + station + ".TXT";
		
		//StrictMode.setThreadPolicy(ThreadPolicy.LAX);		// For ICS: They want threaded Internet activities

		pd = ProgressDialog.show(this, "METAR", "NOAA Online Query", true, false, null);

		DownloadMetarTask task = new DownloadMetarTask();
		task.execute(new String[] { urlstring });		
    }

    private void msgDisplay(String s) {
    	pd.dismiss();
    	display = s;
		Intent in = new Intent(this, ScrollView.class);
		in.putExtra("display", display);
		startActivity(in);
    }

	public void onBackPressed() {
		this.finish();
	}

	public void pressFinish() {
		this.finish();
	}
}