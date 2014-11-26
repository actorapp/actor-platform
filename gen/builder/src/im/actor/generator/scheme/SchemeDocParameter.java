package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 18.11.14.
 */
public class SchemeDocParameter extends SchemeDoc {
    private String argument;
    private String description;

    public SchemeDocParameter(String argument, String description) {
        this.argument = argument;
        this.description = description;
    }

    public String getArgument() {
        return argument;
    }

    public String getDescription() {
        return description;
    }
}
