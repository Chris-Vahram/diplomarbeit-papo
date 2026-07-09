package at.papo.partnerportal.product;

import at.papo.partnerportal.common.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class ProductImageService {

    public List<ProductImageDto> list() {
        return ProductImage.<ProductImage>listAll().stream().map(ProductImageDto::from).toList();
    }

    @Transactional
    public ProductImageDto upload(byte[] data, String filename) {
        String contentType = ProductImageValidator.detectContentType(data);

        ProductImage image = new ProductImage();
        image.data = data;
        image.contentType = contentType;
        image.filename = filename != null ? filename : "bild";
        image.sizeBytes = data.length;
        image.uploadedAt = Instant.now();
        image.persist();

        return ProductImageDto.from(image);
    }

    ProductImage findOrThrow(Long id) {
        ProductImage image = ProductImage.findById(id);
        if (image == null) {
            throw new ApiException(404, "Bild nicht gefunden.");
        }
        return image;
    }
}
