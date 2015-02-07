package de.eidottermihi.rpicheck.activity;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sheetrock.panda.changelog.ChangeLog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Constants;
import de.eidottermihi.rpicheck.activity.helper.LoggingHelper;

/**
 * Settings activity. Settings items are inflated from xml.
 * 
 * @author Michael
 * 
 */
public class SettingsActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	private static final String LOG_LOCATION = Environment
			.getExternalStorageDirectory().getPath()
			+ Constants.SD_LOCATION
			+ "rpicheck.log";
	/** Preference keys. */
	public static final String KEY_PREF_TEMPERATURE_SCALE = "pref_temperature_scala";
	public static final String KEY_PREF_QUERY_HIDE_ROOT_PROCESSES = "pref_query_hide_root";
	public static final String KEY_PREF_FREQUENCY_UNIT = "pref_frequency_unit";
	public static final String KEY_PREF_DEBUG_LOGGING = "pref_debug_log";

	private static final String KEY_PREF_LOG = "pref_log";
	private static final String KEY_PREF_CHANGELOG = "pref_changelog";
	private static final String KEY_PREF_LOAD_AVG_PERIOD = "pref_load_avg";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SettingsActivity.class);

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		// adding preference listener to log / changelog
		Preference prefLog = findPreference(KEY_PREF_LOG);
		prefLog.setOnPreferenceClickListener(this);
		Preference prefChangelog = findPreference(KEY_PREF_CHANGELOG);
		prefChangelog.setOnPreferenceClickListener(this);
		// init summary texts to reflect users choice
		this.initSummaries();
		// ancestral navigation
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void initSummaries() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		initSummary(prefs, KEY_PREF_TEMPERATURE_SCALE);
		initSummary(prefs, KEY_PREF_FREQUENCY_UNIT);
	}

	@SuppressWarnings("deprecation")
	private void initSummary(SharedPreferences prefs, String prefKey) {
		final Preference pref = findPreference(prefKey);
		final String prefValue = prefs.getString(prefKey, null);
		if (prefValue != null) {
			pref.setSummary(prefValue);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// home button is pressed
			NavUtils.navigateUpFromSameTask(this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(KEY_PREF_FREQUENCY_UNIT)) {
			initSummary(sharedPreferences, KEY_PREF_FREQUENCY_UNIT);
		}
		if (key.equals(KEY_PREF_TEMPERATURE_SCALE)) {
			initSummary(sharedPreferences, KEY_PREF_TEMPERATURE_SCALE);
		}
		if (key.equals(KEY_PREF_DEBUG_LOGGING)) {
			boolean debugEnabled = sharedPreferences.getBoolean(key, false);
			if (debugEnabled) {
				LOGGER.warn("Enabling debug logging. Be warned that the log file can get huge because of this.");
			} else {
				LOGGER.info("Disabled debug logging.");
			}
			LoggingHelper.changeLogger(debugEnabled);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		boolean clickHandled = false;
		if (preference.getKey().equals(KEY_PREF_LOG)) {
			LOGGER.trace("View log was clicked.");
			clickHandled = true;
			File log = new File(LOG_LOCATION);
			if (log.exists()) {
				final Intent intent = new Intent();
				intent.setDataAndType(Uri.fromFile(log), "text/plain");
				intent.setAction(android.content.Intent.ACTION_VIEW);
				startActivity(intent);
			} else {
				Toast.makeText(this, "Log file does not exist.",
						Toast.LENGTH_LONG).show();
			}
		} else if (preference.getKey().equals(KEY_PREF_CHANGELOG)) {
			LOGGER.trace("View changelog was clicked.");
			clickHandled = true;
			ChangeLog cl = new ChangeLog(this);
			cl.getFullLogDialog().show();
		}
		return clickHandled;
	}

}
