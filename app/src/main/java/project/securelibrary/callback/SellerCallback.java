package project.securelibrary.callback;

import java.io.Serializable;

import android.app.Activity;
import android.content.Context;

public interface SellerCallback extends Serializable {

	public void transactionCallback(String data);

	public void transactionConflictCallback(String data);
	
	public void transactionResolveConflict(String data);

}
