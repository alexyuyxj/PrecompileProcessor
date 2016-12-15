package m.pcp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class PCP {
	private File baseDir;
	private ArrayList<File> srcDir;
	private File buildDir;
	private ArrayList<String> preDefines;

	public PCP() {
		srcDir = new ArrayList<File>();
		preDefines = new ArrayList<String>();
	}
	
	public void start(String[] args) {
		if (args.length >= 3 && (args.length - 3) % 2 == 0) {
			try {
				setBaseDir(args[0]);
				setSrcDir(args[1]);
				setBuildDir(args[2]);
				dumpDefines(args);
				copyFiles(listSrc());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	
 	private void setBaseDir(String baseDir) {
		this.baseDir = new File(baseDir);
		if (!this.baseDir.exists()) {
			this.baseDir = null;
			throw new RuntimeException("\"" + baseDir + "\" does not exist");
		}
	}
	
	private void setSrcDir(String srcDirs) {
		String[] rawArray = srcDirs.split(",");
		HashSet<File> set = new HashSet<File>();
		for (String item : rawArray) {
			File f = new File(baseDir, item);
			if (!f.exists()) {
				throw new RuntimeException("\"" + item + "\" does not exist");
			}
			set.add(new File(baseDir, item));
		}
		srcDir.clear();
		srcDir.addAll(set);
	}
	
	private void setBuildDir(String buildDir) {
		this.buildDir = new File(baseDir.getPath(), buildDir);
		if (this.buildDir.exists()) {
			this.buildDir.delete();
		}
		this.buildDir.mkdirs();
	}
	
	private void dumpDefines(String[] args) {
		if (args.length > 3) {
			preDefines.clear();
			for (int i = 3; i < args.length; i += 2) {
				preDefines.add("//#define " + args[i] + " " + args[i + 1]);
			}
		}
	}
	
	private HashMap<File, ArrayList<File>> listSrc() {
		HashMap<File, ArrayList<File>> allSrc = new HashMap<File, ArrayList<File>>();
		for (File dir : srcDir) {
			allSrc.put(dir, listFile(dir));
		}
		return allSrc;
	}
	
	private ArrayList<File> listFile(File folder) {
		ArrayList<File> fileList = new ArrayList<File>();
		if (folder.isDirectory()) {
			File[] children = folder.listFiles();
			for (File child : children) {
				if (child.isDirectory()) {
					ArrayList<File> childList = listFile(child);
					fileList.addAll(childList);
				} else {
					fileList.add(child);
				}
			}
		}
		return fileList;
	}
	
	private void copyFiles(HashMap<File, ArrayList<File>> allSrc) throws Throwable {
		byte[] buf = new byte[1024 * 64];
		MacrosParser parser = new MacrosParser();
		for (String def : preDefines) {
			parser.parseLine(def);
		}
		
		for (Entry<File, ArrayList<File>> ent : allSrc.entrySet()) {
			String folderPath = ent.getKey().getAbsolutePath();
			if(!folderPath.endsWith(File.separator)) {
				folderPath += File.separator;
			}
			
			ArrayList<File> srcs = ent.getValue();
			for (File src : srcs) {
				String srcPath = src.getAbsolutePath();
				String path = srcPath.substring(folderPath.length());
				File dst = new File(buildDir, path);
				if (dst.exists()) {
					dst.delete();
				}
				if (!dst.getParentFile().exists()) {
					dst.getParentFile().mkdirs();
				}
				
				if (path.endsWith(".java") || path.endsWith(".groovy")) {
					copySrc(src, dst, parser);
				} else {
					copyFile(src, dst, buf);
				}
			}
		}
	}
	
	private void copySrc(File src, File dst, MacrosParser parser) throws Throwable {
		FileInputStream fis = new FileInputStream(src);
		InputStreamReader isr = new InputStreamReader(fis, "utf-8");
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		int lineCount = 0;
		String line = null;
		try {
			line = br.readLine();
			while (line != null) {
				lineCount++;
				String resLine = parser.parseLine(line);
				if (!resLine.trim().startsWith("//#")) {
					sb.append(resLine).append("\n");
				}
				line = br.readLine();
			}
		} catch (Throwable t) {
			System.err.println("File: " + src);
			System.err.println("Line: " + line.trim());
			System.err.println("Line Number: " + lineCount + "\n");
			throw t;
		} finally {
			fis.close();
		}
		
		FileOutputStream fos = new FileOutputStream(dst);
		fos.write(sb.toString().getBytes("utf-8"));
		fos.flush();
		fos.close();
	}
	
	private void copyFile(File src, File dst, byte[] buf) throws Throwable {
		FileInputStream fis = new FileInputStream(src);
		FileOutputStream fos = new FileOutputStream(dst);
		int len = fis.read(buf);
		while (len != -1) {
			fos.write(buf, 0, len);
			len = fis.read(buf);
		}
		fos.flush();
		fos.close();
		fis.close();
	}
	
}
