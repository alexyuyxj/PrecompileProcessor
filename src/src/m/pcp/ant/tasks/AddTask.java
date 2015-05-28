package m.pcp.ant.tasks;

import m.pcp.ExpressionParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class AddTask extends Task {
	private String list;
	private String value;
	
	public void setList(String list) {
		this.list = list;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void execute() throws BuildException {
		String listSize = getProject().getProperty(list + "#size");
		if (listSize == null) {
			throw new BuildException(list + " is not defined");
		}
		
		int size = Integer.parseInt(listSize);
		getProject().setProperty(list + "#size", String.valueOf(size + 1));
		
		ExpressionParser parser = new ExpressionParser();
		getProject().setProperty(list + "#" + size, parser.parse(value));
	}
	
}
