/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * TTCN-3 bitstring
 * @author Arpad Lovassy
 */
public class TitanBitString extends Base_Type {

	/**
	 * bitstring value.
	 *
	 * Packed storage of bits, filled from LSB.
	 */
	private List<Byte> bits_ptr;

	/** number of bits */
	private int n_bits;

	public TitanBitString() {
		bits_ptr = new ArrayList<Byte>();
		calculateNoBits();
	}

	public TitanBitString( final List<Byte> aOtherValue ) {
		bits_ptr = copyList( aOtherValue );
		calculateNoBits();
	}

	public TitanBitString( final TitanBitString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound bitstring value." );

		bits_ptr = copyList( aOtherValue.bits_ptr );
		n_bits = aOtherValue.n_bits;
	}

	public TitanBitString( final byte aValue ) {
		bits_ptr = new ArrayList<Byte>();
		bits_ptr.add( aValue );
		calculateNoBits();
	}

	/**
	 * calculate number of bits from list data, leading zeros should not be counted
	 */
	private void calculateNoBits() {
		final int size = bits_ptr.size();
		if ( size == 0 ) {
			n_bits = 0;
			return;
		}

		int bits = size * 8;
		final byte last = bits_ptr.get( size - 1 );
		for ( int i = 7; i >= 0 && ( last & ( 1 << i ) ) == 0; i-- ) {
			bits--;
		}
		n_bits = bits;
	}

	public final List<Byte> copyList( final List<Byte> srcList ) {
		if ( srcList == null ) {
			return null;
		}

		final List<Byte> newList = new ArrayList<Byte>( srcList.size() );
		for (Byte uc : srcList) {
			newList.add( Byte.valueOf( uc ) );
		}
		return newList;
	}

	//TODO: remove if not needed
	private void clear_unused_bits() {
		final int listIndex = (n_bits - 1) / 8;
		byte bytevalue = bits_ptr.get( listIndex );
		if (n_bits % 8 != 0) {
			bytevalue &= 0xFF >> (7 - (n_bits - 1) % 8);
		}
		bits_ptr.set( listIndex, bytevalue );
	}

	/** Return the nibble at index i
	 *
	 * @param aBitIndex
	 * @return bit value ( 0 or 1 )
	 */
	public byte getBit( final int aBitIndex ) {
		return (byte) ( bits_ptr.get( aBitIndex / 8 ) & ( 1 << ( aBitIndex % 8 ) ) );
	}

	public void setBit( final int aBitIndex, final byte aNewValue ) {
		final int mask = 1 << ( aBitIndex % 8 );
		// the index of the actual byte, where the modification is made
		final int listIndex = aBitIndex / 8;
		byte bytevalue = bits_ptr.get( listIndex );
		if ( aNewValue != 0 ) {
			bytevalue |= mask;
		} else {
			bytevalue &= ~mask;
		}
		bits_ptr.set( listIndex, bytevalue );
		calculateNoBits();
	}

	//originally char*()
	public List<Byte> getValue() {
		return bits_ptr;
	}

	//takes ownership of aOtherValue
	public void setValue( final List<Byte> aOtherValue ) {
		bits_ptr = aOtherValue;
		calculateNoBits();
	}

	//originally operator=
	public TitanBitString assign( final TitanBitString aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound bitstring value." );

		bits_ptr = copyList( aOtherValue.bits_ptr );
		n_bits = aOtherValue.n_bits;
		return this;
	}

	@Override
	public TitanBitString assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanBitString) {
			return assign((TitanBitString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	public boolean isBound() {
		return bits_ptr != null;
	}

	public boolean isValue() {
		return isBound();
	}

	public void mustBound( final String aErrorMessage ) {
		if ( !isBound() ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally operator==
	public boolean operatorEquals( final TitanBitString otherValue ) {
		mustBound("Unbound left operand of bitstring comparison.");
		otherValue.mustBound("Unbound right operand of bitstring comparison.");

		return n_bits == otherValue.n_bits && bits_ptr.equals( otherValue.bits_ptr );
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanBitString) {
			return operatorEquals((TitanBitString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanBitString aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	public void cleanUp() {
		n_bits = 0;
		bits_ptr = null;
	}

	//originally operator[](int)
	public TitanBitString_Element getAt(final int index_value) {
		if (bits_ptr == null && index_value == 0) {
			bits_ptr = new ArrayList<Byte>();
			return new TitanBitString_Element(false, this, 0);
		} else {
			mustBound("Accessing an element of an unbound bitstring value.");

			if (index_value < 0) {
				throw new TtcnError("Accessing an bitstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = bits_ptr.size();
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a bitstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " hexadecimal digits.");
			}
			if (index_value == n_nibbles) {
				return new TitanBitString_Element( false, this, index_value );
			} else {
				return new TitanBitString_Element( true, this, index_value );
			}
		}
	}

	//originally operator[](const INTEGER&)
	public TitanBitString_Element getAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a bitstring value with an unbound integer value.");

		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	public final TitanBitString_Element constGetAt( final int index_value ) {
		mustBound("Accessing an element of an unbound bitstring value.");

		if (index_value < 0) {
			throw new TtcnError("Accessing an bitstring element using a negative index (" + index_value + ").");
		}

		final int n_nibbles = bits_ptr.size();
		if (index_value >= n_nibbles) {
			throw new TtcnError("Index overflow when accessing a bitstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " hexadecimal digits.");
		}

		return new TitanBitString_Element(true, this, index_value);
	}

	//originally operator[](const INTEGER&) const
	public final TitanBitString_Element constGetAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a bitstring value with an unbound integer value.");

		return constGetAt( index_value.getInt() );
	}

	@Override
	public boolean isPresent() {
		return isBound();
	}
}