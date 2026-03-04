package gift.option;

import gift.product.Product;
import gift.product.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OptionService {
    private final OptionRepository optionRepository;
    private final ProductService productService;

    public OptionService(OptionRepository optionRepository, ProductService productService) {
        this.optionRepository = optionRepository;
        this.productService = productService;
    }

    public Option findById(Long id) {
        return optionRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Option not found."));
    }

    public void subtractQuantity(Long optionId, int quantity) {
        Option option = findById(optionId);
        option.subtractQuantity(quantity);
        optionRepository.save(option);
    }

    public List<OptionResponse> getOptions(Long productId) {
        productService.findById(productId);
        return optionRepository.findByProductId(productId).stream()
            .map(OptionResponse::from)
            .toList();
    }

    public OptionResponse createOption(Long productId, OptionRequest request) {
        Product product = productService.findById(productId);
        if (optionRepository.existsByProductIdAndName(productId, request.name())) {
            throw new IllegalArgumentException("이미 존재하는 옵션명입니다.");
        }
        Option saved = optionRepository.save(request.toEntity(product));
        return OptionResponse.from(saved);
    }

    public void deleteOption(Long productId, Long optionId) {
        productService.findById(productId);
        List<Option> options = optionRepository.findByProductId(productId);
        if (options.size() <= 1) {
            throw new IllegalArgumentException("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다.");
        }
        Option option = optionRepository.findById(optionId)
            .filter(o -> o.getProduct().getId().equals(productId))
            .orElseThrow(() -> new NoSuchElementException("Option not found."));
        optionRepository.delete(option);
    }
}
