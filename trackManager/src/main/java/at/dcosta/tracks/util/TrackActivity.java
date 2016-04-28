package at.dcosta.tracks.util;

import at.dcosta.tracks.validator.DistanceValidator;
import at.dcosta.tracks.validator.Validators;

public interface TrackActivity {

	public static final TrackActivity SELECT = new TrackActivity() {

		private final DistanceValidator validator = Validators.DEFAULT;

		@Override
		public int getColor() {
			return 0;
		}

		@Override
		public DistanceValidator getDistanceValidator() {
			return validator;
		};

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
		};

		@Override
		public String toString() {
			return getName();
		}

	};

	public int getColor();

	public DistanceValidator getDistanceValidator();

	public String getIcon();

	public int getIconId();

	public String getName();
}