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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.Toast;

public class TripUploader extends AsyncTask<Long, Integer, Boolean> {
	Context mCtx;
	DbAdapter mDb;
	public static final String TRIP_COORDS_TIME = "rec";
	public static final String TRIP_COORDS_LAT = "lat";
	public static final String TRIP_COORDS_LON = "lon";
	public static final String TRIP_COORDS_ALT = "alt";
	public static final String TRIP_COORDS_SPEED = "spd";
	public static final String TRIP_COORDS_HACCURACY = "hac";
	public static final String TRIP_COORDS_VACCURACY = "vac";

	public static final String USER_UVA_CLASSIFICATION = "uvaClassification";
	public static final String USER_UVA_AFFILIATION = "uvaAffiliated";
	public static final String USER_DRAWING = "enterDrawing";
	public static final String USER_NAME = "name";
	public static final String USER_EMAIL = "email";
	public static final String USER_CYCLING_LEVEL = "cyclingLevel";

	public static final String TRIP_COORDS = "coords";
	public static final String TRIP_USER = "user";
	public static final String TRIP_DEVICE = "device";
	public static final String TRIP_NOTES = "notes";
	public static final String TRIP_WEATHER = "weather";
	public static final String TRIP_PURPOSE = "purpose";
	public static final String TRIP_START = "start";
	public static final String TRIP_END = "end";
	public static final String TRIP_VERSION = "version";
	
	//final String postUrl = "http://mnemia.dyndns.org/tracks/index_new.php?";
	final String postUrl = "http://www.cvillebikemapp.com/tracks/index_new.php?";
	
	public TripUploader(Context ctx) {
		super();
		this.mCtx = ctx;
		this.mDb = new DbAdapter(this.mCtx);

	}

	private JSONObject getCoordsJSON(long tripId) throws JSONException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		mDb.openReadOnly();
		Cursor tripCoordsCursor = mDb.fetchAllCoordsForTrip(tripId);

		// ********************************************
		// requirement to send maximum 100 trip points

		int pointsCount = tripCoordsCursor.getCount();
		Vector<Integer> indexesToSend = new Vector<Integer>();
		if (pointsCount > 100) {
			Random generator = new Random();

			for (int i = 0; i < 98; i++) {
				int newIndex =  (generator.nextInt(pointsCount - 2) + 1);
				//Integer newIndex = new Integer(
				//		(int) (((generator.nextInt(pointsCount - 2) + 1))));

				if (!indexesToSend.contains(newIndex)) {

					indexesToSend.add(newIndex);
				} else
					i--;
			}
			indexesToSend.add(0);
			indexesToSend.add(pointsCount - 1);
			Collections.sort(indexesToSend);
		}
		// ********************************************

		// Build the map between JSON fieldname and phone db fieldname:
		Map<String, Integer> fieldMap = new HashMap<String, Integer>();
		fieldMap.put(TRIP_COORDS_TIME,
				tripCoordsCursor.getColumnIndex(DbAdapter.K_POINT_TIME));
		fieldMap.put(TRIP_COORDS_LAT,
				tripCoordsCursor.getColumnIndex(DbAdapter.K_POINT_LAT));
		fieldMap.put(TRIP_COORDS_LON,
				tripCoordsCursor.getColumnIndex(DbAdapter.K_POINT_LGT));
		fieldMap.put(TRIP_COORDS_ALT,
				tripCoordsCursor.getColumnIndex(DbAdapter.K_POINT_ALT));
		fieldMap.put(TRIP_COORDS_SPEED,
				tripCoordsCursor.getColumnIndex(DbAdapter.K_POINT_SPEED));
		fieldMap.put(TRIP_COORDS_HACCURACY,
				tripCoordsCursor.getColumnIndex(DbAdapter.K_POINT_ACC));
		fieldMap.put(TRIP_COORDS_VACCURACY,
				tripCoordsCursor.getColumnIndex(DbAdapter.K_POINT_ACC));

