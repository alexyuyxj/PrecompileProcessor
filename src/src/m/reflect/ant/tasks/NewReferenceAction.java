package m.reflect.ant.tasks;

public class NewReferenceAction extends Action {
	private String name;
	private String type;
	private String value;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void onExecute() throws Throwable {
		if (name == null) {
			return;
		} else if (value == null) {
			objPool.put(name, null);
		} else if (type == null || "String".equals(type)) {
			objPool.put(name, value);
		} else if ("byte".equals(type)) {
			objPool.put(name, Byte.parseByte(value));
		} else if ("short".equals(type)) {
			objPool.put(name, Short.parseShort(value));
		} else if ("int".equals(type)) {
			objPool.put(name, Integer.parseInt(value));
		} else if ("long".equals(type)) {
			objPool.put(name, Long.parseLong(value));
		} else if ("float".equals(type)) {
			objPool.put(name, Float.parseFloat(value));
		} else if ("double".equals(type)) {
			objPool.put(name, Double.parseDouble(value));
		} else if ("boolean".equals(type)) {
			objPool.put(name, Boolean.parseBoolean(value));
		} else if ("char".equals(type)) {
			objPool.put(name, value.charAt(0));
		}
	}
	
}
