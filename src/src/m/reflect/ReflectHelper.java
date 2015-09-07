package m.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ReflectHelper {
	private static HashSet<String> packageSet;
	private static HashMap<String, Class<?>> classMap;
	private static CachePool<String, Method> cachedMethod;
	private static CachePool<String, Constructor<?>> cachedConstr;
	
	static {
		packageSet = new HashSet<String>();
		packageSet.add("java.lang");
		packageSet.add("java.io");
		packageSet.add("java.net");
		packageSet.add("java.util");
		packageSet.add("com.mob.tools");
		packageSet.add("com.mob.tools.gui");
		packageSet.add("com.mob.tools.log");
		packageSet.add("com.mob.tools.network");
		packageSet.add("com.mob.tools.utils");
		
		classMap = new HashMap<String, Class<?>>();
		cachedMethod = new CachePool<String, Method>(25);
		cachedConstr = new CachePool<String, Constructor<?>>(5);
	}
	
	public static void importClass(String className) throws Throwable {
		importClass(null, className);
	}
	
	public static synchronized void importClass(String name, String className) throws Throwable {
		if (className.endsWith(".*")) {
			packageSet.add(className.substring(0, className.length() - 2));
			return;
		}
		
		Class<?> clz = Class.forName(className);
		if (name == null) {
			name = clz.getSimpleName();
		}
		classMap.put(name, clz);
	}

	private static synchronized Class<?> getClass(String className) {
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
				|| (primitive == short.class && target == Short.class)
				|| (primitive == char.class && target == Character.class)
				|| (primitive == int.class && target == Integer.class)
				|| (primitive == long.class && target == Long.class)
				|| (primitive == float.class && target == Float.class)
				|| (primitive == double.class && target == Double.class)
				|| (primitive == boolean.class && target == Boolean.class));
	}
	
	private static boolean matchParams(Class<?>[] mTypes, Class<?>[] types) {
		if (mTypes.length == types.length) {
			boolean match = true;
			for (int i = 0; i < mTypes.length; i++) {
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
	
	public static Object newInstance(String className, Object... args) throws Throwable {
		try {
			return onNewInstance(className, args);
		} catch (Throwable t) {
			if (t instanceof NoSuchMethodException) {
				throw t;
			} else {
				String msg = "className: " + className + ", methodName: <init>, args: " + Arrays.toString(args);
				throw new Throwable(msg, t);
			}
		}
	}
	
	private static Object onNewInstance(String className, Object... args) throws Throwable {
		String mthSign = className + "#" + args.length;
		Constructor<?> con = cachedConstr.get(mthSign);
		Class<?>[] types = getTypes(args);
		if (con != null && matchParams(con.getParameterTypes(), types)) {
			con.setAccessible(true);
			return con.newInstance(args);
		}
		
		Class<?> clz = getClass(className);
		Constructor<?>[] cons = clz.getDeclaredConstructors();
		for (Constructor<?> c : cons) {
			if (matchParams(c.getParameterTypes(), types)) {
				cachedConstr.put(mthSign, c);
				c.setAccessible(true);
				return c.newInstance(args);
			}
		}
		
		throw new NoSuchMethodException("className: " + className + ", methodName: <init>, args: " + Arrays.toString(args));
	}
	
	public static <T extends Object> T invokeStaticMethod(String className, String methodName, 
			Object... args) throws Throwable {
		try {
			return onInvokeStaticMethod(className, methodName, args);
		} catch (Throwable t) {
			if (t instanceof NoSuchMethodException) {
				throw t;
			} else {
				String msg = "className: " + className + ", methodName: " + methodName + ", args: " + Arrays.toString(args);
				throw new Throwable(msg, t);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Object> T onInvokeStaticMethod(String className, String methodName, 
			Object... args) throws Throwable {
		String mthSign = className + "#" + methodName + "#" + args.length;
		Method mth = cachedMethod.get(mthSign);
		Class<?>[] types = getTypes(args);
		if (mth != null && Modifier.isStatic(mth.getModifiers()) && matchParams(mth.getParameterTypes(), types)) {
			mth.setAccessible(true);
			if (mth.getReturnType() == Void.TYPE) {
				mth.invoke(null, args);
				return null;
			} else {
				return (T) mth.invoke(null, args);
			}
		}
		
		ArrayList<Class<?>> clzs = new ArrayList<Class<?>>();
		Class<?> clz = getClass(className);
		while (clz != null) {
			clzs.add(clz);
			clz = clz.getSuperclass();
		}
		
		for (Class<?> c : clzs) {
			Method[] mths = c.getDeclaredMethods();
			for (Method m : mths) {
				if (m.getName().equals(methodName) && Modifier.isStatic(m.getModifiers()) 
						&& matchParams(m.getParameterTypes(), types)) {
					cachedMethod.put(mthSign, m);
					m.setAccessible(true);
					if (m.getReturnType() == Void.TYPE) {
						m.invoke(null, args);
						return null;
					} else {
						return (T) m.invoke(null, args);
					}
				}
			}
		}
		
		throw new NoSuchMethodException("className: " + className + ", methodName: " + methodName + ", args: " + Arrays.toString(args));
	}
	
	public static <T extends Object> T invokeInstanceMethod(Object receiver, String methodName, 
			Object... args) throws Throwable {
		try {
			return onInvokeInstanceMethod(receiver, methodName, args);
		} catch (Throwable t) {
			if (t instanceof NoSuchMethodException) {
				throw t;
			} else {
				String msg = "className: " + receiver.getClass() + ", methodName: " + methodName + ", args: " + Arrays.toString(args);
				throw new Throwable(msg, t);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Object> T onInvokeInstanceMethod(Object receiver, String methodName, 
			Object... args) throws Throwable {
		Class<?> clz = receiver.getClass();
		String mthSign = clz.getName() + "#" + methodName + "#" + args.length;
		Method mth = cachedMethod.get(mthSign);
		Class<?>[] types = getTypes(args);
		if (mth != null && !Modifier.isStatic(mth.getModifiers()) && matchParams(mth.getParameterTypes(), types)) {
			mth.setAccessible(true);
			if (mth.getReturnType() == Void.TYPE) {
				mth.invoke(receiver, args);
				return null;
			} else {
				return (T) mth.invoke(receiver, args);
			}
		}
		
		ArrayList<Class<?>> clzs = new ArrayList<Class<?>>();
		while (clz != null) {
			clzs.add(clz);
			clz = clz.getSuperclass();
		}
		
		for (Class<?> c : clzs) {
			Method[] mths = c.getDeclaredMethods();
			for (Method m : mths) {
				if (m.getName().equals(methodName) && !Modifier.isStatic(m.getModifiers()) 
						&& matchParams(m.getParameterTypes(), types)) {
					cachedMethod.put(mthSign, m);
					m.setAccessible(true);
					if (m.getReturnType() == Void.TYPE) {
						m.invoke(receiver, args);
						return null;
					} else {
						return (T) m.invoke(receiver, args);
					}
				}
			}
		}
		
		throw new NoSuchMethodException("className: " + receiver.getClass() + ", methodName: " + methodName + ", args: " + Arrays.toString(args));
	}
	
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
		Class<?> clz = getClass(className);
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
		Class<?> clz = getClass(className);
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
	public static <T extends Object> T onGetInstanceField(Object receiver, String fieldName) 
			throws Throwable {
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
	
	public static interface ReflectRunnable {
		public Object run(Object arg);
	}
	
}
