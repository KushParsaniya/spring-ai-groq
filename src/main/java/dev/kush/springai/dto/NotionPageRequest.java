package dev.kush.springai.dto;

import java.util.List;

public class NotionPageRequest {

    public record NotionPagePayload(
            Parent parent,
            Icon icon,
            Cover cover,
            Properties properties,
            List<Child> children
    ) {}

    // Parent Information
    public record Parent(String database_id) {}

    // Icon Information
    public record Icon(String emoji) {}

    // Cover Information
    public record Cover(External external) {}

    public record External(String url) {}

    // Properties Section
    public record Properties(
            Title name,
            RichText description,
            Select category,
            NumberField complexity
    ) {}

    public record Title(List<Text> title) {}

    public record RichText(List<Text> rich_text) {}

    public record Text(TextContent text) {}

    public record TextContent(String content, String link) {}

    public record Select(String name) {}

    public record NumberField(Double number) {}

    // Children Section
    public sealed interface Child permits Heading2, Paragraph, Code, BulletedListItem {}

    public record Heading2(RichText rich_text) implements Child {}

    public record Paragraph(RichText rich_text) implements Child {}

    public record BulletedListItem(RichText rich_text) implements Child {}

    public record Code(String language, RichText rich_text) implements Child {}

}