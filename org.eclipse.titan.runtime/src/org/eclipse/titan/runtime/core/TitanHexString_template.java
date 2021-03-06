/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
import java.util.List;

/**
 * TTCN-3 hexstring template
 *
 * @author Arpad Lovassy
 */
public class TitanHexString_template extends Base_Template {

	private TitanHexString single_value;

	// value_list part
	private ArrayList<TitanHexString_template> value_list;

	/**
	 * hexstring pattern value.
	 *
	 * Each element occupies one byte. Meaning of values:
	 * 0 .. 15 -> 0 .. F, 16 -> ?, 17 -> *
	 */
	private List<Byte> pattern_value;

	//TODO: implement: dec_match part

	public TitanHexString_template () {
		//do nothing
	}

	public TitanHexString_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanHexString_template (final List<Byte> otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);
	}

	public TitanHexString_template (final TitanHexString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound hexstring value.");

		single_value = new TitanHexString(otherValue);
	}

	public TitanHexString_template (final TitanHexString_template otherValue) {
		copyTemplate(otherValue);
	}

	//originally clean_up
	public void cleanUp() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			single_value = null;
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list.clear();
			value_list = null;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	//originally operator=
	public TitanHexString_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator=
	public TitanHexString_template assign( final List<Byte> otherValue ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);

		return this;
	}

	//originally operator=
	public TitanHexString_template assign( final TitanHexString otherValue ) {
		otherValue.mustBound("Assignment of an unbound hexstring value to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);

		return this;
	}

	//originally operator=
	public TitanHexString_template assign( final TitanHexString_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyTemplate(final TitanHexString_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanHexString(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanHexString_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanHexString_template temp = new TitanHexString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported hexstring template.");
		}

		setSelection(otherValue);
	}

	//originally operator[](int)
	public TitanHexString_Element getAt( final int index_value ) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a hexstring element of a non-specific hexstring template.");
		}

		return single_value.getAt( index_value );
	}

	//originally operator[](const INTEGER&)
	public TitanHexString_Element getAt( final TitanInteger index_value ) {
		index_value.mustBound("Indexing a hexstring template with an unbound integer value.");

		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	public TitanHexString_Element constGetAt( final int index_value ) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a hexstring element of a non-specific hexstring template.");
		}

		return single_value.constGetAt( index_value );
	}

	//originally operator[](const INTEGER&) const
	public TitanHexString_Element constGetAt( final TitanInteger index_value) {
		index_value.mustBound("Indexing a hexstring template with an unbound integer value.");

		return constGetAt( index_value.getInt() );
	}

	// originally match
	public boolean match(final TitanHexString otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public boolean match(final TitanHexString otherValue, final boolean legacy) {
		if(! otherValue.isBound()) {
			return false;
		}

		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.operatorEquals( otherValue );
		case OMIT_VALUE:
			return false;
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for(int i = 0 ; i < value_list.size(); i++) {
				if(value_list.get(i).match(otherValue, legacy)) {
					return templateSelection == template_sel.VALUE_LIST;
				}
			}
			return templateSelection == template_sel.COMPLEMENTED_LIST;
		case STRING_PATTERN:{
			//TODO: implement
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported hexstring template.");
		}
	}
}
