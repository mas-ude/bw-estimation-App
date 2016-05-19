package com.test.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;

import com.test.app.model.DataModel;
import com.test.app.model.DataObject;

public class BandwidthDetailsActivity extends Activity
{
	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	List<String> listDataHeader;
	HashMap<String, List<String>> listDataChild;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);

		// get the listview
		expListView = (ExpandableListView) findViewById(R.id.lvExp);

		// preparing list data
		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();

		Intent intent = getIntent();

		@SuppressWarnings("unchecked")
		ArrayList<DataObject> results = (ArrayList<DataObject>) intent
				.getSerializableExtra("Result");

		String temp = "";

		for (int i = 0; i < results.size(); i++)
		{
			temp = DataModel.getMethodName(results.get(i).getMethod()) + ": "
					+ results.get(i).getAvgBandwidth() + " KB/s";
			listDataHeader.add(temp);
			listDataChild.put(listDataHeader.get(i), results.get(i)
					.getDisplayInformation());
		}

		listAdapter = new ExpandableListAdapter(this, listDataHeader,
				listDataChild);

		// setting list adapter
		expListView.setAdapter(listAdapter);
	}

}