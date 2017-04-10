package de.dosmike.twitch.knarf;

public class Console {
	public static final String RESET = "\u001B[0m";
	public class FG {
		public static final String BLACK = "\u001B[30m";
		public static final String RED = "\u001B[31m";
		public static final String GREEN = "\u001B[32m";
		public static final String YELLOW = "\u001B[33m";
		public static final String BLUE = "\u001B[34m";
		public static final String PURPLE = "\u001B[35m";
		public static final String CYAN = "\u001B[36m";
		public static final String WHITE = "\u001B[37m";
	}
	public class BG {
		public static final String BLACK = "\u001B[40m";
		public static final String RED = "\u001B[41m";
		public static final String GREEN = "\u001B[42m";
		public static final String YELLOW = "\u001B[43m";
		public static final String BLUE = "\u001B[44m";
		public static final String PURPLE = "\u001B[45m";
		public static final String CYAN = "\u001B[46m";
		public static final String WHITE = "\u001B[47m";
	}
	//Will do bright colors
	public static final String BOLD = "\u001B[1m";
	//Same as FG, but turning BOLD on with it
	public class FB {
		public static final String BLACK = "\u001B[1;30m";
		public static final String RED = "\u001B[1;31m";
		public static final String GREEN = "\u001B[1;32m";
		public static final String YELLOW = "\u001B[1;33m";
		public static final String BLUE = "\u001B[1;34m";
		public static final String PURPLE = "\u001B[1;35m";
		public static final String CYAN = "\u001B[1;36m";
		public static final String WHITE = "\u001B[1;37m";
	}
	public static final String UNDERLINE = "\u001B[4m";
	public static final String BLINKING = "\u001B[5m";
	public static final String NEGATIVE_IMAGE = "\u001B[7m";
	public static final String INVISIBLE_IMAGE = "\u001B[8m";
	//Will turn off bright colors
	public static final String BOLD_OFF = "\u001B[22m";
	public static final String UNDERLINE_OFF = "\u001B[24m";
	public static final String BLINKING_OFF = "\u001B[25m";
	public static final String NEGATIVE_IMAGE_OFF = "\u001B[27m";
	public static final String INVISIBLE_IMAGE_OFF = "\u001B[28m";
	public static final String LINE_RESET = "\u001B[M\r";
	
	public static void printf(String format, Object...args) {
		System.out.printf(format, args);
	}
	public static void println(String... args) {
		System.out.println(String.join("", args));
	}
	public static void print(String... args) {
		System.out.print(String.join("", args));
	}
}
