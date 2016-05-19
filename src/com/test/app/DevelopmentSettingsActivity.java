package com.test.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class DevelopmentSettingsActivity extends PreferenceActivity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new Prefs1Fragment()).commit();

	}

	public static class Prefs1Fragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			// Make sure default values are applied. In a real app, you would
			// want this in a shared function that is used to retrieve the
			// SharedPreferences wherever they are needed.
			PreferenceManager.setDefaultValues(getActivity(),
					R.xml.developerpreferences, false);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.developerpreferences);
		}
	}

}
