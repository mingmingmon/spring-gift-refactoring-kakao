package gift.product;

import gift.category.Category;
import gift.category.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Product not found."));
        return ProductResponse.from(product);
    }

    public ProductResponse createProduct(ProductRequest request) {
        ProductNameValidator.validateOrThrow(request.name());
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NoSuchElementException("Category not found."));
        Product saved = productRepository.save(request.toEntity(category));
        return ProductResponse.from(saved);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        ProductNameValidator.validateOrThrow(request.name());
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NoSuchElementException("Category not found."));
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Product not found."));
        product.update(request.name(), request.price(), request.imageUrl(), category);
        return ProductResponse.from(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
