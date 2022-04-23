package de.danielkoellgen.srscsprodtestservice.domain.card.domain;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class TextElement implements ContentElement {

    @NotNull
    private ContentType contentType = ContentType.TEXT;

    @NotNull
    private final String text;

    public TextElement(@NotNull String text) {
        this.text = text;
    }

    public TextElement(@NotNull String text, @NotNull ContentType contentType) {
        this.text = text;
        this.contentType = contentType;
    }
}
