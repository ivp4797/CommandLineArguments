package frut.cli.commandline;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public final class ParserTest {
	@Test
	public void testEmptyArgsListWhenTarEnabled() {
		try {
			final Parser parser = new Parser(true);
			final List<Entry> entries = parser.parse();
			Assertions.assertTrue(entries.isEmpty(), "entries should be empty");
		} catch (final IOException e) {
			Assertions.fail(e);
		}
	}

	@Test
	public void testEmptyArgsListWhenTarDisabled() {
		try {
			final Parser parser = new Parser(false);
			final List<Entry> entries = parser.parse();
			Assertions.assertTrue(entries.isEmpty(), "entries should be empty");
		} catch (final IOException e) {
			Assertions.fail(e);
		}
	}

	@Test
	public void testAllStandaloneWhenNoOptionsAndTarDisabled() {
		try {
			final String[] args = { "-", "arg1", "arg2", "arg3", "-" };
			final Parser parser = new Parser(false);
			final List<Entry> entries = parser.parse(args);
			Assertions.assertEquals(args.length, entries.size(), "args and entries should be of the same size");
			for (int i = 0; i < entries.size(); i++) {
				final Entry entry = entries.get(i);
				Assertions.assertNull(entry.name, "entry[" + i + "] should have null name");
				Assertions.assertEquals(entry.param, args[i]);
			}
		} catch (final IOException e) {
			Assertions.fail(e);
		}
	}

	@Test
	public void testTarAllRequireParams() {
		try {
			final String[] args = { "abcd", "aqwerty", "bqwerty", "cqwerty", "dqwerty", "free1", "free2" };
			final Option[] opts = {
				new Option("a", "aopt", true, true),
				new Option("b", "bopt", true, true),
				new Option("c", "copt", true, true),
				new Option("d", "dopt", true, true),
			};
			final Parser parser = new Parser(true, opts);
			final List<Entry> entries = parser.parse(args);
			Assertions.assertEquals(entries.size(), 6);
			for (int i = 0; i < 4; i++) {
				final Entry entry = entries.get(i);
				Assertions.assertEquals(entry.name, opts[i].name);
				Assertions.assertEquals(entry.param, args[i + 1]);
			}
			Assertions.assertNull(entries.get(4).name);
			Assertions.assertEquals(entries.get(4).param, args[5]);
			Assertions.assertNull(entries.get(5).name);
			Assertions.assertEquals(entries.get(5).param, args[6]);
		} catch (final IOException e) {
			Assertions.fail(e);
		}
	}

	@Test
	public void testTarSomeRequireParamsAndOthersDontTakeParams() {
		try {
			final String[] args = { "abcd", "aqwerty", "dqwerty", "free1", "free2" };
			final Option[] opts = {
				new Option("a", "aopt", true, true),
				new Option("b", "bopt", false, false),
				new Option("c", "copt", false, false),
				new Option("d", "dopt", true, true),
			};
			final Parser parser = new Parser(true, opts);
			final List<Entry> entries = parser.parse(args);
			Assertions.assertEquals(entries.size(), 6);
			Assertions.assertEquals(entries.get(0).name, opts[0].name);
			Assertions.assertEquals(entries.get(0).param, args[1]);
			Assertions.assertEquals(entries.get(1).name, opts[1].name);
			Assertions.assertNull(entries.get(1).param);
			Assertions.assertEquals(entries.get(2).name, opts[2].name);
			Assertions.assertNull(entries.get(2).param);
			Assertions.assertEquals(entries.get(3).name, opts[3].name);
			Assertions.assertEquals(entries.get(3).param, args[2]);
			Assertions.assertNull(entries.get(4).name);
			Assertions.assertEquals(entries.get(4).param, args[3]);
			Assertions.assertNull(entries.get(5).name);
			Assertions.assertEquals(entries.get(5).param, args[4]);
		} catch (final IOException e) {
			Assertions.fail(e);
		}
	}

	@Test
	public void testTarOptionalParamsWhenTheyArePresent() {
		try {
			final String[] args = { "abcd", "aqwerty", "bqwerty", "cqwerty", "dqwerty", "free1", "free2" };
			final Option[] opts = {
				new Option("a", "aopt", true, true),
				new Option("b", "bopt", true, false),
				new Option("c", "copt", true, false),
				new Option("d", "dopt", true, true),
			};
			final Parser parser = new Parser(true, opts);
			final List<Entry> entries = parser.parse(args);
			Assertions.assertEquals(entries.size(), 6);
			for (int i = 0; i < 4; i++) {
				final Entry entry = entries.get(i);
				Assertions.assertEquals(entry.name, opts[i].name);
				Assertions.assertEquals(entry.param, args[i + 1]);
			}
			Assertions.assertNull(entries.get(4).name);
			Assertions.assertEquals(entries.get(4).param, args[5]);
			Assertions.assertNull(entries.get(5).name);
			Assertions.assertEquals(entries.get(5).param, args[6]);
		} catch (final IOException e) {
			Assertions.fail(e);
		}
	}

	@Test
	public void testTarOptionalParamsWhenTheyAreOmitted() {
		try {
			final String[] args = { "abcd", "aqwerty", "bqwerty" };
			final Option[] opts = {
				new Option("a", "aopt", true, false),
				new Option("b", "bopt", true, true),
				new Option("c", "copt", true, false),
				new Option("d", "dopt", false, false),
			};
			final Parser parser = new Parser(true, opts);
			final List<Entry> entries = parser.parse(args);
			Assertions.assertEquals(entries.size(), 4);
			Assertions.assertEquals(entries.get(0).name, opts[0].name);
			Assertions.assertEquals(entries.get(0).param, args[1]);
			Assertions.assertEquals(entries.get(1).name, opts[1].name);
			Assertions.assertEquals(entries.get(1).param, args[2]);
			Assertions.assertEquals(entries.get(2).name, opts[2].name);
			Assertions.assertNull(entries.get(2).param);
			Assertions.assertEquals(entries.get(3).name, opts[3].name);
			Assertions.assertNull(entries.get(3).param);
		} catch (final IOException e) {
			Assertions.fail(e);
		}
	}

	@Test
	public void testRealWorldExample() {
		try {
			final String[] args = {
				"cxjf", "filename.dat",
				"-o", "output.txt",
				"-vqr", "random",
				"--longopt=value",
				"--",
				"-i", "--iii", "qq"
			};
			final Option[] options = {
				new Option("create", "create", false, false),
				new Option("extract", "x", false, false),
				new Option("whatever", "j", false, false),
				new Option("file", "file", true, true),
				new Option("output", "output", true, true),
				new Option("v", "v", true, false),
				new Option("q", "q", true, true),
				new Option("r", "r", true, true),
				new Option("longopt", "longopt", true, true),
				new Option("input", "input", true, true)
			};
			final Parser parser = new Parser(true, options);
			final List<Entry> entries = parser.parse(args);
			final String[] expectedNames = {
				"create", "extract", "whatever", "file",
				"output", "v", null, "longopt", null, null, null
			};
			final String[] expectedParams = {
				null, null, null, "filename.dat",
				"output.txt", "qr", "random",
				"value", "-i", "--iii", "qq"
			};
			for (int i = 0; i < entries.size(); i++) {
				Assertions.assertEquals(entries.get(i).name, expectedNames[i]);
				Assertions.assertEquals(entries.get(i).param, expectedParams[i]);
			}
		} catch (final IOException e) {
			Assertions.fail(e);
		}
	}
}
