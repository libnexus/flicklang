package org.flick.lang;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * The command interface the the flick language which contains the main method
 * to parse the arguments
 * 
 * @author Q
 *
 */
public class CommandInterface {
	/**
	 * The only scanner that will be needed because we'll only read from System.in
	 */
	private static Scanner scanner = new Scanner(System.in);

	/**
	 * The main method to run with given arguments
	 * 
	 * @param args the run arguments
	 */
	public static void main(String[] args) {
		executeFile("module.fl");
	}

	/**
	 * Returns a string input from the command line
	 * 
	 * @param prompt what to prompt the user with
	 * @return what the user typed in
	 */
	public static String input(String prompt) {
		System.out.print(prompt);
		return scanner.nextLine();
	}

	/**
	 * Returns a string that can properly compile across multiple lines
	 * 
	 * @return the user string
	 */
	public static String multiLineInput() {
		String script = "";
		System.out.print(">>> ");
		String line = scanner.nextLine();
		script += line;

		while (!line.isBlank()) {
			System.out.print("... ");
			line = scanner.nextLine();
			script += line + "\n";
		}

		return script;
	}
	
	
	public static void executeFile(String fileName) {
		Scanner file;
		String script = "";
		
		try {
			file = new Scanner(new File(fileName));
			while (file.hasNextLine()) {
				script += file.nextLine() + "\n";
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		long startTime = 0, endTime = 0;
		
		BytecodeParser codeParser = new BytecodeParser(script);
		HashMap<String, Object> options = codeParser.parseOptions();
				
		if ((Boolean) options.getOrDefault("printScript", false))
			System.out.println(script);
		
		BytecodeInstructionSet code = codeParser.newInstructionSet((String) options.getOrDefault("scriptName", "<module.fl>"));
		Controller.makeModuleInstructionSet(code);
		
		if ((Boolean) options.getOrDefault("timeExecution", false))
			startTime = System.nanoTime();
		codeParser.parseModule();
		if ((Boolean) options.getOrDefault("timeExecution", false)) {
			endTime = System.nanoTime();
			System.out.println(String.format("Parsing of %s took %fms", code.name, (endTime - startTime) / 1000000f));
		}		        
		
		if ((Boolean) options.getOrDefault("disScript", false))
			BytecodeDisassembler.disassemble(code);
		
		String decompiled = BytecodeCompilerParser.disDecompile(code);
								
		StackFrame frame = new StackFrame(code, new ScopedTable(null));
		Controller.makeModuleStackFrame(frame);
		BytecodeInterpreter interpreter = new BytecodeInterpreter();
		interpreter.addStackFrame(frame);
		
		if ((Boolean) options.getOrDefault("timeExecution", false))
			startTime = System.nanoTime();
		interpreter.execute();
		if ((Boolean) options.getOrDefault("timeExecution", false)) {
			endTime = System.nanoTime();
			System.out.println(String.format("Execution of %s took %fms", code.name, (endTime - startTime) / 1000000f));
		}
		
		File recompiledCode = new File(fileName + ".flcm");
		
		try {
			recompiledCode.createNewFile();
			FileWriter recompiledCodeWriter = new FileWriter(recompiledCode);
			recompiledCodeWriter.write(decompiled);
			recompiledCodeWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
}
