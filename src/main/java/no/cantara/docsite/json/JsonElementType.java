package no.cantara.docsite.json;

public enum JsonElementType {
    ROOT("[root]"),
    OBJECT("[object]"),
    ARRAY("[array]"),
    VALUE("[value]"),
    ARRAY_VALUE("[array_value]");

    private final String typeName;

    JsonElementType(final String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}