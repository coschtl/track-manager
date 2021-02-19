package at.dcosta.tracks.validator;

import at.dcosta.tracks.track.Distance;

public class DistanceValidator {

    private final String name;
    private final double horizontalMin, horizontalMax, verticalUpMin, verticalUpMax, verticalDownMin, verticalDownMax;
    private boolean moving, valid;
    private String invalidReason;

    DistanceValidator(String name, double horizontalMin, double horizontalMax, double verticalUpMin, double verticalUpMax, double verticalDownMin, double verticalDownMax) {
        this.name = name;
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
        invalidReason = "";
        if (distance.getTime() == 0) {
            valid = true;
            moving = true;
        } else if (distance.getTime() > 0) {
            double secs = distance.getTime() / 1000;
            double verticalPerSec = distance.getVertical() / secs;
            double horizontalPerSec = Math.abs(distance.getHorizontal() / secs);

            if (verticalPerSec > 0) {
                valid = verticalPerSec < verticalUpMax && horizontalPerSec < horizontalMax;
                if (!valid) {
                    if (verticalPerSec >= verticalUpMax) {
                        invalidReason = "verticalPerSec to high: " + verticalPerSec + " >= " + verticalUpMax;
                    } else {
                        invalidReason = "horizontalPerSec to high: " + horizontalPerSec + " >= " + horizontalMax;
                    }
                }
            } else {
                verticalPerSec = verticalPerSec * -1;
                valid = verticalPerSec < verticalDownMax && horizontalPerSec < horizontalMax;
                if (!valid) {
                    if (verticalPerSec >= verticalDownMax) {
                        invalidReason = "verticalPerSecDown to high: " + verticalPerSec + " >= " + verticalDownMax;
                    } else {
                        invalidReason = "horizontalPerSec to high: " + horizontalPerSec + " >= " + horizontalMax;
                    }
                }
            }
            valid = valid && horizontalPerSec < horizontalMax;
            if (horizontalPerSec >= horizontalMax) {
                invalidReason = "horizontalPerSec to high: " + horizontalPerSec + " >= " + horizontalMax;
            }
            moving = verticalPerSec > verticalUpMin || verticalPerSec > verticalDownMin || horizontalPerSec > horizontalMin;
        } else {
            valid = false;
            moving = false;
            invalidReason = "time negative";
        }
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    @Override
    public String toString() {
        return "DistanceValidator{" +
                "name='" + name + '\'' +
                '}';
    }
}
