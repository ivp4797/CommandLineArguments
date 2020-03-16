package frut.cli.commandline;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Parser {
	private final boolean tarEnabled;
	private final Option[] options;

	public Parser(final boolean tarEnabled, final Option... options) {
		this.tarEnabled = tarEnabled;
		this.options = options;
	}

	public List<Entry> parse(final String... args) throws IOException {
		if (args.length == 0) {
			return Collections.emptyList();
		}
		final List<Entry> entries = new LinkedList<>();
		final int pos;

		// TAR
		if (tarEnabled && !args[0].startsWith("-")) {
			// treat this as a tar master arg
			final String master = args[0];
			final char[] tokens = master.toCharArray();
			int nextParamIndex = 1;
			for (final char token : tokens) {
				final Option opt = findByFirstLetter(token);
				if (opt == null) {
					throw new IOException("unrecognized tar-style option " + opt);
				}
				final String param;
				if (opt.takesParam) {
					final boolean hasParam = nextParamIndex < args.length;
					if (opt.requiresParam && !hasParam) {
						throw new IOException("tar-style option " + opt + " requires a parameter, but none was supplied");
					}
					else if (hasParam) {
						param = args[nextParamIndex++];
					}
					else {
						param = null;
					}
				}
				else {
					param = null;
				}
				entries.add(new Entry(opt.name, param));
			}
			pos = nextParamIndex;
		}
		else {
			pos = 0;
		}

		boolean terminated = false;
		String currentOptName = null;
		for (int i = pos; i < args.length; i++) {
			final String arg = args[i];
			if (terminated || arg.equals("-") || !arg.startsWith("-")) {
				entries.add(new Entry(currentOptName, arg));
				currentOptName = null;
				continue;
			}
			if (arg.equals("--")) {
				terminated = true;
				continue;
			}
			if (currentOptName != null) {
				throw new IOException("option " + currentOptName + " requires an argument, but none was supplied");
			}
			if (arg.startsWith("--")) {
				final String master = arg.substring(2);
				final int eqIndex = master.indexOf('=');
				final String opt, param;
				if (eqIndex < 0) {
					opt = master;
					param = null;
				}
				else if (eqIndex == 0) {
					throw new IOException("empty option name in long-style option " + master);
				}
				else {
					opt = master.substring(0, eqIndex);
					param = master.substring(eqIndex + 1);
				}
				final Option option = findByAbbreviation(opt);
				if (option == null) {
					throw new IOException("unrecognized long-style option " + opt);
				}
				if (!option.takesParam && param != null) {
					throw new IOException("long-style option " + opt + " does not take parameters, but supplied " + param);
				}
				if (option.takesParam && !option.requiresParam && param == null) {
					throw new IOException("optional parameter must be provided after = for long-style option " + opt);
				}
				if (option.requiresParam && param == null) {
					currentOptName = option.name;
				}
				else {
					entries.add(new Entry(option.name, param));
				}
				continue;
			}
			assert arg.startsWith("-");
			final String body = arg.substring(1);
			for (int j = 0; j < body.length(); j++) {
				final char c = body.charAt(j);
				final Option opt = findByFirstLetter(c);
				if (opt == null) {
					throw new IOException("unrecognized short-style option " + opt);
				}
				if (opt.takesParam) {
					final boolean hasParam = j < body.length() - 1;
					final String param = hasParam ? body.substring(j + 1) : null;
					if (opt.requiresParam && !hasParam) {
						currentOptName = opt.name;
					}
					else {
						entries.add(new Entry(opt.name, param));
					}
					break;
				}
				else {
					entries.add(new Entry(opt.name, null));
				}
			}
		}

		if (currentOptName != null) {
			throw new IOException("option " + currentOptName + " requires a parameter but none was supplied");
		}
		return entries;
	}

	private Option findByFirstLetter(final char letter) {
		for (final Option option : options) {
			if (option.alias.charAt(0) == letter) {
				return option;
			}
		}
		return null;
	}

	private Option findByAbbreviation(final String abbr) {
		for (final Option option : options) {
			if (option.alias.startsWith(abbr)) {
				return option;
			}
		}
		return null;
	}
}
