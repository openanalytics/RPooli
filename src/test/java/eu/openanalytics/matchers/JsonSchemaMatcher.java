
package eu.openanalytics.matchers;

import static com.github.fge.jackson.JsonLoader.fromString;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Lists;
import com.jayway.restassured.module.jsv.JsonSchemaValidationException;

public class JsonSchemaMatcher extends TypeSafeMatcher<String>
{
    private final JsonSchemaFactory jsonSchemaFactory;
    private final URI schemaUri;
    private ProcessingReport report;

    private JsonSchemaMatcher(final JsonSchemaFactory jsonSchemaFactory, final URI schemaUri)
    {
        this.jsonSchemaFactory = jsonSchemaFactory;
        this.schemaUri = schemaUri;
    }

    public static JsonSchemaMatcher matchesJsonSchema(final JsonSchemaFactory jsonSchemaFactory,
                                                      final URI schemaUri)
    {
        return new JsonSchemaMatcher(jsonSchemaFactory, schemaUri);
    }

    @Override
    public void describeTo(final Description description)
    {
        if (report != null)
        {
            description.appendText("The content to match the given JSON schema: " + schemaUri + "\n");
            final List<ProcessingMessage> messages = Lists.newArrayList(report);
            if (!messages.isEmpty())
            {
                for (final ProcessingMessage message : messages)
                {
                    description.appendText(message.toString());
                }
            }
        }
        else
        {
            description.appendText("Validate against JSON schema: " + schemaUri);
        }
    }

    @Override
    protected boolean matchesSafely(final String json)
    {
        try
        {
            report = jsonSchemaFactory.getJsonSchema(schemaUri.toString()).validate(fromString(json));

            return report.isSuccess();
        }
        catch (ProcessingException | IOException e)
        {
            throw new JsonSchemaValidationException(e);
        }
    }
}
