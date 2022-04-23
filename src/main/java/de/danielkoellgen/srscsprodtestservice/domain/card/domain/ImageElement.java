package de.danielkoellgen.srscsprodtestservice.domain.card.domain;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class ImageElement implements ContentElement {

    @NotNull
    private ContentType contentType = ContentType.IMAGE;

    @NotNull
    private final String url;

    public ImageElement(@NotNull String url) {
        this.url = url;
    }

    public ImageElement(@NotNull String url, @NotNull ContentType contentType) {
        this.url = url;
        this.contentType = contentType;
    }
}