		// Build JSON objects for each coordinate:
		JSONObject tripCoords = new JSONObject();
		int index = 0;
		while (!tripCoordsCursor.isAfterLast()) {

			// ********************************************
			// requirement to send maximum 100 trip points
			if (pointsCount > 100)
				tripCoordsCursor.moveToPosition(((Integer) indexesToSend
						.get(index)).shortValue());
			// ********************************************

			JSONObject coord = new JSONObject();

			coord.put(TRIP_COORDS_TIME, df.format(tripCoordsCursor
					.getDouble(fieldMap.get(TRIP_COORDS_TIME))));
			coord.put(
					TRIP_COORDS_LAT,
					tripCoordsCursor.getDouble(fieldMap.get(TRIP_COORDS_LAT)) / 1E6);
			coord.put(
					TRIP_COORDS_LON,
					tripCoordsCursor.getDouble(fieldMap.get(TRIP_COORDS_LON)) / 1E6);
			coord.put(TRIP_COORDS_ALT,
					tripCoordsCursor.getDouble(fieldMap.get(TRIP_COORDS_ALT)));
			coord.put(TRIP_COORDS_SPEED,
					tripCoordsCursor.getDouble(fieldMap.get(TRIP_COORDS_SPEED)));
			coord.put(TRIP_COORDS_HACCURACY, tripCoordsCursor
					.getDouble(fieldMap.get(TRIP_COORDS_HACCURACY)));
			coord.put(TRIP_COORDS_VACCURACY, tripCoordsCursor
					.getDouble(fieldMap.get(TRIP_COORDS_VACCURACY)));

			tripCoords.put(coord.getString("rec"), coord);
			index++;
			tripCoordsCursor.moveToNext();
		}
		tripCoordsCursor.close();
		mDb.close();
		return tripCoords;
	}

	private JSONObject getUserJSON() throws JSONException {
		JSONObject user = new JSONObject();
		Map<String, Integer> fieldMap = new HashMap<String, Integer>();
		fieldMap.put(USER_UVA_CLASSIFICATION, new Integer(UserInfoActivity.PREF_UVA_CLASSIFICATION));
		fieldMap.put(USER_UVA_AFFILIATION, new Integer(UserInfoActivity.PREF_UVA_AFFILIATION));
		fieldMap.put(USER_DRAWING, new Integer(UserInfoActivity.PREF_DRAWING));
		fieldMap.put(USER_NAME, new Integer(UserInfoActivity.PREF_NAME));
		fieldMap.put(USER_EMAIL, new Integer(UserInfoActivity.PREF_EMAIL));
		fieldMap.put(USER_CYCLING_LEVEL, new Integer(UserInfoActivity.PREF_CYCLING_RATING));
		
		SharedPreferences settings = this.mCtx.getSharedPreferences("PREFS", 0);
		for (Entry<String, Integer> entry : fieldMap.entrySet()) {
			user.put(entry.getKey(),
					settings.getString(entry.getValue().toString(), null));
		}

		return user;
	}

	private Vector<String> getTripData(long tripId) {
		Vector<String> tripData = new Vector<String>();
		mDb.openReadOnly();
		Cursor tripCursor = mDb.fetchTrip(tripId);

		String note = tripCursor.getString(tripCursor
				.getColumnIndex(DbAdapter.K_TRIP_NOTE));
		String weather = tripCursor.getString(tripCursor
				.getColumnIndex(DbAdapter.K_TRIP_WEATHER));
		String purp = tripCursor.getString(tripCursor
				.getColumnIndex(DbAdapter.K_TRIP_PURP));
		Double startTime = tripCursor.getDouble(tripCursor
				.getColumnIndex(DbAdapter.K_TRIP_START));
		Double endTime = tripCursor.getDouble(tripCursor
				.getColumnIndex(DbAdapter.K_TRIP_END));
		tripCursor.close();
		mDb.close();

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		tripData.add(note);
		tripData.add(weather);
		tripData.add(purp);
		tripData.add(df.format(startTime));
		tripData.add(df.format(endTime));

		return tripData;
	}

	public String getDeviceId() {
		String androidId = System.getString(this.mCtx.getContentResolver(),
				System.ANDROID_ID);
		String androidBase = "androidDeviceId-";

		if (androidId == null) { // This happens when running in the Emulator
			final String emulatorId = "android-RunningAsTestingDeleteMe";
			return emulatorId;
		}
		String deviceId = androidBase.concat(androidId);
		return deviceId;
	}

	private List<NameValuePair> getPostData(long tripId) throws JSONException {
		JSONObject coords = getCoordsJSON(tripId);
		JSONObject user = getUserJSON();
		String deviceId = getDeviceId();
		Vector<String> tripData = getTripData(tripId);
		String notes = tripData.get(0);
		String weather = tripData.get(1);
		String purp = tripData.get(2);
		String startTime = tripData.get(3);
		String endTime = tripData.get(4);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair(TRIP_COORDS, coords.toString()));
		nameValuePairs.add(new BasicNameValuePair(TRIP_USER, user.toString()));
		nameValuePairs.add(new BasicNameValuePair(TRIP_DEVICE, deviceId));
		nameValuePairs.add(new BasicNameValuePair(TRIP_NOTES, notes));
		nameValuePairs.add(new BasicNameValuePair(TRIP_WEATHER, weather));
		nameValuePairs.add(new BasicNameValuePair(TRIP_PURPOSE, purp));
		nameValuePairs.add(new BasicNameValuePair(TRIP_START, startTime));
		nameValuePairs.add(new BasicNameValuePair(TRIP_END, endTime));
		nameValuePairs.add(new BasicNameValuePair(TRIP_VERSION, "2"));

		return nameValuePairs;
	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	boolean uploadOneTrip(long currentTripId) {
		boolean result = false;

		List<NameValuePair> nameValuePairs;
		try {
			nameValuePairs = getPostData(currentTripId);
		} catch (JSONException e) {
			e.printStackTrace();
			return result;
		}
		Log.v("PostData", nameValuePairs.toString());

		HttpClient client = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(postUrl);

		try {
			postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(postRequest);
			String responseString = convertStreamToString(response.getEntity()
					.getContent());
			Log.v("httpResponse", responseString);

			JSONObject responseData = new JSONObject(responseString);

			if (responseData.getString("status").equals("success")) {
				mDb.open();
				mDb.updateTripStatus(currentTripId, TripData.STATUS_SENT);
				mDb.close();
				result = true;
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();

			return false;
		} catch (IOException e) {
			e.printStackTrace();

			return false;
		} catch (JSONException e) {
			e.printStackTrace();

			return false;
		}
		return result;
	}

	@Override
	protected Boolean doInBackground(Long... tripid) {

		// First, send the trip user asked for:
		Boolean result = uploadOneTrip(tripid[0]);

		// Then, automatically try and send previously-completed trips
		// that were not sent successfully.
		Vector<Long> unsentTrips = new Vector<Long>();

		mDb.openReadOnly();
		Cursor cur = mDb.fetchUnsentTrips();
		if (cur != null && cur.getCount() > 0) {
			// pd.setMessage("Sent. You have previously unsent trips; submitting those now.");
			while (!cur.isAfterLast()) {
				unsentTrips.add(new Long(cur.getLong(0)));
				cur.moveToNext();
			}
			cur.close();
		}
		if(cur != null) {
			cur.close();
		}
		mDb.close();

		for (Long trip : unsentTrips) {
			result &= uploadOneTrip(trip);
		}
		return result;
	}

	@Override
	protected void onPreExecute() {
		Toast.makeText(mCtx.getApplicationContext(),
				"Submitting trip.  Thanks for using C'Ville Bike mApp!",
				Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		try {
			if (result) {
				Toast.makeText(mCtx.getApplicationContext(),
						"Trip uploaded successfully.", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(mCtx.getApplicationContext(), "Error Uploading",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			// Just don't toast if the view has gone out of context
		}
	}
}
