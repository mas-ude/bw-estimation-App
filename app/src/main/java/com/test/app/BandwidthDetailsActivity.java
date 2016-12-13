package com.test.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.test.app.model.DataModel;
import com.test.app.model.Bandwidths;
import com.test.app.model.Results;

public class BandwidthDetailsActivity extends Activity implements
		OnClickListener
{
	private ExpandableListAdapter listAdapter;
	private ExpandableListView expListView;
	private List<String> listDataHeader;
	private HashMap<String, List<String>> listDataChild;
	private Button overview, results;
	private ArrayList<Results> resultsTemp;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);

		// get the listview
		expListView = (ExpandableListView) findViewById(R.id.lvExp);

		// Get Shared Preferences
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Get Buttons
		overview = (Button) findViewById(R.id.overview_button);
		overview.setOnClickListener(this);

		results = (Button) findViewById(R.id.result_button);
		results.setOnClickListener(this);

		// Get Status View
		TextView status = (TextView) findViewById(R.id.status_icon);
		if (sharedPrefs.getBoolean("Status", false))
		{
			status.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ok,
					0);
		} else
		{
			status.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.not_ok, 0);
		}

		// preparing list data
		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();

		Intent intent = getIntent();

		@SuppressWarnings("unchecked")
		ArrayList<Bandwidths> resultObjects = (ArrayList<Bandwidths>) intent
				.getSerializableExtra("ResultObjects");

		@SuppressWarnings("unchecked")
		final ArrayList<Results> results = (ArrayList<Results>) intent
				.getSerializableExtra("Result");
		resultsTemp = results;

		String temp = "";

		for (int i = 0; i < resultObjects.size(); i++)
		{
			temp = DataModel.getMethodName(resultObjects.get(i).getMethod())
					+ ": " + resultObjects.get(i).getAvgBandwidth() + " KB/s";
			listDataHeader.add(temp);
			listDataChild.put(listDataHeader.get(i), resultObjects.get(i)
					.getDisplayInformation());
		}

		listAdapter = new ExpandableListAdapter(this, listDataHeader,
				listDataChild);

		// setting list adapter
		expListView.setAdapter(listAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		if (id == R.id.action_development_settings)
		{
			startActivity(new Intent(this, DevelopmentSettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.overview_button)
		{
			startActivity(new Intent(this, MainActivity.class));
		} else if (v.getId() == R.id.result_button)
		{
			Intent nextScreen = new Intent(getApplicationContext(),
					ResultActivity.class);
			nextScreen.putExtra("Result", resultsTemp);
			startActivity(nextScreen);
		}
	}

}