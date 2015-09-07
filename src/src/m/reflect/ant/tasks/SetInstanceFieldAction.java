package m.reflect.ant.tasks;

import m.reflect.ReflectHelper;

public class SetInstanceFieldAction extends Action {
	private String receiver;
	private String fieldName;
	private String value;
	
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void onExecute() throws Throwable {
		Object rec = objPool.get(receiver);
		ReflectHelper.setInstanceField(rec, fieldName, objPool.get(value));
	}
	
}
