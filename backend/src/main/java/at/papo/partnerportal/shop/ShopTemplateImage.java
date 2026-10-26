package at.papo.partnerportal.shop;

// Bundled sample photo of the template catalogue (shop-templates spec "Bundled sample images"),
// listed in shop-templates/images/index.json with its source and licence. Templates reference it
// as "vorlage:<name>"; applying a template copies it into the shop's own images.
public record ShopTemplateImage(String name, String file, String alt, String title, String author, String license,
                                String source) {
}
