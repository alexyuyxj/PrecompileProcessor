package m.pcp.ant.tasks;

import org.apache.tools.ant.Task;

public class AliasTask extends Task {
	private String name;
	private String property;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setProperty(String property) {
		this.property = property;
	}
	
	public void execute() throws org.apache.tools.ant.BuildException {
		Object obj = getProject().getProperty(property);
		if (obj != null) {
			getProject().setProperty(name, String.valueOf(obj));
		}
	}
	
}
