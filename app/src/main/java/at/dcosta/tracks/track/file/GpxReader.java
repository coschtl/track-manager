package at.dcosta.tracks.track.file;

import org.w3c.dom.Element;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import at.dcosta.android.fw.dom.ElementFinder;
import at.dcosta.android.fw.dom.NamedElementIterator;
import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.validator.DistanceValidator;

public class GpxReader extends TrackReader {

    public static final String EXTENSION = "gpx";
    public static final String SUFFIX = "." + EXTENSION;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public GpxReader(Content trackContent, DistanceValidator validator) {
        super(trackContent, validator);
    }

    @Override
    public GpxReader readTrack() throws ParsingException {
        if (listener == null) {
            return this;
        }
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Element root = db.parse(trackContent.getInputStream()).getDocumentElement();

            Element trk = ElementFinder.findFirstChild(root, "trk");
            trackName = ElementFinder.findFirstChild(trk, "name").getTextContent();

            Element trkseg = ElementFinder.findFirstChild(trk, "trkseg");
            NamedElementIterator it = new NamedElementIterator(trkseg, "trkpt");
            Element e;
            Date time;
            Point point;
            while (it.hasNext() && pointsRead++ < readLimit) {
                e = it.next();
                double lat = Double.parseDouble(e.getAttribute("lat"));
                double lon = Double.parseDouble(e.getAttribute("lon"));
                int height = (int) Double.parseDouble(ElementFinder.findFirstChild(e, "ele").getTextContent());
                time = DATE_FORMAT.parse(ElementFinder.findFirstChild(e, "time").getTextContent());

                point = new Point(lat, lon, height, time.getTime());
                updateListener(point);
            }
        } catch (Exception e) {
            throw new ParsingException(e);
        }
        return this;
    }
}
