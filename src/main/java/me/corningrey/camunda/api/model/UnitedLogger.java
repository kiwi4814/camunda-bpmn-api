
package me.corningrey.camunda.api.model;

public class UnitedLogger {
	public static void debug(Object s) {
		System.err.println(s);
		org.apache.log4j.Logger.getLogger(UnitedLogger.class).debug(s);
	}

	public static void error(Object s) {
		System.err.println(s);
		org.apache.log4j.Logger.getLogger(UnitedLogger.class).error(s);
	}

	public static void info(Object s) {
		System.err.println(s);
	}

	public static void debug(Object s, Object invoder) {
		System.err.println(s);
		org.apache.log4j.Logger.getLogger(UnitedLogger.class).error(s);
	}

	public static void error(Object message, Throwable t) {
		System.err.println(message);
		org.apache.log4j.Logger.getLogger(UnitedLogger.class).error(
				message, t);
	}

	public static void error(Throwable t) {
		System.err.println(t);
		org.apache.log4j.Logger.getLogger(UnitedLogger.class).error(
				t.getMessage(), t);
	}
}
