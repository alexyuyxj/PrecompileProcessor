package m.pcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;

public class PCP {
	private File baseDir;
	private String[] srcDir;
	private String buildDir;
	private MacrosParser parser;

	public PCP() {
		parser = new MacrosParser();
	}
	
	public void start(String[] args) {
		if (args.length >= 3 && (args.length - 3) % 2 == 0) {
			setBaseDir(args[0]);
			setSrcDir(args[1]);
			setBuildDir(args[2]);
			File defineFile = null;
			if (args.length > 3) {
				try {
					String[] srcs = args[1].split(",");
					defineFile = new File(args[0] + File.separatorChar 
							+ srcs[0] + File.separatorChar +".define");
					if (defineFile.exists()) {
						defineFile.delete();
					}
					defineFile.createNewFile();
					OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(defineFile), "UTF-8");
					BufferedWriter bw = new BufferedWriter(osw);
					for (int i = 3; i < args.length; i++) {
						String fieldName = args[i];
						i++;
						String fieldValue = args[i];
						bw.append("//#define " + fieldName + ' ' + fieldValue + '\n');
					}
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			copySrc();
			if (defineFile != null && defineFile.exists()) {
				defineFile.delete();
			}
		}
	}
	
 	private void setBaseDir(String baseDir) {
		this.baseDir = new File(baseDir);
		if (!this.baseDir.exists()) {
			this.baseDir = null;
			System.out.println("\"" + baseDir + "\" does not exist");
		}
	}
	
	private void setSrcDir(String srcDirs) {
		srcDir = srcDirs.split(",");
		for (int i = 0; i < srcDir.length; i++) {
			File dirFile = new File(baseDir.getPath() + File.separatorChar + srcDir[i]);
			if (!dirFile.exists()) {
				srcDir[i] = null;
				System.err.println("\"" + dirFile + "\" does not exist");
			}
		}
	}
	
	private void setBuildDir(String buildDir) {
		this.buildDir = buildDir;
		File dir = new File(baseDir.getPath() + File.separatorChar + buildDir);
		if (dir.exists()) {
			dir.delete();
		}
		dir.mkdirs();
	}
	
	private int copySrc() {
		for (String dir : srcDir) {
			if (dir != null && buildDir != null) {
				String srcDirStr = baseDir.getPath() + File.separatorChar + dir;
				String buildDirStr = baseDir.getPath() + File.separatorChar + buildDir;
				File[] srcFileList = listFile(new File(srcDirStr), "java");			
				File defineFile = new File(srcDirStr + File.separatorChar + ".define");
				if (defineFile.exists()) {
					File[] fileList = new File[srcFileList.length + 1];
					fileList[0] = defineFile;
					System.arraycopy(srcFileList, 0, fileList, 1, srcFileList.length);
					srcFileList = fileList;
				}
				
				try {
					for (int i = 0; i < srcFileList.length; i++) {
						String srcFileStr = srcFileList[i].getPath();
						File buildFile = new File (buildDirStr 
								+ srcFileStr.substring(srcDirStr.length()));
						if (!buildFile.getParentFile().exists()) {
							buildFile.getParentFile().mkdirs();
						}
						if (buildFile.exists()) {
							buildFile.delete();
						}
						buildFile.createNewFile();
						copyFile(srcFileList[i], buildFile);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return srcFileList.length;
			}
		}
		return 0;
	}
	
	private File[] listFile(File folder, String ext) {
		if (folder.isDirectory()) {
			File[] children = folder.listFiles();
			Vector<File> fileList = new Vector<File>();
			for (int i = 0; i < children.length; i++) {
				if (children[i].isDirectory()) {
					File[] childList = listFile(children[i], ext);
					for (int j = 0; j < childList.length; j++) {
						fileList.add(childList[j]);
					}
				} else if (children[i].getName().endsWith("." + ext)) {
					fileList.add(children[i]);
				}
			}
			File[] res = new File[fileList.size()];
			fileList.copyInto(res);
			return res;
		}
		return new File[0];
	}
	
	private void copyFile(File srcFile, File destFile) {
		if (srcFile != null && destFile != null 
				&& srcFile.exists() && destFile.exists() 
				&& srcFile.isFile() && destFile.isFile()) {
			int lineCount = 0;
			String line = null;
			try {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(srcFile), "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(destFile), "UTF-8");
				BufferedWriter bw = new BufferedWriter(osw);
				line = br.readLine();
				while (line != null) {
					lineCount++;
					String resLine = parser.parseLine(line);
					if (!resLine.trim().startsWith("//#")) {
						bw.append(resLine + "\n");
					}
					line = br.readLine();
				}
				bw.flush();
				bw.close();
				br.close();
			} catch (Exception ex) {
				System.err.println("File: " + srcFile.getPath());
				System.err.println("Line: " + line.trim());
				System.err.println("Line Number: " + lineCount + "\n");
				ex.printStackTrace();
			}
		}
	}
	
}
