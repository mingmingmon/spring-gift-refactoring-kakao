package gift.product;

import gift.category.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "product/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "product/new";
    }

    @PostMapping
    public String create(
        @RequestParam String name,
        @RequestParam int price,
        @RequestParam String imageUrl,
        @RequestParam Long categoryId,
        Model model
    ) {
        List<String> errors = ProductNameValidator.validate(name, true);
        if (!errors.isEmpty()) {
            populateNewFormError(model, errors, name, price, imageUrl, categoryId);
            return "product/new";
        }

        productService.create(name, price, imageUrl, categoryId);
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        return "product/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
        @PathVariable Long id,
        @RequestParam String name,
        @RequestParam int price,
        @RequestParam String imageUrl,
        @RequestParam Long categoryId,
        Model model
    ) {
        Product product = productService.findById(id);

        List<String> errors = ProductNameValidator.validate(name, true);
        if (!errors.isEmpty()) {
            populateEditForm(model, product, errors, name, price, imageUrl, categoryId);
            return "product/edit";
        }

        productService.update(id, name, price, imageUrl, categoryId);
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }

    private void populateNewFormError(
        Model model,
        List<String> errors,
        String name,
        int price,
        String imageUrl,
        Long categoryId
    ) {
        model.addAttribute("errors", errors);
        model.addAttribute("name", name);
        model.addAttribute("price", price);
        model.addAttribute("imageUrl", imageUrl);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.findAll());
    }

    private void populateEditForm(
        Model model,
        Product product,
        List<String> errors,
        String name,
        int price,
        String imageUrl,
        Long categoryId
    ) {
        model.addAttribute("errors", errors);
        model.addAttribute("product", product);
        model.addAttribute("name", name);
        model.addAttribute("price", price);
        model.addAttribute("imageUrl", imageUrl);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.findAll());
    }
}
