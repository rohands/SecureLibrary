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
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import project.securelibrary.callback.SellerCallback;
import project.securelibrary.db.Self;
import project.securelibrary.main.Base;


public class Seller extends Activity {
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
	String sendvia = "QRCODE";//BLUETOOTH";
	SellerCallback callback;

	public Seller( Activity act,Context mcontext) {
		this( act,mcontext, null);
	}

	public Seller( Activity act,Context mcontext, SellerCallback callback) {
		this.context = mcontext;
		this.activity = act;
		this.callback = callback;
		base = new Base(this.activity, this.context);
	}

	public void register(String name, String phno) {
		System.out.println("hello!");
		System.out.println(this.context);
		System.out.println("hey");
		base.createTable(this.context);
		String result = "";
		try {
			result = base.populateSelf(name, phno);
			toast = Toast.makeText(this.context, result, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
		} catch (Exception exception) {
		
			String errorMessage = "Error:" + exception;
			toast = Toast.makeText(this.context, errorMessage, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
		}

	}

	

}