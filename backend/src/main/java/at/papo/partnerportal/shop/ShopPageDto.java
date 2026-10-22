package at.papo.partnerportal.shop;

import java.time.Instant;
import java.util.Objects;

// Editor view of one page (no content). draftChanged = draft differs from the published
// version; restorable = a previous published version exists.
public record ShopPageDto(
        Long id,
        ShopPageKind kind,
        ShopPageSlot slot,
        String segment,
        String menuName,
        int position,
        boolean online,
        boolean draftChanged,
        boolean restorable,
        String seoTitle,
        String seoDescription,
        long version,
        Instant publishedAt
) {
    static ShopPageDto from(ShopPage page) {
        return new ShopPageDto(page.id, page.kind, page.slot, page.segment, page.menuName, page.position,
                page.online, !Objects.equals(page.draftContent, page.publishedContent),
                page.previousPublishedContent != null, page.seoTitle, page.seoDescription, page.version,
                page.publishedAt);
    }
}
