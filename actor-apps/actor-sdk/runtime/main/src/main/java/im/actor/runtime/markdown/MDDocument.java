package im.actor.runtime.markdown;

public class MDDocument {
    private MDSection[] sections;

    public MDDocument(MDSection[] sections) {
        this.sections = sections;
    }

    public MDSection[] getSections() {
        return sections;
    }

    public boolean isTrivial() {
        if (sections.length == 1) {
            if (sections[0].getType() == MDSection.TYPE_TEXT) {
                if (sections[0].getText().length == 1) {
                    if (sections[0].getText()[0] instanceof MDRawText) {
                        return true;
                    }
                }
            }
        }
        return false;
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
