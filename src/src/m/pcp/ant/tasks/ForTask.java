package m.pcp.ant.tasks;

import java.util.Hashtable;
import java.util.Map.Entry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ForTask extends LoopTask {
	private String limit;
	private String iterator;
	private String list;
	private String map;
	
	public void setIterator(String iterator) {
		this.iterator = iterator;
	}
	
	public void setLimit(String limit) {
		this.limit = limit;
	}
	
	public void setList(String list) {
		this.list = list;
	}
	
	public void setMap(String map) {
		this.map = map;
	}
	
	protected void onExecute() throws BuildException {
		if (map != null) {
			forMap();
		} else if (list != null) {
			forList();
		} else if (limit != null) {
			forLimit();
		}
	}

	private void forMap() throws BuildException {
		String mapSize = getProject().getProperty(map + "#size");
		if (mapSize == null) {
			throw new BuildException(map + " is not defined");
		}
		
		Hashtable<String, Object> props = getProject().getProperties();
		for (Entry<String, Object> ent : props.entrySet()) {
			String key = ent.getKey();
			if (key.startsWith(map + "#") && !key.equals(map + "#size")) {
				String itemValue = getProject().getProperty(key);
				getProject().setProperty(iterator + "#name", key.substring(map.length() + 1));
				getProject().setProperty(iterator + "#value", itemValue);

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
			}
		}
		
		getProject().setProperty(iterator + "#name", null);
		getProject().setProperty(iterator + "#value", null);
	}

	private void forList() throws BuildException {
		String listSize = getProject().getProperty(list + "#size");
		if (listSize == null) {
			throw new BuildException(list + " is not defined");
		}
		int size = Integer.parseInt(listSize);
		
		for (int i = 0; i < size; i++) {
			String itemName = list + "#" + i;
			String itemValue = getProject().getProperty(itemName);
			getProject().setProperty(iterator + "#value", itemValue);
			getProject().setProperty(iterator + "#index", String.valueOf(i));
			
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
		}
		
		getProject().setProperty(iterator + "#value", null);
		getProject().setProperty(iterator + "#index", null);
	}
	
	private void forLimit() throws BuildException {
		int size = Integer.parseInt(limit);
		for (int i = 0; i < size; i++) {
			getProject().setProperty(iterator + "#index", String.valueOf(i));
			
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
		}
		getProject().setProperty(iterator + "#index", null);
	}
	
}
