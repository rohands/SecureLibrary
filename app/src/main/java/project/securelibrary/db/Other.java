package project.securelibrary.db;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

public class Other implements Serializable {
	private static final long serialVersionUID = 1L;
	private String phno;
	private String name;
	private BigInteger modpubkey;
	private BigInteger expopubkey;
	private String previousEncrypted;

	public Other() {
	}

	public Other(String phno, String name) throws Exception {

		this.phno = phno;
		this.name = name;
		this.previousEncrypted = null;
	}

	public String toString() {
		return "Other [phoneno=" + phno + ", name=" + name + ", modpubkey="
				+ modpubkey + ", expopubkey=" + expopubkey
				+ ", previousEncrypted=" + previousEncrypted + "]";
	}

	public String getPhoneNumber() {
		return phno;
	}

	public String getName() {
		return name;
	}

	public BigInteger getModpubkey() {
		return modpubkey;
	}

	public BigInteger getExpopubkey() {
		return expopubkey;
	}

	public String getPreviousEncrypted() {
		return previousEncrypted;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhoneno(String phno) {
		this.phno = phno;
	}

	public void setModpubkey(String key) {
		BigInteger component = new BigInteger(key);
		this.modpubkey = component;
	}

	public void setExpopubkey(String key) {
		BigInteger component = new BigInteger(key);
		this.expopubkey = component;
	}

	public void setPreviousEncrypted(String data) {
		this.previousEncrypted = data;
	}

	public String toJSON() {

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("phno", getPhoneNumber());
			jsonObject.put("name", getName());
			jsonObject.put("modpubkey", getModpubkey());
			jsonObject.put("expopubkey", getExpopubkey());
			return jsonObject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}

	}

	public PublicKey formpubkey() throws Exception {
		try {
			BigInteger modulus = (BigInteger) getModpubkey();
			BigInteger exponent = (BigInteger) getExpopubkey();
			// Get Public Key
			RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus,
					exponent);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PublicKey publicKey = fact.generatePublic(rsaPublicKeySpec);
			Log.d("publicKey", publicKey.toString());
			return publicKey;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String decryptData(List<String> data) throws Exception {
		System.out.println("\n----------------DECRYPTION STARTED------------");
		String result = "";
		byte[] decryptedData = new byte[0];
		PublicKey pubKey = this.formpubkey();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, pubKey);
		for (String encryptedValue : data) {
			try {
				byte[] databytes = Base64
						.decode(encryptedValue, Base64.DEFAULT);
				decryptedData = cipher.doFinal(databytes);
				Log.d("Decrypted Data: ", new String(decryptedData));
				result += new String(decryptedData);
			} catch (BadPaddingException bpe) {
				throw bpe;
			} catch (IllegalArgumentException iae) {
				throw iae;
			} catch (Exception e) {
				throw e;
			}
		}
		return result;
	}
}