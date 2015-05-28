package m.pcp.ant.tasks;

import java.util.ArrayList;
import m.pcp.ExpressionParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

public class IfTask extends Task {
	private String condition;
	private ThenTask thenTask;
	private ElseTask elseTask;
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public ThenTask createThen() {
		thenTask = new ThenTask();
		return thenTask;
	}
	
	public ElseTask createElse() {
		elseTask = new ElseTask();
		return elseTask;
	}
	
	public void execute() throws BuildException {
		ExpressionParser parser = new ExpressionParser();
		String res = parser.parse(condition);
		boolean cond = "true".equals(String.valueOf(res).toLowerCase());
		if (cond && thenTask != null) {
			thenTask.execute();
		} else if (!cond && elseTask != null) {
			elseTask.execute();
		}
	}
	
	public class ThenTask implements TaskContainer {
		private ArrayList<Task> tasks = new ArrayList<Task>();

		public void addTask(Task task) {
			tasks.add(task);
		}
		
		public void execute() throws BuildException {
			for (Task task : tasks) {
				task.perform();
			}
		}
	}
	
	public class ElseTask implements TaskContainer {
		private ArrayList<Task> tasks = new ArrayList<Task>();

		public void addTask(Task task) {
			tasks.add(task);
		}
		
		public void execute() throws BuildException {
			for (Task task : tasks) {
				task.perform();
			}
		}
		
	}
	
}
