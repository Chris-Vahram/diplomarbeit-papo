package at.papo.partnerportal.shop;

import java.util.List;

// Image library plus storage use for the editor ("12,3 MB von 50 MB belegt").
public record ShopAssetListDto(List<ShopAssetDto> assets, long usedBytes, long quotaBytes) {
}
