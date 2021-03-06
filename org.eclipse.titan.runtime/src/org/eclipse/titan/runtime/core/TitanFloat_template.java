/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;

/**
 * TTCN-3 float template
 *
 * Not yet complete rewrite
 */
public class TitanFloat_template extends Base_Template {
	// int_val part
	// TODO maybe should be renamed in core
	private TitanFloat single_value;

	// value_list part
	private ArrayList<TitanFloat_template> value_list;

	// value range part
	private boolean min_is_present, max_is_present;
	private boolean min_is_exclusive, max_is_exclusive;
	private TitanFloat min_value, max_value;


	public TitanFloat_template () {
		//do nothing
	}

	public TitanFloat_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanFloat_template (final int otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);
	}

	public TitanFloat_template (final TitanFloat otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound float value.");

		single_value = new TitanFloat(otherValue);
	}

	public TitanFloat_template (final TitanFloat_template otherValue) {
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
		case VALUE_RANGE:
			min_value = null;
			max_value = null;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	//originally operator=
	public TitanFloat_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator=
	public TitanFloat_template assign( final double otherValue ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);

		return this;
	}

	//originally operator=
	public TitanFloat_template assign( final TitanFloat otherValue ) {
		otherValue.mustBound("Assignment of an unbound float value to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);

		return this;
	}

	//originally operator=
	public TitanFloat_template assign( final TitanFloat_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyTemplate(final TitanFloat_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanFloat(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanFloat_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanFloat_template temp = new TitanFloat_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_present = otherValue.min_is_present;
			min_is_exclusive = otherValue.min_is_exclusive;
			if(min_is_present) {
				min_value = new TitanFloat(otherValue.min_value);
			}
			max_is_present = otherValue.max_is_present;
			max_is_exclusive = otherValue.max_is_exclusive;
			if(max_is_present) {
				max_value = new TitanFloat(otherValue.max_value);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported float template.");
		}

		setSelection(otherValue);
	}

	// originally match
	public boolean match(final TitanFloat otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public boolean match(final TitanFloat otherValue, final boolean legacy) {
		if(! otherValue.isBound()) {
			return false;
		}

		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.operatorEquals(otherValue);
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
		case VALUE_RANGE:{
			boolean lowerMissMatch = true;
			boolean upperMissMatch = true;
			if(min_is_present) {
				if(min_is_exclusive) {
					lowerMissMatch = min_value.isLessThanOrEqual(otherValue);
				} else {
					lowerMissMatch = min_value.isLessThan(otherValue);
				}
			}
			if(max_is_present) {
				if (max_is_exclusive) {
					upperMissMatch = min_value.isGreaterThanOrEqual(otherValue);
				} else {
					upperMissMatch = min_value.isGreaterThan(otherValue);
				}
			}
			return lowerMissMatch && upperMissMatch;
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported float template.");
		}
	}
}
