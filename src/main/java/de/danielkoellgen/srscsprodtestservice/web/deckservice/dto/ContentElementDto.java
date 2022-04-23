package de.danielkoellgen.srscsprodtestservice.web.deckservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.ContentType;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.ImageElement;
import de.danielkoellgen.srscsprodtestservice.domain.card.domain.TextElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record
ContentElementDto(

    @NotNull
    String contentType,

    @Nullable
    String text,

    @Nullable
    String url
) {
    public static ContentElementDto makeAsText(@NotNull TextElement textElement) {
        return new ContentElementDto("text", textElement.getText(), null);
    }

    public static ContentElementDto makeAsImage(@NotNull ImageElement imageElement) {
        return new ContentElementDto("image", null, imageElement.getUrl());
    }

    @JsonIgnore
    public @NotNull ContentType getMappedContentType() {
        return switch (contentType) {
            case "text" -> ContentType.TEXT;
            case "image" -> ContentType.IMAGE;
            default -> throw new RuntimeException("Invalid content-type.");
        };
    }

    @JsonIgnore
    public @NotNull ImageElement getAsImageElement() {
        if (getMappedContentType() != ContentType.IMAGE) {
            throw new RuntimeException("Invalid type-cast to type ImageElement.");
        }
        if (url == null) {
            throw new RuntimeException("Url not set while trying to cast to type ImageElement.");
        }
        return new ImageElement(url);
    }

    @JsonIgnore
    public @NotNull TextElement getAsTextElement() {
        if (getMappedContentType() != ContentType.TEXT) {
            throw new RuntimeException("Invalid type-cast to type TextElement.");
        }
        if (text == null) {
            throw new RuntimeException("Text not set while trying to cast to type TextElement.");
        }
        return new TextElement(text);
    }
}
