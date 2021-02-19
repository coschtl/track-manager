package at.dcosta.tracks;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class ShowPhotoDetails extends AlertDialog {

	private final Context context;
	private String photoPath;

	public ShowPhotoDetails(Context context) {
		super(context);
		this.context = context;
	}

	public ShowPhotoDetails setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
		return this;
	}

	@Override
	public void show() {
		super.show();
		setContentView(R.layout.photo_details);
		setCancelable(true);

		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ShowPhotoDetails.this.dismiss();
			}
		});

		TextView path = (TextView) findViewById(R.id.photo_path);
		TextView date = (TextView) findViewById(R.id.photo_date);
		TextView size = (TextView) findViewById(R.id.photo_size);
		TextView location = (TextView) findViewById(R.id.photo_location);
		final TextView address = (TextView) findViewById(R.id.photo_address);
		TableRow locationRow = (TableRow) findViewById(R.id.row_photo_location);
		final TableRow addressRow = (TableRow) findViewById(R.id.row_photo_address);
		addressRow.setVisibility(View.GONE);

		if (photoPath != null) {
			StringBuilder pathToDisplay = new StringBuilder();
			int pos = 0;
			while (pos < photoPath.length()) {
				int end = Math.min(pos + 25, photoPath.length());
				if (pos > 0) {
					pathToDisplay.append('\n');
				}
				pathToDisplay.append(photoPath, pos, end);
				pos = end;
			}
			path.setText(pathToDisplay.toString());
			try {
				ExifInterface exif = new ExifInterface(photoPath);
				String dateValue = exif.getAttribute(ExifInterface.TAG_DATETIME);
				date.setText(dateValue);

				String length = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
				String width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
				size.setText(new StringBuilder(length).append("x").append(width).toString());

				final float[] latLon = new float[2];
				boolean geoAvailable = exif.getLatLong(latLon);
				if (geoAvailable) {
					location.setText(new StringBuilder().append(latLon[0]).append(", ").append(latLon[1]).toString());
					new Thread() {
						@Override
						public void run() {
							int count = 0;
							do {
								count++;
								System.out.println("geocoding");
								Geocoder geocoder = new Geocoder(context);
								try {
									final List<Address> location = geocoder.getFromLocation(latLon[0], latLon[1], 1);
									address.post(new Runnable() {

										@Override
										public void run() {
											address.setText(location.get(0).getAddressLine(0));
											addressRow.setVisibility(View.VISIBLE);
										}
									});
									count = 3;
								} catch (IOException e) {
									// no address available or no geocoder
								}
							} while (count < 3);
						}
					}.start();
				} else {
					locationRow.setVisibility(View.GONE);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
