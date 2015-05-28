package m.pcp.ant.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import m.pcp.PCP;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class PCPTask extends Task {
	private String baseDir;
	private String srcDir;
	private String buildDir;
	private Vector<Define> preDefines;
	
	public PCPTask() {
		preDefines = new Vector<Define>(); 
	}
	
	public void execute() throws BuildException {
		// print log
		System.out.println("PCPAnt executed");
		System.out.println("\tbaseDir: " + baseDir);
		System.out.println("\tsrcDir: " + srcDir);
		System.out.println("\tbuildDir: " + buildDir);
		if (preDefines.size() > 0) {
			System.out.println("\tdefined: ");
			for (Define def : preDefines) {
				if (def.file != null) {
					System.out.println("\t\tfile = " + def.file);
				} else {
					System.out.println("\t\t" + def.name + " = " + def.value);
				}
			}
		}
		
		// call pcpmain
		Vector<String> vStr = new Vector<String>();
		vStr.add(baseDir);
		vStr.add(srcDir);
		vStr.add(buildDir);
		for (Define def : preDefines) {
			if (def.file != null) {
				readFileDef(def.file, vStr);
			} else {
				vStr.add(def.name);
				vStr.add(def.value);
			}
		}
		String[] args = new String[vStr.size()];
		vStr.copyInto(args);

		PCP pcp = new PCP();
		pcp.start(args);
	}
	
	private void readFileDef(String file, Vector<String> vStr) {
		if (file == null) {
			return;
		}
		
		File defFile = new File(file);
		if (!defFile.exists() && defFile.isFile()) {
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(defFile));
			String line = br.readLine();
			while (line != null) {
				if (line.trim().length() > 0) {
					String key = null;
					String value = null;
					int index = line.indexOf("=");
					if (index >= 0) {
						key = line.substring(0, index).trim();
						value = line.substring(index + 1).trim();
					} else {
						key = line.trim();
						value = "";
					}
					vStr.add(key);
					vStr.add(value);
				}
				line = br.readLine();
			}
			br.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
	
	public void setSrcDir(String srcDir) {
		this.srcDir = srcDir;
	}
	
	public void setBuildDir(String buildDir) {
		this.buildDir = buildDir;
	}
	
	public Define createDefine() {
		Define define = new Define();
		preDefines.add(define);
        return define;
    }
	
	public class Define {
		private String file;
		private String name;
		private String value;
		
		public void setFile(String file) {
			this.file = file;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
	}

}
