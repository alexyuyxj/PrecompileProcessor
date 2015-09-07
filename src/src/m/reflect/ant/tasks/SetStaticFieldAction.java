package m.reflect.ant.tasks;

import m.reflect.ReflectHelper;

public class SetStaticFieldAction extends Action {
	private String className;
	private String fieldName;
	private String value;
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void onExecute() throws Throwable {
		ReflectHelper.setStaticField(className, fieldName, objPool.get(value));
	}
}
