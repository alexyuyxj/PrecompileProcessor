package m.reflect.ant.tasks;

import m.reflect.ReflectHelper;

public class GetStaticFieldAction extends Action {
	private String className;
	private String fieldName;
	private String result;
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public void onExecute() throws Throwable {
		Object ret = ReflectHelper.getStaticField(className, fieldName);
		if (result != null) {
			objPool.put(result, ret);
		}
	}
	
}
