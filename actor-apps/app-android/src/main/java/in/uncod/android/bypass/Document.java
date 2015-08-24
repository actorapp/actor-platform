package in.uncod.android.bypass;

public class Document {

    private Element[] elements;

    public Document(Element[] elements) {
        this.elements = elements;
    }

    public int getElementCount() {
        return elements.length;
    }

    public Element getElement(int pos) {
        return elements[pos];
    }
}
