package project.securelibrary.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelperSelf extends SQLiteOpenHelper {
	SQLiteDatabase db = this.getWritableDatabase();
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "Prepaye";
	private static final String TABLE_NAME = "self";
	private static final String KEY_NAME = "name";
	private static final String KEY_PHNO = "phno";
	private static final String KEY_MODPUBKEY = "modpubkey";
	private static final String KEY_EXPOPUBKEY = "expopubkey";
	private static final String KEY_MODPRIKEY = "modprikey";
	private static final String KEY_EXPOPRIKEY = "expoprikey";
	private static final String[] COLUMNS = { KEY_PHNO, KEY_NAME,
			KEY_MODPUBKEY, KEY_EXPOPUBKEY, KEY_MODPRIKEY, KEY_EXPOPRIKEY };

	public DBHelperSelf(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		System.out.println("In constr of self");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_SELF_TABLE = "CREATE TABLE IF NOT EXISTS self ( "
				+ "phno TEXT PRIMARY KEY, " + "name TEXT, " + "modpubkey TEXT,"
				+ "expopubkey TEXT," + "modprikey TEXT," + "expoprikey TEXT)";
		System.out.println(CREATE_SELF_TABLE);
		db.execSQL(CREATE_SELF_TABLE);
		String CREATE_OTHERS_TABLE = "CREATE TABLE IF NOT EXISTS others ( "
				+ "phno TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, "
				+ "modpubkey TEXT NOT NULL,expopubkey TEXT NOT NULL,"
				+ "previous_encrypted TEXT DEFAULT NULL)";
		System.out.println(CREATE_OTHERS_TABLE);
		db.execSQL(CREATE_OTHERS_TABLE);
		// db.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS self");
		db.execSQL("DROP TABLE IF EXISTS others");
		this.onCreate(db);
	}

	public long addSelf(Self user) throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(KEY_PHNO, user.getPhoneNumber());
		values.put(KEY_NAME, user.getName());
		values.put(KEY_MODPUBKEY, user.getModpubkey().toString());
		values.put(KEY_EXPOPUBKEY, user.getExpopubkey().toString());
		values.put(KEY_MODPRIKEY, user.getModprikey().toString());
		values.put(KEY_EXPOPRIKEY, user.getExpoprikey().toString());
		long row = db.insert(TABLE_NAME, // table
				null, // nullColumnHack
				values); // key/value -> keys = column names/ values = column
		System.out.println("ROW!!!!" + row);
		getSelf();
		db.close();
		return row;
	}

	public Self getSelf() {
		Self user = null;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, // a. table
				COLUMNS, // b. column names
				null, // c. selections
				null, // d. selections args
				null, // e. group by
				null, // f. having
				null, // g. order by
				null); // h. limit
		System.out.println("CURSOR" + cursor);
		if (cursor.getCount() == 0) {
			db.close();
			return null;
		} else {
			cursor.moveToFirst();
			user = new Self();
			System.out.println("CURSOR STUFF " + cursor.getString(0));
			user.setPhoneno(cursor.getString(0));
			user.setName(cursor.getString(1));
			user.setModpubkey(cursor.getString(2));
			user.setExpopubkey(cursor.getString(3));
			user.setModprikey(cursor.getString(4));
			user.setExpoprikey(cursor.getString(5));
		}
		System.out.println(user.getName());
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		db.close();
		return user;
	}
}
