package m.pcp.ant.tasks;

import java.util.Hashtable;
import java.util.Map.Entry;
import m.pcp.ExpressionParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Task;

public class WhileTask extends LoopTask {
	private String condition;
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	protected void onExecute() throws BuildException {
		ExpressionParser parser = new ExpressionParser();
		boolean cond = isCondition(parser);
		while (cond) {
			for (Task task : tasks) {
				if (breakFound || continueFound) {
					break;
				} else {
					task.perform();
				}
			}
			
			continueFound = false;
			if (breakFound) {
				break;
			}
			
			cond = isCondition(parser);
		}
	}
	
	private boolean isCondition(ExpressionParser parser) {
		String cStr = "true";
		if (condition != null) {
			RuntimeConfigurable rcf = getWrapper();
			cStr = String.valueOf(rcf.getAttributeMap().get("condition"));
			Hashtable<String, Object> props = getProject().getProperties();
			for (Entry<String, Object> ent : props.entrySet()) {
				String key = "${" + ent.getKey() + "}";
				cStr = cStr.replace(key, String.valueOf(ent.getValue()));
			}
		}
		
		String res = parser.parse(cStr);
		return !"false".equals(String.valueOf(res).toLowerCase());
	}
	
}
