package at.papo.partnerportal.partner;

public record PartnerTradeProofDownload(byte[] data, String contentType, String filename) {
}
