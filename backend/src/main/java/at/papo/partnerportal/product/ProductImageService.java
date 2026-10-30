package at.papo.partnerportal.product;

import at.papo.partnerportal.business.CompanyProfile;
import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class ProductImageService {

    public List<ProductImageDto> list(CompanyProfile company) {
        return ProductImage.<ProductImage>list("company", company).stream().map(ProductImageDto::from).toList();
    }

    @Transactional
    public ProductImageDto upload(CompanyProfile company, byte[] data, String filename) {
        String contentType = ProductImageValidator.detectContentType(data);

        ProductImage image = new ProductImage();
        image.company = company;
        image.data = data;
        image.contentType = contentType;
        image.filename = filename != null ? filename : "bild";
        image.sizeBytes = data.length;
        image.uploadedAt = Instant.now();
        image.persist();

        return ProductImageDto.from(image);
    }

    // An image of another company is indistinguishable from a missing one.
    ProductImage findOrThrow(CompanyProfile company, Long id) {
        ProductImage image = ProductImage.find("id = ?1 and company = ?2", id, company).firstResult();
        if (image == null) {
            throw new ApiException(404, "Bild nicht gefunden.");
        }
        return image;
    }
}
