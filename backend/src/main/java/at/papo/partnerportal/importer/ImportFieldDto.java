package at.papo.partnerportal.importer;

public record ImportFieldDto(String key, String label, boolean required) {
    public static ImportFieldDto from(ImportField field) {
        return new ImportFieldDto(field.key(), field.label(), field.required());
    }
}
