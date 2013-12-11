package com.ineatconseil.networkmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public abstract class NetworkTaskBitmapCallback extends NetworkTaskCallback {

	private static final int INVALID_SIZE = -1;

	private int mWidth;
	private int mHeight;

	public NetworkTaskBitmapCallback() {
		mWidth = mHeight = INVALID_SIZE;
	}

	public NetworkTaskBitmapCallback(int size) {
		mWidth = mHeight = size;
	}

	public NetworkTaskBitmapCallback(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	private boolean isScaledBitmap() {
		return mWidth > 0 && mHeight > 0;
	}

	@Override
	public void onNetworkTaskResponse(NetworkTask networkTask) {
		byte[] data = networkTask.getResponse();
		if (data == null) {
			onNetworkTaskError(networkTask, new IllegalArgumentException(
					"Les datas de la bitmap sont vides"));
		} else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
					options);
			if (isScaledBitmap()) {
				options.inSampleSize = calculateInSampleSize(options, mWidth,
						mHeight);
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
						options);
			}

			onNetworkTaskBitmapResponse(networkTask, bitmap);
		}

	}

	public abstract void onNetworkTaskBitmapResponse(NetworkTask networkTask,
			Bitmap bitmap);

	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

}
