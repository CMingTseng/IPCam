package idv.neo.ipcam;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Random;

public class Utils {
	public static int b2Int(byte paramByte) {
		return (paramByte + 256) % 256;
	}

	public static int byteArray2Int(byte[] paramArrayOfByte) {
		return byteArray2Int(paramArrayOfByte, 0);
	}

	public static int byteArray2Int(byte[] paramArrayOfByte, int paramInt) {
		return (b2Int(paramArrayOfByte[(paramInt + 3)]) << 24)
				+ (b2Int(paramArrayOfByte[(paramInt + 2)]) << 16)
				+ (b2Int(paramArrayOfByte[(paramInt + 1)]) << 8)
				+ b2Int(paramArrayOfByte[paramInt]);
	}

	public static short byteArray2Short(byte[] paramArrayOfByte, int paramInt) {
		return (short) ((b2Int(paramArrayOfByte[(paramInt + 1)]) << 8) + b2Int(paramArrayOfByte[paramInt]));
	}

	public static String byteArray2String(byte[] paramArrayOfByte) {
		return byteArray2String(paramArrayOfByte, " ");
	}

	public static String byteArray2String(byte[] paramArrayOfByte,
			String paramString) {

		if (paramArrayOfByte == null)
			return null;

		StringBuilder localStringBuilder = new StringBuilder();
		int i = paramArrayOfByte.length;
		for (int j = 0;; j++) {
			if (j >= i)
				return localStringBuilder.toString();
			byte b = paramArrayOfByte[j];
			String str = "%02x" + paramString;
			Object[] arrayOfObject = new Object[1];
			arrayOfObject[0] = Byte.valueOf(b);
			localStringBuilder.append(String.format(str, arrayOfObject));
		}
	}

	public static String getDigest(String paramString) {
		try {
			String str = byteArray2String(MessageDigest.getInstance("MD5")
					.digest(paramString.getBytes("UTF-8")), "");
			return str;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return null;
	}

	public static String getRandomHexString(int paramInt) {
		Random localRandom = new Random(System.currentTimeMillis());
		byte[] arrayOfByte = new byte[paramInt];
		localRandom.nextBytes(arrayOfByte);
		return byteArray2String(arrayOfByte, "");
	}

	public static byte[] int2ByteArray(int paramInt) {
		byte[] arrayOfByte = new byte[4];
		arrayOfByte[0] = ((byte) paramInt);
		arrayOfByte[1] = ((byte) (paramInt >> 8));
		arrayOfByte[2] = ((byte) (paramInt >> 16));
		arrayOfByte[3] = ((byte) (paramInt >> 24));
		return arrayOfByte;
	}

	public static int readDataToAudioBuffer(InputStream paramInputStream,
			byte[] paramArrayOfByte) throws IOException {
		return readDataToAudioBuffer(paramInputStream, paramArrayOfByte, 0,
				paramArrayOfByte.length);
	}

	public static int readDataToAudioBuffer(InputStream paramInputStream,
			byte[] paramArrayOfByte, int paramInt1, int paramInt2)
			throws IOException {
		int i;
		if (paramInputStream != null) {
			i = 0;
			if (paramArrayOfByte != null)
				;
		} else {
			throw new IOException("stream or buffer is null!!!");
		}
		while (i < paramInt2) {
			int j = paramInputStream.read(paramArrayOfByte, paramInt1 + i,
					paramInt2 - i);
			if (j < 0)
				return -1;
			i += j;
		}
		return i;
	}

	public static int readDataToVideoBuffer(InputStream paramInputStream,
			byte[] paramArrayOfByte) throws IOException {

		return readDataToVideoBuffer(paramInputStream, paramArrayOfByte, 0,
				paramArrayOfByte.length);
	}

	public static int readDataToVideoBuffer(InputStream paramInputStream,
			byte[] paramArrayOfByte, int paramInt1, int paramInt2)
			throws IOException {

		int i;
		if (paramInputStream != null) {
			i = 0;
			if (paramArrayOfByte != null)
				;
		} else {
			throw new IOException("stream or buffer is null!!!");
		}

		while (i < paramInt2) {
			int j = paramInputStream.read(paramArrayOfByte, paramInt1 + i,
					paramInt2 - i);
			if (j < 0)
				return -1;
			i += j;
		}

		return i;
	}

	public static int skipAudio(InputStream paramInputStream, int paramInt)
			throws IOException {

		readDataToAudioBuffer(paramInputStream, new byte[paramInt]);

		return paramInt;
	}

	public static int skipVideo(InputStream paramInputStream, int paramInt)
			throws IOException {

		readDataToVideoBuffer(paramInputStream, new byte[paramInt]);

		return paramInt;
	}

	public static void writeToFile(String paramString, byte[] paramArrayOfByte,
			int paramInt) throws IOException {

		FileOutputStream localFileOutputStream = new FileOutputStream(
				paramString, true);
		localFileOutputStream.write(paramArrayOfByte, 0, paramInt);
		localFileOutputStream.flush();
		localFileOutputStream.close();
	}
}