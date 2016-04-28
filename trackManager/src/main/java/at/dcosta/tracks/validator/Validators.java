package at.dcosta.tracks.validator;

import at.dcosta.tracks.track.Distance;

public class Validators {

	private static final double HOUR = 3600d;

	public static final DistanceValidator DEFAULT = new DistanceValidator(0, 0, 0, 0, 0, 0) {

		@Override
		public boolean isMoving() {
			return true;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public void setDistance(Distance d) {
			// ignore
		}

	};

	public static final DistanceValidator BIKE = new DistanceValidator(100d / HOUR, // horizontal min: 0,1 km/h
			100000d / HOUR, // horizontal max: 100km/h
			1d / HOUR, // vertical up min: 1 HM / h
			3000d / HOUR, // vertical up max: 3000 HM / h
			100d / HOUR, // vertical down min: 100 HM / h
			8000d / HOUR // vertical down max: 10000 HM / h
	);

	public static final DistanceValidator HIKE = new DistanceValidator(100d / HOUR, // horizontal min: 0,1 km/h
			15000d / HOUR, // horizontal max: 15km/h
			50d / HOUR, // vertical up min: 50 HM / h
			3000d / HOUR, // vertical up max: 3000 HM / h
			100d / HOUR, // vertical down min: 100 HM / h
			5000d / HOUR // vertical down max: 3000 HM / h
	);

	public static final DistanceValidator CLIMBING = new DistanceValidator(100d / HOUR, // horizontal min: 0,1 km/h
			15000d / HOUR, // horizontal max: 15km/h
			100d / HOUR, // vertical up min: 100 HM / h
			3000d / HOUR, // vertical up max: 3000 HM / h
			100d / HOUR, // vertical down min: 100 HM / h
			3000d / HOUR // vertical down max: 3000 HM / h
	);

	public static final DistanceValidator SKITOUR = new DistanceValidator(100d / HOUR, // horizontal min: 0,1 km/h
			20000d / HOUR, // horizontal max: 20km/h
			100d / HOUR, // vertical up min: 100 HM / h
			3000d / HOUR, // vertical up max: 3000 HM / h
			100d / HOUR, // vertical down min: 100 HM / h
			10000d / HOUR // vertical down max: 10000 HM / h
	);

	public static final DistanceValidator SLEDGE = new DistanceValidator(1000d / HOUR, // horizontal min: 1 km/h
			15000d / HOUR, // horizontal max: 15km/h
			100d / HOUR, // vertical up min: 100 HM / h
			3000d / HOUR, // vertical up max: 3000 HM / h
			100d / HOUR, // vertical down min: 100 HM / h
			7000d / HOUR // vertical down max: 7000 HM / h
	);

}
