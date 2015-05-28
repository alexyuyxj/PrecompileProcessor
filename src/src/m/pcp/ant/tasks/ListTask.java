package m.pcp.ant.tasks;

import java.util.ArrayList;
import m.pcp.ExpressionParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ListTask extends Task {
	private String name;
	private ArrayList<ListItem> list = new ArrayList<ListItem>();
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setList(String list) {
		String[] arr = list.split(",");
		if (arr != null) {
			for (String str : arr) {
				ListItem i = new ListItem();
				i.value = str;
				this.list.add(i);
			}
		}
	}
	
	public ListItem createItem() {
		ListItem item = new ListItem();
		list.add(item);
		return item;
    }
	
	public void execute() throws BuildException {
		int size = list.size();
		getProject().setProperty(name + "#size", String.valueOf(size));
		ExpressionParser parser = new ExpressionParser();
		for (int i = 0; i < size; i++) {
			getProject().setProperty(name + "#" + i, parser.parse(list.get(i).value));
		}
	}
	
	public class ListItem {
		private String value;
		
		public void setValue(String value) {
			this.value = value;
		}
	
	}
	
}
