package com.o3.storyinspector.api;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
class UploadFileApiTest {

    private static final String API_UPLOAD_FILE = "http://localhost:8081/api/upload/book";

    private static final String INPUT_PLAINTEXT_BOOK = "Chapter 1: A Startling Start.\n" +
            "This is an example chapter wherein wondrous things would be expected by its eager author.\n" +
            "            \n" +
            "Chapter 2: The Unexciting Aftermath.\n" +
            "This is another example chapter, but the action seems to unfold slower than expected. \n";

    private static final String USER_ID = "108700212624021084744";

    private static final String BOUNDARY = "q1w2e3r4t5y6u7i8o9";

    private static final Random RANDOM_GENERATOR = new Random();

    private static final int LARGE_PAYLOAD_SIZE = 10 * 1024 * 1024;

    @Autowired
    MockMvc mockMvc;

    @Test
    void testUploadSmallFile() throws Exception {
        // given
        MockMultipartHttpServletRequestBuilder builder = multipart(API_UPLOAD_FILE);
        builder = builder.part(new MockPart("file", "book.txt", INPUT_PLAINTEXT_BOOK.getBytes()));
        builder.param("title", "Mock Title")
                .param("author", "Mock Author")
                .param("id_token", USER_ID);

        // when
        mockMvc.perform(builder
                .content(createFileContent(INPUT_PLAINTEXT_BOOK.getBytes(), BOUNDARY, MediaType.TEXT_PLAIN_VALUE, "book.txt"))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE + "; boundary=" + BOUNDARY))
                // then
                .andExpect(status().isOk());

    }

    @Test
    void testUploadLargeFile() throws Exception {
        // given
        final byte[] fileContents = createPayload(LARGE_PAYLOAD_SIZE);
        MockMultipartHttpServletRequestBuilder builder = multipart(API_UPLOAD_FILE);
        builder = builder.part(new MockPart("file", "book.txt", fileContents));
        builder.param("title", "Mock Title")
                .param("author", "Mock Author")
                .param("id_token", USER_ID);

        // when
        mockMvc.perform(builder
                .content(createFileContent(fileContents, BOUNDARY, MediaType.TEXT_PLAIN_VALUE, "book.txt"))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE + "; boundary=" + BOUNDARY))
                // then
                .andExpect(status().isOk());

    }

    private static byte[] createFileContent(final byte[] data, final String boundary, final String contentType, final String fileName) {
        final String start = "--" + boundary + "\r\n Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-type: " + contentType + "\r\n\r\n";
        final String end = "\r\n--" + boundary + "--";
        return ArrayUtils.addAll(start.getBytes(), ArrayUtils.addAll(data, end.getBytes()));
    }

    private static byte[] createPayload(final int sizeInBytes) {
        final int leftLimit = 48; // numeral '0'
        final int rightLimit = 122; // letter 'z'
        final int targetStringLength = 10;

        final String generatedString = RANDOM_GENERATOR.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString.getBytes();
    }

}