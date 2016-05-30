package com.test.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.test.app.model.ResultObject;

public class ResultActivity extends Activity
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
		// prepareListData();

		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();

		Intent intent = getIntent();

		@SuppressWarnings("unchecked")
		final ArrayList<ResultObject> results = (ArrayList<ResultObject>) intent
				.getSerializableExtra("Result");

		for (int i = 0; i < results.size(); i++)
		{
			listDataHeader.add((i + 1) + ": " + results.get(i).getDate());
			listDataChild.put(listDataHeader.get(i), results.get(i)
					.getDisplayInformation());
		}

		listAdapter = new ExpandableListAdapter(this, listDataHeader,
				listDataChild);

		// Listview on child click listener
		expListView.setOnChildClickListener(new OnChildClickListener()
		{

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id)
			{
				// If User clicked last Child in Group then new Activity started
				if (childPosition == (results.get(groupPosition)
						.getDisplayInformation().size() - 1))
				{
					Intent nextScreen = new Intent(getApplicationContext(),
							BandwidthDetailsActivity.class);
					nextScreen.putExtra("Result", results.get(groupPosition)
							.getObjects());
					startActivity(nextScreen);
				}
				return false;
			}
		});

		// setting list adapter
		expListView.setAdapter(listAdapter);
	}

}
