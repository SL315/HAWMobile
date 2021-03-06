package de.haw_landshut.hawmobile.news;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import de.haw_landshut.hawmobile.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference preference = findPreference("pref_notification_time");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int prefNotificationTime = sharedPref.getInt("pref_notification_time", 600);
        String dailyAt = getString(R.string.news_daily_at);
        String clock = getString(R.string.news_clock);
        if((prefNotificationTime%100) < 10)
            preference.setSummary(dailyAt+" " + (prefNotificationTime/100) +":0"+ (prefNotificationTime%100)+" "+clock);
        else
            preference.setSummary(dailyAt+" " + (prefNotificationTime/100) +":"+ (prefNotificationTime%100)+" "+clock);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);

        /* update summary */
        if (key.equals("pref_faculty")) {
            preference.setSummary(((ListPreference) preference).getEntry());
        }
    }
}