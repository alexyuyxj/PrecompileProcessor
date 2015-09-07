package m.reflect.ant.tasks;

import java.util.ArrayList;
import m.reflect.ReflectHelper;

public class NewInstanceAction extends Action {
	private String className;
	private ArrayList<ActionParam> params = new ArrayList<ActionParam>();
	private String result;
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public ActionParam createParam() {
		ActionParam param = new ActionParam();
		params.add(param);
		return param;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public void onExecute() throws Throwable {
		Object[] args = new Object[params.size()];
		for (int i = 0; i < args.length; i++) {
			args[i] = objPool.get(params.get(i).reference);
		}
		Object ret = ReflectHelper.newInstance(className, args);
		if (result != null) {
			objPool.put(result, ret);
		}
	}
	
}