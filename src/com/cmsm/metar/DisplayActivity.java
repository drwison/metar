package com.cmsm.metar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayActivity extends Activity {
	private TextView tv;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle xfer = getIntent().getExtras();
        String display = (String)xfer.get("display");
        setContentView(R.layout.sub);
        tv = (TextView)this.findViewById(R.id.textViewSub);
        
        tv.setText(display);
	}
}
