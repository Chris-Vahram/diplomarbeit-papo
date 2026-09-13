package at.papo.partnerportal.category;

public record CategoryDto(Long id, String name, boolean active) {

    public static CategoryDto from(Category category) {
        return new CategoryDto(category.id, category.name, category.active);
    }
}
