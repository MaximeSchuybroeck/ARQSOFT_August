package pt.psoft.g1.psoftg1.bookmanagement.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookServiceImpl;
import pt.psoft.g1.psoftg1.bookmanagement.services.TopByGenreDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final BookServiceImpl recommendationService;

    @GetMapping("/books")
    public List<Book> getRecommendedBooks(@RequestParam int age, @RequestParam Long userId) {
        return recommendationService.recommendBooksByAge(age, userId);
    }

    @GetMapping("/topxy")
    public List<TopByGenreDTO> topXWithinTopY(
            @RequestParam(name = "x", defaultValue = "3") int topX,
            @RequestParam(name = "y", defaultValue = "5") int topY) {
        return recommendationService.findTopXWithinTopYGenres(topX, topY);
    }
}
