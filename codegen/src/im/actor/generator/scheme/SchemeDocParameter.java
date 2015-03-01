package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 18.11.14.
 */
public class SchemeDocParameter extends SchemeDoc {
    private String argument;
    private String description;
    private ParameterCategory category;

    public SchemeDocParameter(String argument, String description, ParameterCategory category) {
        this.argument = argument;
        this.description = description.trim();
        this.category = category;
    }

    public ParameterCategory getCategory() {
        return category;
    }

    public String getArgument() {
        return argument;
    }

    public String getDescription() {
        return description;
    }
}
