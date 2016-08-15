package project.securelibrary.db;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

public class Self implements Serializable {
	private static final long serialVersionUID = 1L;
	private String phno;
	private String name;
	private BigInteger modpubkey;
	private BigInteger expopubkey;
	private BigInteger modprikey;
	private BigInteger expoprikey;

	public Self() {
	}

	public Self(String phno, String name) throws Exception {

		this.phno = phno;
		this.name = name;

	}

	public String toString() {
		return "Self [phoneno=" + phno + ", name=" + name + "]";
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

	public BigInteger getModprikey() {
		return modprikey;
	}

	public BigInteger getExpopubkey() {
		return expopubkey;
	}

	public BigInteger getExpoprikey() {
		return expoprikey;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhoneno(String phno) {
		this.phno = phno;
	}

	public void setModprikey(String key) {
		BigInteger component = new BigInteger(key);
		this.modprikey = component;
	}

	public void setExpoprikey(String key) {
		BigInteger component = new BigInteger(key);
		this.expoprikey = component;
	}

	public void setModpubkey(String key) {
		BigInteger component = new BigInteger(key);
		this.modpubkey = component;
	}

	public void setExpopubkey(String key) {
		BigInteger component = new BigInteger(key);
		this.expopubkey = component;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

	}

	@SuppressLint("TrulyRandom")
	public void generate_key() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024); // 1024 used for normal securities
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec rsaPubKeySpec = keyFactory.getKeySpec(publicKey,
				RSAPublicKeySpec.class);
		RSAPrivateKeySpec rsaPrivKeySpec = keyFactory.getKeySpec(privateKey,
				RSAPrivateKeySpec.class);
		this.modpubkey = rsaPubKeySpec.getModulus();
		this.expopubkey = rsaPubKeySpec.getPublicExponent();
		this.modprikey = rsaPrivKeySpec.getModulus();
		this.expoprikey = rsaPrivKeySpec.getPrivateExponent();
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

	public PrivateKey formprikey() throws Exception {
		try {
			BigInteger modulus = (BigInteger) getModprikey();
			BigInteger exponent = (BigInteger) getExpoprikey();
			// Get Private Key
			RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(
					modulus, exponent);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = fact.generatePrivate(rsaPrivateKeySpec);
			Log.d("privateKey", privateKey.toString());
			return privateKey;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<String> encryptData(String data) throws Exception {
		Log.d("Data Before Encryption :", data);
		byte[] dataToEncrypt = data.getBytes();
		byte[] encryptedData = new byte[0];
		List<String> result = new ArrayList<String>();
		Map<Integer, byte[]> dataToBeEncrypted = new LinkedHashMap<Integer, byte[]>();
		int blocks = 0;
		int dataLength = dataToEncrypt.length;
		Log.d("LENGTH OF DATA:", "" + dataLength);
		if (dataLength > 117) {
			blocks = (int) (dataLength / 117);
			blocks += dataLength % 117 == 0 ? 0 : 1;
		} else {
			blocks = 1;
		}
		Log.d("NUMBER OF BLOCKS", "" + "" + blocks);
		if (blocks == 1) {
			dataToBeEncrypted.put(0, dataToEncrypt);
		} else {
			int j = 0;
			for (int i = 0; i < blocks; i++) {
				if (i != (blocks - 1)) {
					dataToBeEncrypted.put(i,
							Arrays.copyOfRange(dataToEncrypt, j, j + 117));

					j += 117;
				} else {
					dataToBeEncrypted.put(i, Arrays.copyOfRange(dataToEncrypt,
							j, dataToEncrypt.length));
				}
				Log.d("String stored", new String(dataToBeEncrypted.get(i)));
			}
		}

		PrivateKey privateKey = this.formprikey();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		for (byte[] value : dataToBeEncrypted.values()) {
			try {
				Log.d("Data here:", new String(value));
				encryptedData = cipher.doFinal(value);
				Log.d("Encryted Data: ", new String(encryptedData));
				result.add(Base64.encodeToString(encryptedData, Base64.DEFAULT));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
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
