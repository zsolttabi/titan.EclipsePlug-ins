package org.eclipse.titan.runtime.core;

import java.util.LinkedList;

/**
 * This class is used to provide default handling in the runtime.
 *
 * @author Kristof Szabados
 * */
public class TTCN_Default {
	// TODO check why does this never decrease
	private static int defaultCount = 0;
	private static int backupCount = 0;
	private static final LinkedList<Default_Base> DEFAULTS = new LinkedList<Default_Base>();
	private static final LinkedList<Default_Base> BACKUP_DEFAULTS = new LinkedList<Default_Base>();
	private static boolean controlDefaultsSaved = false;

	private TTCN_Default() {
		//intentionally empty to disable instantiation
	}

	public static int activate(final Default_Base newDefault) {
		DEFAULTS.addLast(newDefault);
		return defaultCount++;
	}

	public static void deactivate(final Default_Base removableDefault) {
		if (DEFAULTS.contains(removableDefault)) {
			DEFAULTS.remove(removableDefault);
			return;
		}

		TtcnError.TtcnWarning("Performing a deactivate operation on an inactive default reference.");
	}

	public static void deactivate(final TitanDefault removableDefault) {
		if (removableDefault.default_ptr == TitanDefault.UNBOUND_DEFAULT) {
			throw new TtcnError("Performing a deactivate operation on an unbound default reference.");
		}
		if (removableDefault.default_ptr == null) {
			//TODO log
		} else {
			deactivate(removableDefault.default_ptr);
		}
	}

	public static void deactivateAll() {
		DEFAULTS.clear();
	}

	public static TitanAlt_Status tryAltsteps() {
		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;

		for (int i = DEFAULTS.size() - 1; i >= 0; i--) {
			Default_Base actualDefault = DEFAULTS.get(i);
			switch (actualDefault.call_altstep()) {
			case ALT_YES:
				//TODO log
				return TitanAlt_Status.ALT_YES;
			case ALT_REPEAT:
				//TODO log
				return TitanAlt_Status.ALT_REPEAT;
			case ALT_BREAK:
				//TODO log
				return TitanAlt_Status.ALT_BREAK;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
				break;
			default:
				break;
			}
		}

		return returnValue;
	}

	// originally TTCN_Default::save_control_defaults
	public static void save_control_defaults() {
		if (controlDefaultsSaved) {
			throw new TtcnError("Internal error: Control part defaults are already saved.");
		}

		BACKUP_DEFAULTS.addAll(DEFAULTS);
		DEFAULTS.clear();
		backupCount = defaultCount;
		defaultCount = 0;
		controlDefaultsSaved = true;
	}

	// originally TTCN_Default::restore_control_defaults
	public static void restoreControlDefaults() {
		if (!controlDefaultsSaved) {
			throw new TtcnError("Internal error: Control part defaults are not saved.");
		}

		if (DEFAULTS.size() > 0) {
			throw new TtcnError("Internal Error: There are defaults. Control part defaults can not be restored.");
		}

		DEFAULTS.addAll(BACKUP_DEFAULTS);
		BACKUP_DEFAULTS.clear();
		defaultCount = backupCount;
		backupCount = 0;
		controlDefaultsSaved = false;
	}

	// originally TTCN_Default::reset_counter
	public static void resetCounter() {
		if (controlDefaultsSaved) {
			throw new TtcnError("Internal error: Default counter cannot be reset when the control part defaults are saved.");
		}
		if (DEFAULTS.size() > 0) {
			throw new TtcnError("Internal error: Default counter cannot be reset when there are active defaults.");
		}

		defaultCount = 0;
	}
}
