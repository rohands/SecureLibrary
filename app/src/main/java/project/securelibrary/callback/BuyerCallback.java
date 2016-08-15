package project.securelibrary.callback;

import java.io.Serializable;

public interface BuyerCallback extends Serializable {

	public void transactionCallback(String data);

	public void transactionNotification(String data);
}
