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
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MetarActivity extends Activity {
	private static final String TAG = "Metar";
	private TextView tvr;
	private EditText edit;
	private String display;
	private Button query, select;
	AlertDialog alert;
	CharSequence[] items;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tvr = (TextView)this.findViewById(R.id.screen);
        edit = (EditText)this.findViewById(R.id.editStation);
        query = (Button)this.findViewById(R.id.buttonQuery);
        select = (Button)this.findViewById(R.id.buttonSelect);
        Resources res = getResources();
        items = res.getStringArray(R.array.prefStations);
        
        // ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.prefStations, android.R.layout.select_dialog_item);
        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // select.setAdapter(adapter);
        
        query.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		pressQuery();
        	}
        });
        
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a station");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        // Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
		    	edit.setText(items[item].subSequence(0, 4));
		    	pressQuery();
		    }
		});
		alert = builder.create();
		
		select.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		alert.show();
        	}
        });
        
        edit.setOnEditorActionListener(new OnEditorActionListener() {
        	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        		if (actionId == EditorInfo.IME_ACTION_GO) {
        			query.performClick();
        			return true;
        		}
        		return false;
        	}
        });
    }
    
    public void pressQuery() {
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
		String station = edit.getText().toString();
		station = station.toUpperCase();
		String urlstring = "http://weather.noaa.gov/pub/data/observations/metar/stations/" + station + ".TXT";

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
    	display = s;
		tvr.setTextColor(Color.CYAN);
		tvr.post(new Runnable() {
			public void run() {
				tvr.setText(display);
			}
		});
    }

	public void onBackPressed() {
		this.finish();
	}

	public void pressFinish() {
		this.finish();
	}
}