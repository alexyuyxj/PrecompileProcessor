package m.pcp;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public strictfp class ExpressionParser {
	private String[] funcList = new String[] { 
			"sin", "cos", "tan", "asin", "acos", "atan", 
			"toRadians", "toDegrees", "exp", "log", "log10", 
			"sqrt", "cbrt", "ceil", "floor", "rint", "round", 
			"abs", "ulp", "signum", "sinh", "cosh", "tanh", 
			"expm1", "log1p", "getExponent", "nextUp", "random",
			"IEEEremainder", "atan2", "pow", "max", "min", 
			"hypot", "copySign", "nextAfter", "scalb"};
	private HashMap<String, String> fieldMap;
	
	public ExpressionParser() {
		fieldMap = new HashMap<String, String>();
		fieldMap.put("E", "2.7182818284590452354");
		fieldMap.put("PI", "3.14159265358979323846");
	}
	
	public void clearFields() {
		fieldMap.clear();
	}
	
	public void newField(String key, String value) {
		fieldMap.put(key, value);
	}
	
	public void deleteField(String key) {
		fieldMap.remove(key);
	}
	
	public String getField(String key) {
		String value = null;
		if (key.equals("pcp.version")) {
			value = "def{version}";
		} else if (key.equals("pcp.author")) {
			value = "def{author}";
		} else if (key.equals("pcp.email")) {
			value = "def{email}";
		} else if (key.equals("pcp.date")) {
			Calendar cal = Calendar.getInstance();
			String year = String.valueOf(cal.get(Calendar.YEAR));
			String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
			if (month.length() < 2) {
				month = "0" + month;
			}
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() < 2) {
				day = "0" + day;
			}
			value = year + '-' + month + '-' + day;
		} else if (key.equals("pcp.week")) {
			Calendar cal = Calendar.getInstance();
			value = String.valueOf(cal.get(Calendar.DAY_OF_WEEK) - 1);
		} else if (key.equals("pcp.time")) {
			Calendar cal = Calendar.getInstance();
			String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
			if (hour.length() < 2) {
				hour = "0" + hour;
			}
			String minute = String.valueOf(cal.get(Calendar.MINUTE));
			if (minute.length() < 2) {
				minute = "0" + minute;
			}
			String second = String.valueOf(cal.get(Calendar.SECOND));
			if (second.length() < 2) {
				second = "0" + second;
			}
			value = hour + ":" + minute + ":" + second;
		} else if (key.equals("pcp.rndb")) {
			Random rnd = new Random(System.currentTimeMillis());
			value = String.valueOf(rnd.nextInt() % Byte.MAX_VALUE);
		} else if (key.equals("pcp.rnds")) {
			Random rnd = new Random(System.currentTimeMillis());
			value = String.valueOf(rnd.nextInt() % Short.MAX_VALUE);
		} else if (key.equals("pcp.rndi")) {
			Random rnd = new Random(System.currentTimeMillis());
			value = String.valueOf(rnd.nextInt());
		} else if (key.equals("pcp.rndl")) {
			Random rnd = new Random(System.currentTimeMillis());
			value = String.valueOf(rnd.nextLong());
		} else if (key.equals("pcp.rndf")) {
			Random rnd = new Random(System.currentTimeMillis());
			value = String.valueOf(rnd.nextFloat());
		} else if (key.equals("pcp.rndd")) {
			Random rnd = new Random(System.currentTimeMillis());
			value = String.valueOf(rnd.nextDouble());
		} else if (key.equals("pcp.rnd")) {
			Random rnd = new Random(System.currentTimeMillis());
			int type = Math.abs(rnd.nextInt()) % 6;
			switch (type) {
				case 0: value = getField("pcp.rndb"); break;
				case 1: value = getField("pcp.rnds"); break;
				case 2: value = getField("pcp.rndi"); break;
				case 3: value = getField("pcp.rndl"); break;
				case 4: value = getField("pcp.rndf"); break;
				case 5: value = getField("pcp.rndd"); break;
				default: value = "0"; break;
			}
		} else {
			value = fieldMap.get(key);
		}
		return value;
	}

	public String parse(String expression) {
		if (expression != null && expression.length() > 0) {
			Vector<String> sepExp = decompose(expression);
			setField(sepExp);
			return interprete(sepExp);
		}
		return null;
	}
	
	private Vector<String> decompose(String expression) {
		Vector<String> vList = new Vector<String>();
		StringBuffer sb = new StringBuffer();
		int i = 0;
		int size = expression.length();
		while (i < size) {
			char ctmp = expression.charAt(i);
			switch (ctmp) {
				case '(':
				case ')':
				case '+':
				case '-':
				case '*':
				case '/':
				case '^':
				case '&':
				case '|':
				case '%': 
				case ',': {
					if (sb.length() > 0) {
						vList.add(sb.toString());
						sb = new StringBuffer();
					}
					vList.add(String.valueOf(ctmp));
				} break;
				case '!': {
					if (sb.length() > 0) {
						vList.add(sb.toString());
						sb = new StringBuffer();
					}
					if (i + 1 < size) {
						char ctmp1 = expression.charAt(i + 1);
						if (ctmp1 == '=') {
							sb.append(ctmp);
							sb.append(ctmp1);
							vList.add(sb.toString());
							sb = new StringBuffer();
							i++;
						} else {
							vList.add("!");
						}
					} else {
						vList.add("!");
					}
				} break;
				case '=' : {
					if (sb.length() > 0) {
						vList.add(sb.toString());
						sb = new StringBuffer();
					}
					if (i + 1 < size) {
						char ctmp1 = expression.charAt(i + 1);
						if (ctmp1 == '=') {
							sb.append(ctmp);
							sb.append(ctmp1);
							vList.add(sb.toString());
							sb = new StringBuffer();
							i++;
						} else {
							vList.add("=");
						}
					} else {
						vList.add("=");
					}
				} break;
				case '<': {
					if (sb.length() > 0) {
						vList.add(sb.toString());
						sb = new StringBuffer();
					}
					if (i + 1 < size) {
						char ctmp1 = expression.charAt(i + 1);
						if (ctmp1 == '=') {
							sb.append(ctmp);
							sb.append(ctmp1);
							vList.add(sb.toString());
							sb = new StringBuffer();
							i++;
						} else {
							vList.add("<");
						}
					} else {
						vList.add("<");
					}
				} break;
				case '>': {
					if (sb.length() > 0) {
						vList.add(sb.toString());
						sb = new StringBuffer();
					}
					if (i + 1 < size) {
						char ctmp1 = expression.charAt(i + 1);
						if (ctmp1 == '=') {
							vList.add(">=");
							i++;
						} else {
							vList.add(">");
						}
					} else {
						vList.add(">");
					}
				} break;
				case ' ':
				case '\t':
				break;
				default: {
					sb.append(ctmp);
				} break;
			}
			i++;
		}
		if (sb.length() > 0) {
			vList.add(sb.toString());
		}
		return vList;
	}

	private void setField(Vector<String> sepExp) {
		for (int i = 0, size = sepExp.size(); i < size; i++) {
			String value = fieldMap.get(sepExp.get(i));
			if (value != null) {
				sepExp.setElementAt(value, i);
			}
		}
	}
	
	private String interprete(Vector<String> expList) {
		int startIndex = 0;
		int bracketCounter = 0;
		String leftBracket = "(";
		String rightBracket = ")";
		String elementI;
		if (expList.size() > 0) {
			char e0 = expList.get(0).charAt(0);
			switch (e0) {
				case '-':
				case '+': {
					expList.insertElementAt("0", 0);
				} break;
			}
		}
		for (int i = 0, size = expList.size(); i < size; i++) {
			elementI = expList.get(i);
			if (elementI.equals(leftBracket)) {
				bracketCounter++;
				if (bracketCounter == 1) {
					startIndex = i;
				}
			} else if (elementI.equals(rightBracket)) {
				bracketCounter--;
				if (bracketCounter == 0) {
					if (startIndex + 2 <= i) {
						int chiSize = i - startIndex - 1;
						Vector<String> chiList = new Vector<String>(chiSize);
						expList.removeElementAt(startIndex);
						for (int j = 0; j < chiSize; j++) {
							chiList.addElement(expList.get(startIndex));
							expList.removeElementAt(startIndex);
						}
						expList.removeElementAt(startIndex);
						
						expList.insertElementAt(interprete(chiList), startIndex);
						i = startIndex;
						size = expList.size();
					}
				}
			}
		}

		return calculate(expList);
	}

	private String calculate(Vector<String> expList) {
		String elementI;
		int highestLevele = 0;
		int hlPos = 0;
		while (expList.size() > 1) {
			int expListSize = expList.size();
			for (int i = 0; i < expListSize; i++) {
				elementI = expList.get(i);
				if (highestLevele < 8 && elementI.equals(",")) {
					highestLevele = 8;
				} else if (highestLevele < 7 && isInFuncList(elementI)) {
					highestLevele = 7;
				} else if (highestLevele < 6
						&& (elementI.equals("*") || elementI.equals("/"))) {
					highestLevele = 6;
				} else if (highestLevele < 5 && elementI.equals("%")) {
					highestLevele = 5;
				} else if (highestLevele < 4
						&& (elementI.equals("+") || elementI.equals("-"))) {
					highestLevele = 4;
				} else if (highestLevele < 3 && isRelOpt(elementI)) {
					highestLevele = 3;
				} else if (highestLevele < 2 && elementI.equals("!")) {
					highestLevele = 2;
				} else if (highestLevele < 1 
						&& (elementI.equals("&") || elementI.equals("^") || elementI.equals("|"))) {
					highestLevele = 1;
				} else {
					continue;
				}
				hlPos = i;
			}

			switch (highestLevele) {
				case 8: {
					elementI = expList.get(hlPos);
					String stry = expList.get(hlPos + 1);
					expList.removeElementAt(hlPos + 1);
					String strx = expList.get(hlPos - 1);
					expList.removeElementAt(hlPos - 1);
					expList.setElementAt(String.valueOf(strx + "," + stry), hlPos - 1);
				} break;
				case 7: {
					elementI = expList.get(hlPos);
					String args = expList.get(hlPos + 1);
					int sepIndex = args.indexOf(",");
					Vector<String> argList = new Vector<String>();
					while (sepIndex > -1) {
						String arg = args.substring(0, sepIndex);
						args = args.substring(sepIndex + 1);
						argList.add(arg);
						sepIndex = args.indexOf(",");
					}
					argList.add(args);
					int argsSize = getFuncArgsSize(elementI);
					double rn = 0;
					if (argsSize != argList.size()) {
						throw new RuntimeException("Function " + elementI + " not found");
					}
					expList.removeElementAt(hlPos + 1);
					if (argsSize == 1) {
						rn = Double.parseDouble(argList.get(0));
						rn = getFuncValue(elementI, rn);
					} else if (argsSize == 2) {
						rn = Double.parseDouble(argList.get(0));
						double rny = Double.parseDouble(argList.get(1));
						rn = getFuncValue(elementI, rn, rny);
					} else {
						throw new RuntimeException("Function " + elementI + " not found");
					}
					expList.setElementAt(String.valueOf(rn), hlPos);
				} break;
				case 6: {
					elementI = expList.get(hlPos);
					double rny = Double.parseDouble(expList.get(hlPos + 1));
					expList.removeElementAt(hlPos + 1);
					double rnx = Double.parseDouble(expList.get(hlPos - 1));
					expList.removeElementAt(hlPos - 1);
					if (elementI.equals("*")) {
						rnx = rnx * rny;
					} else if (elementI.equals("/")) {
						rnx = rnx / rny;
					}
					expList.setElementAt(String.valueOf(rnx), hlPos - 1);
				} break;
				case 5: {
					elementI = expList.get(hlPos);
					double rny = Double.parseDouble(expList.get(hlPos + 1));
					expList.removeElementAt(hlPos + 1);
					double rnx = Double.parseDouble(expList.get(hlPos - 1));
					expList.removeElementAt(hlPos - 1);
					expList.setElementAt(String.valueOf(rnx % rny), hlPos - 1);
				} break;
				case 4: {
					elementI = expList.get(hlPos);
					double rny = Double.parseDouble(expList.get(hlPos + 1));
					expList.removeElementAt(hlPos + 1);
					double rnx = Double.parseDouble(expList.get(hlPos - 1));
					expList.removeElementAt(hlPos - 1);
					if (elementI.equals("+")) {
						rnx = rnx + rny;
					} else if (elementI.equals("-")) {
						rnx = rnx - rny;
					}
					expList.setElementAt(String.valueOf(rnx), hlPos - 1);
				} break;
				case 3: {
					elementI = expList.elementAt(hlPos).toString();
					String rny = expList.get(hlPos + 1);
					if (rny.startsWith(".") || rny.endsWith(".")) {
						try {
							Double.parseDouble(rny);
							if (rny.startsWith(".")) {
								rny = rny.substring(1);
							}
							if (rny.endsWith(".")) {
								rny = rny.substring(0, rny.length() - 1);
							}
						} catch (Exception ex) {
							
						}
					}
					expList.removeElementAt(hlPos + 1);
					String rnx = expList.get(hlPos - 1);
					if (rnx.startsWith(".") || rnx.endsWith(".")) {
						try {
							Double.parseDouble(rnx);
							if (rnx.startsWith(".")) {
								rnx = rnx.substring(1);
							}
							if (rnx.endsWith(".")) {
								rnx = rnx.substring(0, rnx.length() - 1);
							}
						} catch (Exception ex) {
							
						}
					}
					expList.removeElementAt(hlPos - 1);
					String res0 = "false";
					if (elementI.equals("<")) {
						try {
							double x = Double.parseDouble(rnx);
							double y = Double.parseDouble(rny);
							res0 = String.valueOf(x < y);
						} catch (Exception e) {
							res0 = String.valueOf(rnx.compareTo(rny) < 0);
						}
					} else if (elementI.equals(">")) {
						try {
							double x = Double.parseDouble(rnx);
							double y = Double.parseDouble(rny);
							res0 = String.valueOf(x > y);
						} catch (Exception e) {
							res0 = String.valueOf(rnx.compareTo(rny) > 0);
						}
					} else if (elementI.equals("!=")) {
						try {
							double x = Double.parseDouble(rnx);
							double y = Double.parseDouble(rny);
							res0 = String.valueOf(x != y);
						} catch (Exception e) {
							res0 = String.valueOf(rnx.compareTo(rny) != 0);
						}
					} else if (elementI.equals("<=")) {
						try {
							double x = Double.parseDouble(rnx);
							double y = Double.parseDouble(rny);
							res0 = String.valueOf(x <= y);
						} catch (Exception e) {
							res0 = String.valueOf(rnx.compareTo(rny) <= 0);
						}
					} else if (elementI.equals(">=")) {
						try {
							double x = Double.parseDouble(rnx);
							double y = Double.parseDouble(rny);
							res0 = String.valueOf(x >= y);
						} catch (Exception e) {
							res0 = String.valueOf(rnx.compareTo(rny) >= 0);
						}
					} else if (elementI.equals("==")) {
						try {
							double x = Double.parseDouble(rnx);
							double y = Double.parseDouble(rny);
							res0 = String.valueOf(x == y);
						} catch (Exception e) {
							res0 = String.valueOf(rnx.compareTo(rny) == 0);
						}
					}
					expList.setElementAt(res0, hlPos - 1);
				} break;
				case 2: {
					elementI = expList.get(hlPos);
					boolean bl = expList.get(hlPos + 1).equals("true");
					expList.removeElementAt(hlPos + 1);
					expList.setElementAt(String.valueOf(!bl), hlPos);
				} break;
				case 1: {
					elementI = expList.get(hlPos);
					boolean bly = expList.get(hlPos + 1).equals("true");
					expList.removeElementAt(hlPos + 1);
					boolean blx = expList.get(hlPos - 1).equals("true");
					expList.removeElementAt(hlPos - 1);
					if (elementI.equals("&")) {
						blx = blx & bly;
					} else if (elementI.equals("^")) {
						blx = blx ^ bly;
					} else if (elementI.equals("|")) {
						blx = blx | bly;
					}
					expList.setElementAt(String.valueOf(blx), hlPos - 1);
				} break;
			}
			highestLevele = hlPos = 0;
			
			if (expListSize == expList.size()) {
				break;
			}
		}
		return expList.get(0);
	}
	
	private boolean isInFuncList(String funcName) {
		for (int i = 0; i < funcList.length; i++) {
			if (funcList[i].equals(funcName)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isRelOpt(String funcName) {
		return (funcName.equals("<") || funcName.equals(">")
				|| funcName.equals("!=") || funcName.equals("<=")
				|| funcName.equals(">=") || funcName.equals("=="));
	}
	
	private int getFuncArgsSize(String name) {
		if (name.equals("sin") || name.equals("cos") 
				|| name.equals("tan") || name.equals("asin") 
				|| name.equals("acos") || name.equals("atan") 
				|| name.equals("toRadians") || name.equals("toDegrees") 
				|| name.equals("exp") || name.equals("log") 
				|| name.equals("log10") || name.equals("sqrt") 
				|| name.equals("cbrt") || name.equals("ceil") 
				|| name.equals("floor") || name.equals("rint") 
				|| name.equals("round") || name.equals("abs") 
				|| name.equals("ulp") || name.equals("signum") 
				|| name.equals("sinh") || name.equals("cosh") 
				|| name.equals("tanh") || name.equals("expm1") 
				|| name.equals("log1p") || name.equals("getExponent") 
				|| name.equals("nextUp") || name.equals("random")
				|| name.equals("random")) {
			return 1;
		} else if (name.equals("IEEEremainder") || name.equals("atan2") 
				|| name.equals("pow") || name.equals("max") 
				|| name.equals("min") || name.equals("hypot") 
				|| name.equals("copySign") || name.equals("nextAfter") 
				|| name.equals("scalb")) {
			return 2;
		}
		return -1;
	}
	
	private double getFuncValue(String name, double x) {
		if (name.toLowerCase().equals("sin")) {
			return Math.sin(x);
		} else if (name.toLowerCase().equals("cos")) {
			return Math.cos(x);
		} else if (name.toLowerCase().equals("tan")) {
			return Math.tan(x);
		} else if (name.toLowerCase().equals("asin")) {
			return Math.asin(x);
		} else if (name.toLowerCase().equals("acos")) {
			return Math.acos(x);
		} else if (name.toLowerCase().equals("atan")) {
			return Math.atan(x);
		} else if (name.toLowerCase().equals("toRadians")) {
			return Math.toRadians(x);
		} else if (name.toLowerCase().equals("toDegrees")) {
			return Math.toDegrees(x);
		} else if (name.toLowerCase().equals("exp")) {
			return Math.exp(x);
		} else if (name.toLowerCase().equals("log")) {
			return Math.log(x);
		} else if (name.toLowerCase().equals("log10")) {
			return Math.log10(x);
		} else if (name.toLowerCase().equals("sqrt")) {
			return Math.sqrt(x);
		} else if (name.toLowerCase().equals("cbrt")) {
			return Math.cbrt(x);
		} else if (name.toLowerCase().equals("ceil")) {
			return Math.ceil(x);
		} else if (name.toLowerCase().equals("floor")) {
			return Math.floor(x);
		} else if (name.toLowerCase().equals("rint")) {
			return Math.rint(x);
		} else if (name.toLowerCase().equals("round")) {
			return Math.round(x);
		} else if (name.toLowerCase().equals("abs")) {
			return Math.abs(x);
		} else if (name.toLowerCase().equals("ulp")) {
			return Math.ulp(x);
		} else if (name.toLowerCase().equals("signum")) {
			return Math.signum(x);
		} else if (name.toLowerCase().equals("sinh")) {
			return Math.sinh(x);
		} else if (name.toLowerCase().equals("cosh")) {
			return Math.cosh(x);
		} else if (name.toLowerCase().equals("tanh")) {
			return Math.tanh(x);
		} else if (name.toLowerCase().equals("expm1")) {
			return Math.expm1(x);
		} else if (name.toLowerCase().equals("log1p")) {
			return Math.log1p(x);
		} else if (name.toLowerCase().equals("getExponent")) {
			return Math.getExponent(x);
		} else if (name.toLowerCase().equals("nextUp")) {
			return Math.nextUp(x);
		} else if (name.equals("random")) {
			Random rnd = new Random();
			rnd.setSeed(x > 0 ? (long)x : System.currentTimeMillis());
			return rnd.nextDouble();
		}
		throw new RuntimeException("Can not find method named \"" + name + "\"");
	}
	
	private double getFuncValue(String name, double x, double y) {
		if (name.toLowerCase().equals("IEEEremainder")) {
			return Math.IEEEremainder(x, y);
		} else if (name.toLowerCase().equals("atan2")) {
			return Math.atan2(x, y);
		} else if (name.toLowerCase().equals("pow")) {
			return Math.pow(x, y);
		} else if (name.toLowerCase().equals("max")) {
			return Math.max(x, y);
		} else if (name.toLowerCase().equals("min")) {
			return Math.min(x, y);
		} else if (name.toLowerCase().equals("hypot")) {
			return Math.hypot(x, y);
		} else if (name.toLowerCase().equals("copySign")) {
			return Math.copySign(x, y);
		} else if (name.toLowerCase().equals("nextAfter")) {
			return Math.nextAfter(x, y);
		} else if (name.toLowerCase().equals("scalb")) {
			return Math.scalb(x, (int)y);
		}
		throw new RuntimeException("Can not find method named \"" + name + "\"");
	}
}
