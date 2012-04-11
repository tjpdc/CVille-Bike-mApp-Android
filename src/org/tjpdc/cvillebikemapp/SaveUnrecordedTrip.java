package org.tjpdc.cvillebikemapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.tjpdc.cvillebikemapp.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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

public class SaveUnrecordedTrip extends Activity {
	long tripid;
	DbAdapter mDb;
	Context mCtx;
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
						"<b>Class:</b> The primary reason for this trip is going to or from  a  class for which you are registered at either Texas A&M or Blinn.");
		purpDescriptions
				.put(2,
						"<b>Home/Dorm:</b> The primary reason for this trip is going to or from the location where you are living here in Bryan-College Station (on-campus dorm room, apartment, house, etc).");
		purpDescriptions
				.put(3,
						"<b>Work:</b> The primary reason for this trip is going to or from the location where you are employed, if you are employed.");
		purpDescriptions
				.put(4,
						"<b>School-related:</b> The primary reason for this trip is going to or from a non-class school related activity such as to the library, attending a study group, meeting with an advisor or prof.");
		purpDescriptions
				.put(5,
						"<b>Social/Recreation:</b> The primary reason for this trip purpose going to or from a social activity such as going to The Chicken, a restaurant, the movies or a friend’s house.");
		purpDescriptions
				.put(6,
						"<b>Shopping:</b> The primary reason for this trip is go to a retail store to buy something (e.g. food, soda, clothes, books, etc).");
		purpDescriptions
				.put(7,
						"<b>Errand:</b> The primary reason for this trip can be anything where a service is sought such as to a bank or to a Doctor’s appointment.");
		purpDescriptions
				.put(8,
						"<b>Other(Specify):</b> The primary reason for this trip is anything not related to any of the above categories.  When in doubt, specify it below and tell us the reason for the trip.");
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

		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, purposes);
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
				// ((TextView) findViewById(R.id.TextPurpDescription)).setText(
				// Html.fromHtml(purpDescriptions.get(v.getId())));
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

		// TripData trip = TripData.fetchTrip(SaveUnrecordedTrip.this, tripid);
		// trip.initializeData();

		// SaveUnrecordedTrip.this.activateSubmitButton();

		weather = "";
		purpose = "";
		prepareModeButtons();

		prepareSpinner();

		final Button btnDiscard = (Button) findViewById(R.id.ButtonDiscard);
		btnDiscard.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getBaseContext(), "Trip discarded.",
						Toast.LENGTH_SHORT).show();

				Intent i = new Intent(SaveUnrecordedTrip.this, MainInput.class);
				i.putExtra("keepme", true);
				startActivity(i);
				SaveUnrecordedTrip.this.finish();
			}
		});

		// Don't pop up the soft keyboard until user clicks!
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// Submit btn
		final Button btnSubmit = (Button) findViewById(R.id.ButtonSubmit);
		btnSubmit.setEnabled(true);
		// final Intent xi = new Intent(this, ShowMap.class);

		btnSubmit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDb = new DbAdapter(SaveUnrecordedTrip.this);
				mDb.open();
				tripid = mDb.createTrip();
				mDb.close();
				Log.e("@xxnxxn", "#1");
				TripData trip = TripData.fetchTrip(SaveUnrecordedTrip.this,
						tripid);
				Log.e("@xxnxxn", "#2");
				trip.startTime = System.currentTimeMillis();
				trip.distance = 0;
				trip.populateDetails();
				Log.e("@xxnxxn", "#3");
				EditText oWeather = (EditText) findViewById(R.id.OtherWeatherField);
				EditText opurpose = (EditText) findViewById(R.id.OtherPurposeField);
				EditText usernotes = (EditText) findViewById(R.id.NotesField);
				String otherWeather = oWeather.getEditableText().toString();
				String otherpurpose = opurpose.getEditableText().toString();
				String notes = "unrecorded  "
						+ usernotes.getEditableText().toString();

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
				if (notes.equals("unrecorded  ")) {
					Toast.makeText(
							getBaseContext(),
							"Please enter the approximate start time and end time of your unrecorded trip in the corresponding field.",
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

				String fancyStartTime = DateFormat.getInstance().format(
						trip.startTime);

				// "3.5 miles in 26 minutes"
				SimpleDateFormat sdf = new SimpleDateFormat("m");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				String minutes = sdf.format(0);
				String fancyEndInfo = String.format(
						"%1.1f miles, %s minutes.  %s",
						(0.0006212f * trip.distance), minutes, notes);
				Log.e("@xxnxxn", "#4");
				// Save the trip details to the phone database. W00t!
				trip.updateTrip(weather, purpose, fancyStartTime,
						fancyEndInfo, notes);
				trip.updateTripStatus(TripData.STATUS_COMPLETE);
				Log.e("@xxnxxn", "#5");
				// Force-drop the soft keyboard for performance
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				Log.e("@xxnxxn", "#6");
				// Now create the MainInput Activity so BACK btn works properly
				Intent i = new Intent(getApplicationContext(), MainInput.class);
				Log.e("@xxnxxn", "#7");
				startActivity(i);
				Log.e("@xxnxxn", "#8");
				// And, show the map!0

				TripUploader uploader = new TripUploader(
						SaveUnrecordedTrip.this);
				Log.e("@xxnxxn", "#12");
				uploader.execute(tripid);
				Log.e("@xxnxxn", "#13");

				SaveUnrecordedTrip.this.finish();

			}
		});

	}

}
