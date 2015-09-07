package m.reflect.ant.tasks;

public class GetReferenceAction extends Action {
	private String name;
	private String property;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setProperty(String property) {
		this.property = property;
	}
	
	public void onExecute() throws Throwable {
		Object obj = objPool.get(name);
		if (obj != null) {
			getProject().setProperty(property, String.valueOf(obj));
		}
	}
	
}
