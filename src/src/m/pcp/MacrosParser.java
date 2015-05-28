package m.pcp;

import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

public class MacrosParser {
	private Vector<boolean[]> ifList;
	private boolean codeEnable; // true -- remove "//#="; false -- add "//#="
	private boolean normalIf; // true -- start with "if"; false -- start with "ifdef" or "ifndef"
	private ExpressionParser parser;
	private File logFileBuf;
	private FileWriter logger;
	
	public MacrosParser() {
		parser = new ExpressionParser();
		ifList = new Vector<boolean[]>();
		codeEnable = true;
	}
	
	public String parseLine(String line) throws Exception {
		int defIndex = line.indexOf("def{");
		while (defIndex > -1) {
			int endIndex = line.indexOf("}", defIndex);
			if (endIndex < 0) {
				return line;
			}
			
			int nextIndex = line.indexOf("def{", defIndex + 1);
			if (endIndex > nextIndex && nextIndex >= 0) {
				String innerLine = line.substring(nextIndex);
				line = line.substring(0, nextIndex) + parseLine(innerLine);
				continue;
			}
			
			String fieldName = line.substring(defIndex + 4, endIndex);
			String fieldValue = parser.getField(fieldName);
			fieldValue = fieldValue == null ? "null" : fieldValue;
			line = line.substring(0, defIndex) + fieldValue + line.substring(endIndex + 1);
			defIndex = line.indexOf("def{");
		}
		return parse(line);
	}
	
//	public String parseLine(String line) throws Exception {
//		int i = line.indexOf("def{");
//		while (i > -1) {
//			int j = line.indexOf("}", i);
//			if (j < 0) {
//				return line;
//			}
//			String fieldName = line.substring(i + 4, j);
//			String fieldValue = parser.getField(fieldName);
//			fieldValue = fieldValue == null ? "null" : fieldValue;
//			line = line.substring(0, i) + fieldValue + line.substring(j + 1);
//			i = line.indexOf("def{");
//		}
//		
//		return parse(line);
//	}
	
	private String parse(String line) throws Exception {
		String macrosLine = line.trim();
		int marcroIndex = line.indexOf(macrosLine);
		String prefix = line.substring(0, marcroIndex);
		if (macrosLine.startsWith("//#ifdef")) { //#ifdef <fieldName>
			boolean[] newLevel = new boolean[] {codeEnable, normalIf};
			ifList.add(newLevel);
			normalIf = false;
			if (codeEnable) {
				String fieldName = macrosLine.substring(8).trim();
				if (fieldName != null && fieldName.length() > 0) {
					codeEnable = (parser.getField(fieldName) != null);
				} else {
					throw new RuntimeException("Illegal command");
				}
			}
		} else if (macrosLine.startsWith("//#ifndef")) { //#ifndef <fieldName>
			boolean[] newLevel = new boolean[] {codeEnable, normalIf};
			ifList.add(newLevel);
			normalIf = false;
			if (codeEnable) {
				String fieldName = macrosLine.substring(9).trim();
				if (fieldName != null && fieldName.length() > 0) {
					codeEnable = (parser.getField(fieldName) == null);
				} else {
					throw new RuntimeException("Illegal command");
				}
			}
		} else if (macrosLine.startsWith("//#if")) { //#if <exp>
			boolean[] newLevel = new boolean[] {codeEnable, normalIf};
			ifList.add(newLevel);
			normalIf = true;
			if (codeEnable) {
				String exp = macrosLine.substring(5).trim();
				if (exp != null && exp.length() > 0) {
					codeEnable = Boolean.parseBoolean(parser.parse(exp));
				} else {
					throw new RuntimeException("Illegal command");
				}
			}
		} else if (macrosLine.startsWith("//#elif")) { //#elif <exp>
			if (!normalIf) {
				throw new RuntimeException("Illegal command");
			}
			boolean[] lastLevel = ifList.lastElement();
			if (lastLevel[0]) { // "ifState" of last level
				if (codeEnable) {
					codeEnable = false;
				} else {
					String exp = macrosLine.substring(7).trim();
					if (exp != null && exp.length() > 0) {
						codeEnable = Boolean.parseBoolean(parser.parse(exp));
					} else {
						throw new RuntimeException("Illegal command");
					}
				}
			}
		} else if (macrosLine.startsWith("//#else")) { //#else
			boolean[] lastLevel = ifList.lastElement();
			if (lastLevel[0]) { // "ifState" of last level
				codeEnable = !codeEnable;
			}
		} else if (macrosLine.startsWith("//#endif")) { //#endif
			boolean[] lastLevel = ifList.lastElement();
			ifList.remove(ifList.size() - 1);
			codeEnable = lastLevel[0];
			normalIf = lastLevel[1];
		} else if (codeEnable) {
			if (macrosLine.startsWith("//#define")) { //#define <fieldName> <fieldValue>
				macrosLine = macrosLine.substring(9).trim();
				int spIndex = macrosLine.indexOf(' ');
				if (spIndex > 0) {
					String fieldName = macrosLine.substring(0, spIndex);
					String fieldValue = macrosLine.substring(spIndex).trim();
					String value = null;
					try {
						value = parser.parse(fieldValue);
					} catch(Throwable t) {
						value = null;
					}
					parser.newField(fieldName, value == null 
							? fieldValue.replace("\\n", "\n") : value);
				} else {
					throw new RuntimeException("Illegal command");
				}
			}
			if (macrosLine.startsWith("//#undefine")) { //#undefine <fieldName>
				String fieldName = macrosLine.substring(11).trim();
				if (fieldName != null && fieldName.length() > 0) {
					parser.deleteField(fieldName);
				} else {
					throw new RuntimeException("Illegal command");
				}
			} else if (macrosLine.startsWith("//#logf")) { //#logf <logFile> <message>
				macrosLine = macrosLine.substring(7).trim();
				int spIndex = macrosLine.indexOf(' ');
				if (spIndex > 0) {
					String logFile = macrosLine.substring(0, spIndex);
					String message = macrosLine.substring(spIndex).trim();
					File file = new File(logFile);
					if (!file.exists()) {
						file.createNewFile();
					}
					if (logFileBuf != null && file.equals(logFileBuf)) {
						if (logger == null) {
							logger = new FileWriter(file, true);
						}
						logger.write(message + "\n");
						logger.flush();
					} else {
						if (logger != null) {
							logger.close();
						}
						logFileBuf = file;
						logger = new FileWriter(file, true);
						logger.write(message + "\n");
						logger.flush();
					}
				} else {
					throw new RuntimeException("Illegal command");
				}
			} else if (macrosLine.startsWith("//#logi")) { //#logi <infor>
				String infor = macrosLine.substring(7).trim();
				System.out.println(infor);
			} else if (macrosLine.startsWith("//#loge")) { //#loge <error>
				String error = macrosLine.substring(7).trim();
				System.err.println(error);
			} else if (macrosLine.startsWith("//#=")) {
				line = macrosLine.substring(4).trim();
			}
		} else if (!codeEnable && !macrosLine.startsWith("//#=") && !macrosLine.startsWith("//#")) {
			line = "//#=" + line.trim();
		}
		return prefix + line.trim();
	}
	
}
