package hermes.swing;

public class PropertyRow {
	public String name ;
	public PropertyType type = PropertyType.STRING ;
	public Object value = "" ;
	public String renderType() {
		return type.toString() ;
	}
}
