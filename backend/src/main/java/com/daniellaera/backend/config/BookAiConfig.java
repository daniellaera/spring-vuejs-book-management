package com.daniellaera.backend.config;


import com.daniellaera.backend.dao.BookAiView;
import com.daniellaera.backend.service.BookAiTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

@Configuration
public class BookAiConfig {

    @Bean
    public ToolCallback listBooksTool(BookAiTools bookAiTools) throws NoSuchMethodException {
        Method method = BookAiTools.class.getMethod("listBooks", int.class, int.class);

        // MANUALLY define the schema to stop the "Unrecognized token 'public'" error
        String schema = """
        {
          "type": "object",
          "properties": {
            "page": { "type": "integer", "description": "The page number (0-based)" },
            "size": { "type": "integer", "description": "Number of items per page" }
          },
          "required": ["page", "size"]
        }
        """;

        return MethodToolCallback.builder()
                .toolDefinition(ToolDefinition.builder()
                        .name("listBooks")
                        .description("Get a list of available books. page starts at 0, size max 50")
                        .inputSchema(schema) // Use the String schema, NOT the method object
                        .build())
                .toolMethod(method)
                .toolObject(bookAiTools)
                .build();
    }

    @Bean
    public ToolCallback getBookByIdTool(BookAiTools bookAiTools) throws NoSuchMethodException {
        Method method = BookAiTools.class.getMethod("getBookById", int.class);

        String schema = """
        {
          "type": "object",
          "properties": {
            "bookId": { "type": "integer", "description": "The numeric ID of the book" }
          },
          "required": ["bookId"]
        }
        """;

        return MethodToolCallback.builder()
                .toolDefinition(ToolDefinition.builder()
                        .name("getBookById")
                        .description("Get details for a specific book by its numeric ID")
                        .inputSchema(schema)
                        .build())
                .toolMethod(method)
                .toolObject(bookAiTools)
                .build();
    }
}