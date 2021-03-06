package morpho.morphosmart.sdk.demo.ennum;

/**
 * 
 * This class store different possible values of a coding algorithm.
 * 
 */
public enum Coder {

	/*
	 * Default coder, this setting exists since the 08.04 firmware version and
	 * allow the MorphoSmartâ„¢ to keep its default choice.
	 * 
	 */
	MORPHO_DEFAULT_CODER(0, "Default"),
	/*
	 * Standard coder selection (this is the default choice).
	 */
	MORPHO_MSO_V9_CODER(3, "Standard"),
	/*
	 * Juvenile coder selection.
	 */
	MORPHO_MSO_V9_JUV_CODER(7, "Juvenile"),
	/*
	 * Thin finger coder selection.
	 */
	MORPHO_MSO_V9_THIN_FINGER_CODER(8, "Thin Finger");

	private int code;
	private String label;

	/**
	 * @brief Getter for coder code.
	 * @return code integer
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @brief Getter for coder label.
	 * @return label string
	 * */
	public String getLabel() {
		return label;
	}

	/**
	 * Constructor
	 * 
	 * @param code
	 * @param label
	 */
	private Coder(int code, String label) {
		this.code = code;
		this.label = label;
	}

	/**
	 * Get code from label.
	 * 
	 * @param label
	 * @return
	 */
	public static int getValue(String label) {
		if ("Standard".equals(label))
			return 3;
		else if ("Juvenile".equals(label))
			return 7;
		else if ("Thin Finger".equals(label))
			return 8;
		else
			return 0; // Default
	}
	
}
