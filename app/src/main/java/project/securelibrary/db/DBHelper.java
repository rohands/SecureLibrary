package project.securelibrary.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	SQLiteDatabase db = this.getWritableDatabase();
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "Prepaye";
	private static final String TABLE_NAME = "others";
	private static final String KEY_NAME = "name";
	private static final String KEY_PHNO = "phno";
	private static final String KEY_MODPUBKEY = "modpubkey";
	private static final String KEY_EXPOPUBKEY = "expopubkey";
	private static final String KEY_PREVIOUS_ENCRYPTED = "previous_encrypted";
	private static final String[] COLUMNS = { KEY_PHNO, KEY_NAME,
			KEY_MODPUBKEY, KEY_EXPOPUBKEY, KEY_PREVIOUS_ENCRYPTED };

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		System.out.println("In constr of others");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public long addOther(Other user) throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		System.out.println("Name:" + user.getName());
		values.put(KEY_PHNO, user.getPhoneNumber());
		values.put(KEY_NAME, user.getName());
		values.put(KEY_MODPUBKEY, user.getModpubkey().toString());
		values.put(KEY_EXPOPUBKEY, user.getExpopubkey().toString());
		values.putNull(KEY_PREVIOUS_ENCRYPTED);
		long row = 0;
		try {
			row = db.insert(TABLE_NAME, // table
					null, // nullColumnHack
					values);
		} catch (SQLiteException exception) {
			throw new Exception(
					"A user with the same phone number already exists.");
		}
		db.close();
		return row;
	}

	//
	// public void getList() {
	// Cursor cursor = db.rawQuery("select * from others", null);
	// if (cursor.moveToFirst()) {
	// do {
	// Log.d("RECORD", cursor.getString(0) + ";" + cursor.getString(1));
	// } while (cursor.moveToNext());
	// }
	//
	// cursor.close();
	// db.close();
	// }

	public Other getOther(String phno) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, // a. table
				COLUMNS, // b. column names
				"phno=?", // c. selections
				new String[] { String.valueOf(phno) }, // d. selections args
				null, // e. group by
				null, // f. having
				null, // g. order by
				null); // h. limit

		if (cursor.getCount() == 0) {
			db.close();
			return null;
		} else {
			cursor.moveToFirst();
			Other user = new Other();
			user.setPhoneno(cursor.getString(0));
			user.setName(cursor.getString(1));
			user.setModpubkey(cursor.getString(2));
			user.setExpopubkey(cursor.getString(3));
			user.setPreviousEncrypted(cursor.getString(4));
			Log.d("OTHER USER", user.getName());
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			db.close();
			return user;
		}
	}

	public int updateOther(Other user) throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_PREVIOUS_ENCRYPTED, user.getPreviousEncrypted());
		int i = db.update(TABLE_NAME, // table
				values, // column/value
				KEY_PHNO + " = ?", // selections
				new String[] { String.valueOf(user.getPhoneNumber()) });
		db.close();
		return i;

	}

	public List<Other> getAllContacts() {
		List<Other> contactList = new ArrayList<Other>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_NAME;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Other contact = new Other();
				// contact.setID(Integer.parseInt(cursor.getString(0)));
				contact.setName(cursor.getString(1));
				contact.setPhoneno(cursor.getString(0));
				// Adding contact to list
				contactList.add(contact);
			} while (cursor.moveToNext());
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		} else {
			db.close();
			return null;
		}
		db.close();
		return contactList;
	}

	public Other getOtherByName(String name) {

		String selectQuery = "SELECT phno FROM " + TABLE_NAME
				+ " where name= '" + name + "'";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.getCount() == 0) {
			db.close();
			return null;
		} else {
			cursor.moveToFirst();
			Other user = new Other();
			user.setPhoneno(cursor.getString(0));
			// user.setName(cursor.getString(1));
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			db.close();
			return user;
		}
	}
}
