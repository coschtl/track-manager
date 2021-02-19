package at.dcosta.tracks.track.file;

public class ParsingException extends Exception {

    private static final long serialVersionUID = -7111473918443370990L;

    public ParsingException() {
        super();
    }

    public ParsingException(String detailMessage) {
        super(detailMessage);
    }

    public ParsingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ParsingException(Throwable throwable) {
        super(throwable);
    }

}
