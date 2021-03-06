/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

/**
 * TTCN-3 float
 * @author Arpad Lovassy
 */
public class TitanFloat extends Base_Type {

	/**
	 * float value.
	 */
	private Ttcn3Float float_value;

	public TitanFloat() {
		super();
	}

	public TitanFloat( final double aOtherValue ) {
		float_value = new Ttcn3Float( aOtherValue );
	}

	public TitanFloat( final Ttcn3Float aOtherValue ) {
		float_value = aOtherValue;
	}

	public TitanFloat( final TitanFloat aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound float value." );

		float_value = aOtherValue.float_value;
	}

	public Double getValue() {
		return float_value.getValue();
	}

	//originally operator=
	public TitanFloat assign( final double aOtherValue ) {
		float_value = new Ttcn3Float( aOtherValue );

		return this;
	}

	//originally operator=
	public TitanFloat assign( final TitanFloat aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound float value." );

		float_value = aOtherValue.float_value;

		return this;
	}

	@Override
	public TitanFloat assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanFloat) {
			return assign((TitanFloat)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to float", otherValue));
	}

	public boolean isBound() {
		return float_value != null;
	}

	public boolean isPresent() {
		return isBound();
	};

	public boolean isValue() {
		return float_value != null;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( float_value == null ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	/**
	 * this + aOtherValue
	 * originally operator+
	 */
	public TitanFloat add( final TitanFloat aOtherValue ) {
		mustBound( "Unbound left operand of float addition." );
		aOtherValue.mustBound( "Unbound right operand of float addition." );

		return new TitanFloat( float_value.add( aOtherValue.float_value.getValue() ) );
	}

	/**
	 * this + aOtherValue
	 * originally operator+
	 */
	public TitanFloat add( final double aOtherValue ) {
		mustBound( "Unbound left operand of float addition." );

		return new TitanFloat( float_value.add( aOtherValue ) );
	}

	/**
	 * this - aOtherValue
	 * originally operator-
	 */
	public TitanFloat sub( final TitanFloat aOtherValue ) {
		mustBound( "Unbound left operand of float subtraction." );
		aOtherValue.mustBound( "Unbound right operand of float subtraction." );

		return new TitanFloat( float_value.sub( aOtherValue.float_value.getValue() ) );
	}

	/**
	 * this - aOtherValue
	 * originally operator-
	 */
	public TitanFloat sub( final double aOtherValue ) {
		mustBound( "Unbound left operand of float subtraction." );

		return new TitanFloat( float_value.sub( aOtherValue ) );
	}

	/**
	 * this * aOtherValue
	 * originally operator*
	 */
	public TitanFloat mul( final TitanFloat aOtherValue ) {
		mustBound( "Unbound left operand of float multiplication." );
		aOtherValue.mustBound( "Unbound right operand of float multiplication." );

		return new TitanFloat( float_value.mul( aOtherValue.float_value.getValue() ) );
	}

	/**
	 * this * aOtherValue
	 * originally operator*
	 */
	public TitanFloat mul( final double aOtherValue ) {
		mustBound( "Unbound left operand of float multiplication." );

		return new TitanFloat( float_value.mul( aOtherValue ) );
	}

	/**
	 * this / aOtherValue
	 * originally operator/
	 */
	public TitanFloat div( final TitanFloat aOtherValue ) {
		mustBound( "Unbound left operand of float division." );
		aOtherValue.mustBound( "Unbound right operand of float division." );

		final double otherValue = aOtherValue.float_value.getValue();
		if ( otherValue == 0.0 ) {
			throw new TtcnError("Float division by zero.");
		}

		return new TitanFloat( float_value.mul( otherValue ) );
	}

	/**
	 * this / aOtherValue
	 * originally operator/
	 */
	public TitanFloat div( final double aOtherValue ) {
		mustBound( "Unbound left operand of float division." );

		if ( aOtherValue == 0.0 ) {
			throw new TtcnError("Float division by zero.");
		}

		return new TitanFloat( float_value.mul( aOtherValue ) );
	}

	//originally operator==
	public boolean operatorEquals( final TitanFloat aOtherValue ) {
		mustBound("Unbound left operand of float comparison.");
		aOtherValue.mustBound("Unbound right operand of float comparison.");

		return float_value.equalsTo( aOtherValue.float_value.getValue() );
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanFloat) {
			return operatorEquals((TitanFloat)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanFloat aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	//originally operator <
	public boolean isLessThan(final TitanFloat otherValue) {
		mustBound("Unbound left operand of float comparison.");
		otherValue.mustBound("Unbound right operand of float comparison.");

		return float_value.isLessThan( otherValue.float_value.getValue() );
	}

	//originally operator >
	public boolean isGreaterThan(final TitanFloat otherValue) {
		mustBound("Unbound left operand of float comparison.");
		otherValue.mustBound("Unbound right operand of float comparison.");

		return float_value.isGreaterThan( otherValue.float_value.getValue() );
	}

	//originally operator <=
	public boolean isLessThanOrEqual(final TitanFloat otherValue) {
		return !isGreaterThan(otherValue);
	}

	//originally operator >=
	public boolean isGreaterThanOrEqual(final TitanFloat otherValue) {
		return !isGreaterThan(otherValue);
	}

	public void cleanUp() {
		float_value = null;
	}

	@Override
	public String toString() {
		if ( float_value == null ) {
			return "<unbound>";
		}

		return float_value.toString();
	}
}
