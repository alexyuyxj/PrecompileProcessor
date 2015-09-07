package m.reflect.ant.tasks;

import java.util.HashMap;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public abstract class Action extends Task {
	protected static final HashMap<String, Object> objPool = new HashMap<String, Object>();

	public final void execute() throws BuildException {
		try {
			onExecute();
		} catch (Throwable t) {
			throw new BuildException(t);
		}
	}
	
	public abstract void onExecute() throws Throwable;
	
	public class ActionParam {
		public String reference;
		
		public void setReference(String reference) {
			this.reference = reference;
		}
	
	}
	
}
