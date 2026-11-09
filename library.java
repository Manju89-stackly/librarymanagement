package com.example.library.model;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class Book {

    private Long bookId;

    @NotBlank(message="Book name is required")
    private String bookName;

    @NotBlank(message="Author name is required")
    private String author;

    @NotNull(message="Price is required")
    @Positive(message="Price must be positive")
    private Double price;

    private boolean available = true;
    public Book() {
    }

    public Book(Long bookId, String bookName, String author, Double price, boolean available) {
        this.bookId=bookId;
        this.bookName=bookName;
        this.author=author;
        this.price=price;
        this.available=available;
    }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId=bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName=bookName; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author=author; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price=price; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available=available; }
}

package com.example.library.controller;
import com.example.library.model.Book;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service=service;
    }

    // Add book
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book addBook(@Valid @RequestBody Book book) {
        return service.addBook(book);
    }

    // View all books
    @GetMapping
    public List<Book> getAllBooks() {
        return service.getAllBooks();
    }

    // Search book by ID
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable("id") Long id) {
        return service.getBookById(id);
    }

    // Update availability
    @PutMapping("/{id}/availability")
    public Book updateAvailability(@PathVariable("id") Long id,
                                   @RequestParam("available") boolean available) {
        return service.updateAvailability(id, available);
    }

    // Delete book
    @DeleteMapping("/{id}")
    public Map<String, String> deleteBook(@PathVariable("id") Long id) {
        service.deleteBook(id);
        return Map.of("message", "Book deleted successfully");
    }
}

package com.example.library.exception;
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
}

package com.example.library.service;
import com.example.library.exception.BookNotFoundException;
import com.example.library.model.Book;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    private final List<Book> books = new ArrayList<>();
    private long nextId = 1L;

    public synchronized Book addBook(Book request) {
        Book book = new Book(nextId++, request.getBookName().trim(),
                request.getAuthor().trim(), request.getPrice(), request.isAvailable());
        books.add(book);
        return book;
    }

    public synchronized List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public synchronized Book getBookById(Long id) {
        return books.stream()
                .filter(b -> b.getBookId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
    }

    public synchronized Book updateAvailability(Long id, boolean available) {
        Book book = getBookById(id);
        book.setAvailable(available);
        return book;
    }

    public synchronized void deleteBook(Long id) {
        Book book = getBookById(id);
        books.remove(book);
    }
}

package com.example.library.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return errors;
    }

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(BookNotFoundException ex) {
        return Map.of("message", ex.getMessage());
    }
}

package com.example.library;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
} 
spring.application.name=library-book-api
server.port=8080
