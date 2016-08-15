package project.securelibrary.seller;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import project.securelibrary.R;
import project.securelibrary.bluetooth.BluetoothReceive;
import project.securelibrary.bluetooth.BluetoothSend;
import project.securelibrary.callback.SellerCallback;
import project.securelibrary.db.Other;
import project.securelibrary.db.Self;
import project.securelibrary.generator.Generator;
import project.securelibrary.main.Base;
import project.securelibrary.scanner.Scanner;


public class SellerTransaction extends Activity {
	int duration = Toast.LENGTH_LONG;
	Context context;
	Toast toast;
	String receivedDetails = "";
	SharedPreferences pref;
	public static final String Prepaye = "Prepaye";
	Activity activity;
	Base base;
	String otherPhno = "";
	Self selfObject = null;
	String returnString = "";
	String sendvia = "QRCODE";//"BLUETOOTH";
	SellerCallback callback;
	Intent parentIntent;
	Activity first;
	String otherPhnoReceived ="";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transaction);
		this.activity = this;
		this.context = getApplicationContext();
		base = new Base(this.activity, this.context);
		first = getParent();
		Intent intent = getIntent();
		 otherPhno = intent.getStringExtra("phoneNumber");
			pref = context.getSharedPreferences(Prepaye,
					Context.MODE_PRIVATE);
			Editor editor = pref.edit();
			editor.putString("otherPhno", otherPhno);
			editor.commit();
		this.callback = (SellerCallback) intent
				.getSerializableExtra("callbackObject");
		transaction();
	}
