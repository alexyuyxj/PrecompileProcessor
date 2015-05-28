package m.pcp.ant.tasks;

import java.util.Hashtable;
import java.util.Map.Entry;
import m.pcp.ExpressionParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class PutTask extends Task {
	private String map;
	private String name;
	private String value;
	
	public void setMap(String map) {
		this.map = map;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void execute() throws BuildException {
		String mapSize = getProject().getProperty(map + "#size");
		if (mapSize == null) {
			throw new BuildException(map + " is not defined");
		}
		
		ExpressionParser parser = new ExpressionParser();
		getProject().setProperty(map + "#" + name, parser.parse(value));
		
		Hashtable<String, Object> props = getProject().getProperties();
		int size = -1;
		for (Entry<String, Object> ent : props.entrySet()) {
			if (ent.getKey().startsWith(map + "#")) {
				size++;
			}
		}
		getProject().setProperty(map + "#size", String.valueOf(size));
	}
	
}
