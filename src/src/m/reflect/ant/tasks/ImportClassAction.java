package m.reflect.ant.tasks;

import m.reflect.ReflectHelper;

public class ImportClassAction extends Action {
	private String name;
	private String className;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public void onExecute() throws Throwable {
		if (name == null) {
			ReflectHelper.importClass(className);
		} else {
			ReflectHelper.importClass(name, className);
		}
	}
	
}
