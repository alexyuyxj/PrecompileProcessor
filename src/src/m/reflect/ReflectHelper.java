package m.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/** ReflectHelper用于更简单地实现java的反射操作 */
public class ReflectHelper {
	private static HashSet<String> packageSet;
	private static HashMap<String, Class<?>> classMap;
	private static HashMap<Class<?>, String> nameMap;
	private static CachePool<String, Method> cachedMethod;
	private static CachePool<String, Constructor<?>> cachedConstr;
	
	static {
		packageSet = new HashSet<String>();
		packageSet.add("java.lang");
		packageSet.add("java.io");
		packageSet.add("java.nio");
		packageSet.add("java.net");
		packageSet.add("java.util");
		packageSet.add("com.mob.tools");
		packageSet.add("com.mob.tools.gui");
		packageSet.add("com.mob.tools.log");
		packageSet.add("com.mob.tools.network");
		packageSet.add("com.mob.tools.utils");
		
		classMap = new HashMap<String, Class<?>>();
		classMap.put("double", double.class);
		classMap.put("float", float.class);
		classMap.put("long", long.class);
		classMap.put("int", int.class);
		classMap.put("short", short.class);
		classMap.put("byte", byte.class);
		classMap.put("char", char.class);
		classMap.put("boolean", boolean.class);
		classMap.put("Object", Object.class);
		classMap.put("String", String.class);
		classMap.put("Thread", Thread.class);
		classMap.put("Runnable", Runnable.class);
		classMap.put("System", System.class);
		classMap.put("double", Double.class);
		classMap.put("Float", Float.class);
		classMap.put("Long", Long.class);
		classMap.put("Integer", Integer.class);
		classMap.put("Short", Short.class);
		classMap.put("Byte", Byte.class);
		classMap.put("Character", Character.class);
		classMap.put("Boolean", Boolean.class);
		
		nameMap = new HashMap<Class<?>, String>();
		for (Entry<String, Class<?>> ent : classMap.entrySet()) {
			nameMap.put(ent.getValue(), ent.getKey());
		}
		
		cachedMethod = new CachePool<String, Method>(25);
		cachedConstr = new CachePool<String, Constructor<?>>(5);
	}
	
	/**
	 * 类似于关键词import的功能，在内存在加载一个指定的类
	 * 
	 * @param className 完整的类名（package.class），如果是内部类，须将类名前的“.”改为“$”
	 * 
	 * @return 加载成功的话，会返回这个类的simple name，用于以后获取类对象使用
	 */
	public static String importClass(String className) throws Throwable {
		return importClass(null, className);
	}
	
	/**
	 * 类似于关键词import的功能，在内存在加载一个指定的类
	 * 
	 * @param className 完整的类名（package.class），如果是内部类，须将类名前的“.”改为“$”
	 * @param name 指定的simple name
	 * 
	 * @return 加载成功的话，会返回这个类的simple name，用于以后获取类对象使用
	 */
	public static synchronized String importClass(String name, String className) throws Throwable {
		if (className.endsWith(".*")) {
			packageSet.add(className.substring(0, className.length() - 2));
			return "*";
		}
		
		Class<?> clz = Class.forName(className);
		if (name == null) {
			name = clz.getSimpleName();
		}
		classMap.put(name, clz);
		nameMap.put(clz, name);
		return name;
	}

	private static synchronized Class<?> getImportedClass(String className) {
		Class<?> clz = classMap.get(className);
		if (clz == null) {
			for (String packageName : packageSet) {
				try {
					importClass(packageName + "." + className);
				} catch (Throwable t) {}
				clz = classMap.get(className);
				if (clz != null) {
					break;
				}
			}
		}
		return clz;
	}
	
