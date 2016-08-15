package project.securelibrary.buyer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import project.securelibrary.callback.BuyerCallback;
import project.securelibrary.main.Base;


public class Buyer extends Activity {
	int duration = Toast.LENGTH_LONG;
	Context context;
	Toast toast;
	String receivedDetails = "";
	SharedPreferences pref;
	public static final String Prepaye = "Prepaye";
	Activity activity;
	Base base;
	String otherPhno;
	BuyerCallback callback;

	public Buyer(Activity act, Context context) {
		this(act, context, null);
	}

	public Buyer(Activity act, Context context, BuyerCallback callback) {
		this.activity = act;
		this.context = context;
		this.callback = callback;
		base = new Base(this.activity, this.context);
	}

	final public void register(String name, String phno) {
		System.out.println("hello!");
		System.out.println(this.context);
		System.out.println("hey");
		base.createTable(this.context);
		String result = "";
		try {
			result = base.populateSelf(name, phno);
			System.out.println("TRIED!");
		} catch (Exception exception) {
			System.out.println("CAUGHT!!!" + exception);
			String errorMessage = exception.toString();
			toast = Toast.makeText(this.context, errorMessage, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
		}
		Log.d("In register", "Added self");
		toast = Toast.makeText(this.context, "Added successfully", duration);
		toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
		toast.show();
	}
}