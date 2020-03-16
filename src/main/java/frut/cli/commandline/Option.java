package frut.cli.commandline;

public final class Option {
	public final String name;
	public final String alias;
	public final boolean takesParam;
	public final boolean requiresParam;

	public Option(final String name, final String alias, final boolean takesParam, final boolean requiresParam) {
		this.name = name;
		this.alias = alias;
		this.takesParam = takesParam;
		this.requiresParam = requiresParam;
	}
}
