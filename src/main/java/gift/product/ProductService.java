package gift.product;

import gift.category.Category;
import gift.category.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다. id=" + id));
    }

    public Product create(String name, int price, String imageUrl, Long categoryId) {
        Category category = findCategory(categoryId);
        return productRepository.save(new Product(name, price, imageUrl, category));
    }

    @Transactional
    public Product update(Long id, String name, int price, String imageUrl, Long categoryId) {
        Product product = findById(id);
        Category category = findCategory(categoryId);
        product.update(name, price, imageUrl, category);
        return productRepository.save(product);
    }

    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("상품이 존재하지 않습니다."));
        return ProductResponse.from(product);
    }

    public ProductResponse createProduct(ProductRequest request) {
        ProductNameValidator.validateOrThrow(request.name());
        Category category = findCategory(request.categoryId());
        Product saved = productRepository.save(request.toEntity(category));
        return ProductResponse.from(saved);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        ProductNameValidator.validateOrThrow(request.name());
        Product product = update(id, request.name(), request.price(), request.imageUrl(), request.categoryId());
        return ProductResponse.from(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new NoSuchElementException("카테고리가 존재하지 않습니다. id=" + categoryId));
    }
}
