package im.actor.runtime.markdown;

public class MDDocument {
    private MDSection[] sections;

    public MDDocument(MDSection[] sections) {
        this.sections = sections;
    }

    public MDSection[] getSections() {
        return sections;
    }

    public String toMarkdown() {
        String res = "";
        for (MDSection section : sections) {
            if (res.length() > 0) {
                res += "\n";
            }
            res += section.toMarkdown();
        }
        return res;
    }
}
