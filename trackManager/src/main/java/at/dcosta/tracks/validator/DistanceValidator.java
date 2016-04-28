package at.dcosta.tracks.validator;

import at.dcosta.tracks.track.Distance;

public class DistanceValidator {

	private final double horizontalMin, horizontalMax, verticalUpMin, verticalUpMax, verticalDownMin, verticalDownMax;
	private boolean moving, valid;

	DistanceValidator(double horizontalMin, double horizontalMax, double verticalUpMin, double verticalUpMax, double verticalDownMin, double verticalDownMax) {
		this.horizontalMin = horizontalMin;
		this.horizontalMax = horizontalMax;
		this.verticalUpMin = verticalUpMin;
		this.verticalUpMax = verticalUpMax;
		this.verticalDownMin = verticalDownMin;
		this.verticalDownMax = verticalDownMax;
	}

	public boolean isMoving() {
		return moving;
	}

	public boolean isValid() {
		return valid;
	}

	public void setDistance(Distance distance) {
		validate(distance);
	}

	private void validate(Distance distance) {
		if (distance.getTime() == 0) {
			valid = true;
			moving = true;
		} else if (distance.getTime() > 0) {
			double secs = distance.getTime() / 1000;
			double verticalPerSec = distance.getVertical() / secs;
			double horizontalPerSec = Math.abs(distance.getHorizontal() / secs);

			if (verticalPerSec > 0) {
				valid = verticalPerSec < verticalUpMax && horizontalPerSec < horizontalMax;
			} else {
				valid = verticalPerSec * -1 < verticalDownMax && horizontalPerSec < horizontalMax;
			}
			valid = valid && horizontalPerSec < horizontalMax;
			verticalPerSec = Math.abs(verticalPerSec);
			moving = verticalPerSec > verticalUpMin || verticalPerSec > verticalDownMin || horizontalPerSec > horizontalMin;
		} else {
			valid = false;
			moving = false;
		}
	}

}
