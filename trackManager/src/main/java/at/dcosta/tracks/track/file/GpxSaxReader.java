package at.dcosta.tracks.track.file;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.validator.DistanceValidator;

public class GpxSaxReader extends TrackReader {

	private static enum STATUS {
		START, TRKSEG, TRKPT, ELE, TIME;
	}

	public static final String EXTENSION = "gpx";

	public static final String SUFFIX = "." + EXTENSION;

	private static final String ELM_ELE = "ele";
	private static final String ELM_TIME = "time";
	private static final String ELM_TRKPT = "trkpt";
	private static final String ELM_TRKSEG = "trkseg";

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	static {
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private StringBuilder text;
	private STATUS status = STATUS.START;

	private double lat, lon;
	private int height;
	private Date time;

	private final DefaultHandler handler = new DefaultHandler() {
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (status == STATUS.ELE || status == STATUS.TIME) {
				text.append(new String(ch, start, length));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (ELM_ELE.equals(qName)) {
				status = STATUS.TRKPT;
				height = (int) Double.parseDouble(text.toString());
			} else if (ELM_TIME.equals(qName)) {
				status = STATUS.TRKPT;
				try {
					time = DATE_FORMAT.parse(text.toString());
				} catch (ParseException e) {
					throw new SAXException(e);
				}
			} else if (ELM_TRKPT.equals(qName)) {
				status = STATUS.TRKSEG;
				Point point = new Point(lat, lon, height, time.getTime());
				updateListener(point);
			} else if (ELM_TRKSEG.equals(qName)) {
				status = STATUS.START;
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (ELM_TRKSEG.equals(qName)) {
				status = STATUS.TRKSEG;
			} else if (ELM_TRKPT.equals(qName)) {
				status = STATUS.TRKPT;
				lat = Double.parseDouble(attributes.getValue("lat"));
				lon = Double.parseDouble(attributes.getValue("lon"));
			} else if (ELM_ELE.equals(qName)) {
				status = STATUS.ELE;
				text = new StringBuilder();
			} else if (ELM_TIME.equals(qName)) {
				status = STATUS.TIME;
				text = new StringBuilder();
			} else if (ELM_TRKPT.equals(qName)) {
				status = STATUS.TRKSEG;
				text = new StringBuilder();
			} else if (ELM_TRKSEG.equals(qName)) {
				status = STATUS.START;
			}
		}

	};

	public GpxSaxReader(File trackfile, DistanceValidator validator) {
		super(trackfile, validator);
	}

	@Override
	public TrackReader readTrack() throws ParsingException {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(trackfile, handler);
		} catch (SAXException e) {
			throw new ParsingException(e);
		} catch (IOException e) {
			throw new ParsingException(e);
		} catch (ParserConfigurationException e) {
			throw new ParsingException(e);
		}
		return this;
	}

}
