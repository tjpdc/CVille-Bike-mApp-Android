/**	 CycleTracks, Copyright 2009,2010 San Francisco County Transportation Authority
 *                                    San Francisco, CA, USA
 *
 * 	 @author Billy Charlton <billy.charlton@sfcta.org>
 *
 *   This file is part of CycleTracks.
 *
 *   CycleTracks is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tjpdc.cvillebikemapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.tjpdc.cvillebikemapp.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SaveTrip extends Activity {
	long tripid;
	HashMap<Integer, ToggleButton> weatherButtons = new HashMap<Integer, ToggleButton>();
	String weather = "";
	String purpose = "";

	int position;
	HashMap<Integer, String> purpDescriptions = new HashMap<Integer, String>();

	// Set up the purpose buttons to be one-click only
	void prepareModeButtons() {
		weatherButtons.put(R.id.ToggleSunny,
				(ToggleButton) findViewById(R.id.ToggleSunny));
		weatherButtons.put(R.id.ToggleRaining,
				(ToggleButton) findViewById(R.id.ToggleRaining));
		weatherButtons.put(R.id.ToggleSnowing,
				(ToggleButton) findViewById(R.id.ToggleSnowing));
		weatherButtons.put(R.id.ToggleOther,
				(ToggleButton) findViewById(R.id.ToggleOther));

		CheckListener cl = new CheckListener();
		for (Entry<Integer, ToggleButton> e : weatherButtons.entrySet()) {
			e.getValue().setOnCheckedChangeListener(cl);
		}

	}

	void prepareSpinner() {
		Spinner spinner1 = (Spinner) findViewById(R.id.spnPurpose);
		purpDescriptions.put(0, "");
		purpDescriptions
				.put(1,
						"<b>Class:</b> The primary reason for this trip is going to or from a class.");
		purpDescriptions
				.put(2,
						"<b>Home/Dorm:</b> The primary reason for this trip is going to or from the location where you live.");
		purpDescriptions
				.put(3,
						"<b>Work:</b> The primary reason for this trip is going to or from the location where you are employed, if you are employed.");
		purpDescriptions
				.put(4,
						"<b>Exercise:</b> The primary reason for this trip is exercise.");
		purpDescriptions
				.put(5,
						"<b>Social/Rec.:</b> The primary reason for this trip is social (e.g., visiting a friend).");
		purpDescriptions
				.put(6,
						"<b>Shopping:</b> The primary reason for this trip is go to a retail store to buy something (e.g. food, soda, clothes, books, etc).");
		purpDescriptions
				.put(7,
						"<b>Errand:</b> The primary reason for this trip can be anything where a service is sought such as to a bank errand or a doctor’s appointment.");
		purpDescriptions
				.put(8,
						"<b>Other(Specify):</b> The primary reason for this trip is anything not related to any of the above categories.  When in doubt, specify it below and tell us the reason for the trip.");
		ArrayList<String> purposes = new ArrayList<String>();
		purposes.add("Choose One...");
		purposes.add("Class");
		purposes.add("Home/Dorm");
		purposes.add("Work");
		purposes.add("Exercise");
		purposes.add("Social/Rec.");
		purposes.add("Shopping");
		purposes.add("Errand");
		purposes.add("Other");

		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, purposes);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter1);

		spinner1.setOnItemSelectedListener(new MyOnItemSelectedListener());

	}

	// Called every time a purp togglebutton is changed:
	class CheckListener implements CompoundButton.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
			// First, uncheck all purp buttons
			if (isChecked) {
				for (Entry<Integer, ToggleButton> e : weatherButtons.entrySet()) {
					e.getValue().setChecked(false);
				}
				v.setChecked(true);
				weather = v.getText().toString();

			}
		}
	}

	public class MyOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			ArrayList<String> purposes = new ArrayList<String>();
			purposes.add("Choose One...");
			purposes.add("Class");
			purposes.add("Home/Dorm");
			purposes.add("Work");
			purposes.add("School-related");
			purposes.add("Social/Recreation");
			purposes.add("Shopping");
			purposes.add("Errand");
			purposes.add("Other(Specify)");
			position = parent.getSelectedItemPosition();
			purpose = purposes.get(position);
			((TextView) findViewById(R.id.TextPurpDescription)).setText(Html
					.fromHtml(purpDescriptions.get(position)));

		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.save);

		finishRecording();

		// Set up trip purpose buttons
		weather = "";
		purpose = "";
		prepareModeButtons();

		prepareSpinner();

		final Button btnDiscard = (Button) findViewById(R.id.ButtonDiscard);
		btnDiscard.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getBaseContext(), "Trip discarded.",
						Toast.LENGTH_SHORT).show();

				cancelRecording();

				Intent i = new Intent(SaveTrip.this, MainInput.class);
				i.putExtra("keepme", true);
				startActivity(i);
				SaveTrip.this.finish();
			}
		});

		// Submit btn
		final Button btnSubmit = (Button) findViewById(R.id.ButtonSubmit);
		btnSubmit.setEnabled(false);

		// Don't pop up the soft keyboard until user clicks!
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	// submit btn is only activated after the service.finishedRecording() is
	// completed.
	void activateSubmitButton() {
		final Button btnSubmit = (Button) findViewById(R.id.ButtonSubmit);
		final Intent xi = new Intent(this, ShowMap.class);
		btnSubmit.setEnabled(true);

		btnSubmit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				TripData trip = TripData.fetchTrip(SaveTrip.this, tripid);
				trip.populateDetails();
				EditText oWeather = (EditText) findViewById(R.id.OtherWeatherField);
				EditText opurpose = (EditText) findViewById(R.id.OtherPurposeField);

				String otherWeather = oWeather.getEditableText().toString();
				String otherpurpose = opurpose.getEditableText().toString();

				// Make sure trip purpose has been selected
				if (weather.equals("")) {
					// Oh no! No trip purpose!
					Toast.makeText(
							getBaseContext(),
							"You must select weather! Choose from the options above.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (purpose.equals("Choose One...")) {
					// Oh no! No trip purpose!
					Toast.makeText(
							getBaseContext(),
							"You must select purpose of transportation! Choose from the options above.",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if ((weather.equals("Other(Specify)"))
						&& (otherWeather.equals(""))) {
					Toast.makeText(
							getBaseContext(),
							"Since you choose 'other', plese specify the weather.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if ((purpose.equals("Other(Specify)"))
						&& (otherpurpose.equals(""))) {
					Toast.makeText(
							getBaseContext(),
							"Since you choose 'other', plese specify the purpose of transportation.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if ((((weather.equals("Other(Specify)")) && !(otherWeather
						.equals(""))))
						| (((purpose.equals("Other(Specify)")) && !(otherpurpose
								.equals(""))))) {
					weather = weather + "  " + otherWeather;
					purpose = purpose + "  " + otherpurpose;
				}

				EditText usernotes = (EditText) findViewById(R.id.NotesField);
				String notes = usernotes.getEditableText().toString();

				String fancyStartTime = DateFormat.getInstance().format(
						trip.startTime);

				// "3.5 miles in 26 minutes"
				SimpleDateFormat sdf = new SimpleDateFormat("m");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				String minutes = sdf.format(trip.endTime - trip.startTime);
				String fancyEndInfo = String.format(
						"%1.1f miles, %s minutes.  %s",
						(0.0006212f * trip.distance), minutes, notes);

				// Save the trip details to the phone database. W00t!
				trip.updateTrip(weather, purpose, fancyStartTime,
						fancyEndInfo, notes);
				trip.updateTripStatus(TripData.STATUS_COMPLETE);
				resetService();

				// Force-drop the soft keyboard for performance
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

				// Now create the MainInput Activity so BACK btn works properly
				// Intent i = new Intent(getApplicationContext(),
				// MainInput.class);

				Intent i = new Intent(getApplicationContext(), MainInput.class);
				startActivity(i);

				// And, show the map!
				xi.putExtra("showtrip", trip.tripid);
				xi.putExtra("uploadTrip", true);
				startActivity(xi);
				SaveTrip.this.finish();
			}
		});

	}

	void cancelRecording() {
		Intent rService = new Intent(this, RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				rs.cancelRecording();
				unbindService(this);
			}
		};
		// This should block until the onServiceConnected (above) completes.
		bindService(rService, sc, Context.BIND_AUTO_CREATE);
	}

	void resetService() {
		Intent rService = new Intent(this, RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				rs.reset();
				unbindService(this);
			}
		};
		// This should block until the onServiceConnected (above) completes.
		bindService(rService, sc, Context.BIND_AUTO_CREATE);
	}

	void finishRecording() {
		Intent rService = new Intent(this, RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				tripid = rs.finishRecording();
				SaveTrip.this.activateSubmitButton();
				unbindService(this);
			}
		};
		// This should block until the onServiceConnected (above) completes.
		bindService(rService, sc, Context.BIND_AUTO_CREATE);
	}
}
