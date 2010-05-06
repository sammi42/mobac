package mobac.gui.components;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JSlider;

public class JTimeSlider extends JSlider {

	private static final long serialVersionUID = 1L;

	private static final Hashtable<Integer, JLabel> LABEL_TABLE;

	private static final double LOG2 = Math.log(2d);
	private static final double C1 = -1000.4d / LOG2;
	private static final double C2 = 3d * LOG2;

	private static final int MIN = 10000;

	private static final int MAX = 31000;

	static {
		LABEL_TABLE = new Hashtable<Integer, JLabel>();
		int hour = timeToSliderValue(TimeUnit.HOURS.toMillis(1));
		int day = timeToSliderValue(TimeUnit.DAYS.toMillis(1));
		int month = timeToSliderValue(TimeUnit.DAYS.toMillis(31));
		int year = timeToSliderValue(TimeUnit.DAYS.toMillis(365));
		LABEL_TABLE.put(new Integer(MIN), new JLabel("0 sec"));
		LABEL_TABLE.put(new Integer(hour), new JLabel("1 hour"));
		LABEL_TABLE.put(new Integer(day), new JLabel("1 day"));
		LABEL_TABLE.put(new Integer(month), new JLabel("1 month"));
		LABEL_TABLE.put(new Integer(year), new JLabel("1 year"));
		LABEL_TABLE.put(new Integer(MAX), new JLabel("never"));
	}

	public JTimeSlider() {
		super(MIN, MAX);
		setMinorTickSpacing(1000);
		setPaintTicks(true);
		setLabelTable(LABEL_TABLE);
		setPaintLabels(true);
	}

	public long getTimeSecondsValue() {
		return sliderToTimeValue(super.getValue()) / 1000;
	}

	public long getTimeMilliValue() {
		return sliderToTimeValue(super.getValue());
	}

	public void setTimeMilliValue(long time) {
		super.setValue(timeToSliderValue(time));
	}

	private static long sliderToTimeValue(int sliderValue) {
		return -1024000 + (long) (1000 * Math.pow(2d, sliderValue / 1000d));
	}

	private static int timeToSliderValue(long timeValue) {

		return (int) (C1 * (C2 - Math.log((timeValue + 1024000) / 125d)));
	}
}