public SellerTransaction()
{
	
}
	public SellerTransaction(Activity act, Context con)

	{
		this.activity = act;
		this.context = con;
		base = new Base(this.activity, this.context);
	}

	public void transaction() {

		if (sendvia.compareToIgnoreCase("QRCODE") == 0) {
			Intent intentScanner = new Intent(this, Scanner.class);
			intentScanner.putExtra("SCANNER_WORKING", "yes");
			activity.startActivityForResult(intentScanner, 446);
		} else {
			Intent intentScanner = new Intent(this, BluetoothReceive.class);
			intentScanner.putExtra("SCANNER_WORKING", "yes");
			activity.startActivityForResult(intentScanner, 446);
		}

	}

	public void transactionPhase1() {
		boolean conflict_flag = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		String now = new String(sdf.format(cal2.getTime()));
		// String receivedDetails = receive();
		first = getParent();
		
		if (receivedDetails != null) {
			StringTokenizer tokenizer = new StringTokenizer(receivedDetails,
					";");
			List<String> packet = new ArrayList<String>();
			while (tokenizer.hasMoreElements()) {
				packet.add((String) tokenizer.nextElement());
			}
			
			otherPhnoReceived = packet.get(0);
			if(otherPhno.equals(otherPhnoReceived) == false)
			{
				String errorMessage = "Please transact with authorised person only";
				toast = Toast.makeText(this.context, errorMessage, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
				toast.show();
				this.finish();
			}
			try {
				Other otherObject = base.readOtherDetails(otherPhnoReceived);

				if (otherObject.getPreviousEncrypted() == null
						&& packet.get(1).equals("null")) {
					String Message = "Congrats you did your first Transaction with this person";
					toast = Toast.makeText(this.context, Message, duration);
					toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
					toast.show();

				}

				List<String> encryptedData = new ArrayList<String>();
				String prevEncryptedData = "";
				for (int i = 2; i < packet.size(); i++) {
					encryptedData.add(packet.get(i));
					prevEncryptedData += packet.get(i) + ":";
				}
				prevEncryptedData = prevEncryptedData.substring(0,
						prevEncryptedData.length() - 1);

				String decryptedData = base.unsign(encryptedData, otherPhno,
						this.context);


				StringTokenizer tokenizer2 = new StringTokenizer(decryptedData,
						";");
				List<String> Data = new ArrayList<String>();
				while (tokenizer2.hasMoreElements()) {
					Data.add((String) tokenizer2.nextElement());
				}
				if((Data.get(0)).equals(otherPhno)==false)
				{
					String errorMessage = "Message was corrupted. Please try again with authorised person";
					toast = Toast.makeText(this.context, errorMessage, duration);
					toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
					toast.show();
					this.finish();
				}
				cal1.setTime(sdf.parse(Data.get(1)));
				cal2.setTime(sdf.parse(now));
				long difference;
				if ((difference = cal2.getTimeInMillis()
						- cal1.getTimeInMillis()) > 300000) {
					throw new Exception(
							"Transaction is invalid.Took too much time.");
				}
				int conflictReturn = -1;
			
				System.out.println(" Seller Tran packet" + packet.get(1));
				if ((otherObject.getPreviousEncrypted() != null || (packet.get(
						1).equals("null") == false))) {

					conflictReturn = conflictResolution(
							otherObject.getPreviousEncrypted(), packet.get(1));
					System.out.println("ConflictReturn " + conflictReturn);
					if (conflictReturn == 0 || conflictReturn == 1) {

						conflict_flag = false;

					} else if (conflictReturn == 2) {
						conflict_flag = true;

					}

				}

				else {
					otherObject.setPreviousEncrypted(prevEncryptedData);
					base.updateTable(otherObject);
				}

				pref = context.getSharedPreferences(Prepaye,
						Context.MODE_PRIVATE);
				Editor editor = pref.edit();
				editor.putString("dev_data", Data.get(2));
				editor.putBoolean("conflict_flag", conflict_flag);
				editor.putString("otherPhno", otherPhno);
				editor.putString("previousEncrypted", prevEncryptedData);
				editor.commit();

				String dataFromDeveloper;
				if (conflict_flag == false) {
					if (callback != null) {
						if (conflictReturn == 1) {
							// When buyer is right notify that seller has wrong
							// data
							// and should approve of buyer's data
//							callback.transactionConflictCallback(returnString
//									+ ";" + Data.get(2), this.context, activity);
						
							 Intent myIntent = new Intent(this,Class.forName("com.example.demoshopkeeper.CallbackImplementor"));
						      myIntent.putExtra("data", returnString+ ";" + Data.get(2));
						      myIntent.putExtra("flag", 2);
						     startActivityForResult(myIntent, 452);
						} else {
							System.out.println("Going to transaction  callback ");
							 Intent myIntent = new Intent(this,Class.forName("com.example.demoshopkeeper.CallbackImplementor"));
						      myIntent.putExtra("data", Data.get(2));
						      myIntent.putExtra("flag", 1);
						     startActivityForResult(myIntent, 451);
						}

					}
					
				} else {
					
					try {
					     Intent myIntent = new Intent(this,Class.forName("com.example.demoshopkeeper.CallbackImplementor"));
					      myIntent.putExtra("data", Data.get(2));
					      myIntent.putExtra("flag", 3);
					     startActivityForResult(myIntent, 453);
					} catch (ClassNotFoundException e) {
					     e.printStackTrace();
					}

				}
			}

			catch (Exception exception) {
				String errorMessage = "Error:" + exception;
				System.out.println("ERROR in catch " + errorMessage);
				toast = Toast.makeText(this.context, errorMessage, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
				toast.show();

			}
		}

	}

	public void transactionPhase2(String data) {
	
	

		pref = context.getSharedPreferences(Prepaye, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString("dev_data", data);
		editor.commit();
	
		pref = context.getSharedPreferences(Prepaye, Context.MODE_PRIVATE);
		boolean flag = pref.getBoolean("conflict_flag", false);
		String otherPhno = pref.getString("otherPhno", null);


		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Calendar cal = Calendar.getInstance();
		String dataToEncrypt = "";
		final String transaction;
		try {
			selfObject = base.readOwnerDetails();

		
			String myPhno = selfObject.getPhoneNumber();
			String lastTranTime = new String(sdf.format(cal.getTime()));

			Other otherObject = base.readOtherDetails(otherPhno);

			dataToEncrypt = myPhno + ";" + lastTranTime + ";" + data;

			List<String> array = base.sign(dataToEncrypt, this.context);
			String encryptedData = "";
			int i = 0;
			for (i = 0; i < array.size() - 1; i++) {
				encryptedData = encryptedData + array.get(i) + ";";
			}
			encryptedData += array.get(i);
			if (flag == false) {
				transaction = myPhno + ";null;" + encryptedData;
				
				pref = context.getSharedPreferences(Prepaye,
						Context.MODE_PRIVATE);
				Editor editor1 = pref.edit();
				editor1.putString("dataToBeEncoded", transaction);
				editor1.commit();
				if (sendvia.compareToIgnoreCase("QRCODE") == 0) {
					System.out.println("In generate " + transaction);
					Intent intent = new Intent(this, Generator.class);
					parentIntent = getIntent();
					activity.startActivityForResult(intent, 447);
				} else {
					System.out.println("In generate " + transaction);
					Intent intent = new Intent(this, BluetoothSend.class);
					parentIntent = getIntent();
					activity.startActivityForResult(intent, 447);
				}
			} else {
				transaction = myPhno + ";conflict_yes;" + encryptedData;
				String Message = "Conflict occured Please cooperate";
				toast = Toast.makeText(this.context, Message, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
				toast.show();
				pref = context.getSharedPreferences(Prepaye,
						Context.MODE_PRIVATE);
				Editor editor1 = pref.edit();
				editor1.putString("dataToBeEncoded", transaction);
				editor1.commit();
				if (sendvia.compareToIgnoreCase("QRCODE") == 0) {
					System.out.println("In generate " + transaction);
					Intent intent = new Intent(activity, Generator.class);
					// intent.putExtra("dataToBeEncoded", transaction);
					activity.startActivityForResult(intent, 448);
				} else {
					System.out.println("In bluetooth generate " + transaction);
					Intent intent = new Intent(activity, BluetoothSend.class);
					// intent.putExtra("dataToBeEncoded", transaction);
					activity.startActivityForResult(intent, 448);
				}
			}

		} catch (Exception exception) {
			String errorMessage = "Error:" + exception;
			toast = Toast.makeText(this.context, errorMessage, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
			this.finish();
			// startActivity(getIntent());
		}

	}

	public void transactionPhase3(String data) {

		boolean conflict_flag = false;
		pref = context.getSharedPreferences("Prepaye",
				Context.MODE_PRIVATE);
		String customerPhno = pref.getString("otherPhno", null);
		Editor editor = pref.edit();
		editor.putBoolean("conflict_flag", conflict_flag);
		editor.commit();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		String now = new String(sdf.format(cal2.getTime()));
		// String receivedDetails = receive();
		Log.d("Recieved from Scanner", data);
		if (data != null) {
			StringTokenizer tokenizer = new StringTokenizer(data, ";");
			List<String> packet = new ArrayList<String>();
			while (tokenizer.hasMoreElements()) {
				packet.add((String) tokenizer.nextElement());
			}
		
			otherPhnoReceived = packet.get(0);
		
			if(otherPhnoReceived.equals(customerPhno) == false)
			{
				String Message = "Transacting with wrong person";
				Toast toast = Toast.makeText(this.context, Message, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
				toast.show();
				finish();
			}
			try {
				Other otherObject = base.readOtherDetails(otherPhno);

				List<String> encryptedData = new ArrayList<String>();
				String prevEncryptedData = "";
				for (int i = 2; i < packet.size(); i++) {
					encryptedData.add(packet.get(i));
					prevEncryptedData += packet.get(i) + ":";
				}
				prevEncryptedData = prevEncryptedData.substring(0,
						prevEncryptedData.length() - 1);

				String decryptedData = base.unsign(encryptedData, otherPhno,
						this.context);

	

				StringTokenizer tokenizer2 = new StringTokenizer(decryptedData,
						";");
				List<String> Data = new ArrayList<String>();
				while (tokenizer2.hasMoreElements()) {
					Data.add((String) tokenizer2.nextElement());
				}
				cal1.setTime(sdf.parse(Data.get(1)));
				cal2.setTime(sdf.parse(now));
				long difference;
				if ((difference = cal2.getTimeInMillis()
						- cal1.getTimeInMillis()) > 300000) {
					throw new Exception(
							"Transaction is invalid.Timeout happened");
				} else {
					otherObject.setPreviousEncrypted(prevEncryptedData);
					base.updateTable(otherObject);
					System.out.println("PREVIOUS ENCRYTED"
							+ otherObject.getPreviousEncrypted());
				}
		
				String errorMessage = "Congratulations Conflict Resolved and Transaction Completed";
				toast = Toast.makeText(this.context, errorMessage, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
				toast.show();
				Intent intent = getIntent();

				intent.putExtra("dataFromTransaction", Data.get(2));
				
				setResult(Activity.RESULT_OK, intent);
				
				activity.finish();
				// startActivity(getIntent());

			}
			

			catch (Exception exception) {
				String errorMessage = "Error:" + exception;
				System.out.println(errorMessage);
				toast = Toast.makeText(this.context, errorMessage, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
				toast.show();

			}
		}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("IN BUYER RESULT");

		if (requestCode == 446) {
			
			if (resultCode == Activity.RESULT_OK) {
				
				SharedPreferences pref = this.context.getSharedPreferences(
						"Prepaye", Context.MODE_PRIVATE);
				System.out.println("PRINTING scanner result :"
						+ pref.getString("Scanner_result", "none"));
				receivedDetails = pref.getString("Scanner_result", "none");
				this.activity.finishActivity(requestCode);
				transactionPhase1();

			}
		}
		if (requestCode == 447) {
			SharedPreferences pref = context.getSharedPreferences("Prepaye",
					Context.MODE_PRIVATE);
			String dev_data = pref.getString("dev_data", "none");
			
			Intent intent = new Intent();
			intent.putExtra("dataFromTransaction", dev_data);
			if (getParent() == null) {
				setResult(Activity.RESULT_OK, intent);
			} else {
				getParent().setResult(Activity.RESULT_OK, intent);
			}
			finish();
			// startActivity(getIntent());
		}
		if (requestCode == 448) {
			if (sendvia.compareToIgnoreCase("QRCODE") == 0) {
				Intent intentScanner = new Intent(this, Scanner.class);
				intentScanner.putExtra("SCANNER_WORKING", "yes");
				activity.startActivityForResult(intentScanner, 449);
			} else {
				Intent intentScanner = new Intent(this, BluetoothReceive.class);
				intentScanner.putExtra("SCANNER_WORKING", "yes");
				activity.startActivityForResult(intentScanner, 449);
			}
		}
		if (requestCode == 449) {
			
			if (resultCode == Activity.RESULT_OK) {
				Log.d("INFORMATION", "Recieved data");

				SharedPreferences pref = this.context.getSharedPreferences(
						"Prepaye", Context.MODE_PRIVATE);
				
				receivedDetails = pref.getString("Scanner_result", "none");
				this.activity.finishActivity(requestCode);
				transactionPhase3(receivedDetails);
			}

		}
		if(requestCode == 453)
		{
			
			String devData = data.getStringExtra("data");
			transactionPhase2(devData);
			
		}
		if(requestCode == 451)
		{
			boolean val = data.getBooleanExtra("Clickno", false);
			if(val == true)
			{
				finish();
			}
			else
			{
				String devData = data.getStringExtra("data");
				SharedPreferences pref = context.getSharedPreferences("Prepaye",
						Context.MODE_PRIVATE);
				String customerPhno = pref.getString("otherPhno", null);
				String prev = pref.getString("previousEncrypted", null);
				try{
				Other otherObject = base.readOtherDetails(customerPhno);
				otherObject.setPreviousEncrypted(prev);
				base.updateTable(otherObject);
				transactionPhase2(devData);
				}
				catch(Exception ex)
				{
					finish();
				}
			}
		}
		if(requestCode == 452)
		{
			
			SharedPreferences pref = context.getSharedPreferences("Prepaye",
					Context.MODE_PRIVATE);
			String customerPhno = pref.getString("otherPhno", null);
			String prev = pref.getString("previousEncrypted", null);
			String devData = pref.getString("dev_data",null);
			try{
			Other otherObject = base.readOtherDetails(customerPhno);
			otherObject.setPreviousEncrypted(prev);
			base.updateTable(otherObject);
			transactionPhase2(devData);
			}
			catch(Exception ex)
			{
				
				finish();
			}
		}

	}

	public int conflictResolution(String myStored, String received) {
		String receivedLastTranTime = "";
		String storedLastTranTime = "";
		String receivedData = "";
		String storedData = "";

		Log.d("ERROR", "Came to conflict resolution");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		try {
			List<String> storedEncrypted = new ArrayList<String>(
					Arrays.asList(myStored.split(":")));
			String decryptedData = base.unsign(storedEncrypted, otherPhno,
					this.context);
			List<String> storedDecrypted = new ArrayList<String>(
					Arrays.asList(decryptedData.split(";")));
			storedLastTranTime = storedDecrypted.get(1);

			storedData = storedDecrypted.get(2);
			System.out.println("decrypted data from db" + storedLastTranTime
					+ " " + storedData);
		} catch (Exception ex) {
			Log.d("ERROR", "seller prev encrypted decrypt error");
			String Message = "Conflict arised in seller side";
			toast = Toast.makeText(this.context, Message, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
			return 1;

		}

		try {
			List<String> receivedEncrypted = new ArrayList<String>(
					Arrays.asList(received.split(":")));
			String decryptedData = base.unsignSelf(receivedEncrypted,
					this.context);
			List<String> receivedDecrypted = new ArrayList<String>(
					Arrays.asList(decryptedData.split(";")));
			receivedLastTranTime = receivedDecrypted.get(1);

			receivedData = receivedDecrypted.get(2);
			
		} catch (Exception ex) {
			System.out.println("error from catch " + ex);
			Log.d("ERROR", "buyer prev encrypted decrypt error");
			String Message = "Conflict arised";
			toast = Toast.makeText(this.context, Message, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
			return 2;
		}
		if (receivedData.compareTo(storedData) != 0) {
			try {
				cal1.setTime(sdf.parse(storedLastTranTime));
				cal2.setTime(sdf.parse(receivedLastTranTime));
			} catch (Exception ex) {
				System.out.println("error from catch " + ex);
				Log.d("ERROR", "error in calculating time difference");

			}
			long difference = cal1.getTimeInMillis() - cal2.getTimeInMillis();

			if (difference > 0) {
				returnString = storedData;
				// returnString = receivedData;
				return 2;

			} else if (difference < 0) {
				returnString = receivedData;
				return 1;
			} else {
				Log.d("DEBUG", "No error found in decrypt ");
				Log.d("DEBUG", "No Conflict");
				System.out.println(returnString);
			}
		}

		return 0;

	}

	// public void search() {
	// Log.d("WARNING", "calling intent");
	// Intent intent = new Intent(this, SearchFunctionality.class);
	// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	// // intent.putExtra("searchName", name);
	// activity.startActivityForResult(intent, 456);
	// }

}
