package com.cmsm.metar;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import ucar.nc2.dt.point.decode.MetarParseReport;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MetarActivity extends Activity {
	private static final String TAG = "Metar";
	private ListView list;
	private String display;
	private Activity currentActivity=this;
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
        		pressQuery(items[position].subSequence(0, 4).toString());
        	}
        });
        
    }
    
    public void pressQuery(String station) {
		LinkedHashMap lhm;
		HashMap hm;
		int count;
		URL url;
		HttpURLConnection connection;
		InputStream stream;
		byte[] b = new byte[1024];
		int nbytes;
		
		Log.d(TAG, "Start");
		MetarParseReport mpr = new MetarParseReport();
		station = station.toUpperCase();
		String urlstring = "http://weather.noaa.gov/pub/data/observations/metar/stations/" + station + ".TXT";

		pd = ProgressDialog.show(this, "This is the title", "This is the detail text", true, false, null);

		try {
			url = new URL(urlstring);
			try {
				connection = (HttpURLConnection)url.openConnection();
				try {
					connection.setRequestMethod("GET");
					try {
						connection.connect();
						try {
							stream = connection.getInputStream();
						} catch (IOException e) {
							msgDisplay("Exception: Cannot getInputStream");
							Log.e(TAG, "getInputSTream", e); return;
						}
					} catch (IOException e) {
						msgDisplay("Exception: Cannot connect");
						Log.d(TAG, "connect", e); return;
					}
				} catch (ProtocolException e) {
					msgDisplay("Exception: Cannot setRequstMethod");
					Log.d(TAG, "setRequestMethod", e); return;
				}
			} catch (IOException e) {
				msgDisplay("Exception: Cannot HttpURLCOnnection");
				Log.d(TAG, "HttpURLConnection", e); return;
			}
		} catch (MalformedURLException e) {
			msgDisplay("Exception: Malformed URL");
			Log.d(TAG, "URL", e); return;
		}
	
		try {
			nbytes = stream.read(b);
		} catch (IOException e) {
			msgDisplay("Exception: Read IO from URL failed");
			Log.d(TAG, e.getMessage()); return;
		}
		
		String ibuffer = new String(b);
		String s[] = ibuffer.split("\n");
		String metarDate, metar;
		display = ibuffer + "\n";
		for (int i=0; i<s.length; i++) {
			if (s[i].matches("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}")) {
				metarDate = s[i];
			} else if (s[i].startsWith(station)) {
				metar = s[i];
				mpr.parseReport(metar);
				lhm = mpr.getFields();
				hm = mpr.getUnits();
				
				Iterator it = lhm.keySet().iterator();
				count = 0;
				while (it.hasNext()) {
					count++;
					Object o = it.next();
					String val = lhm.get(o).toString();
					Log.d(TAG, Integer.toString(count));
					Log.d(TAG, o.toString());
					Log.d(TAG, val);
					Log.d(TAG, hm.get(o).toString());
					display += o.toString() + ": " + val + " " + hm.get(o).toString() + "\n";
				}
			}
		}
		msgDisplay(display);
    }

    private void msgDisplay(String s) {
    	pd.dismiss();
    	display = s;
		Intent in = new Intent(this, DisplayActivity.class);
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