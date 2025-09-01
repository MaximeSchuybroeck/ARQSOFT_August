package pt.psoft.g1.psoftg1.bookmanagement.services;

import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import java.util.List;

public record TopByGenreDTO(String genre, List<Book> books) { }