	private static Class<?>[] getTypes(Object[] args) {
		Class<?>[] types = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			types[i] = args[i] == null ? null : args[i].getClass();
		}
		return types;
	}
	
	private static boolean primitiveEquals(Class<?> primitive, Class<?> target) {
		return ((primitive == byte.class && target == Byte.class)
				|| (primitive == short.class && (target == Short.class || target == Byte.class || target == Character.class))
				|| (primitive == char.class && (target == Character.class || target == Short.class || target == Byte.class))
				|| (primitive == int.class && (target == Integer.class || target == Short.class || target == Byte.class 
						|| target == Character.class))
				|| (primitive == long.class && (target == Long.class || target == Integer.class || target == Short.class 
						|| target == Byte.class || target == Character.class))
				|| (primitive == float.class && (target == Float.class || target == Long.class || target == Integer.class
						|| target == Short.class || target == Byte.class || target == Character.class))
				|| (primitive == double.class && (target == Double.class || target == Float.class || target == Long.class
						|| target == Integer.class || target == Short.class || target == Byte.class || target == Character.class))
				|| (primitive == boolean.class && target == Boolean.class));
	}
	
	private static boolean matchParams(Class<?>[] mTypes, Class<?>[] types) {
		if (mTypes.length == types.length) {
			boolean match = true;
			for (int i = 0; i < types.length; i++) {
				if (types[i] != null && !primitiveEquals(mTypes[i], types[i]) 
						&& !mTypes[i].isAssignableFrom(types[i])) {
					match = false;
					break;
				}
			}
			return match;
		}
		return false;
	}
	
	private static boolean tryMatchParams(Class<?>[] mTypes, Class<?>[] types) {
		if (mTypes.length - types.length == 1) {
			boolean match = true;
			for (int i = 0; i < types.length; i++) {
				if (types[i] != null && !primitiveEquals(mTypes[i], types[i]) 
						&& !mTypes[i].isAssignableFrom(types[i])) {
					match = false;
					break;
				}
			}
			return (match && mTypes[mTypes.length - 1].isArray());
		}
		return false;
	}
	
	/**
	 * 根据类名实例化一个对象
	 * 
	 * @param className {@link #importClass(String, String)}方法返回的类名，如果要实例化的是一个数组，须在元素类型前加上“[”符
	 * @param args 构造方法所需要的参数列表
	 */
	public static Object newInstance(String className, Object... args) throws Throwable {
		try {
			return onNewInstance(className, args);
		} catch (Throwable t) {
			if (t instanceof NoSuchMethodException) {
				throw t;
			} else {
				//#if def{debuggable}
				String msg = "className: " + className + ", methodName: <init>, args: " + Arrays.toString(args);
				//#else
				//#=String msg = "className: " + className + ", methodName: <init>";
				//#endif
				throw new Throwable(msg, t);
			}
		}
	}
	
	private static Object onNewInstance(String className, Object... args) throws Throwable {
		if (className.startsWith("[")) {
			return newArray(className, args);
		}
		
		String mthSign = className + "#" + args.length;
		Constructor<?> con = cachedConstr.get(mthSign);
		Class<?>[] types = getTypes(args);
		if (con != null && matchParams(con.getParameterTypes(), types)) {
			con.setAccessible(true);
			return con.newInstance(args);
		}
		
		Class<?> clz = getImportedClass(className);
		Constructor<?>[] cons = clz.getDeclaredConstructors();
		ArrayList<Constructor<?>> overloads = new ArrayList<Constructor<?>>();
		ArrayList<Class<?>[]> paramsTypes = new ArrayList<Class<?>[]>();
		for (Constructor<?> c : cons) {
			Class<?>[] paramTypes = c.getParameterTypes();
			if (matchParams(paramTypes, types)) {
				cachedConstr.put(mthSign, c);
				c.setAccessible(true);
				return c.newInstance(args);
			} else if (paramTypes.length > 0 && paramTypes[paramTypes.length - 1].isArray() 
					&& types.length >= paramTypes.length - 1) {
				overloads.add(c);
				paramsTypes.add(paramTypes);
			}
		}
		
		for (int i = 0; i < paramsTypes.size(); i++) {
			Class<?>[] paramTypes = paramsTypes.get(i);
			Class<?> componentType = paramTypes[paramTypes.length - 1].getComponentType();
			if (tryMatchParams(paramTypes, types)) {
				Object[] newArgs = new Object[args.length + 1];
				System.arraycopy(args, 0, newArgs, 0, args.length);
				newArgs[args.length] = Array.newInstance(componentType, 0);
				Constructor<?> c = overloads.get(i);
				c.setAccessible(true);
				return c.newInstance(args);
			} else {
				boolean isElement = true;
				for (int t = paramTypes.length - 1; t < types.length; t++) {
					if (!types[t].equals(componentType)) {
						isElement = false;
						break;
					}
				}
				if (isElement) {
					int arrLen = types.length - paramTypes.length + 1;
					Object arr = Array.newInstance(componentType, arrLen);
					for (int e = 0; e < arrLen; e++) {
						Array.set(arr, e, args[paramTypes.length - 1 + e]);
					}
					Object[] newArgs = new Object[args.length + 1];
					System.arraycopy(args, 0, newArgs, 0, args.length);
					newArgs[args.length] = arr;
					Constructor<?> c = overloads.get(i);
					c.setAccessible(true);
					return c.newInstance(args);
				}
			}
		}
		
		//#if def{debuggable}
		throw new NoSuchMethodException("className: " + className + ", methodName: <init>, args: " + Arrays.toString(args));
		//#else
		//#=throw new NoSuchMethodException("className: " + className + ", methodName: <init>");
		//#endif
	}
	
	private static Object newArray(String className, Object... args) throws Throwable {
		String tmp = className;
		int dimension = 0;
		while (tmp.startsWith("[")) {
			dimension++;
			tmp = tmp.substring(1);
		}
		
		int[] lens = null;
		if (dimension == args.length) {
			lens = new int[dimension];
			for (int i = 0; i < dimension; i++) {
				try {
					lens[i] = Integer.parseInt(String.valueOf(args[i]));
				} catch (Throwable t) {
					lens = null;
					break;
				}
			}
		}
		
		if (lens != null) {
			Class<?> eleClz = null;
			if ("B".equals(tmp)) {
				eleClz = byte.class;
			} else if ("S".equals(tmp)) {
				eleClz = short.class;
			} else if ("I".equals(tmp)) {
				eleClz = int.class;
			} else if ("J".equals(tmp)) {
				eleClz = long.class;
			} else if ("F".equals(tmp)) {
				eleClz = float.class;
			} else if ("D".equals(tmp)) {
				eleClz = double.class;
			} else if ("Z".equals(tmp)) {
				eleClz = boolean.class;
			} else if ("C".equals(tmp)) {
				eleClz = char.class;
			} else {
				eleClz = getImportedClass(tmp);
			}
			
			if (eleClz != null) {
				return Array.newInstance(eleClz, lens);
			}
		}
		
		//#if def{debuggable}
		throw new NoSuchMethodException("className: " + className + ", methodName: <init>, args: " + Arrays.toString(args));
		//#else
		//#=throw new NoSuchMethodException("className: [" + className + ", methodName: <init>");
		//#endif
	}
	
	/**
	 * 调用指定类的一个静态方法
	 * 
	 * @param className {@link #importClass(String, String)}方法返回的类名
	 * @param methodName 方法名称
	 * @param args 参数列表
	 */
	public static <T extends Object> T invokeStaticMethod(String className, String methodName, 
			Object... args) throws Throwable {
		try {
			return invokeMethod(className, null, methodName, args);
		} catch (Throwable t) {
			if (t instanceof NoSuchMethodException) {
				throw t;
			} else {
				//#if def{debuggable}
				String msg = "className: " + className + ", methodName: " + methodName + ", args: " + Arrays.toString(args);
				//#else
				//#=String msg = "className: " + className + ", methodName: " + methodName;
				//#endif
				throw new Throwable(msg, t);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Object> T invokeMethod(String className, Object receiver, String methodName, 
			Object... args) throws Throwable {
		Class<?> clz;
		if (receiver == null) {
			clz = getImportedClass(className);
		} else {
			clz = receiver.getClass();
		}
		String mthSign = clz.getName() + "#" + methodName + "#" + args.length;
		Method mth = cachedMethod.get(mthSign);
		Class<?>[] types = getTypes(args);
		if (mth != null) {
			boolean isReqStatic = Modifier.isStatic(mth.getModifiers());
			boolean reqModifier = (receiver == null) ? isReqStatic : !isReqStatic;
			if (reqModifier && matchParams(mth.getParameterTypes(), types)) {
				mth.setAccessible(true);
				if (mth.getReturnType() == Void.TYPE) {
					mth.invoke(receiver, args);
					return null;
				} else {
					return (T) mth.invoke(receiver, args);
				}
			}
		}
		
		ArrayList<Class<?>> clzs = new ArrayList<Class<?>>();
		while (clz != null) {
			clzs.add(clz);
			clz = clz.getSuperclass();
		}
		
		ArrayList<Method> overloads = new ArrayList<Method>();
		ArrayList<Class<?>[]> paramsTypes = new ArrayList<Class<?>[]>();
		for (Class<?> c : clzs) {
			Method[] mths = c.getDeclaredMethods();
			for (Method m : mths) {
				boolean isStatic = Modifier.isStatic(m.getModifiers());
				boolean modifier = (receiver == null) ? isStatic : !isStatic;
				if (m.getName().equals(methodName) && modifier) {
					Class<?>[] paramTypes = m.getParameterTypes();
					if (matchParams(paramTypes, types)) {
						cachedMethod.put(mthSign, m);
						m.setAccessible(true);
						if (m.getReturnType() == Void.TYPE) {
							m.invoke(receiver, args);
							return null;
						} else {
							return (T) m.invoke(receiver, args);
						}
					} else if (paramTypes.length > 0 && paramTypes[paramTypes.length - 1].isArray() 
							&& types.length >= paramTypes.length - 1) {
						overloads.add(m);
						paramsTypes.add(paramTypes);
					}
				}
			}
		}
		
		for (int i = 0; i < paramsTypes.size(); i++) {
			Class<?>[] paramTypes = paramsTypes.get(i);
			Class<?> componentType = paramTypes[paramTypes.length - 1].getComponentType();
			if (tryMatchParams(paramTypes, types)) {
				Object[] newArgs = new Object[args.length + 1];
				System.arraycopy(args, 0, newArgs, 0, args.length);
				newArgs[args.length] = Array.newInstance(componentType, 0);
				Method m = overloads.get(i);
				m.setAccessible(true);
				if (m.getReturnType() == Void.TYPE) {
					m.invoke(receiver, newArgs);
					return null;
				} else {
					return (T) m.invoke(receiver, newArgs);
				}
			} else {
				boolean isElement = true;
				for (int t = paramTypes.length - 1; t < types.length; t++) {
					if (!types[t].equals(componentType)) {
						isElement = false;
						break;
					}
				}
				if (isElement) {
					int arrLen = types.length - paramTypes.length + 1;
					Object arr = Array.newInstance(componentType, arrLen);
					for (int e = 0; e < arrLen; e++) {
						Array.set(arr, e, args[paramTypes.length - 1 + e]);
					}
					Object[] newArgs = new Object[paramTypes.length];
					System.arraycopy(args, 0, newArgs, 0, paramTypes.length - 1);
					newArgs[paramTypes.length - 1] = arr;
					Method m = overloads.get(i);
					m.setAccessible(true);
					if (m.getReturnType() == Void.TYPE) {
						m.invoke(receiver, newArgs);
						return null;
					} else {
						return (T) m.invoke(receiver, newArgs);
					}
				}
			}
		}
		
		//#if def{debuggable}
		throw new NoSuchMethodException("className: " + receiver.getClass() + ", methodName: " + methodName + ", args: " + Arrays.toString(args));
		//#else
		//#=throw new NoSuchMethodException("className: " + receiver.getClass() + ", methodName: " + methodName);
		//#endif
	}
	
	/**
	 * 调用指定对象的一个实例方法
	 * 
	 * @param receiver 执行方法的实例
	 * @param methodName 方法名称
	 * @param args 参数列表
	 */
	public static <T extends Object> T invokeInstanceMethod(Object receiver, String methodName, 
			Object... args) throws Throwable {
		try {
			return invokeMethod(null, receiver, methodName, args);
		} catch (Throwable t) {
			if (t instanceof NoSuchMethodException) {
				throw t;
			} else {
				//#if def{debuggable}
				String msg = "className: " + receiver.getClass() + ", methodName: " + methodName + ", args: " + Arrays.toString(args);
				//#else
				//#=String msg = "className: " + receiver.getClass() + ", methodName: " + methodName;
				//#endif
				throw new Throwable(msg, t);
			}
		}
	}
	
	/**
	 * 获取指定类的一个静态字段的值
	 * 
	 * @param className {@link #importClass(String, String)}方法返回的类名
	 * @param fieldName 字段名称
	 */
	public static <T extends Object> T getStaticField(String className, String fieldName) 
			throws Throwable {
		try {
			return onGetStaticField(className, fieldName);
		} catch (Throwable t) {
			if (t instanceof NoSuchFieldException) {
				throw t;
			} else {
				String msg = "className: " + className + ", fieldName: " + fieldName;
				throw new Throwable(msg, t);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Object> T onGetStaticField(String className, String fieldName) 
			throws Throwable {
		ArrayList<Class<?>> clzs = new ArrayList<Class<?>>();
		Class<?> clz = getImportedClass(className);
		while (clz != null) {
			clzs.add(clz);
			clz = clz.getSuperclass();
		}
		
		for (Class<?> c : clzs) {
			Field fld = null;
			try {
				fld = c.getDeclaredField(fieldName);
			} catch (Throwable t) {}
			if (fld != null && Modifier.isStatic(fld.getModifiers())) {
				fld.setAccessible(true);
				return (T) fld.get(null);
			}
		}
		
		throw new NoSuchFieldException("className: " + className + ", fieldName: " + fieldName);
	}
	
	/**
	 * 向指定类的一个静态字段设置一个值
	 * 
	 * @param className {@link #importClass(String, String)}方法返回的类名
	 * @param fieldName 字段名称
	 * @param value 输入的值
	 */
	public static void setStaticField(String className, String fieldName, Object value) 
			throws Throwable {
		try {
			onSetStaticField(className, fieldName, value);
		} catch (Throwable t) {
			if (t instanceof NoSuchFieldException) {
				throw t;
			} else {
				String msg = "className: " + className + ", fieldName: " + fieldName + ", value: " + String.valueOf(value);
				throw new Throwable(msg, t);
			}
		}
	}
	
	private static void onSetStaticField(String className, String fieldName, Object value) 
			throws Throwable {
		ArrayList<Class<?>> clzs = new ArrayList<Class<?>>();
		Class<?> clz = getImportedClass(className);
		while (clz != null) {
			clzs.add(clz);
			clz = clz.getSuperclass();
		}
		
		for (Class<?> c : clzs) {
			Field fld = null;
			try {
				fld = c.getDeclaredField(fieldName);
			} catch (Throwable t) {}
			if (fld != null && Modifier.isStatic(fld.getModifiers())) {
				fld.setAccessible(true);
				fld.set(null, value);
				return;
			}
		}
		
		throw new NoSuchFieldException("className: " + className + ", fieldName: " + fieldName + ", value: " + String.valueOf(value));
	}
	
	/**
	 * 获取指定对象的一个实例字段的值
	 * 
	 * @param receiver 被读取数据的对象
	 * @param fieldName 字段名称，如果要读取数组实例的元素，字段名称为“[元素索引]”
	 */
	public static <T extends Object> T getInstanceField(Object receiver, String fieldName) 
			throws Throwable {
		try {
			return onGetInstanceField(receiver, fieldName);
		} catch (Throwable t) {
			if (t instanceof NoSuchFieldException) {
				throw t;
			} else {
				String msg = "className: " + receiver.getClass() + ", fieldName: " + fieldName;
				throw new Throwable(msg, t);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Object> T onGetInstanceField(Object receiver, String fieldName) 
			throws Throwable {
		if (receiver.getClass().isArray()) {
			return (T) onGetElement(receiver, fieldName);
		}
		
		ArrayList<Class<?>> clzs = new ArrayList<Class<?>>();
		Class<?> clz = receiver.getClass();
		while (clz != null) {
			clzs.add(clz);
			clz = clz.getSuperclass();
		}
		
		for (Class<?> c : clzs) {
			Field fld = null;
			try {
				fld = c.getDeclaredField(fieldName);
			} catch (Throwable t) {}
			if (fld != null && !Modifier.isStatic(fld.getModifiers())) {
				fld.setAccessible(true);
				return (T) fld.get(receiver);
			}
		}
		
		throw new NoSuchFieldException("className: " + receiver.getClass() + ", fieldName: " + fieldName);
	}
	
	private static Object onGetElement(Object receiver, String fieldName) throws Throwable {
		if ("length".equals(fieldName)) {
			return Array.getLength(receiver);
		} else if (fieldName.startsWith("[") && fieldName.endsWith("]")) {
			int index = -1;
			try {
				String strIndex = fieldName.substring(1, fieldName.length() - 1);
				index = Integer.parseInt(strIndex);
			} catch (Throwable t) {}
			if (index != -1) {
				return Array.get(receiver, index);
			}
		}
		
		throw new NoSuchFieldException("className: " + receiver.getClass() + ", fieldName: " + fieldName);
	}
	
	/**
	 * 向指定对象的一个实例字段设置一个值
	 * 
	 * @param className 被设置数据的对象
	 * @param fieldName 字段名称，如果要修改数组实例的元素，字段名称为“[元素索引]”
	 * @param value 输入的值
	 */
	public static void setInstanceField(Object receiver, String fieldName, Object value) 
			throws Throwable {
		try {
			onSetInstanceField(receiver, fieldName, value);
		} catch (Throwable t) {
			if (t instanceof NoSuchFieldException) {
				throw t;
			} else {
				
				String msg = "className: " + receiver.getClass() + ", fieldName: " + fieldName + ", value: " + String.valueOf(value);
				throw new Throwable(msg, t);
			}
		}
	}
	
	private static void onSetInstanceField(Object receiver, String fieldName, Object value) 
			throws Throwable {
		if (receiver.getClass().isArray()) {
			onSetElement(receiver, fieldName, value);
			return;
		}
		
		ArrayList<Class<?>> clzs = new ArrayList<Class<?>>();
		Class<?> clz = receiver.getClass();
		while (clz != null) {
			clzs.add(clz);
			clz = clz.getSuperclass();
		}
		
		for (Class<?> c : clzs) {
			Field fld = null;
			try {
				fld = c.getDeclaredField(fieldName);
			} catch (Throwable t) {}
			if (fld != null && !Modifier.isStatic(fld.getModifiers())) {
				fld.setAccessible(true);
				fld.set(receiver, value);
				return;
			}
		}
		
		throw new NoSuchFieldException("className: " + receiver.getClass() + ", fieldName: " + fieldName + ", value: " + String.valueOf(value));
	}
	
	private static void onSetElement(Object receiver, String fieldName, Object value) throws Throwable {
		if (fieldName.startsWith("[") && fieldName.endsWith("]")) {
			int index = -1;
			try {
				String strIndex = fieldName.substring(1, fieldName.length() - 1);
				index = Integer.parseInt(strIndex);
			} catch (Throwable t) {}
			if (index != -1) {
				String recClzName = receiver.getClass().getName();
				while (recClzName.startsWith("[")) {
					recClzName = recClzName.substring(1);
				}
				Class<?> vClass = value.getClass();
				
				if ("B".equals(recClzName)) {
					if (vClass == Byte.class) {
						Array.set(receiver, index, value);
						return;
					}
				} else if ("S".equals(recClzName)) {
					Object sValue = null;
					if (vClass == Short.class) {
						sValue = value;
					} else if (vClass == Byte.class) {
						sValue = Short.valueOf(((Byte) value).byteValue());
					}
					if (sValue != null) {
						Array.set(receiver, index, sValue);
						return;
					}
				} else if ("I".equals(recClzName)) {
					Object iValue = null;
					if (vClass == Integer.class) {
						iValue = value;
					} else if (vClass == Short.class) {
						iValue = Integer.valueOf(((Short) value).shortValue());
					} else if (vClass == Byte.class) {
						iValue = Integer.valueOf(((Byte) value).byteValue());
					}
					if (iValue != null) {
						Array.set(receiver, index, iValue);
						return;
					}
				} else if ("J".equals(recClzName)) {
					Object jValue = null;
					if (vClass == Long.class) {
						jValue = value;
					} else if (vClass == Integer.class) {
						jValue = Long.valueOf(((Integer) value).intValue());
					} else if (vClass == Short.class) {
						jValue = Long.valueOf(((Short) value).shortValue());
					} else if (vClass == Byte.class) {
						jValue = Long.valueOf(((Byte) value).byteValue());
					}
					if (jValue != null) {
						Array.set(receiver, index, jValue);
						return;
					}
				} else if ("F".equals(recClzName)) {
					Object fValue = null;
					if (vClass == Float.class) {
						fValue = value;
					} else if (vClass == Long.class) {
						fValue = Float.valueOf(((Long) value).longValue());
					} else if (vClass == Integer.class) {
						fValue = Float.valueOf(((Integer) value).intValue());
					} else if (vClass == Short.class) {
						fValue = Float.valueOf(((Short) value).shortValue());
					} else if (vClass == Byte.class) {
						fValue = Float.valueOf(((Byte) value).byteValue());
					}
					if (fValue != null) {
						Array.set(receiver, index, fValue);
						return;
					}
				} else if ("D".equals(recClzName)) {
					Object dValue = null;
					if (vClass == Double.class) {
						dValue = value;
					} else if (vClass == Float.class) {
						dValue = Double.valueOf(((Float) value).floatValue());
					} else if (vClass == Long.class) {
						dValue = Double.valueOf(((Long) value).longValue());
					} else if (vClass == Integer.class) {
						dValue = Double.valueOf(((Integer) value).intValue());
					} else if (vClass == Short.class) {
						dValue = Double.valueOf(((Short) value).shortValue());
					} else if (vClass == Byte.class) {
						dValue = Double.valueOf(((Byte) value).byteValue());
					}
					if (dValue != null) {
						Array.set(receiver, index, dValue);
						return;
					}
				} else if ("Z".equals(recClzName)) {
					if (vClass == Boolean.class) {
						Array.set(receiver, index, value);
						return;
					}
				} else if ("C".equals(recClzName)) {
					if (vClass == Character.class) {
						Array.set(receiver, index, value);
						return;
					}
				} else if (recClzName.equals(vClass.getName())) {
					Array.set(receiver, index, value);
					return;
				}
			}
		}
		
		throw new NoSuchFieldException("className: " + receiver.getClass() + ", fieldName: " + fieldName + ", value: " + String.valueOf(value));
	}
	
	/**
	 * 根据类名获取缓存的类对象
	 * 
	 * @param name {@link #importClass(String, String)}方法返回的类名
	 */
	public static Class<?> getClass(String name) throws Throwable {
		Class<?> clz = getImportedClass(name);
		if (clz == null) {
			try {
				clz = Class.forName(name);
				if (clz != null) {
					classMap.put(name, clz);
				}
			} catch (Throwable t) {}
		}
		return clz;
	}
	
	/**
	 * 通过类对象换取其缓存的名称
	 * 
	 * @param clz {@link #importClass(String, String)}方法缓存的类对象
	 */
	public static String getName(Class<?> clz) throws Throwable {
		String name = nameMap.get(clz);
		if (name == null) {
			name = clz.getSimpleName();
			if (classMap.containsKey(name)) {
				name = null;
			} else {
				classMap.put(name, clz);
				nameMap.put(clz, name);
			}
		}
		return name;
	}
	
	/**
	 * 创建一组指定接口的代理实例
	 * <p>
	 * 其工作原理是这样子的：<br>
	 * 1、确定须要实现代理的接口的方法名称，这可能会有好几个<br>
	 * 2、为每一个方法创建一个{@link ReflectRunnable}实例，并且将方法体实现在{@link ReflectRunnable}实例中<br>
	 * 3、以方法名称为key，以{@link ReflectRunnable}实例为value，将一系列的方法代理存入proxyHandler中<br>
	 * 4、调用本方法，得到一个接口的代理实例，并将其传递到需要使用的地方<br>
	 * 5、当代理的某个方法被调用时，实际上是通过方法名称到proxyHandler中寻找对应的{@link ReflectRunnable}实例，并执行之
	 * 
	 * @param proxyHandler 代理方法集合
	 * @param proxyInteface 被代理的接口
	 * @return 代理接口的实例
	 */
	public static Object createProxy(final HashMap<String, ReflectRunnable> proxyHandler, Class<?>... proxyIntefaces)
			throws Throwable {
		ClassLoader loader = proxyHandler.getClass().getClassLoader();
		InvocationHandler handler = new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				ReflectRunnable function = proxyHandler.get(method.getName());
				if (function != null) {
					return function.run(args);
				}
				throw new NoSuchMethodException();
			}
		};
		return Proxy.newProxyInstance(loader, proxyIntefaces, handler);
	}
	
	/** 一个类似于{@link Runnable}的类，但是拥有输入参数和返回值。主要用于实现方法代理 */
	public static interface ReflectRunnable {
		public Object run(Object arg);
	}
	
}
