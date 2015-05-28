package m.pcp.ant.tasks;

import m.pcp.ExpressionParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class SetTask extends Task {
	private String name;
	private String value;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void execute() throws BuildException {
		ExpressionParser parser = new ExpressionParser();
		getProject().setProperty(name, parser.parse(value));
	}
	
}
