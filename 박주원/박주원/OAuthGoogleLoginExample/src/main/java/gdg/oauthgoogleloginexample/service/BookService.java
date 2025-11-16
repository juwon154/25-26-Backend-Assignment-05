package gdg.oauthgoogleloginexample.service;

import gdg.oauthgoogleloginexample.domain.Book;
import gdg.oauthgoogleloginexample.domain.User;
import gdg.oauthgoogleloginexample.dto.BookDto;
import gdg.oauthgoogleloginexample.repository.BookRepository;
import gdg.oauthgoogleloginexample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BookDto createBook(Principal principal, BookDto bookDto) {
        User user = getUser(principal);
        Book book = Book.builder()
                .title(bookDto.getTitle())
                .author(bookDto.getAuthor())
                .publisher(bookDto.getPublisher())
                .price(bookDto.getPrice())
                .user(user)
                .build();
        Book savedBook = bookRepository.save(book);
        return toDto(savedBook);
    }

    public List<BookDto> getBooks(Principal principal) {
        User user = getUser(principal);
        return bookRepository.findByUserId(user.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BookDto updateBook(Principal principal, Long bookId, BookDto bookDto) {
        User user = getUser(principal);
        Book book = bookRepository.findById(bookId)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
        book = Book.builder()
                .id(book.getId())
                .title(bookDto.getTitle())
                .author(bookDto.getAuthor())
                .publisher(bookDto.getPublisher())
                .price(bookDto.getPrice())
                .user(user)
                .build();
        Book updated = bookRepository.save(book);
        return toDto(updated);
    }

    public void deleteBook(Principal principal, Long bookId) {
        User user = getUser(principal);
        Book book = bookRepository.findById(bookId)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
        bookRepository.delete(book);
    }

    private BookDto toDto(Book book) {
        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .price(book.getPrice())
                .build();
    }

    private User getUser(Principal principal) {
        Long id = Long.parseLong(principal.getName());
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
    }
}
