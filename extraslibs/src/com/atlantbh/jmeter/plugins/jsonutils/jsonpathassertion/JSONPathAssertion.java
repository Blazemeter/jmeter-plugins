/*!
 * AtlantBH Custom Jmeter Components v1.0.0
 * http://www.atlantbh.com/jmeter-components/
 *
 * Copyright 2011, AtlantBH
 *
 * Licensed under the under the Apache License, Version 2.0.
 */
package com.atlantbh.jmeter.plugins.jsonutils.jsonpathassertion;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.internal.JsonReader;
import java.io.Serializable;
import net.minidev.json.JSONArray;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This is main class for JSONPath Assertion which verifies assertion on
 * previous sample result using JSON path expression
 */
public class JSONPathAssertion extends AbstractTestElement implements Serializable, Assertion {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final long serialVersionUID = 1L;
    private static final String JSONPATH = "JSON_PATH";
    private static final String EXPECTEDVALUE = "EXPECTED_VALUE";
    private static final String JSONVALIDATION = "JSONVALIDATION";
    private static final String EXPECT_NULL = "EXPECT_NULL";

    public String getJsonPath() {
        return getPropertyAsString(JSONPATH);
    }

    public void setJsonPath(String jsonPath) {
        setProperty(JSONPATH, jsonPath);
    }

    public String getExpectedValue() {
        return getPropertyAsString(EXPECTEDVALUE);
    }

    public void setExpectedValue(String expectedValue) {
        setProperty(EXPECTEDVALUE, expectedValue);
    }

    public void setJsonValidationBool(boolean jsonValidation) {
        setProperty(JSONVALIDATION, jsonValidation);
    }

    public void setExpectNull(boolean val) {
        setProperty(EXPECT_NULL, val);
    }

    public boolean isExpectNull() {
        return getPropertyAsBoolean(EXPECT_NULL);
    }

    public boolean isJsonValidationBool() {
        return getPropertyAsBoolean(JSONVALIDATION);
    }

    private void doAssert(String jsonString) {
        JsonReader reader = new JsonReader(Configuration.defaultConfiguration().options(Option.THROW_ON_MISSING_PROPERTY));
        reader.parse(jsonString);
        Object value = reader.read(getJsonPath());

        if (isJsonValidationBool()) {
            if (value instanceof JSONArray) {
                JSONArray arr = (JSONArray) value;

                if (arr.isEmpty() && getExpectedValue().equals("[]")) {
                    return;
                }

                for (Object subj : arr.toArray()) {
                    if (subj.toString().equals(getExpectedValue())) {
                        return;
                    }
                }
            } else {
                if (isExpectNull() && value == null) {
                    return;
                } else if (value.toString().equals(getExpectedValue())) {
                    return;
                }
            }

            if (isExpectNull())
                throw new RuntimeException(String.format("Value expected to be null, but found '%s'", value));
            else
                throw new RuntimeException(String.format("Value expected to be '%s', but found '%s'", getExpectedValue(), value));
        }
    }

    @Override
    public AssertionResult getResult(SampleResult samplerResult) {
        AssertionResult result = new AssertionResult(getName());
        byte[] responseData = samplerResult.getResponseData();
        if (responseData.length == 0) {
            return result.setResultForNull();
        }

        result.setFailure(false);
        result.setFailureMessage("");
        try {
            doAssert(new String(responseData));
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Assertion failed", e);
            }
            result.setFailure(true);
            result.setFailureMessage(e.getMessage());
        }
        return result;
    }
}
