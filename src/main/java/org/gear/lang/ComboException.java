package org.gear.lang;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class ComboException extends RuntimeException {

	private static final long serialVersionUID = 3643627374483504274L;
	
	private List<Throwable> list;
	
	public ComboException() {
		list = new LinkedList<Throwable>();
	}
	
	public ComboException add(Throwable e) {
		list.add(e);
		return this;
	}
	
	public Throwable getCause() {
		return list.isEmpty() ? null : list.get(0);
	}
	
	public String getLocalizedMessage() {
		StringBuilder sb = new StringBuilder();
		for(Throwable e : list)
			sb.append(e.getLocalizedMessage()).append("\n");
		return sb.toString();
	}
	
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		for(Throwable e : list) 
			sb.append(e.getMessage()).append("\n");
		return sb.toString();
	}
	
	public StackTraceElement[] getStackTrace() {
		List<StackTraceElement> eles = new LinkedList<StackTraceElement>();
		for(Throwable e : list)
			for(StackTraceElement ste : e.getStackTrace())
				eles.add(ste);
		return eles.toArray(new StackTraceElement[eles.size()]);
	}
	
	public void printStackTrace() {
		for(Throwable e : list)
			e.printStackTrace();
	}
	
	public void printStackTrace(PrintStream ps) {
		for(Throwable e : list)
			e.printStackTrace(ps);
	}
	
	public void printStackTrace(PrintWriter pw) {
		for(Throwable e : list)
			e.printStackTrace(pw);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Throwable e : list)
			sb.append(e.toString()).append("\n");
		return sb.toString();
	}

}
