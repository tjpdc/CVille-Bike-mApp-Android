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

import org.tjpdc.cvillebikemapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainInput extends Activity {
	private final static int MENU_USER_INFO = 0;
	private final static int MENU_HELP = 1;

	private final static int CONTEXT_RETRY = 0;
	private final static int CONTEXT_DELETE = 1;

	DbAdapter mDb;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Let's handle some launcher lifecycle issues:

		// If we're recording or saving right now, jump to the existing
		// activity.
		// (This handles user who hit BACK button while recording)
		setContentView(R.layout.main);

		Intent rService = new Intent(this, RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				int state = rs.getState();
				if (state > RecordingService.STATE_IDLE) {
					if (state == RecordingService.STATE_FULL) {
						startActivity(new Intent(MainInput.this, SaveTrip.class));
					} else { // RECORDING OR PAUSED:
						startActivity(new Intent(MainInput.this,
								RecordingActivity.class));
					}
					MainInput.this.finish();
				} else {
					// Idle. First run? Switch to user prefs screen if there are
					// no prefs stored yet
					SharedPreferences settings = getSharedPreferences("PREFS",
							0);
					if (settings.getAll().isEmpty()) {
						showWelcomeDialog();
					}
					// Not first run - set up the list view of saved trips
					ListView listSavedTrips = (ListView) findViewById(R.id.ListSavedTrips);
					populateList(listSavedTrips);
				}
				MainInput.this.unbindService(this); // race? this says we no
													// longer care
			}
		};
		// This needs to block until the onServiceConnected (above) completes.
		// Thus, we can check the recording status before continuing on.
		bindService(rService, sc, Context.BIND_AUTO_CREATE);

		// And set up the record button
		final Button startButton = (Button) findViewById(R.id.ButtonStart);
		final Intent i = new Intent(this, RecordingActivity.class);

		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// final LocationListener ll = new MyLocationListener();

		startButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Before we go to record, check GPS status

				if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					buildAlertMessageNoGps();

				} else {

					startActivity(i);

					MainInput.this.finish();

				}
			}

		});

	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your phone's GPS is disabled. C'Ville Bike mApp needs GPS to determine your location.\n\nGo to System Settings now to enable GPS?")
				.setCancelable(false)
				.setPositiveButton("GPS Settings...",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								final ComponentName toLaunch = new ComponentName(
										"com.android.settings",
										"com.android.settings.SecuritySettings");
								final Intent intent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								intent.addCategory(Intent.CATEGORY_LAUNCHER);
								intent.setComponent(toLaunch);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivityForResult(intent, 0);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void showWelcomeDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// builder.setMessage("The purpose of this form is to provide you (as a prospective research study participant) information that may affect your decision as to whether or not to participate in this research. You have been asked to participate in a research study to determine the effect TAMU students have on traffic flow in Bryan/College Station. The purpose of this study is to observe and analyze student movement on and off campus in order to supplement traffic modeling by the Bryan-College Station Metropolitan Planning Organization (BCSMPO). Our hope is that with these data, better transportation planning can be made for future generations of Aggies. You were selected to be a possible participant because you are a TAMU student (graduate or undergraduate) who lives either on or off campus. This study is in partnership with the BCSMPO.What will I be asked to do?If you agree to participate in this study, you will be asked to document your travel on, around, and off campus. This study will take two days to complete. On each day you will document your travel in sequential order (from waking up to going to bed). The survey can be done either on a smartphone or with a paper survey form.What are the risks involved in this study?The risks associated with this study are minimal, and are not greater than risks ordinarily encountered in daily life.What are the possible benefits of this study?You will receive no other direct benefit from participating in this study; however the possible benefit to society is improved traffic flow in and around the university. In addition, traffic planners in college towns around the country will benefit from the increased knowledge and understanding of student travel behavior.Will I be compensated for participating?Five gift cards for $100 each will be awarded to randomly selected participants. If you wish to enter the raffle for the gift cards, add your name and phone number to the paper survey or fill in the contact information section of the smartphone survey. If you wish to participate in the survey without entering the raffle you may do so.Do I have to participate?No.  Your participation is voluntary.  You may decide not to participate or withdraw at any time without your current or future relations with Texas A&M University or the Bryan-College\n\n Station Metropolitan Planning \nOrganization being affected.  ")
		// builder.setMessage("Welcome to AggieTracks! AggieTracks is an application for Texas A&M University URSC 493 Transportation Research Project Travel Survey. Please read the following information to decide whether you want to participate in this survey or not. If you are willing to proceed, click PROCEED button below, and you will be directed to a user information page where you can enter your personal details.")
		builder.setMessage(
				//"Welcome to C'VilleTrack! C'VilleTrack is an application for Texas A&M University URSC 493 Transportation Research Project Travel Survey. Please read the following information to decide whether you want to participate in this survey or not. If you are willing to proceed, click PROCEED button below, and you will be directed to a user information page where you can enter your personal details.\n\nIntroduction\n\nThe purpose of this form is to provide you (as a prospective research study participant) information that may affect your decision as to whether or not to participate in this research.\n\nYou have been asked to participate in a research study to determine the effect TAMU students have on traffic flow in Bryan/College Station. The purpose of this study is to observe and analyze student movement on and off campus in order to supplement traffic modeling by the Bryan-College Station Metropolitan Planning Organization (BCSMPO). Our hope is that with these data, better transportation planning can be made for future generations of Aggies. You were selected to be a possible participant because you are a TAMU student (graduate or undergraduate) who lives either on or off campus. This study is in partnership with the BCSMPO.\n\nWhat will I be asked to do?\n\nIf you agree to participate in this study, you will be asked to document your travel on, around, and off campus. This study will take two days to complete. On each day you will document your travel in sequential order (from waking up to going to bed). The survey can be done either on a smartphone or with a paper survey form.\n\nWhat are the risks involved in this study?\n\nThe risks associated with this study are minimal, and are not greater than risks ordinarily encountered in daily life.\n\nWhat are the possible benefits of this study?\n\nYou will receive no other direct benefit from participating in this study; however the possible benefit to society is improved traffic flow in and around the university. In addition, traffic planners in college towns around the country will benefit from the increased knowledge and understanding of student travel behavior.\n\nWill I be compensated for participating?\n\nFive gift cards for $100 each will be awarded to randomly selected participants. If you wish to enter the raffle for the gift cards, add your name and phone number to the paper survey or fill in the contact information section of the smartphone survey. If you wish to participate in the survey without entering the raffle you may do so.\n\nDo I have to participate?\n\nNo.  Your participation is voluntary.  You may decide not to participate or withdraw at any time without your current or future relations with Texas A&M University or the Bryan-College Station Metropolitan Planning Organization being affected.\n\nWho will know about my participation in this research study?\n\nThis study is confidential and the records of this study will be kept private.  No identifiers linking you to this study will be included in any sort of report that might be published.  Research records will be stored securely and only the survey administrators will have access to the records. When the raffle is finished, all contact information will be removed from the data and the original records will be destroyed.\n\nWhom do I contact with questions about the research?\n\nIf you have questions regarding this study, you may contact Dr. Carla Prater at 979-862-3970, email address Carla@archone.tamu.edu or Dr. Forster Ndubisi, 979-845-1019, fndubisi@arch.tamu.edu.\n\nWhom do I contact about my rights as a research participant?\n\nThis research study has been reviewed by the Human Subjects’ Protection Program and/or the Institutional Review Board at Texas A&M University.  For research-related problems or questions regarding your rights as a research participant, you can contact these offices at (979)458-4067 or irb@tamu.edu.\n\nParticipation\n\nPlease be sure you have read the above information, asked questions and received answers to your satisfaction.  If you would like to be in the study:\n\nUse the paper survey form to document your travel on two days, then drop the completed survey off at Langford Architecture Center C 106.\n\nOR\n\nUse the AggieTrack app for Iphone or Android phones to document your travel behavior for two days. The app can be downloaded at the Apple or Android app store.")
				"Welcome to CVille Bike mApp! CVille Bike mApp is an application for the Thomas Jefferson Planning District Transportation Bike Route Survey, with Bike Charlottesville. The app can be downloaded at the Apple or Android app store.")
				.setCancelable(false)
				.setTitle("Information Sheet")
				.setNegativeButton("QUIT",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								MainInput.this.finish();
							}

						})
				.setPositiveButton("PROCEED",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								startActivity(new Intent(MainInput.this,
										UserInfoActivity.class));
							}
						});

		final AlertDialog alert = builder.create();
		alert.show();
	}

	void populateList(ListView lv) {
		// Get list from the real phone database. W00t!
		DbAdapter mDb = new DbAdapter(MainInput.this);
		mDb.open();

		// Clean up any bad trips & coords from crashes
		int cleanedTrips = mDb.cleanTables();
		if (cleanedTrips > 0) {
			Toast.makeText(getBaseContext(),
					"" + cleanedTrips + " bad trip(s) removed.",
					Toast.LENGTH_SHORT).show();
		}

		try {
			Cursor allTrips = mDb.fetchAllTrips();

			SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
					R.layout.twolinelist, allTrips, new String[] { "weather",
							"purpose", "fancystart", "fancyinfo" }, new int[] {
							R.id.TextView01, R.id.TextPurp, R.id.TextView03,
							R.id.TextInfo });

			lv.setAdapter(sca);
			TextView counter = (TextView) findViewById(R.id.TextViewPreviousTrips);

			int numtrips = allTrips.getCount();
			switch (numtrips) {
			case 0:
				counter.setText("No saved trips.");
				break;
			case 1:
				counter.setText("1 saved trip:");
				break;
			default:
				counter.setText("" + numtrips + " saved trips:");
			}
			allTrips.close();
		} catch (SQLException sqle) {
			// Do nothing, for now!
		}
		mDb.close();

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				Intent i = new Intent(MainInput.this, ShowMap.class);
				i.putExtra("showtrip", id);
				startActivity(i);
			}
		});
		registerForContextMenu(lv);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_RETRY, 0, "Retry Upload");
		menu.add(0, CONTEXT_DELETE, 0, "Delete");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case CONTEXT_RETRY:
			retryTripUpload(info.id);
			return true;
		case CONTEXT_DELETE:
			deleteTrip(info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void retryTripUpload(long tripId) {
		TripUploader uploader = new TripUploader(MainInput.this);
		uploader.execute(tripId);
	}

	private void deleteTrip(long tripId) {
		DbAdapter mDbHelper = new DbAdapter(MainInput.this);
		mDbHelper.open();
		mDbHelper.deleteAllCoordsForTrip(tripId);
		mDbHelper.deleteTrip(tripId);
		mDbHelper.close();
		ListView listSavedTrips = (ListView) findViewById(R.id.ListSavedTrips);
		listSavedTrips.invalidate();
		populateList(listSavedTrips);
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_HELP, 0, "Help and FAQ").setIcon(
				android.R.drawable.ic_menu_help);
		menu.add(0, MENU_USER_INFO, 0, "Edit User Info").setIcon(
				android.R.drawable.ic_menu_edit);
		return true;
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_USER_INFO:
			startActivity(new Intent(this, UserInfoActivity.class));
			return true;
		case MENU_HELP:
			Intent myIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://mnemia.dyndns.org/tracks/"));
			startActivity(myIntent);
			return true;
		}
		return false;
	}
}

/*class FakeAdapter extends SimpleAdapter {
	public FakeAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
	}

}*/
