package morpho.morphosmart.sdk.demo.ennum;

/**
 * 
 * This class store possible values of wakeup mode.
 */
public enum MorphoWakeUpMode {
	/*
	 * Only available on MorphoSmartâ„¢ FINGER VP : leds are turned on while
	 * waiting for a finger
	 */
	MORPHO_WAKEUP_LED_ON(0, "Default"),

	/*
	 * Only available on MorphoSmartâ„¢ MSO FFD : leds are turned off while
	 * waiting for a finger (impedance wakeup).
	 */
	MORPHO_WAKEUP_LED_OFF(4, "Check Wake Up Mode");

	private int code;
	private String label;

	/*
	 * Return corresponding code of current wakeup mode
	 * 
	 * @return corresponding code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Return corresponding label of current wakeup mode
	 * 
	 * @return corresponding label
	 */
	public String getLabel() {
		return label;
	}

	private MorphoWakeUpMode(int code, String label) {
		this.code = code;
		this.label = label;
	}

}
