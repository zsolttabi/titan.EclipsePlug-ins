package org.eclipse.titan.runtime.core;

/**
 * A class which behaves almost, but not quite, entirely unlike
 * a floating-point value.
 *
 * @author Arpad Lovassy
 */
public class Ttcn3Float {

	/**
	 * representation of negative 0, used by isNegativeZero() for comparison
	 */
	private static final long NEGATIVE_ZERO = Double.doubleToLongBits( -0.0 );

	/** the floating-point value */
	private double value;

	public Ttcn3Float( final double d ) {
		value = d;
	}

	//originally operator double()
	double getValue() {
		return value;
	}

	// originally operator+
	// this + d
	Ttcn3Float add( final double d ) {
		return new Ttcn3Float( value + d );
	}

	// originally operator-
	Ttcn3Float sub( final double d ) {
		return new Ttcn3Float( value - d );
	}

	// originally operator*
	Ttcn3Float mul( final double d ) {
		return new Ttcn3Float( value * d );
	}

	// originally operator/=
	Ttcn3Float div( final double d ) {
		return new Ttcn3Float( value / d );
	}

	// originally operator<
	boolean isLessThan( final double d ) {
		if ( Double.isNaN( value ) ) {
			return Double.isNaN( d ); // TTCN-3 special: NaN is bigger than anything except NaN
		} else if ( Double.isNaN( d ) ) {
			return true; // TTCN-3 special: NaN is bigger than anything except NaN
		} else if ( value == 0.0 && d == 0.0 ) { // does not distinguish -0.0
			return isNegativeZero(value) && !isNegativeZero(d); // value negative, d non-negative
		} else { // finally, the sensible behavior
			return value < d;
		}
	}

	// originally operator>
	boolean isGreaterThan( final double d ) {
		if ( Double.isNaN( value ) ) {
			return !Double.isNaN( d ); // TTCN-3 special: NaN is bigger than anything except NaN
		} else if ( Double.isNaN( d ) ) {
			return false; // TTCN-3 special: NaN is bigger than anything except NaN
		} else if ( value == 0.0 && d == 0.0 ) { // does not distinguish -0.0
			return !isNegativeZero(value) && isNegativeZero(d); // value non-negative, d negative
		} else { // finally, the sensible behavior
			return value > d;
		}
	}

	// originally operator==
	boolean equalsTo( final double d ) {
		if ( Double.isNaN( value ) ) {
			return Double.isNaN( d ); // TTCN-3 special: NaN is bigger than anything except NaN
		} else if ( Double.isNaN( d ) ) {
			return false;
		} else if ( value == 0.0 && d == 0.0 ) { // does not distinguish -0.0
			return isNegativeZero( value ) == isNegativeZero( d );
		} else { // finally, the sensible behavior
			return value == d;
		}
	}

	// originally signbit
	private boolean isNegativeZero( final double d ) {
		// the original double signbit( double ) on the titan.core side
		// returns the sign bit of the floating point number, which is 1 if negative, 0 if positive or exactly 0
		// TTCN-3 handles 0.0 and 1.0 as one case, the only thing that matters is if the signum is negative or not.
		return Double.doubleToLongBits( d ) == NEGATIVE_ZERO;
	}

	public String createJavaStringRepresentation() {
		if (Double.isNaN(value)){
			return "Double.NaN";
		} else if (Double.isInfinite(value)) {
			if( Double.compare(value,0)>0) {
				return "Double.POSITIVE_INFINITY";
			} else {
				return "-Double.NEGATIVE_INFINITY";
			}
		} else {
			return Double.toString(value);
		}
	}
	/**
	 * Converts the value to ttcn representation
	 * It is useful in the logging, for example
	 * @return the converted string
	 */
	public String createTtcn3StringRepresentation() {
		if (Double.isNaN(value)){
			return "not_a_number";
		} else if (Double.isInfinite(value)) {
			if( Double.compare(value,0)>0) {
				return "+infinity";
			} else {
				return "-infinity";
			}
		} else {
			return Double.toString(value);
		}
	}

	@Override
	public String toString() {
		return createTtcn3StringRepresentation();
	}
}
