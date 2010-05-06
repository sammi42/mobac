package mobac.program.model;

public enum UnitSystem {

	Metric(6367.5, 1000, "km", "m"), Imperial(3963.192, 5280, "mi", "ft");

	public final double earthRadius;
	public final String unitLarge;
	public final String unitSmall;
	public final int unitFactor;
	public final double maxAngularDistSmall;

	private UnitSystem(double earthRadius, int unitFactor, String unitLarge, String unitSmall) {
		this.earthRadius = earthRadius;
		this.unitFactor = unitFactor;
		this.unitLarge = unitLarge;
		this.unitSmall = unitSmall;
		this.maxAngularDistSmall = 1 / (earthRadius * unitFactor);
	}

}
