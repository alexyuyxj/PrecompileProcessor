package m.pcp.ant.tasks;

import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

public abstract class LoopTask extends Task implements TaskContainer {
	private static LoopTask currentInstance;
	
	private LoopTask previous;
	protected boolean breakFound;
	protected boolean continueFound;
	protected ArrayList<Task> tasks = new ArrayList<Task>();
	
	public final void addTask(Task task) {
		tasks.add(task);
	}
	
	public final void execute() throws BuildException {
		previous = currentInstance;
		currentInstance = this;
		onExecute();
		currentInstance = previous;
	}
	
	protected abstract void onExecute() throws BuildException;
	
	public static void pushBreak() {
		if (currentInstance != null) {
			currentInstance.breakFound = true;
		}
	}
	
	public static void pushContinue() {
		if (currentInstance != null) {
			currentInstance.continueFound = true;
		}
	}
	
}
