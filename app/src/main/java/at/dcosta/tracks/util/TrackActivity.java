package at.dcosta.tracks.util;

import at.dcosta.tracks.validator.DistanceValidator;
import at.dcosta.tracks.validator.Validators;

public interface TrackActivity {

    TrackActivity SELECT = new TrackActivity() {

        private final DistanceValidator validator = Validators.DEFAULT;

        @Override
        public int getColor() {
            return 0;
        }

        @Override
        public DistanceValidator getDistanceValidator() {
            return validator;
        }

        @Override
        public String getIcon() {
            return null;
        }

        @Override
        public int getIconId() {
            return -1;
        }

        @Override
        public String getName() {
            return "---";
        }

        @Override
        public String toString() {
            return getName();
        }

    };

    int getColor();

    DistanceValidator getDistanceValidator();

    String getIcon();

    int getIconId();

    String getName();
}