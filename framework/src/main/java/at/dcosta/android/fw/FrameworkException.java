package at.dcosta.android.fw;

public class FrameworkException extends RuntimeException {

    private static final long serialVersionUID = -1823045292936441856L;

    public FrameworkException() {
        super();
    }

    public FrameworkException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public FrameworkException(String arg0) {
        super(arg0);
    }

    public FrameworkException(Throwable arg0) {
        super(arg0);
    }

}
