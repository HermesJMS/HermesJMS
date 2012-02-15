package hermes.swing;

public enum PropertyType {
	DOUBLE, INT, LONG, STRING, BOOLEAN, BYTE, CHAR ;

	public static PropertyType fromObject(Object value) {
		if (value instanceof String) {
			return STRING ;
		} else if (value instanceof Integer) {
			return INT ;
		} else if (value instanceof Double) {
			return DOUBLE ;
		} else if (value instanceof Boolean) {
			return BOOLEAN ;
		} else if (value instanceof Byte) {
			return BYTE ;
		} else if (value instanceof Character) {
			return CHAR ;
		} else if (value instanceof Long) {
			return LONG ;
		} else {
			return STRING ;
		}
	}

	public static PropertyType fromString(String string) {
		if ("STRING".equals(string)) {
			return STRING ;
		} else if ("INT".equals(string)) {
			return INT ;
		} else if ("CHAR".equals(string)) {
			return CHAR ;
		} else if ("DOUBLE".equals(string)) {
			return DOUBLE ;
		}else if ("BOOLEAN".equals(string)) {
			return BOOLEAN ;
		} else if ("BYTE".equals(string)) {
			return BYTE ;
		} else if ("LONG".equals(string)) {
			return LONG ;
		} else {
			return STRING ;
		}
	}
}
