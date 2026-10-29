package at.papo.partnerportal.shop;

import java.util.List;

// Designable pages in navigation order plus the limits ("4 von 6 Seiten online").
// setupNeeded: the builder opens "Shop einrichten" first.
public record ShopPageListDto(List<ShopPageDto> pages, long onlineCount, int maxOnline, long pageCount, int maxPages,
                              boolean setupNeeded) {
}
