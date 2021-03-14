package com.o3.storyinspector.api;

import com.o3.storyinspector.db.BookDAO;
import com.o3.storyinspector.domain.BookStructure;
import com.o3.storyinspector.storydom.Book;
import com.o3.storyinspector.storydom.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.StringReader;

@RestController
@RequestMapping("/api/bookstructure")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BookStructureApi {

    final Logger logger = LoggerFactory.getLogger(BookStructureApi.class);

    @Autowired
    private JdbcTemplate db;

    @GetMapping("/{bookId}")
    public BookStructure one(@PathVariable final Long bookId) {
        logger.trace("BOOK STRUCTURE BOOK ID=[" + bookId + "]");
        final BookDAO bookDAO = BookDAO.findByBookId(bookId, db);
        BookStructure bookStructure;
        try {
            final String annotatedStoryDom = bookDAO.getAnnotatedStoryDom();
            final Book book = XmlReader.readBookFromXmlStream(new StringReader(annotatedStoryDom));
            book.setAuthor(bookDAO.getAuthor());    // FIXME: parse this at the appropriate spot
            bookStructure = BookStructure.buildFromBook(book);
        } catch (final Exception e) {
            final String errMsg = "Unexpected error when building book structure report. Book bookId: " +
                    bookId + "Exception: " + e.getLocalizedMessage();
            logger.error(errMsg);
            return null;
        }

        return bookStructure;
    }

}
