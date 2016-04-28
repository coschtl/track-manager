package at.dcosta.android.fw.props;

public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConfigurationException() {
		super();
	}

	public ConfigurationException(String detailMessage) {
		super(detailMessage);
	}

	public ConfigurationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ConfigurationException(Throwable throwable) {
		super(throwable);
	}

}
