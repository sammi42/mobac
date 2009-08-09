package tac.utilities;

public class MyMath {

	public static int ceil(double value) {
		return (int) Math.ceil(value);
	}

	/**
	 * Divides <code>value</code> by <code>divisor</code> and performs the
	 * function "ceil" on the fractional result. <b>Warning: This method works
	 * only with positive input values!</b>
	 * <p>
	 * Internally only integer arithmetics is used (no floating point
	 * arithmetics)
	 * </p>
	 * 
	 * @param value
	 *            positive value
	 * @param divisor
	 *            positive value
	 * @return
	 */
	public static int divCeil(int value, int divisor) {
		int result = value / divisor;
		int remainder = value % divisor;
		if (remainder != 0)
			result++;
		return result;
	}

	/**
	 * Divides <code>value</code> by <code>divisor</code> and performs the
	 * function "ceil" on the fractional result.<b>Warning: This method works
	 * only with positive input values!</b>
	 * <p>
	 * Internally only integer arithmetics is used (no floating point
	 * arithmetics)
	 * </p>
	 * 
	 * @param value
	 *            positive value
	 * @param divisor
	 *            positive value
	 * @return
	 */
	public static long divCeil(long value, long divisor) {
		long result = value / divisor;
		if (value % divisor != 0)
			result++;
		return result;
	}

	/**
	 * Divides <code>value</code> by <code>divisor</code> and performs the
	 * function "round" on the fractional result. <b>Warning: This method works
	 * only with positive input values!</b>
	 * <p>
	 * Internally only integer arithmetics is used (no floating point
	 * arithmetics)
	 * </p>
	 * 
	 * @param value
	 *            positive value
	 * @param divisor
	 *            positive value
	 * @return
	 */
	public static int divRound(int value, int divisor) {
		int result = value / divisor;
		int remainder = value % divisor;
		if (remainder > (divisor >> 1))
			result++;
		return result;
	}

	/**
	 * Divides <code>value</code> by <code>divisor</code> and performs the
	 * function "round" on the fractional result. <b>Warning: This method works
	 * only with positive input values!</b>
	 * <p>
	 * Internally only integer arithmetics is used (no floating point
	 * arithmetics)
	 * </p>
	 * 
	 * @param value
	 *            positive value
	 * @param divisor
	 *            positive value
	 * @return
	 */
	public static long divRound(long value, long divisor) {
		long result = value / divisor;
		long remainder = value % divisor;
		if (remainder > (divisor >> 1))
			result++;
		return result;
	}
}
