package im.actor.runtime.markdown;

public class MDDocument {
    private MDSection[] sections;

    public MDDocument(MDSection[] sections) {
        this.sections = sections;
    }

    public MDSection[] getSections() {
        return sections;
    }
}
