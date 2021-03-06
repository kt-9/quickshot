package kz.edu.nu.sst.quickshot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	private LocationManager locationManager;
	private Location loc;
	public static HashMap<String, Bitmap> photosMap = new HashMap<String, Bitmap>();
	private GetImageHashMapTask getImagesTask;

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1 = 100;
	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_2 = 101;
	Button image1Button;
	Button image2Button;
	/*public static*/ ImageView imageView;
	Bitmap image1, image2;
	Uri mImageUri1, mImageUri2;

	Bitmap image;

	boolean second = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findCurrLocation();
		if (loc == null) {
			getImagesTask = new GetImageHashMapTask(51.090299, 71.399707);
			getImagesTask.execute();
		} else {
			getImagesTask = new GetImageHashMapTask(loc.getLatitude(),
					loc.getLongitude());
			getImagesTask.execute();
		}
		image1Button = new Button(this);
		image1Button.setText("Shot an object");

		image2Button = new Button(this);
		image2Button.setText("Shot a scene");

		imageView = new ImageView(this);

		image = Bitmap.createBitmap(600, 800, Config.ARGB_4444);
		imageView.setImageBitmap(image);

		image1Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				File photo;
				try {
					photo = createTemproraryFile("picture1", "jpg");
					photo.delete();

					mImageUri1 = Uri.fromFile(photo);
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri1);
					startActivityForResult(cameraIntent,
							CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1);
				} catch (IOException e) {
					Log.v("MAIN", "can't create file to take pic!!");
				}

			}
		});

		image2Button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				File photo;
				try {
					photo = createTemproraryFile("picture2", "jpg");
					photo.delete();

					mImageUri2 = Uri.fromFile(photo);
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri2);
					startActivityForResult(cameraIntent,
							CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_2);
				} catch (IOException e) {
					Log.v("MAIN", "can't create file to take pic!!");
				}

			}
		});

		Button saveButton = new Button(this);
		saveButton.setText("Start detection!");

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (image1 != null && image2 != null) {
					imageView.setImageBitmap(image);
					ObjectRecognitionTask task = new ObjectRecognitionTask(
							imageView);
					task.execute(image1, image2);
					// ObjectRecognizer detector = new ObjectRecognizer(image1,
					// image2, image);
					// new Thread(detector).start();
				}
			}
		});
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		layout.addView(image1Button);
		layout.addView(image2Button);
		layout.addView(saveButton);
		layout.addView(imageView);

		setContentView(layout);

	}

	private File createTemproraryFile(String part, String ext)
			throws IOException {
		File tempDir = Environment.getExternalStorageDirectory();
		tempDir = new File(tempDir.getAbsolutePath() + "/.temp");
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
		return File.createTempFile(part, ext, tempDir);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_1
				&& resultCode == RESULT_OK) {

			// this.getContentResolver().notifyChange(mImageUri1, null);
			// ContentResolver cr = this.getContentResolver();
			Bitmap photo;

			// photo = android.provider.MediaStore.Images.Media.getBitmap(cr,
			// mImageUri1);
			photo = decodeSampledBitmapFromFile(mImageUri1.getPath(), 480, 640);
			Log.d("RESULT", "Path = " + mImageUri1.getPath());
			image1 = photo;
			imageView.setImageBitmap(photo);
			Log.d("MAIN", "Successfully made imageView");
		} else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_2
				&& resultCode == RESULT_OK) {

			// this.getContentResolver().notifyChange(mImageUri2, null);
			// ContentResolver cr = this.getContentResolver();
			Bitmap photo;
			photo = decodeSampledBitmapFromFile(mImageUri2.getPath(), 480, 640);
			Log.d("MAIN",
					"w,h = " + photo.getWidth() + " ," + photo.getHeight());
			image2 = photo;
			imageView.setImageBitmap(photo);
			Log.d("MAIN", "Successfully made imageView");

		}
	}

	private void findCurrLocation() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		String provider = locationManager
				.getBestProvider(new Criteria(), false);

		Location location = locationManager.getLastKnownLocation(provider);

		if (location == null) {
			locationManager.requestLocationUpdates(provider, 10000, 10,
					listener);
		} else {
			loc = location;
		}
	}

	private LocationListener listener = new LocationListener() {

		public void onLocationChanged(Location location) {
			System.out.println("location changed: " + location);
			loc = location;
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}
	};

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromFile(String pathName,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

}
