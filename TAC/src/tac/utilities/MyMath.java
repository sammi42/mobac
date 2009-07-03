package tac.utilities;

public class MyMath {

	public static int ceil(double value) {
		return (int) Math.ceil(value);
	}

	public static int divCeil(int value, int divisor) {
		int result = value / divisor;
		if (value % divisor != 0)
			result++;
		return result;
	}
}
