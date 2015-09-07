package m.reflect.ant.tasks;

import m.reflect.ReflectHelper;

public class GetInstanceFieldAction extends Action {
	private String receiver;
	private String fieldName;
	private String result;
	
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public void onExecute() throws Throwable {
		Object rec = objPool.get(receiver);
		Object ret = ReflectHelper.getInstanceField(rec, fieldName);
		if (result != null) {
			objPool.put(result, ret);
		}
	}
	
}
