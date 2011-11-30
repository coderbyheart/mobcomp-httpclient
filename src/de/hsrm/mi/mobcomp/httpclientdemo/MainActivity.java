package de.hsrm.mi.mobcomp.httpclientdemo;

import org.apache.http.client.HttpClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Demo zur Verwendung des {@link HttpClient} auf Android.
 * 
 * Quellcode unter: https://github.com/tacker/mobcomp-httpclient
 * 
 * @author Markus Tacker <m@coderbyheart.de>
 */
public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ListView tasks = (ListView) findViewById(R.id.taskListView);
		tasks.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, getResources()
						.getStringArray(R.array.tasks)));
		tasks.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch(position) {
				case 0:
					startActivity(new Intent(getApplicationContext(), LoadActivity.class));
					break;
				}
			}
		});
	}
	

	
	@Override 
    public boolean onCreateOptionsMenu(Menu menu) { 
        new MenuInflater(this).inflate(R.menu.menu, menu);
        return true; 
    } 
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
		case R.id.menu_settings:
			Log.v(getClass().getCanonicalName(), "Settings!");
			startActivity(new Intent(getApplicationContext(), PrefsActivity.class));
			return true;
		}
		return false;
    }	
}