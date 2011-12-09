package de.hsrm.mi.mobcomp.httpclientdemo;

import org.apache.http.client.HttpClient;

import android.content.Intent;
import android.os.Bundle;
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
public class MainActivity extends MenuActivity {
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
				case 1:
					startActivity(new Intent(getApplicationContext(), FlickrAuthActivity.class));
					break;
				}
			}
		});
	}	
}