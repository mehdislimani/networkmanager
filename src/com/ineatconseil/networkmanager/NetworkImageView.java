package com.ineatconseil.networkmanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.util.HashSet;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class NetworkImageView extends ImageView{

	//private static NetworkManager sNetworkManager;
	
	protected int mDefaultDrawableId;
	private NetworkTask mNetworkTask;
	private static LruCache<String, BitmapDrawable> sCache;
	private HashSet<SoftReference<Bitmap>> sReusableBitmaps;
	private boolean mAnimated;
	
	public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		buildCache();
		//buildNetworkManager(context);
	}

	public NetworkImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		buildCache();
		//buildNetworkManager(context);
	}

	public NetworkImageView(Context context) {
		super(context);
		buildCache();
		//buildNetworkManager(context);
	}
	
	/*private void buildNetworkManager(Context context){
		if(sNetworkManager != null){
			return;
		}
		
		sNetworkManager = NetworkManager.createInstance(context.getApplicationContext());
	}*/
	
	private void buildCache(){
		if(sCache != null){
			return;
		}
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			sReusableBitmaps = new HashSet<SoftReference<Bitmap>>();
		}
		
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		sCache = new LruCache<String, BitmapDrawable>(cacheSize){
			@Override
			protected int sizeOf(String key, BitmapDrawable value) {
				return (int) (getSizeInBytes(value) / 1024);
			}
			
			@Override
			protected void entryRemoved(boolean evicted, String key,
					BitmapDrawable oldValue, BitmapDrawable newValue) {
				if(RecyclingBitmapDrawable.class.isInstance(oldValue)){
					((RecyclingBitmapDrawable)oldValue).setIsCached(false);
				}else{
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
						//sReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));
					}
				}
				super.entryRemoved(evicted, key, oldValue, newValue);
			}
		};
	}
	
	private static long getSizeInBytes(BitmapDrawable bitmap) {
	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
	        return bitmap.getBitmap().getByteCount();
	    } else {
	        return bitmap.getBitmap().getRowBytes() * bitmap.getBitmap().getHeight();
	    }
	}
	
	public void load(String url, int defaultDrawable, boolean animated){
		mDefaultDrawableId = defaultDrawable;
		load(url, animated);
	}
	
	public void load(String url, boolean animated){
		mAnimated = animated;
		load(url);
	}
	
	public void load(String url){
		if(mNetworkTask != null){
			mNetworkTask.cancel();
		}
		
		// CACHE
		BitmapDrawable currentDrawable = getBitmapFromCache(url);
		if(currentDrawable != null){
			setImageDrawable(currentDrawable, true);
			return;
		}
		
		// DISK
		String fileName = new File(url).getName();
		currentDrawable = getBitmapInDisk(fileName);
		if(currentDrawable != null){
			addBitmapToCache(url, currentDrawable);
			setImageDrawable(currentDrawable,true);
			return;
		}
		
		setImageDrawable(null, true);
		
		//DOWNLOAD
		mNetworkTask = NetworkManager.getInstance().start(url, new NetworkTaskBitmapCallback(getWidth(), getHeight()) {
			
			@Override
			public void onNetworkTaskError(NetworkTask networkTask, Exception e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onNetworkTaskBitmapResponse(NetworkTask networkTask, Bitmap bitmap) {
				NetworkRequest request = networkTask.getRequest();
				if(request != null && bitmap != null){
					BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
					String path = getImagesPath();
					String fileName = new File(request.getUrl()).getName();
					saveBitmap(path, fileName, bitmap);
					addBitmapToCache(request.getUrl(), drawable);
					setImageDrawable(drawable, true);
				}else{
					setImageDrawable(null, true);
				}
				
				
			}
		});
	}
	
	private BitmapDrawable getBitmapFromCache(String key){
		return sCache.get(key);
	}
	
	private void addBitmapToCache(String key, BitmapDrawable bitmap){
		if(getBitmapFromCache(key) == null){
			if(RecyclingBitmapDrawable.class.isInstance(bitmap)){
				((RecyclingBitmapDrawable)bitmap).setIsCached(true);
			}
			sCache.put(key, bitmap);
		}
	}
	
	private String getImagesPath(){
		return getContext().getFilesDir().getPath() +"/images";
	}
	
	private void saveBitmap(final String path, final String name, final Bitmap bitmap){
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try{
					File directory = new File(path);
					if(!directory.exists()){
						directory.mkdirs();
					}
					
					FileOutputStream out = new FileOutputStream(directory.getAbsolutePath() + "/" + name);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
					out.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				
				return null;
			}
		}.execute();
		
	}
	
	public static void clearCache(){
		sCache.evictAll();
	}
	
	private BitmapDrawable getBitmapInDisk(String fileName){
		String path = getImagesPath() + "/" + fileName;
		File file = new File(path);
		if(!file.exists()){
			return null;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = calculateInSampleSize(options, getWidth(),getHeight());
		return new BitmapDrawable(getResources(),BitmapFactory.decodeFile(path, options));
	}
	
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
	

	protected void setImageDrawable(Drawable drawable, boolean newDrawable) {
		super.setImageDrawable(drawable);
	}

	
	
}
