package gdg.oauthgoogleloginexample.controller;

import gdg.oauthgoogleloginexample.dto.BookDto;
import gdg.oauthgoogleloginexample.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    @PostMapping
    public BookDto createBook(Principal principal, @RequestBody BookDto bookDto) {
        return bookService.createBook(principal, bookDto);
    }

    @GetMapping
    public List<BookDto> getBooks(Principal principal) {
        return bookService.getBooks(principal);
    }

    @PutMapping("/{id}")
    public BookDto updateBook(Principal principal, @PathVariable Long id, @RequestBody BookDto bookDto) {
        return bookService.updateBook(principal, id, bookDto);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(Principal principal, @PathVariable Long id) {
        bookService.deleteBook(principal, id);
    }
}
