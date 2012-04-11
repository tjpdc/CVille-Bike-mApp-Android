/**	 CycleTracks, (c) 2009 San Francisco County Transportation Authority
 * 					  San Francisco, CA, USA
 *
 *   Licensed under the GNU GPL version 3.0.
 *   See http://www.gnu.org/licenses/gpl-3.0.txt for a copy of GPL version 3.0.
 *
 * 	 @author Billy Charlton <billy.charlton@sfcta.org>
 *
 */
package org.tjpdc.cvillebikemapp;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.tjpdc.cvillebikemapp.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class UserInfoActivity extends Activity {
	public final static int PREF_UVA_CLASSIFICATION = 1;
	public final static int PREF_UVA_AFFILIATION = 2;
	public final static int PREF_DRAWING = 3;
	public final static int PREF_NAME = 4;
	public final static int PREF_EMAIL = 5;
	public final static int PREF_CYCLING_RATING = 6;
	String uvaClassification, uvaAffiliation, drawing, name, email, cyclingRating;

	private final static int MENU_SAVE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userprefs);

		// Don't pop up the soft keyboard until user clicks!
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		//Set up UVA classification spinner
		int uvaClassPosition;
		ArrayList<String> uvaClassifications = new ArrayList<String>();
		uvaClassifications.add("Choose One...");
		uvaClassifications.add("Faculty");
		uvaClassifications.add("Staff");
		uvaClassifications.add("Undergraduate Student");
		uvaClassifications.add("Graduate Student");
		uvaClassifications.add("Postdoc");
		Spinner uvaClassSpinner = (Spinner) findViewById(R.id.spnClass);
		ArrayAdapter<String> uvaClassAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, uvaClassifications);
		uvaClassAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		uvaClassSpinner.setAdapter(uvaClassAdapter);
		uvaClassSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
		
		//Set up cycling self-rating spinner
		int cyclingRating;
		ArrayList<String> cyclingRatings = new ArrayList<String>();
		cyclingRatings.add("Choose One...");
		cyclingRatings.add("Beginner");
		cyclingRatings.add("Intermediate");
		cyclingRatings.add("Advanced");
		Spinner cyclingRatingSpinner = (Spinner) findViewById(R.id.SpinnerCyclingRating);
		ArrayAdapter<String> cyclingRatingAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cyclingRatings);
		cyclingRatingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cyclingRatingSpinner.setAdapter(cyclingRatingAdapter);
		cyclingRatingSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
		
		SharedPreferences settings = getSharedPreferences("PREFS", 0);
		Map<String, ?> prefs = settings.getAll();
		for (Entry<String, ?> p : prefs.entrySet()) {
			int key = Integer.parseInt(p.getKey());
			CharSequence value = (CharSequence) p.getValue();

			switch (key) {
			case PREF_UVA_CLASSIFICATION:
				uvaClassPosition = uvaClassifications.indexOf(settings.getString("" + PREF_UVA_CLASSIFICATION,
						null));
				uvaClassSpinner.setSelection(uvaClassPosition);
				break;
			case PREF_UVA_AFFILIATION:
				if (value.equals("Y")) {
					((RadioButton) findViewById(R.id.ButtonYes01))
							.setChecked(true);
				} else if (value.equals("N")) {
					((RadioButton) findViewById(R.id.ButtonNo01))
							.setChecked(true);
				}
				break;
			case PREF_DRAWING:

				if (value.equals("Y")) {
					((RadioButton) findViewById(R.id.ButtonYes03))
							.setChecked(true);
				} else if (value.equals("N")) {
					((RadioButton) findViewById(R.id.ButtonNo03))
							.setChecked(true);
				}
				break;
			case PREF_NAME:
				((EditText) findViewById(R.id.TextName)).setText(value);
				break;
			case PREF_EMAIL:
				((EditText) findViewById(R.id.TextEmail)).setText(value);
				break;
			case PREF_CYCLING_RATING:
				cyclingRating = cyclingRatings.indexOf(settings.getString("" + PREF_CYCLING_RATING, null));
				cyclingRatingSpinner.setSelection(cyclingRating);
				break;
			}

		}
	}

	public class MyOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {

		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.

		}
	}

	@Override
	public void onDestroy() {
		savePreferences();
		super.onDestroy();
	}

	private void savePreferences() {
		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		ArrayList<String> uvaClassifications = new ArrayList<String>();
		uvaClassifications.add("Choose One...");
		uvaClassifications.add("Faculty");
		uvaClassifications.add("Staff");
		uvaClassifications.add("Undergraduate Student");
		uvaClassifications.add("Graduate Student");
		uvaClassifications.add("Postdoc");
		
		ArrayList<String> cyclingRatings = new ArrayList<String>();
		cyclingRatings.add("Choose One...");
		cyclingRatings.add("Beginner");
		cyclingRatings.add("Intermediate");
		cyclingRatings.add("Advanced");
		
		SharedPreferences settings = getSharedPreferences("PREFS", 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString("" + PREF_UVA_CLASSIFICATION, uvaClassifications
				.get(((Spinner) findViewById(R.id.spnClass))
						.getSelectedItemPosition()));

		editor.putString("" + PREF_NAME,
				((EditText) findViewById(R.id.TextName)).getText().toString());
		editor.putString("" + PREF_EMAIL,
				((EditText) findViewById(R.id.TextEmail)).getText().toString());

		RadioGroup rbg1 = (RadioGroup) findViewById(R.id.RadioGroup01);
		editor.putString("" + PREF_UVA_AFFILIATION, "");
		if (rbg1.getCheckedRadioButtonId() == R.id.ButtonYes01)
			editor.putString("" + PREF_UVA_AFFILIATION, "Y");
		if (rbg1.getCheckedRadioButtonId() == R.id.ButtonNo01)
			editor.putString("" + PREF_UVA_AFFILIATION, "N");

		RadioGroup rbg3 = (RadioGroup) findViewById(R.id.RadioGroup03);
		editor.putString("" + PREF_DRAWING, "");
		if (rbg3.getCheckedRadioButtonId() == R.id.ButtonYes03)
			editor.putString("" + PREF_DRAWING, "Y");
		if (rbg3.getCheckedRadioButtonId() == R.id.ButtonNo03)
			editor.putString("" + PREF_DRAWING, "N");
		editor.putString("" + PREF_CYCLING_RATING, cyclingRatings.get(((Spinner)findViewById(R.id.SpinnerCyclingRating)).getSelectedItemPosition()));
		
		// Don't forget to commit your edits!!!

		editor.commit();

	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SAVE, 0, "Save").setIcon(
				android.R.drawable.ic_menu_save);
		return true;
	}

	@Override
	public void onBackPressed() {

		savePreferences();
		SharedPreferences settings = getSharedPreferences("PREFS", 0);
		uvaClassification = settings.getString("" + PREF_UVA_CLASSIFICATION, null);
		uvaAffiliation = settings.getString("" + PREF_UVA_AFFILIATION, null);
		drawing = settings.getString("" + PREF_DRAWING, null);
		name = settings.getString("" + PREF_NAME, null);
		email = settings.getString("" + PREF_EMAIL, null);
		cyclingRating = settings.getString("" + PREF_CYCLING_RATING, null);

		if (uvaAffiliation.equals("")
				|| drawing.equals("") || cyclingRating.equals("Choose One...")) {
			Toast.makeText(getBaseContext(),
					"You must anwser all the asterisk questions.",
					Toast.LENGTH_SHORT).show();

			return;
		} else if (uvaAffiliation.equals("Y") && uvaClassification.equals("Choose One...")) {
			Toast.makeText(getBaseContext(), "You must choose a classification if you are affiliated with UVA.", Toast.LENGTH_SHORT).show();
			return;
		}
		else if ((drawing.equals("Y")) && ((name.equals("")) || (email.equals("")))) {
			Toast.makeText(
					getBaseContext(),
					"If you choose to participate in the drawing, please enter your name and email address.",
					Toast.LENGTH_SHORT).show();

			return;
		} else {
			Toast.makeText(getBaseContext(), "User preferences saved.",
					Toast.LENGTH_SHORT).show();
			this.finish();
		}
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences settings = getSharedPreferences("PREFS", 0);

		switch (item.getItemId()) {
		case MENU_SAVE:
			savePreferences();
			uvaClassification = settings.getString("" + PREF_UVA_CLASSIFICATION, null);
			uvaAffiliation = settings.getString("" + PREF_UVA_AFFILIATION, null);
			drawing = settings.getString("" + PREF_DRAWING, null);
			name = settings.getString("" + PREF_NAME, null);
			email = settings.getString("" + PREF_EMAIL, null);
			cyclingRating = settings.getString("" + PREF_CYCLING_RATING, null);
			
			if (uvaAffiliation.equals("")
					|| drawing.equals("") || cyclingRating.equals("Choose One...")) {
				Toast.makeText(getBaseContext(),
						"You must anwser all the asterisk questions.",
						Toast.LENGTH_SHORT).show();

				return false;

			} else if (uvaAffiliation.equals("Y") && uvaClassification.equals("Choose One...")) {
				Toast.makeText(getBaseContext(), "You must choose a classification if you are affiliated with UVA.", Toast.LENGTH_SHORT).show();
				return false;
			}
			else if (
					drawing.equals("Y") && ( name.equals("") || email.equals(""))
						) {
				Toast.makeText(
						getBaseContext(),
						"If you choose to participate in the drawing, please enter your name and email address.",
						Toast.LENGTH_SHORT).show();

				return false;
			} else {
				Toast.makeText(getBaseContext(), "User preferences saved.",
						Toast.LENGTH_SHORT).show();
				this.finish();

			}
			return true;
		}
		return false;

	}
}
