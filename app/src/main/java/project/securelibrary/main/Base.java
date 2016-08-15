package project.securelibrary.main;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import project.securelibrary.db.DBHelper;
import project.securelibrary.db.DBHelperSelf;
import project.securelibrary.db.Other;
import project.securelibrary.db.Self;


public class Base {
	Context context;
	private DBHelper dbH;
	private DBHelperSelf dbHSelf;
	private Self objSelf;
	private Other objOther;
	public String sendvia = "BLUETOOTH"; // default selected bluetooth
	String result = "";
	Activity activity;

	public Base(Activity activity, Context context) {
		this.activity = activity;
		this.context = context;
		dbHSelf = new DBHelperSelf(context);
		System.out.println("aFTER Table creation");
		dbH = new DBHelper(context);
	}

	public void createTable(Context context) {
		System.out.println("In create table base");
		dbHSelf = new DBHelperSelf(context);
		System.out.println("aFTER Table creation");
		dbH = new DBHelper(context);
		System.out.println("aGAIN HERE");
	}

	public Self readOwnerDetails() throws Exception {
		System.out.println("In owner read details");
		objSelf = dbHSelf.getSelf();
		if (objSelf == null) {
			throw new Exception("Cannot read self details.");
		}
		return objSelf;
	}

	public Other readOtherDetails(String phno) throws Exception {
		objOther = dbH.getOther(phno);
		if (objOther == null) {
			throw new Exception("Cannot read other details.");
		}
		return objOther;
	}

	public void updateTable(Other other) throws Exception {
		dbH.updateOther(other);
	}

	public String populateSelf(String name, String phno) throws Exception {

		validatePhoneNumber(phno);
		validateName(name);
		System.out.println("Validated successfully!");
		if (dbHSelf.getSelf() != null) {
			throw new Exception("Phone number already exists.");
		}
		objSelf = new Self();
		objSelf.setName(name);
		objSelf.setPhoneno(phno);
		objSelf.generate_key();
		System.out.println("In populate self" + objSelf.getName());
		long rowId = dbHSelf.addSelf(objSelf);
		if (rowId == -1) {
			throw new Exception("Could not add new row.");
		} else {
			return "Added Successfully.";
		}
	}

	public String populateOther(String name, String phno, String modpubkey,
			String expopubkey) throws Exception {
		validatePhoneNumber(phno);
		validateName(name);
		if (dbH.getOther(phno) != null) {
			throw new Exception("Phone number already exists.");
		}
		objOther = new Other();
		objOther.setName(name);
		objOther.setPhoneno(phno);
		objOther.setExpopubkey(expopubkey);
		objOther.setModpubkey(modpubkey);
		System.out.println("In populate other:" + objOther.getName()
				+ "phone no" + objOther.getPhoneNumber());
		long rowId = dbH.addOther(objOther);
		if (rowId == -1) {
			throw new Exception("Could not add new row.");
		} else {
			return "Added Successfully.";
		}
	}

	public List<String> sign(String data, Context context) throws Exception {
		dbHSelf = new DBHelperSelf(context);
		objSelf = dbHSelf.getSelf();
		System.out.println("In sign method");
		List<String> encryptedData = new ArrayList<String>();
		if (objSelf == null) {
			throw new Exception("Database connection could not be made.");
		}
		try {
			encryptedData = objSelf.encryptData(data);
		} catch (Exception exception) {
		}

		return encryptedData;
	}

	public String unsign(List<String> data, String phno, Context context)
			throws Exception {
		dbH = new DBHelper(context);
		objOther = dbH.getOther(phno);
		String decryptedData = "";
		if (objOther == null) {
			throw new Exception("Database connection could not be made.");
		}
		try {
			decryptedData = objOther.decryptData(data);
		} catch (Exception exception) {
			throw exception;
		}
		return decryptedData;
	}

	public String unsignSelf(List<String> data, Context context)
			throws Exception {
		dbHSelf = new DBHelperSelf(context);
		objSelf = dbHSelf.getSelf();
		String decryptedData = "";
		if (objOther == null) {
			throw new Exception("Database connection could not be made.");
		}
		try {
			decryptedData = objSelf.decryptData(data);
		} catch (Exception exception) {
			throw exception;
		}
		return decryptedData;
	}

	public void validatePhoneNumber(String phone) throws Exception {
		if (!phone.matches("^[0-9]{10}$"))
			throw new Exception("Phone number should have only 10 digits.");
	}

	public void validateName(String name) throws Exception {
		System.out.println("Validating Name:" + name);
		if (name.length() < 6 || name.length() >= 20
				|| !name.matches("^[a-zA-Z][A-Za-z0-9 ]+")) {
			throw new Exception(
					"Name should begin with an alphabet and have only 6 to 15 characters. ");
		}
	}
}